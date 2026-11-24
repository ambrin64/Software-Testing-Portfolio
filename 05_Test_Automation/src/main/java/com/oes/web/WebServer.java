package com.oes.web;

import com.oes.auth.AuthService;
import com.oes.auth.LoginResult;
import com.oes.exam.ExamSession;
import com.oes.exam.ExamState;
import com.oes.exam.Question;
import com.oes.exam.TimeSource;
import com.oes.registration.RegistrationService;
import com.oes.registration.RegistrationStatus;
import com.oes.result.ResultCalculator;
import com.oes.result.ResultStatus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Web front end for the Online Examination System.
 *
 * <p>This class is only a thin HTTP layer. Every business rule (registration validation, login
 * lock-out, exam state machine, scoring, grading and result status) is delegated to the same
 * classes that the JUnit 5 suite tests.
 *
 * <p>Run with {@code .\run_tests.ps1 -Web} and open http://localhost:9090/.
 */
public class WebServer {

    /** Leaving the exam window this many times is recorded as malpractice. */
    static final int MAX_VIOLATIONS = 3;

    private static final String DEMO_NAME = "Demo Student";
    private static final String DEMO_EMAIL = "demo@oes.edu";
    private static final String DEMO_PASSWORD = "Exam2026";

    private final RegistrationService registration = new RegistrationService();
    private final AuthService auth = new AuthService();
    private final ResultCalculator calculator = new ResultCalculator();

    private final Map<String, String> names = new HashMap<>();      // e-mail -> display name
    private final Map<String, String> sessions = new HashMap<>();   // token  -> e-mail
    private final Map<String, Attempt> attempts = new HashMap<>();  // e-mail -> latest attempt

    private final long examSeconds;
    private final Path webRoot;

    private static final class Attempt {
        final ExamSession session;
        int violations;
        boolean malpractice;

        Attempt(ExamSession session) {
            this.session = session;
        }
    }

    private static final class ApiException extends RuntimeException {
        final int status;

        ApiException(int status, String message) {
            super(message);
            this.status = status;
        }
    }

    WebServer(long examSeconds, Path webRoot) {
        this.examSeconds = examSeconds;
        this.webRoot = webRoot;
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.getInteger("oes.port", 9090);
        long examSeconds = Long.getLong("oes.examSeconds", 600L);
        Path webRoot = Paths.get(System.getProperty("oes.webRoot", "src/main/resources/web"))
                .toAbsolutePath().normalize();
        new WebServer(examSeconds, webRoot).start(port);
    }

    void start(int port) throws IOException {
        if (!Files.isRegularFile(webRoot.resolve("index.html"))) {
            throw new IllegalStateException("Web files not found in " + webRoot);
        }
        registerUser(DEMO_NAME, DEMO_EMAIL, 21, DEMO_PASSWORD);

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        } catch (BindException e) {
            System.err.println("Port " + port + " is already in use (is the server already running in another terminal?).");
            System.err.println("Stop the other program, or start on a different port, e.g.:  .\\run_tests.ps1 -Web -Port " + (port + 1));
            System.exit(1);
            return;
        }
        server.createContext("/api/", this::handleApi);
        server.createContext("/", this::handleStatic);
        server.start();

        String url = "http://localhost:" + port + "/";
        System.out.println("=== Online Examination System - Web UI ===");
        System.out.println("Open in browser : " + url);
        System.out.println("Demo account    : " + DEMO_EMAIL + " / " + DEMO_PASSWORD);
        System.out.println("Exam duration   : " + formatDuration(examSeconds));
        System.out.println("Press Ctrl+C to stop the server.");

        if (!Boolean.getBoolean("oes.noBrowser")) {
            openBrowser(url);
        }
    }

    // ------------------------------------------------------------------ API routing

    private synchronized void handleApi(HttpExchange ex) throws IOException {
        int status = 200;
        Object body;
        try {
            String path = ex.getRequestURI().getPath().substring("/api/".length());
            String method = ex.getRequestMethod();
            Map<String, String> params = "POST".equals(method)
                    ? parseForm(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8))
                    : parseForm(ex.getRequestURI().getRawQuery());
            String token = ex.getRequestHeaders().getFirst("X-Token");
            body = route(method + " " + path, params, token);
        } catch (ApiException e) {
            status = e.status;
            body = Json.obj("error", e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            status = 409;
            body = Json.obj("error", e.getMessage());
        } catch (RuntimeException e) {
            status = 500;
            body = Json.obj("error", "Internal error: " + e);
        }
        send(ex, status, "application/json; charset=utf-8", Json.write(body).getBytes(StandardCharsets.UTF_8));
    }

    private Object route(String endpoint, Map<String, String> p, String token) {
        return switch (endpoint) {
            case "POST register" -> register(p);
            case "POST login" -> login(p);
            case "POST admin/unlock" -> unlock(p);
            case "POST logout" -> logout(token);
            case "GET me" -> me(token);
            case "GET exam/info" -> examInfo();
            case "POST exam/start" -> startExam(token);
            case "GET exam/state" -> examState(token);
            case "POST exam/answer" -> answer(token, p);
            case "POST exam/violation" -> violation(token);
            case "POST exam/submit" -> submit(token);
            case "GET result" -> result(token);
            default -> throw new ApiException(404, "Unknown endpoint: " + endpoint);
        };
    }

    // ------------------------------------------------------------------ registration & login

    private Object register(Map<String, String> p) {
        int age;
        try {
            age = Integer.parseInt(p.getOrDefault("age", "").trim());
        } catch (NumberFormatException e) {
            return Json.obj("status", RegistrationStatus.INVALID_AGE,
                    "message", registrationMessage(RegistrationStatus.INVALID_AGE));
        }
        RegistrationStatus status = registerUser(p.get("name"), p.get("email"), age, p.get("password"));
        return Json.obj("status", status, "message", registrationMessage(status));
    }

    private RegistrationStatus registerUser(String name, String email, int age, String password) {
        RegistrationStatus status = registration.register(name, email, age, password);
        if (status == RegistrationStatus.SUCCESS) {
            String key = key(email);
            auth.addUser(key, password);
            names.put(key, name.trim());
        }
        return status;
    }

    private Object login(Map<String, String> p) {
        String key = key(p.get("email"));
        LoginResult result = auth.login(key, p.getOrDefault("password", ""));
        Map<String, Object> out = Json.obj("result", result);
        switch (result) {
            case SUCCESS -> {
                String token = UUID.randomUUID().toString();
                sessions.put(token, key);
                out.put("message", "Welcome back.");
                out.put("token", token);
                out.put("user", userJson(key));
            }
            case INVALID_CREDENTIALS -> {
                int left = AuthService.MAX_FAILED_ATTEMPTS - auth.getFailedAttempts(key);
                out.put("attemptsLeft", left);
                out.put("message", "Incorrect password. " + left + (left == 1 ? " attempt" : " attempts")
                        + " left before the account is locked.");
            }
            case ACCOUNT_LOCKED -> out.put("message", "This account is locked after "
                    + AuthService.MAX_FAILED_ATTEMPTS + " failed attempts. An administrator must unlock it.");
            case USER_NOT_FOUND -> out.put("message", "No account is registered with this e-mail address.");
        }
        return out;
    }

    private Object unlock(Map<String, String> p) {
        String key = key(p.get("email"));
        if (!names.containsKey(key)) {
            throw new ApiException(404, "No account is registered with this e-mail address.");
        }
        auth.unlock(key);
        return Json.obj("result", "UNLOCKED", "message", "Account unlocked by the administrator. You may sign in again.");
    }

    private Object logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
        return Json.obj("result", "LOGGED_OUT");
    }

    private Object me(String token) {
        String key = user(token);
        Attempt attempt = attempts.get(key);
        return Json.obj("user", userJson(key),
                "exam", attempt == null ? null : Json.obj("state", attempt.session.getState()));
    }

    // ------------------------------------------------------------------ exam

    private Object examInfo() {
        List<Question> questions = QuestionBank.questions();
        List<Object> sections = new ArrayList<>();
        QuestionBank.SECTION_TITLES.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sections.add(Json.obj("id", e.getKey(), "title", e.getValue(),
                        "questions", questions.stream().filter(q -> q.section().equals(e.getKey())).count())));
        return Json.obj(
                "title", QuestionBank.TITLE,
                "questions", questions.size(),
                "durationSeconds", examSeconds,
                "maxScore", calculator.maxScore(questions),
                "marksCorrect", ResultCalculator.MARKS_PER_CORRECT,
                "marksWrong", ResultCalculator.NEGATIVE_MARKS_PER_WRONG,
                "passPercentage", ResultCalculator.PASS_PERCENTAGE,
                "maxViolations", MAX_VIOLATIONS,
                "sections", sections);
    }

    private Object startExam(String token) {
        String key = user(token);
        Attempt attempt = attempts.get(key);
        if (attempt == null || attempt.session.getState() != ExamState.IN_PROGRESS) {
            attempt = new Attempt(new ExamSession(QuestionBank.questions(), examSeconds, TimeSource.SYSTEM));
            attempt.session.start();
            attempts.put(key, attempt);
        }
        ExamSession session = attempt.session;
        List<Object> questions = new ArrayList<>();
        for (Question q : session.getQuestions()) {
            // The correct option is deliberately NOT sent to the browser.
            questions.add(Json.obj("id", q.id(), "text", q.text(), "options", q.options(),
                    "section", q.section(), "sectionTitle", QuestionBank.SECTION_TITLES.get(q.section())));
        }
        return Json.obj(
                "state", session.getState(),
                "remainingSeconds", session.remainingSeconds(),
                "durationSeconds", examSeconds,
                "violations", attempt.violations,
                "maxViolations", MAX_VIOLATIONS,
                "malpractice", attempt.malpractice,
                "questions", questions,
                "answers", new LinkedHashMap<>(session.getAnswers()));
    }

    private Object examState(String token) {
        ExamSession session = attempt(token).session;
        return Json.obj("state", session.getState(), "remainingSeconds", session.remainingSeconds());
    }

    private Object answer(String token, Map<String, String> p) {
        ExamSession session = attempt(token).session;
        int questionId;
        int option;
        try {
            questionId = Integer.parseInt(p.getOrDefault("questionId", ""));
            option = Integer.parseInt(p.getOrDefault("option", ""));
        } catch (NumberFormatException e) {
            throw new ApiException(400, "questionId and option must be numbers");
        }
        session.answer(questionId, option);  // throws if the exam is SUBMITTED / TIMED_OUT
        return Json.obj("state", session.getState(), "answered", session.getAnswers().size());
    }

    private Object violation(String token) {
        Attempt attempt = attempt(token);
        if (attempt.session.getState() == ExamState.IN_PROGRESS) {
            attempt.violations++;
            if (attempt.violations >= MAX_VIOLATIONS) {
                attempt.malpractice = true;
            }
        }
        return Json.obj("violations", attempt.violations, "maxViolations", MAX_VIOLATIONS,
                "malpractice", attempt.malpractice);
    }

    private Object submit(String token) {
        String key = user(token);
        Attempt attempt = attempt(token);
        if (attempt.session.getState() == ExamState.IN_PROGRESS) {
            attempt.session.submit();
        }
        return resultJson(key, attempt);
    }

    private Object result(String token) {
        String key = user(token);
        Attempt attempt = attempt(token);
        ExamState state = attempt.session.getState();
        if (state != ExamState.SUBMITTED && state != ExamState.TIMED_OUT) {
            throw new ApiException(409, "The exam has not been submitted yet.");
        }
        return resultJson(key, attempt);
    }

    private Map<String, Object> resultJson(String key, Attempt attempt) {
        List<Question> questions = attempt.session.getQuestions();
        Map<Integer, Integer> answers = attempt.session.getAnswers();

        double score = calculator.score(questions, answers);
        double maxScore = calculator.maxScore(questions);
        double percentage = calculator.percentage(score, maxScore);
        boolean allSections = calculator.allSectionsAttempted(questions, answers);
        ResultStatus status = calculator.status(percentage, attempt.malpractice, allSections);

        int correct = 0;
        int wrong = 0;
        Map<String, int[]> perSection = new LinkedHashMap<>();   // section -> {correct, wrong, unanswered}
        List<Object> review = new ArrayList<>();
        for (Question q : questions) {
            Integer chosen = answers.get(q.id());
            int[] counts = perSection.computeIfAbsent(q.section(), s -> new int[3]);
            double marks;
            if (chosen == null) {
                counts[2]++;
                marks = 0;
            } else if (chosen == q.correctOption()) {
                counts[0]++;
                correct++;
                marks = ResultCalculator.MARKS_PER_CORRECT;
            } else {
                counts[1]++;
                wrong++;
                marks = -ResultCalculator.NEGATIVE_MARKS_PER_WRONG;
            }
            review.add(Json.obj("id", q.id(), "section", q.section(), "text", q.text(), "options", q.options(),
                    "correctOption", q.correctOption(), "chosenOption", chosen, "marks", marks));
        }

        List<Object> sections = new ArrayList<>();
        perSection.forEach((section, c) -> sections.add(Json.obj(
                "id", section, "title", QuestionBank.SECTION_TITLES.get(section),
                "correct", c[0], "wrong", c[1], "unanswered", c[2],
                "marks", c[0] * ResultCalculator.MARKS_PER_CORRECT - c[1] * ResultCalculator.NEGATIVE_MARKS_PER_WRONG,
                "attempted", c[0] + c[1] > 0)));

        return Json.obj(
                "name", names.get(key),
                "examTitle", QuestionBank.TITLE,
                "endState", attempt.session.getState(),
                "score", score,
                "maxScore", maxScore,
                "percentage", Math.round(percentage * 10) / 10.0,
                "grade", calculator.grade(percentage),
                "status", status,
                "passPercentage", ResultCalculator.PASS_PERCENTAGE,
                "malpractice", attempt.malpractice,
                "violations", attempt.violations,
                "maxViolations", MAX_VIOLATIONS,
                "allSectionsAttempted", allSections,
                "correct", correct,
                "wrong", wrong,
                "unanswered", questions.size() - correct - wrong,
                "sections", sections,
                "review", review);
    }

    // ------------------------------------------------------------------ helpers

    private String user(String token) {
        String key = token == null ? null : sessions.get(token);
        if (key == null) {
            throw new ApiException(401, "Please sign in again.");
        }
        return key;
    }

    private Attempt attempt(String token) {
        Attempt attempt = attempts.get(user(token));
        if (attempt == null) {
            throw new ApiException(409, "No exam has been started.");
        }
        return attempt;
    }

    private Map<String, Object> userJson(String key) {
        return Json.obj("name", names.get(key), "email", key);
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String registrationMessage(RegistrationStatus status) {
        return switch (status) {
            case SUCCESS -> "Registration successful. You can now sign in.";
            case INVALID_NAME -> "Name must be between 3 and 30 characters.";
            case INVALID_EMAIL -> "Please enter a valid e-mail address, e.g. name@college.edu.";
            case DUPLICATE_EMAIL -> "An account with this e-mail address already exists.";
            case INVALID_AGE -> "Age must be between 16 and 60 years.";
            case INVALID_PASSWORD -> "Password must be 8–16 characters with at least one uppercase letter and one digit.";
        };
    }

    private static String formatDuration(long seconds) {
        return seconds % 60 == 0 ? (seconds / 60) + " min" : seconds + " s";
    }

    private static Map<String, String> parseForm(String raw) {
        Map<String, String> params = new HashMap<>();
        if (raw == null || raw.isEmpty()) {
            return params;
        }
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            String k = eq < 0 ? pair : pair.substring(0, eq);
            String v = eq < 0 ? "" : pair.substring(eq + 1);
            params.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
        }
        return params;
    }

    // ------------------------------------------------------------------ static files

    private void handleStatic(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            send(ex, 405, "text/plain", "Method not allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        Path file = webRoot.resolve(path.substring(1)).normalize();
        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            send(ex, 404, "text/plain", "Not found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        send(ex, 200, contentType(file), Files.readAllBytes(file));
    }

    private static String contentType(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        } else if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        } else if (name.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        } else if (name.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (name.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    private static void send(HttpExchange ex, int status, String contentType, byte[] bytes) throws IOException {
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception ignored) {
            // Opening the browser is a convenience only; the URL is printed above.
        }
    }
}

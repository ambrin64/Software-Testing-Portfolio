package com.oes;

import com.oes.auth.AuthService;
import com.oes.exam.ExamSession;
import com.oes.exam.Question;
import com.oes.exam.TimeSource;
import com.oes.registration.RegistrationService;
import com.oes.result.ResultCalculator;

import java.util.List;
import java.util.Map;

/** Small console demo of the Online Examination System's main flow. */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Online Examination System - Demo ===");

        RegistrationService registration = new RegistrationService();
        System.out.println("Register asha@college.edu : "
                + registration.register("Asha Verma", "asha@college.edu", 20, "Exam2026"));

        AuthService auth = new AuthService();
        auth.addUser("asha@college.edu", "Exam2026");
        System.out.println("Login                     : " + auth.login("asha@college.edu", "Exam2026"));

        List<Question> questions = List.of(
                new Question(1, "Which testing technique tests values at the edges of input ranges?",
                        List.of("Equivalence partitioning", "Boundary value analysis", "Decision table", "Smoke testing"), 1, "A"),
                new Question(2, "JUnit is primarily used for which level of testing?",
                        List.of("Unit testing", "Acceptance testing", "Load testing", "Usability testing"), 0, "A"),
                new Question(3, "A defect found by the customer after release is called a?",
                        List.of("Error", "Failure", "Mistake", "Test case"), 1, "B"),
                new Question(4, "Which document defines scope, approach and schedule of testing?",
                        List.of("Test case", "Defect report", "Test plan", "Traceability matrix"), 2, "B"),
                new Question(5, "State transition testing is best suited for?",
                        List.of("Calculations", "Systems with states and events", "UI layout", "Database indexes"), 1, "B"));

        ExamSession exam = new ExamSession(questions, 30 * 60, TimeSource.SYSTEM);
        exam.start();
        Map<Integer, Integer> picks = Map.of(1, 1, 2, 0, 3, 1, 4, 0);  // 3 correct, 1 wrong, 1 unanswered
        picks.forEach(exam::answer);
        exam.submit();
        System.out.println("Exam state                : " + exam.getState());

        ResultCalculator calc = new ResultCalculator();
        double score = calc.score(questions, exam.getAnswers());
        double pct = calc.percentage(score, calc.maxScore(questions));
        boolean allSections = calc.allSectionsAttempted(questions, exam.getAnswers());
        System.out.printf("Score                     : %.1f / %.1f (%.1f%%)%n", score, calc.maxScore(questions), pct);
        System.out.println("Grade                     : " + calc.grade(pct));
        System.out.println("Result                    : " + calc.status(pct, false, allSections));
    }
}

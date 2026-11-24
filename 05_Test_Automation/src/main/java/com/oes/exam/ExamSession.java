package com.oes.exam;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One student's attempt at an exam (Test Plan REQ-10..REQ-13).
 *
 * NOT_STARTED --start()--> IN_PROGRESS --submit()--> SUBMITTED
 *                          IN_PROGRESS --duration elapsed--> TIMED_OUT
 */
public class ExamSession {

    private final List<Question> questions;
    private final long durationSeconds;
    private final TimeSource clock;
    private final Map<Integer, Integer> answers = new LinkedHashMap<>();

    private ExamState state = ExamState.NOT_STARTED;
    private long startedAt;

    public ExamSession(List<Question> questions, long durationSeconds, TimeSource clock) {
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("An exam needs at least one question");
        }
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.questions = List.copyOf(questions);
        this.durationSeconds = durationSeconds;
        this.clock = clock;
    }

    public void start() {
        if (state != ExamState.NOT_STARTED) {
            throw new IllegalStateException("Exam already started");
        }
        state = ExamState.IN_PROGRESS;
        startedAt = clock.nowSeconds();
    }

    public void answer(int questionId, int optionIndex) {
        refreshState();
        if (state != ExamState.IN_PROGRESS) {
            throw new IllegalStateException("Cannot answer when exam is " + state);
        }
        Question question = findQuestion(questionId);
        if (optionIndex < 0 || optionIndex >= question.options().size()) {
            throw new IllegalArgumentException("Invalid option " + optionIndex);
        }
        answers.put(questionId, optionIndex);
    }

    public void submit() {
        refreshState();
        if (state != ExamState.IN_PROGRESS) {
            throw new IllegalStateException("Cannot submit when exam is " + state);
        }
        state = ExamState.SUBMITTED;
    }

    public ExamState getState() {
        refreshState();
        return state;
    }

    public long remainingSeconds() {
        refreshState();
        if (state != ExamState.IN_PROGRESS) {
            return 0;
        }
        return Math.max(0, startedAt + durationSeconds - clock.nowSeconds());
    }

    public Map<Integer, Integer> getAnswers() {
        return Collections.unmodifiableMap(answers);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    private void refreshState() {
        if (state == ExamState.IN_PROGRESS && clock.nowSeconds() >= startedAt + durationSeconds) {
            state = ExamState.TIMED_OUT;
        }
    }

    private Question findQuestion(int questionId) {
        return questions.stream()
                .filter(q -> q.id() == questionId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown question " + questionId));
    }
}

package com.oes.exam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Exam Session - State Transition")
class ExamSessionStateTransitionTest {

    private static final long DURATION = 30 * 60; // 30 minutes

    private final AtomicLong now = new AtomicLong(1_000);
    private ExamSession session;

    @BeforeEach
    void setUp() {
        List<String> opts = List.of("a", "b", "c", "d");
        List<Question> questions = List.of(
                new Question(1, "Q1", opts, 0, "A"),
                new Question(2, "Q2", opts, 1, "A"),
                new Question(3, "Q3", opts, 2, "B"));
        session = new ExamSession(questions, DURATION, now::get);
    }

    @Test
    @DisplayName("TC_EXM_ST_01: new session -> NOT_STARTED")
    void initialState() {
        assertEquals(ExamState.NOT_STARTED, session.getState());
    }

    @Test
    @DisplayName("TC_EXM_ST_02: NOT_STARTED + start() -> IN_PROGRESS")
    void start() {
        session.start();
        assertEquals(ExamState.IN_PROGRESS, session.getState());
    }

    @Test
    @DisplayName("TC_EXM_ST_03: NOT_STARTED + answer() -> IllegalStateException")
    void answerBeforeStart() {
        assertThrows(IllegalStateException.class, () -> session.answer(1, 0));
    }

    @Test
    @DisplayName("TC_EXM_ST_04: NOT_STARTED + submit() -> IllegalStateException")
    void submitBeforeStart() {
        assertThrows(IllegalStateException.class, session::submit);
    }

    @Test
    @DisplayName("TC_EXM_ST_05: IN_PROGRESS + submit() -> SUBMITTED")
    void submit() {
        session.start();
        session.answer(1, 0);
        session.submit();
        assertEquals(ExamState.SUBMITTED, session.getState());
    }

    @Test
    @DisplayName("TC_EXM_ST_06: SUBMITTED + answer() -> IllegalStateException, answers unchanged")
    void answerAfterSubmit() {
        session.start();
        session.answer(1, 0);
        session.submit();
        assertThrows(IllegalStateException.class, () -> session.answer(2, 1),
                "answer accepted after submission");
        assertEquals(Map.of(1, 0), session.getAnswers());
    }

    @Test
    @DisplayName("TC_EXM_ST_07: IN_PROGRESS + duration elapsed -> TIMED_OUT")
    void timeOut() {
        session.start();
        now.addAndGet(DURATION);
        assertEquals(ExamState.TIMED_OUT, session.getState());
        assertEquals(0, session.remainingSeconds());
    }

    @Test
    @DisplayName("TC_EXM_ST_08: TIMED_OUT + answer() -> IllegalStateException")
    void answerAfterTimeOut() {
        session.start();
        now.addAndGet(DURATION + 1);
        assertThrows(IllegalStateException.class, () -> session.answer(1, 0));
    }

    @Test
    @DisplayName("TC_EXM_ST_09: IN_PROGRESS + start() again -> IllegalStateException")
    void startTwice() {
        session.start();
        assertThrows(IllegalStateException.class, session::start);
    }

    @Test
    @DisplayName("TC_EXM_ST_10: TIMED_OUT + submit() -> IllegalStateException")
    void submitAfterTimeOut() {
        session.start();
        now.addAndGet(DURATION + 1);
        assertThrows(IllegalStateException.class, session::submit);
    }

    @Test
    @DisplayName("TC_EXM_ST_11: IN_PROGRESS + answer() with invalid option -> IllegalArgumentException")
    void invalidOption() {
        session.start();
        assertThrows(IllegalArgumentException.class, () -> session.answer(1, 7));
    }

    @Test
    @DisplayName("TC_EXM_ST_12: IN_PROGRESS + change answer -> last answer saved")
    void changeAnswer() {
        session.start();
        session.answer(1, 0);
        session.answer(1, 2);
        assertEquals(2, session.getAnswers().get(1));
    }
}

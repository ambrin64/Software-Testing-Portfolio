package com.oes.result;

import com.oes.exam.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Score Calculation - Equivalence Partitioning")
class ScoreEquivalencePartitioningTest {

    private static final List<String> OPTS = List.of("a", "b", "c", "d");
    // Correct options: Q1=0, Q2=1, Q3=2, Q4=3, Q5=0
    private static final List<Question> QUESTIONS = List.of(
            new Question(1, "Q1", OPTS, 0, "A"),
            new Question(2, "Q2", OPTS, 1, "A"),
            new Question(3, "Q3", OPTS, 2, "A"),
            new Question(4, "Q4", OPTS, 3, "B"),
            new Question(5, "Q5", OPTS, 0, "B"));

    private final ResultCalculator calc = new ResultCalculator();

    @Test
    @DisplayName("TC_SCR_EP_01: all answers correct -> 20")
    void allCorrect() {
        assertEquals(20.0, calc.score(QUESTIONS, Map.of(1, 0, 2, 1, 3, 2, 4, 3, 5, 0)));
    }

    @Test
    @DisplayName("TC_SCR_EP_02: all answers wrong -> 0 (never negative)")
    void allWrong() {
        assertEquals(0.0, calc.score(QUESTIONS, Map.of(1, 1, 2, 2, 3, 3, 4, 0, 5, 1)));
    }

    @Test
    @DisplayName("TC_SCR_EP_03: 3 correct, 1 wrong, 1 unanswered -> 11")
    void mixed() {
        assertEquals(11.0, calc.score(QUESTIONS, Map.of(1, 0, 2, 1, 3, 2, 4, 0)));
    }

    @Test
    @DisplayName("TC_SCR_EP_04: no answers -> 0")
    void noAnswers() {
        assertEquals(0.0, calc.score(QUESTIONS, Map.of()));
    }

    @Test
    @DisplayName("TC_SCR_EP_05: score 11 of 20 -> 55.0 %")
    void percentage() {
        assertEquals(55.0, calc.percentage(11, calc.maxScore(QUESTIONS)), 0.0001);
    }

    @Test
    @DisplayName("TC_SCR_EP_06: section B unanswered -> allSectionsAttempted = false")
    void sectionNotAttempted() {
        assertFalse(calc.allSectionsAttempted(QUESTIONS, Map.of(1, 0, 2, 1)));
    }
}

package com.oes.result;

import com.oes.exam.Question;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scoring, grading and result rules (Test Plan REQ-14..REQ-16).
 */
public class ResultCalculator {

    public static final double MARKS_PER_CORRECT = 4.0;
    public static final double NEGATIVE_MARKS_PER_WRONG = 1.0;
    public static final double PASS_PERCENTAGE = 40.0;

    /** +4 per correct, -1 per wrong, 0 for unanswered; never below 0. */
    public double score(List<Question> questions, Map<Integer, Integer> answers) {
        double total = 0;
        for (Question q : questions) {
            Integer chosen = answers.get(q.id());
            if (chosen == null) {
                continue;
            }
            total += (chosen == q.correctOption()) ? MARKS_PER_CORRECT : -NEGATIVE_MARKS_PER_WRONG;
        }
        return Math.max(0, total);
    }

    public double maxScore(List<Question> questions) {
        return questions.size() * MARKS_PER_CORRECT;
    }

    public double percentage(double score, double maxScore) {
        if (maxScore <= 0) {
            throw new IllegalArgumentException("maxScore must be positive");
        }
        return score * 100.0 / maxScore;
    }

    /** 90-100 A+, 75-89 A, 60-74 B, 40-59 C, 0-39 F. */
    public String grade(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        if (percentage >= 90) {
            return "A+";
        } else if (percentage >= 75) {
            return "A";
        } else if (percentage >= 60) {
            return "B";
        } else if (percentage >= 40) {
            return "C";
        }
        return "F";
    }

    /** True if at least one question of every section has been answered. */
    public boolean allSectionsAttempted(List<Question> questions, Map<Integer, Integer> answers) {
        Set<String> sections = questions.stream().map(Question::section).collect(Collectors.toSet());
        Set<String> attempted = questions.stream()
                .filter(q -> answers.containsKey(q.id()))
                .map(Question::section)
                .collect(Collectors.toSet());
        return attempted.containsAll(sections);
    }

    /**
     * Malpractice -> WITHHELD (regardless of marks).
     * Otherwise PASS only if percentage >= 40 and all sections attempted, else FAIL.
     */
    public ResultStatus status(double percentage, boolean malpractice, boolean allSectionsAttempted) {
        if (malpractice) {
            return ResultStatus.WITHHELD;
        }
        if (percentage < PASS_PERCENTAGE || !allSectionsAttempted) {
            return ResultStatus.FAIL;
        }
        return ResultStatus.PASS;
    }
}

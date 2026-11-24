package com.oes.exam;

import java.util.List;

/**
 * A multiple-choice question.
 *
 * @param id            unique question id
 * @param text          question text
 * @param options       answer options (index 0..n-1)
 * @param correctOption index of the correct option
 * @param section       exam section the question belongs to (e.g. "A", "B")
 */
public record Question(int id, String text, List<String> options, int correctOption, String section) {

    public Question {
        options = List.copyOf(options);
        if (correctOption < 0 || correctOption >= options.size()) {
            throw new IllegalArgumentException("correctOption out of range");
        }
    }
}

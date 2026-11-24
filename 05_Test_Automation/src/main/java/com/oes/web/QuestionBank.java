package com.oes.web;

import com.oes.exam.Question;

import java.util.List;
import java.util.Map;

/** The question paper served by the web UI: 10 questions in two sections, 40 marks. */
final class QuestionBank {

    static final String TITLE = "Software Testing — Internal Assessment";

    static final Map<String, String> SECTION_TITLES = Map.of(
            "A", "Testing Fundamentals",
            "B", "Test Design Techniques");

    private static final List<Question> QUESTIONS = List.of(
            new Question(1, "Which level of testing verifies individual units of source code in isolation?",
                    List.of("Unit testing", "Integration testing", "System testing", "Acceptance testing"), 0, "A"),
            new Question(2, "A human action that produces an incorrect result is called a(n):",
                    List.of("Defect", "Failure", "Error (mistake)", "Incident"), 2, "A"),
            new Question(3, "Which document describes the scope, approach, resources and schedule of testing?",
                    List.of("Test case specification", "Test plan", "Defect report", "Test summary report"), 1, "A"),
            new Question(4, "Re-running previously passed tests after a change, to make sure nothing else broke, is called:",
                    List.of("Retesting", "Smoke testing", "Regression testing", "Exploratory testing"), 2, "A"),
            new Question(5, "Which of the following is a white-box testing technique?",
                    List.of("Boundary value analysis", "Statement coverage", "Equivalence partitioning", "Use case testing"), 1, "A"),
            new Question(6, "A field accepts ages from 16 to 60 inclusive. Which set contains only boundary values?",
                    List.of("15, 16, 60, 61", "20, 30, 40, 50", "0, 100, -1, 1000", "16, 38, 60, 99"), 0, "B"),
            new Question(7, "Equivalence partitioning divides the input domain into:",
                    List.of("Individual values", "Classes of data expected to behave the same way",
                            "Random samples", "Branches of the code"), 1, "B"),
            new Question(8, "A full decision table with 3 independent true/false conditions has how many rules?",
                    List.of("3", "6", "8", "9"), 2, "B"),
            new Question(9, "State transition testing is best suited for:",
                    List.of("Arithmetic calculations", "Systems whose behaviour depends on earlier events",
                            "Report layout", "Database indexing"), 1, "B"),
            new Question(10, "Which JUnit 5 annotation runs the same test method with different inputs?",
                    List.of("@Test", "@BeforeEach", "@ParameterizedTest", "@Disabled"), 2, "B"));

    private QuestionBank() {
    }

    static List<Question> questions() {
        return QUESTIONS;
    }
}

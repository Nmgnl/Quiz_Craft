package Naomi;

import java.util.*;

public class Quiz {
    private String title;
    private QuizTag tag;
    private List<String> questions;
    private List<List<String>> answers; // For multiple-choice options
    private List<String> correctAnswers;
    private QuizType type;

    public Quiz(String title, QuizTag tag, List<String> questions, List<List<String>> answers, List<String> correctAnswers, QuizType type) {
        this.title = title;
        this.tag = tag;
        this.questions = questions;
        this.answers = answers;
        this.correctAnswers = correctAnswers;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public QuizTag getTag() {
        return tag;
    }

    public QuizType getType() {
        return type;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public void displayQuiz() {
        System.out.println("\nQuiz Title: " + title);
        System.out.println("Tag: " + tag);
        System.out.println("Quiz Type: " + type);
        for (int i = 0; i < questions.size(); i++) {
            System.out.println("\nQ" + (i + 1) + ": " + questions.get(i));
            if (type == QuizType.MULTIPLE_CHOICE) {
                List<String> options = answers.get(i);
                char optionLabel = 'A';
                for (String option : options) {
                    System.out.println(optionLabel + ") " + option);
                    optionLabel++;
                }
            } else {
                System.out.println("Answer: " + correctAnswers.get(i));
            }
        }
    }

    public boolean checkAnswer(int questionIndex, String userAnswer) {
        return correctAnswers.get(questionIndex).equalsIgnoreCase(userAnswer.trim());
    }
}
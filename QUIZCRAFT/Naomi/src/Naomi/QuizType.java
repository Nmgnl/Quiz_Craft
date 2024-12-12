package Naomi;


public enum QuizType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    IDENTIFICATION;

    public static QuizType fromString(String type) {
        try {
            return QuizType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid quiz type: " + type);
            return null;
        }
    }
}
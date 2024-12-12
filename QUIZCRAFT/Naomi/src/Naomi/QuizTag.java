package Naomi;

public enum QuizTag {
    SCIENCE, MATH, ENGLISH, FILIPINO, SOCIAL_STUDIES, MAPEH;

    public static QuizTag fromString(String tag) {
        try {
            return QuizTag.valueOf(tag.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid quiz tag: " + tag);
            return null;
        }
    }
}
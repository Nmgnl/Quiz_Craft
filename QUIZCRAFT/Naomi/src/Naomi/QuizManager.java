// QuizManager.java
package Naomi;

import java.sql.*;
import java.util.*;

public class QuizManager {
    private int userId;

    public QuizManager(int userId) {
        this.userId = userId;
    }

 // CREATE A QUIZ
    public void createQuiz(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.print("Quiz Title: ");
            String title = scanner.nextLine();
            System.out.print("Quiz Tag (SCIENCE, MATH, etc.): ");
            String tag = scanner.nextLine();
            System.out.print("Quiz Type (MULTIPLE_CHOICE, TRUE_FALSE, IDENTIFICATION): ");
            String type = scanner.nextLine();

            String insertQuiz = "INSERT INTO quizzes (title, tag, type) VALUES (?, ?, ?)";
            PreparedStatement quizStmt = conn.prepareStatement(insertQuiz, Statement.RETURN_GENERATED_KEYS);
            quizStmt.setString(1, title);
            quizStmt.setString(2, tag);
            quizStmt.setString(3, type);
            quizStmt.executeUpdate();

            ResultSet generatedKeys = quizStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int quizId = generatedKeys.getInt(1);

                System.out.print("Number of Questions: ");
                int numQuestions = Integer.parseInt(scanner.nextLine());

                for (int i = 0; i < numQuestions; i++) {
                    System.out.print("Question " + (i + 1) + ": ");
                    String questionText = scanner.nextLine();
                    String correctAnswer = "";

                    String[] choices = new String[4]; // Declare choices array here to be used outside the block

                    if (type.equals("MULTIPLE_CHOICE")) {
                        System.out.println("Enter choices for this multiple choice question:");

                        // Collecting choices for multiple-choice question
                        for (char choice = 'A'; choice <= 'D'; choice++) {
                            System.out.print("Choice " + choice + ": ");
                            choices[choice - 'A'] = scanner.nextLine();
                        }

                        // Display choices
                        System.out.println("Choices:");
                        for (char choice = 'A'; choice <= 'D'; choice++) {
                            System.out.println(choice + ": " + choices[choice - 'A']);
                        }

                        boolean validAnswer = false;
                        while (!validAnswer) {
                            System.out.print("Enter correct answer (A, B, C, D): ");
                            correctAnswer = scanner.nextLine().toUpperCase();

                            if (correctAnswer.equals("A") || correctAnswer.equals("B") || correctAnswer.equals("C") || correctAnswer.equals("D")) {
                                validAnswer = true;
                            } else {
                                System.out.println("Invalid answer. Please enter A, B, C, or D.");
                            }
                        }
                    } else {
                        System.out.print("Correct Answer: ");
                        correctAnswer = scanner.nextLine();
                    }

                    // Insert question into the questions table
                    String insertQuestion = "INSERT INTO questions (quiz_id, question_text, correct_answer) VALUES (?, ?, ?)";
                    PreparedStatement questionStmt = conn.prepareStatement(insertQuestion, Statement.RETURN_GENERATED_KEYS);
                    questionStmt.setInt(1, quizId);
                    questionStmt.setString(2, questionText);
                    questionStmt.setString(3, correctAnswer);
                    questionStmt.executeUpdate();

                    ResultSet questionGeneratedKeys = questionStmt.getGeneratedKeys();
                    if (questionGeneratedKeys.next()) {
                        int questionId = questionGeneratedKeys.getInt(1);

                        // If multiple-choice, save options into the options table
                        if (type.equals("MULTIPLE_CHOICE")) {
                            for (char choice = 'A'; choice <= 'D'; choice++) {
                                String optionText = choices[choice - 'A'];
                                boolean isCorrect = (String.valueOf(choice).equals(correctAnswer));

                                String insertOption = "INSERT INTO options (question_id, option_text, option_letter, is_correct) VALUES (?, ?, ?, ?)";
                                PreparedStatement optionStmt = conn.prepareStatement(insertOption);
                                optionStmt.setInt(1, questionId);
                                optionStmt.setString(2, optionText);
                                optionStmt.setString(3, String.valueOf(choice));
                                optionStmt.setBoolean(4, isCorrect);
                                optionStmt.executeUpdate();
                            }
                        }
                    }
                }

                System.out.println("Quiz created successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }




    // DISPLAY ALL QUIZZES
    public void displayAllQuizzes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM quizzes";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No quizzes available.");
            } else {
                while (rs.next()) {
                    System.out.println(rs.getString("title") + " (" + rs.getString("tag") + ")");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


 // EDIT A QUIZ
    public void editQuiz(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Display available quizzes first
            String getQuizzesQuery = "SELECT title FROM quizzes";
            PreparedStatement getQuizzesStmt = conn.prepareStatement(getQuizzesQuery);
            ResultSet quizzesRs = getQuizzesStmt.executeQuery();

            // Check if any quizzes are available
            if (!quizzesRs.isBeforeFirst()) {
                System.out.println("No quizzes to edit, go create one.");
                return;  // Exit the method if no quizzes are available
            }

            // Show available quizzes
            System.out.println("Available Quizzes:");
            while (quizzesRs.next()) {
                System.out.println("- " + quizzesRs.getString("title"));
            }

            // Ask user for the quiz to edit
            System.out.print("\nEnter the title of the quiz to edit: ");
            String title = scanner.nextLine();

            // Retrieve quiz id based on title
            String getQuizQuery = "SELECT id, title, type FROM quizzes WHERE title = ?";
            PreparedStatement stmt = conn.prepareStatement(getQuizQuery);
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int quizId = rs.getInt("id");
                String currentTitle = rs.getString("title");
                String quizType = rs.getString("type");

                // Optionally change the quiz title
                System.out.print("New Title (leave blank to keep the same): ");
                String newTitle = scanner.nextLine();

                if (!newTitle.isEmpty()) {
                    // Update quiz title if provided
                    String updateQuiz = "UPDATE quizzes SET title = ? WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuiz);
                    updateStmt.setString(1, newTitle);
                    updateStmt.setInt(2, quizId);
                    updateStmt.executeUpdate();
                    System.out.println("Quiz title updated to: " + newTitle);
                } else {
                    System.out.println("Quiz title remains: " + currentTitle);
                }

                // Fetch existing questions
                String getQuestionsQuery = "SELECT id, question_text, correct_answer FROM questions WHERE quiz_id = ?";
                PreparedStatement questionStmt = conn.prepareStatement(getQuestionsQuery);
                questionStmt.setInt(1, quizId);
                ResultSet questionRs = questionStmt.executeQuery();

                int questionCount = 0;
                while (questionRs.next()) {
                    questionCount++;
                    System.out.println("\nEditing Question " + questionCount + ": ");
                    String questionText = questionRs.getString("question_text");
                    String correctAnswer = questionRs.getString("correct_answer");

                    // Show current question and answer
                    System.out.println("Current Question: " + questionText);
                    System.out.println("Current Correct Answer: " + correctAnswer);

                    // For multiple-choice, display choices and allow editing
                    if (quizType.equals("MULTIPLE_CHOICE")) {
                        String getOptionsQuery = "SELECT option_letter, option_text, is_correct FROM options WHERE question_id = ?";
                        PreparedStatement optionStmt = conn.prepareStatement(getOptionsQuery);
                        optionStmt.setInt(1, questionRs.getInt("id"));
                        ResultSet optionsRs = optionStmt.executeQuery();

                        System.out.println("Current Choices (A, B, C, D):");
                        while (optionsRs.next()) {
                            System.out.println(optionsRs.getString("option_letter") + ": " + optionsRs.getString("option_text"));
                        }

                        // Allow user to edit options and correct answer
                        System.out.print("New Correct Answer (A, B, C, D): ");
                        correctAnswer = scanner.nextLine().toUpperCase();
                        while (!correctAnswer.matches("[A-D]")) {
                            System.out.println("Invalid choice. Please select A, B, C, or D.");
                            correctAnswer = scanner.nextLine().toUpperCase();
                        }

                        // Update is_correct flags for options
                        String updateOptionQuery = "UPDATE options SET is_correct = ? WHERE question_id = ? AND option_letter = ?";
                        PreparedStatement updateOptionStmt = conn.prepareStatement(updateOptionQuery);
                        for (char choice = 'A'; choice <= 'D'; choice++) {
                            boolean isCorrect = (String.valueOf(choice).equals(correctAnswer));
                            updateOptionStmt.setBoolean(1, isCorrect);
                            updateOptionStmt.setInt(2, questionRs.getInt("id"));
                            updateOptionStmt.setString(3, String.valueOf(choice));
                            updateOptionStmt.executeUpdate();
                        }
                    }

                    // Allow user to edit the question text (non-multiple-choice questions also)
                    System.out.print("New Question (leave blank to keep the same): ");
                    String newQuestionText = scanner.nextLine();
                    if (!newQuestionText.isEmpty()) {
                        questionText = newQuestionText;
                    }

                    // Update question and answer in the database
                    String updateQuestionQuery = "UPDATE questions SET question_text = ?, correct_answer = ? WHERE id = ?";
                    PreparedStatement updateQuestionStmt = conn.prepareStatement(updateQuestionQuery);
                    updateQuestionStmt.setString(1, questionText);
                    updateQuestionStmt.setString(2, correctAnswer);
                    updateQuestionStmt.setInt(3, questionRs.getInt("id"));
                    updateQuestionStmt.executeUpdate();

                    System.out.println("Question " + questionCount + " updated successfully.");
                }

                // Option to add new questions (optional)
                System.out.print("Do you want to add new questions? (yes/no): ");
                String addMoreQuestions = scanner.nextLine();
                if (addMoreQuestions.equalsIgnoreCase("yes")) {
                    System.out.print("Enter number of new questions to add: ");
                    int newQuestionsCount = Integer.parseInt(scanner.nextLine());

                    for (int i = 0; i < newQuestionsCount; i++) {
                        System.out.println("Enter details for New Question " + (questionCount + 1 + i));
                        System.out.print("Question: ");
                        String newQuestion = scanner.nextLine();
                        System.out.print("Correct Answer: ");
                        String newAnswer = scanner.nextLine();

                        // Insert new question into the database
                        String insertNewQuestionQuery = "INSERT INTO questions (quiz_id, question_text, correct_answer) VALUES (?, ?, ?)";
                        PreparedStatement insertNewQuestionStmt = conn.prepareStatement(insertNewQuestionQuery);
                        insertNewQuestionStmt.setInt(1, quizId);
                        insertNewQuestionStmt.setString(2, newQuestion);
                        insertNewQuestionStmt.setString(3, newAnswer);
                        insertNewQuestionStmt.executeUpdate();

                        System.out.println("New question added successfully.");
                    }
                }
            } else {
                System.out.println("Quiz not found with the title: " + title);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }



 // DELETE A QUIZ
    public void deleteQuiz(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Display available quizzes first
            String getQuizzesQuery = "SELECT title FROM quizzes";
            PreparedStatement getQuizzesStmt = conn.prepareStatement(getQuizzesQuery);
            ResultSet quizzesRs = getQuizzesStmt.executeQuery();

            // Check if any quizzes are available
            if (!quizzesRs.isBeforeFirst()) {
                System.out.println("No quizzes to be deleted, go create one.");
                return;  // Exit the method if no quizzes are available
            }

            // Show available quizzes
            System.out.println("Available Quizzes:");
            while (quizzesRs.next()) {
                System.out.println("- " + quizzesRs.getString("title"));
            }

            // Ask user for the quiz to delete
            System.out.print("\nEnter the title of the quiz to delete: ");
            String title = scanner.nextLine();

            // First, delete related records from the progress table
            String deleteProgressQuery = "DELETE FROM progress WHERE quiz_id = (SELECT id FROM quizzes WHERE title = ?)";
            PreparedStatement deleteProgressStmt = conn.prepareStatement(deleteProgressQuery);
            deleteProgressStmt.setString(1, title);
            deleteProgressStmt.executeUpdate();

            // Delete related records from the bookmarks table
            String deleteBookmarksQuery = "DELETE FROM bookmarks WHERE quiz_id = (SELECT id FROM quizzes WHERE title = ?)";
            PreparedStatement deleteBookmarksStmt = conn.prepareStatement(deleteBookmarksQuery);
            deleteBookmarksStmt.setString(1, title);
            deleteBookmarksStmt.executeUpdate();

            // Now, delete the quiz itself
            String deleteQuizQuery = "DELETE FROM quizzes WHERE title = ?";
            PreparedStatement deleteQuizStmt = conn.prepareStatement(deleteQuizQuery);
            deleteQuizStmt.setString(1, title);

            if (deleteQuizStmt.executeUpdate() > 0) {
                System.out.println("Quiz deleted.");
            } else {
                System.out.println("Quiz not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    // MORE FUNCTIONS HERE (BOOKMARK QUIZ, REVIEW QUIZ, DISPLAY PROGRESS...)
 // BOOKMARK A QUIZ
    public void bookmarkQuiz(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Display available quizzes first
            String getAvailableQuizzes = "SELECT title FROM quizzes";
            PreparedStatement quizStmt = conn.prepareStatement(getAvailableQuizzes);
            ResultSet quizzesRs = quizStmt.executeQuery();

            // Check if any quizzes are available
            if (!quizzesRs.isBeforeFirst()) {
                System.out.println("No quizzes to be bookmarked, create one.");
                return; // Exit the method if no quizzes are available
            }

            // Show available quizzes
            System.out.println("Available Quizzes to Bookmark:");
            while (quizzesRs.next()) {
                System.out.println("- " + quizzesRs.getString("title"));
            }

            System.out.print("\nEnter the title of the quiz to bookmark: ");
            String title = scanner.nextLine();

            // Get the quiz ID
            String getQuizId = "SELECT id FROM quizzes WHERE title = ?";
            PreparedStatement getQuizStmt = conn.prepareStatement(getQuizId);
            getQuizStmt.setString(1, title);
            ResultSet rs = getQuizStmt.executeQuery();

            if (rs.next()) {
                int quizId = rs.getInt("id");

                // Check if the quiz is already bookmarked
                String checkBookmarkQuery = "SELECT * FROM bookmarks WHERE user_id = ? AND quiz_id = ?";
                PreparedStatement checkBookmarkStmt = conn.prepareStatement(checkBookmarkQuery);
                checkBookmarkStmt.setInt(1, userId); // Assuming userId is set elsewhere
                checkBookmarkStmt.setInt(2, quizId);
                ResultSet checkBookmarkRs = checkBookmarkStmt.executeQuery();

                if (checkBookmarkRs.next()) {
                    System.out.println("This quiz is already bookmarked.");
                } else {
                    // Add to bookmarks
                    String bookmarkQuery = "INSERT INTO bookmarks (user_id, quiz_id) VALUES (?, ?)";
                    PreparedStatement bookmarkStmt = conn.prepareStatement(bookmarkQuery);
                    bookmarkStmt.setInt(1, userId);
                    bookmarkStmt.setInt(2, quizId);
                    bookmarkStmt.executeUpdate();

                    System.out.println("Quiz bookmarked successfully.");
                }
            } else {
                System.out.println("Quiz not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    
 // DISPLAY BOOKMARKED QUIZZES
    public void displayBookmarkedQuizzes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT quizzes.title FROM bookmarks JOIN quizzes ON bookmarks.quiz_id = quizzes.id WHERE bookmarks.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId); // Assuming userId is set elsewhere

            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) {  // Check if the result set is empty
                System.out.println("No bookmarked quizzes.");
            } else {
                System.out.println("Your Bookmarked Quizzes:");
                while (rs.next()) {
                    System.out.println("- " + rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


 // REVIEW A QUIZ
    public void reviewQuiz(Scanner scanner) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Display available quizzes first
            String getQuizzes = "SELECT title FROM quizzes";
            PreparedStatement quizStmt = conn.prepareStatement(getQuizzes);
            ResultSet quizzesRs = quizStmt.executeQuery();

            // Check if any quizzes are available
            if (!quizzesRs.next()) {
                System.out.println("No quizzes available, go create one.");
                return; // Exit the method if no quizzes are available
            }

            // Show available quizzes
            System.out.println("Available Quizzes:");
            do {  // Iterate through all quizzes and display them
                System.out.println("- " + quizzesRs.getString("title"));
            } while (quizzesRs.next());  // Loop while there are more quizzes

            System.out.print("\nEnter the title of the quiz to review: ");
            String title = scanner.nextLine();

            // Find the quiz
            String getQuizId = "SELECT id, type FROM quizzes WHERE title = ?";
            PreparedStatement findQuizStmt = conn.prepareStatement(getQuizId);
            findQuizStmt.setString(1, title);
            ResultSet rs = findQuizStmt.executeQuery();

            if (rs.next()) {
                int quizId = rs.getInt("id");
                String quizType = rs.getString("type");

                // Get shuffled questions for the quiz (only for review)
                String getQuestions = "SELECT id, question_text, correct_answer FROM questions WHERE quiz_id = ? ORDER BY RAND()"; // Shuffle questions only for review
                PreparedStatement questionStmt = conn.prepareStatement(getQuestions);
                questionStmt.setInt(1, quizId);
                ResultSet questionRs = questionStmt.executeQuery();

                int correctCount = 0;
                int questionCount = 0;

                while (questionRs.next()) {
                    questionCount++;
                    System.out.println("\nQuestion " + questionCount + ": " + questionRs.getString("question_text"));

                    // Handling for multiple-choice questions
                    if (quizType.equals("MULTIPLE_CHOICE")) {
                        // Fetch shuffled options for this question from the options table (only for review)
                        String getOptionsQuery = "SELECT option_letter, option_text FROM options WHERE question_id = ? ORDER BY RAND()"; // Shuffle options for review
                        PreparedStatement optionStmt = conn.prepareStatement(getOptionsQuery);
                        optionStmt.setInt(1, questionRs.getInt("id"));
                        ResultSet optionsRs = optionStmt.executeQuery();

                        // Display shuffled choices for multiple-choice questions
                        while (optionsRs.next()) {
                            System.out.println(optionsRs.getString("option_letter") + ": " + optionsRs.getString("option_text"));
                        }

                        // Prompt user to answer the question
                        System.out.print("Your Answer (A, B, C, D): ");
                        String userAnswer = scanner.nextLine().toUpperCase();

                        // Check if the user answer is correct
                        if (userAnswer.equalsIgnoreCase(questionRs.getString("correct_answer"))) {
                            correctCount++;
                        }
                    } else {
                        // For other quiz types (e.g., TRUE_FALSE, IDENTIFICATION)
                        System.out.print("Your Answer: ");
                        String userAnswer = scanner.nextLine();

                        if (userAnswer.equalsIgnoreCase(questionRs.getString("correct_answer"))) {
                            correctCount++;
                        }
                    }
                }

                // Calculate progress status
                String status = "";
                int percentage = (correctCount * 100) / questionCount;

                if (percentage == 100) {
                    status = "Mastered";
                } else if (percentage >= 60) {
                    status = "Almost Done";
                } else {
                    status = "Needs Improvement";
                }

                // Save progress
                String saveProgress = "INSERT INTO progress (user_id, quiz_id, correct_answers, incorrect_answers, status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement progressStmt = conn.prepareStatement(saveProgress);
                progressStmt.setInt(1, userId); // Assuming userId is set elsewhere
                progressStmt.setInt(2, quizId);
                progressStmt.setInt(3, correctCount);
                progressStmt.setInt(4, questionCount - correctCount); // Incorrect answers = total - correct
                progressStmt.setString(5, status);
                progressStmt.executeUpdate();

                // Display score
                System.out.println("\nQuiz Completed!");
                System.out.println("Correct Answers: " + correctCount);
                System.out.println("Incorrect Answers: " + (questionCount - correctCount));
                System.out.println("Status: " + status);
            } else {
                System.out.println("Quiz not found with the title: " + title);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


 // DISPLAY PROGRESS
    public void displayProgress() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to retrieve quiz progress for the specific user
            String query = "SELECT quizzes.title, progress.correct_answers, progress.incorrect_answers, progress.status " +
                           "FROM progress " +
                           "JOIN quizzes ON progress.quiz_id = quizzes.id " +
                           "WHERE progress.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId); // Ensure userId is correctly set elsewhere in your program

            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) {  // Check if the result set is empty
                System.out.println("No progress available.");
            } else {
                System.out.println("Your Quiz Progress:");
                while (rs.next()) {
                    String title = rs.getString("title");
                    int correct = rs.getInt("correct_answers");
                    int incorrect = rs.getInt("incorrect_answers");
                    String status = rs.getString("status"); // Retrieve the status directly from the progress table

                    // Display the progress with status
                    System.out.println("- " + title + ": " + correct + " Correct, " + incorrect + " Incorrect, Status: " + status);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // SAVE PROGRESS
    public void saveProgress(int quizId, int correctAnswers, int totalQuestions) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check for division by zero
            if (totalQuestions == 0) {
                System.out.println("Error: Total questions cannot be zero.");
                return;
            }

            // Calculate the percentage of correct answers
            int percentage = (correctAnswers * 100) / totalQuestions;

            // Determine the status based on the percentage
            String status = "";
            if (percentage == 100) {
                status = "Mastered";
            } else if (percentage >= 60) {
                status = "Almost Done";
            } else {
                status = "New";
            }

            // Debugging output for correct answers, total questions, and status
            System.out.println("Correct Answers: " + correctAnswers);
            System.out.println("Total Questions: " + totalQuestions);
            System.out.println("Percentage: " + percentage + "%");
            System.out.println("Status: " + status);

            // Insert or update the user's progress and status in the progress table
            String insertProgressQuery = "INSERT INTO progress (user_id, quiz_id, correct_answers, incorrect_answers, status) " +
                                         "VALUES (?, ?, ?, ?, ?) " +
                                         "ON DUPLICATE KEY UPDATE correct_answers = ?, incorrect_answers = ?, status = ?";
            PreparedStatement stmt = conn.prepareStatement(insertProgressQuery);
            stmt.setInt(1, userId);  // Ensure that userId is passed correctly
            stmt.setInt(2, quizId);
            stmt.setInt(3, correctAnswers);
            stmt.setInt(4, totalQuestions - correctAnswers); // Incorrect answers = total - correct
            stmt.setString(5, status);

            // For update, add the updated values in the ON DUPLICATE KEY UPDATE clause
            stmt.setInt(6, correctAnswers);
            stmt.setInt(7, totalQuestions - correctAnswers); // Incorrect answers
            stmt.setString(8, status);

            stmt.executeUpdate();

            System.out.println("Progress saved! Status: " + status);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


}

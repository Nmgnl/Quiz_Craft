package Naomi;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserManager userManager = new UserManager();

        
        while (true) {
        	System.out.print("Welcome to QuizCraft!!!");
            System.out.println("\n1. Log In\n2. Sign Up\n3. Exit");
            System.out.print("Please choose one action: ");
            String choice = scanner.nextLine();

            
            if (choice.equals("1")) {
                if (userManager.logIn(scanner)) {
                    QuizManager manager = new QuizManager(userManager.getLoggedInUserId());
                    runMainMenu(scanner, manager, userManager); // Pass userManager for logout
                }
            } else if (choice.equals("2")) {
                userManager.signUp(scanner);
            } else if (choice.equals("3")) {
                System.out.println("Goodbye!");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private static void runMainMenu(Scanner scanner, QuizManager manager, UserManager userManager) {
        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Create Quiz");
            System.out.println("2. Display Quizzes");
            System.out.println("3. Edit Quiz");
            System.out.println("4. Delete Quiz");
            System.out.println("5. Review Quiz");
            System.out.println("6. Bookmark Quiz");
            System.out.println("7. View Bookmarked Quizzes");
            System.out.println("8. View Progress");
            System.out.println("9. Logout");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    manager.createQuiz(scanner);
                    break;
                case "2":
                    manager.displayAllQuizzes();
                    break;
                case "3":
                    manager.editQuiz(scanner);
                    break;
                case "4":
                    manager.deleteQuiz(scanner);
                    break;
                case "5":
                    manager.reviewQuiz(scanner);
                    break;
                case "6":
                    manager.bookmarkQuiz(scanner);
                    break;
                case "7":
                    manager.displayBookmarkedQuizzes();
                    break;
                case "8":
                    manager.displayProgress();
                    break;  
                case "9":
                    userManager.logOut(); // Call logOut in UserManager
                    return; // Exit to the login/signup menu
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import model.*;
import service.exportService;
import service.importService;
import service.searchService;

public class Main {

    // In-memory storage
    public static List<Task> tasks = new ArrayList<>();
    public static Map<String, Project> projects = new HashMap<>();
    public static Map<String, Collaborator> collaborators = new HashMap<>();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Ask user to specify the CSV file
        System.out.print("Enter CSV file path to import: ");
        String filePath = scanner.nextLine();

        // Check if file exists
        File csvFile = new File(filePath);
        if (!csvFile.exists()) {
            System.out.println("File not found: " + filePath);
            return;  // Exit if file doesn't exist
        }

        // Import tasks from the specified file
        importService.importTasks(filePath, tasks, projects, collaborators);

        // Print imported tasks
        System.out.println("\nImported Tasks:");
        for (Task t : tasks) {
            System.out.println(t);
        }

        System.out.println("\nProjects loaded: " + projects.size());
        System.out.println("Collaborators loaded: " + collaborators.size());

        System.out.println("\nSearch options:");
        System.out.println("1. Keyword");
        System.out.println("2. Status");
        System.out.println("3. Priority");
        System.out.println("4. Project");
        System.out.println("5. Collaborator");
        System.out.println("6. Due date");
        System.out.print("Choose a search option (1-6): ");

        String choice = scanner.nextLine().trim();
        List<Task> results = new ArrayList<>();

        switch (choice) {
            case "1":
                System.out.print("Enter keyword: ");
                results = searchService.searchByKeyword(tasks, scanner.nextLine());
                break;
            case "2":
                System.out.print("Enter status: ");
                results = searchService.searchByStatus(tasks, scanner.nextLine());
                break;
            case "3":
                System.out.print("Enter priority: ");
                results = searchService.searchByPriority(tasks, scanner.nextLine());
                break;
            case "4":
                System.out.print("Enter project name: ");
                results = searchService.searchByProject(tasks, scanner.nextLine());
                break;
            case "5":
                System.out.print("Enter collaborator name: ");
                results = searchService.searchByCollaborator(tasks, scanner.nextLine());
                break;
            case "6":
                System.out.print("Enter due date (yyyy-MM-dd): ");
                String dueDateInput = scanner.nextLine().trim();

                try {
                    LocalDate dueDate = LocalDate.parse(dueDateInput);
                    results = searchService.searchByDueDate(tasks, dueDate);
                } catch (Exception e) {
                    System.out.println("Invalid date format. Use yyyy-MM-dd.");
                    return;
                }
                break;
            default:
                System.out.println("Invalid option.");
                return;
        }

        System.out.println("\nSearch Results:");
        if (results.isEmpty()) {
            System.out.println("No matching tasks found.");
        } else {
            for (Task task : results) {
                System.out.println(task);
            }
        }

        // Export full in-memory database to CSV
        System.out.print("\nEnter output CSV file path for export (or press Enter for exported_tasks.csv): ");
        String outputPath = scanner.nextLine().trim();
        if (outputPath.isEmpty()) {
            outputPath = "exported_tasks.csv";
        }

        exportService.exportDatabase(outputPath, tasks, projects, collaborators);
    }
}

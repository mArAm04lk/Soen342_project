import model.*;
import service.importService;  // lowercase i, matches your file/class
import java.util.*;
import java.io.File;

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
    }
}
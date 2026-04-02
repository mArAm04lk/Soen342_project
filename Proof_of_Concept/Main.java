import model.*;
import service.CalendarExportGateway;
import service.ICal4jCalendarGateway;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
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

        System.out.println("\nExport options:");
        System.out.println("1. Export filtered search results");
        System.out.println("2. Export a single task");
        System.out.println("3. Export all tasks in a project");
        System.out.println("4. Export all tasks");
        System.out.println("5. Skip export");
        System.out.print("Choose an export scope (1-5): ");

        String exportScopeChoice = scanner.nextLine().trim();
        List<Task> tasksToExport = new ArrayList<>();

        switch (exportScopeChoice) {
            case "1":
                tasksToExport = new ArrayList<>(results);
                break;
            case "2":
                if (tasks.isEmpty()) {
                    System.out.println("No tasks available to export.");
                    scanner.close();
                    return;
                }

                System.out.println("\nAvailable tasks:");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println((i + 1) + ". " + tasks.get(i));
                }

                System.out.print("Enter task number to export: ");
                try {
                    int taskIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (taskIndex < 0 || taskIndex >= tasks.size()) {
                        System.out.println("Invalid task number.");
                        scanner.close();
                        return;
                    }
                    tasksToExport.add(tasks.get(taskIndex));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid task number.");
                    scanner.close();
                    return;
                }
                break;
            case "3":
                System.out.print("Enter project name: ");
                String projectName = scanner.nextLine().trim();
                Project selectedProject = projects.get(projectName);

                if (selectedProject == null) {
                    for (Project project : projects.values()) {
                        if (project.getName() != null && project.getName().equalsIgnoreCase(projectName)) {
                            selectedProject = project;
                            break;
                        }
                    }
                }

                if (selectedProject == null) {
                    System.out.println("Project not found.");
                    scanner.close();
                    return;
                }

                tasksToExport = new ArrayList<>(selectedProject.getTasks());
                break;
            case "4":
                tasksToExport = new ArrayList<>(tasks);
                break;
            case "5":
                System.out.println("Export skipped.");
                scanner.close();
                return;
            default:
                System.out.println("Invalid export option.");
                scanner.close();
                return;
        }

        if (tasksToExport.isEmpty()) {
            System.out.println("No tasks match the selected export scope.");
            scanner.close();
            return;
        }

        System.out.println("\nExport format options:");
        System.out.println("1. CSV");
        System.out.println("2. iCal (.ics)");
        System.out.print("Choose an export format (1-2): ");

        String exportFormatChoice = scanner.nextLine().trim();
        switch (exportFormatChoice) {
            case "1":
                System.out.print("Enter output CSV file path (default: exported_tasks.csv): ");
                String csvOutputPath = scanner.nextLine().trim();
                if (csvOutputPath.isEmpty()) {
                    csvOutputPath = "exported_tasks.csv";
                }

                if (exportScopeChoice.equals("4")) {
                    exportService.exportDatabase(csvOutputPath, tasks, projects, collaborators);
                } else {
                    exportService.exportTasks(csvOutputPath, tasksToExport);
                }
                break;
            case "2":
                System.out.print("Enter output .ics file path (default: tasks.ics): ");
                String icsOutputPath = scanner.nextLine().trim();
                if (icsOutputPath.isEmpty()) {
                    icsOutputPath = "tasks.ics";
                }

                CalendarExportGateway gateway = new ICal4jCalendarGateway();
                gateway.exportTasks(tasksToExport, icsOutputPath);
                System.out.println("iCal export complete: " + icsOutputPath);
                break;
            default:
                System.out.println("Invalid export format.");
                scanner.close();
                return;
        }

        scanner.close();
    }
}

import model.*;
import service.CalendarExportGateway;
import service.ICal4jCalendarGateway;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
import service.OverloadService;
import service.exportService;
import service.importService;
import service.searchService;

public class Main {
    private static final String DIVIDER = "============================================================";

    // In-memory storage
    public static List<Task> tasks = new ArrayList<>();
    public static Map<String, Project> projects = new HashMap<>();
    public static Map<String, Collaborator> collaborators = new HashMap<>();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        printBanner("Task Planner Console");
        System.out.print("CSV file to import: ");
        String filePath = scanner.nextLine();

        // Check if file exists
        File csvFile = new File(filePath);
        if (!csvFile.exists()) {
            printMessage("File not found: " + filePath);
            return;  // Exit if file doesn't exist
        }

        // Import tasks from the specified file
        importService.importTasks(filePath, tasks, projects, collaborators);

        printSection("Import Summary");
        System.out.println("Tasks loaded         : " + tasks.size());
        System.out.println("Projects loaded      : " + projects.size());
        System.out.println("Collaborators loaded : " + collaborators.size());
        printTaskList("Imported Tasks", tasks, "No tasks were imported.");

        printMenu(
                "Search Options",
                "1. Keyword",
                "2. Status",
                "3. Priority",
                "4. Project",
                "5. Collaborator",
                "6. Due date",
                "7. Overloaded collaborators"
        );
        System.out.print("Select a search option (1-7): ");

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
                    printMessage("Invalid date format. Use yyyy-MM-dd.");
                    return;
                }
                break;
            case "7":
                List<OverloadService.CollaboratorLoad> overloadedCollaborators =
                        OverloadService.findOverloadedCollaborators(tasks);

                printSection("Overloaded Collaborators");
                if (overloadedCollaborators.isEmpty()) {
                    System.out.println("No overloaded collaborators found.");
                } else {
                    int collaboratorNumber = 1;
                    for (OverloadService.CollaboratorLoad load : overloadedCollaborators) {
                        System.out.println(collaboratorNumber++ + ". " + load.getName() + " (" + load.getCategory() + ") - "
                                + load.getAssignmentCount() + " active assignments, "
                                + load.getOverdueCount() + " overdue, "
                                + load.getUpcomingCount() + " due within 7 days");
                    }
                }

                results = OverloadService.findTasksForOverloadedCollaborators(tasks);
                break;
            default:
                printMessage("Invalid option.");
                return;
        }

        printTaskList("Search Results", results, "No matching tasks found.");

        printMenu(
                "Export Options",
                "1. Export filtered search results",
                "2. Export a single task",
                "3. Export all tasks in a project",
                "4. Export all tasks",
                "5. Skip export"
        );
        System.out.print("Choose an export scope (1-5): ");

        String exportScopeChoice = scanner.nextLine().trim();
        List<Task> tasksToExport = new ArrayList<>();

        switch (exportScopeChoice) {
            case "1":
                tasksToExport = new ArrayList<>(results);
                break;
            case "2":
                if (tasks.isEmpty()) {
                    printMessage("No tasks available to export.");
                    scanner.close();
                    return;
                }

                printTaskList("Available Tasks", tasks, "No tasks available.");

                System.out.print("Enter task number to export: ");
                try {
                    int taskIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (taskIndex < 0 || taskIndex >= tasks.size()) {
                        printMessage("Invalid task number.");
                        scanner.close();
                        return;
                    }
                    tasksToExport.add(tasks.get(taskIndex));
                } catch (NumberFormatException e) {
                    printMessage("Invalid task number.");
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
                    printMessage("Project not found.");
                    scanner.close();
                    return;
                }

                tasksToExport = new ArrayList<>(selectedProject.getTasks());
                break;
            case "4":
                tasksToExport = new ArrayList<>(tasks);
                break;
            case "5":
                printMessage("Export skipped.");
                scanner.close();
                return;
            default:
                printMessage("Invalid export option.");
                scanner.close();
                return;
        }

        if (tasksToExport.isEmpty()) {
            printMessage("No tasks match the selected export scope.");
            scanner.close();
            return;
        }

        printMenu(
                "Export Format",
                "1. CSV",
                "2. iCal (.ics)"
        );
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
                printMessage("CSV export complete: " + csvOutputPath);
                break;
            case "2":
                System.out.print("Enter output .ics file path (default: tasks.ics): ");
                String icsOutputPath = scanner.nextLine().trim();
                if (icsOutputPath.isEmpty()) {
                    icsOutputPath = "tasks.ics";
                }

                CalendarExportGateway gateway = new ICal4jCalendarGateway();
                gateway.exportTasks(tasksToExport, icsOutputPath);
                printMessage("iCal export complete: " + icsOutputPath);
                break;
            default:
                printMessage("Invalid export format.");
                scanner.close();
                return;
        }

        scanner.close();
    }

    private static void printBanner(String title) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println(centerText(title));
        System.out.println(DIVIDER);
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("-- " + title + " " + "-".repeat(Math.max(0, 48 - title.length())));
    }

    private static void printMenu(String title, String... options) {
        printSection(title);
        for (String option : options) {
            System.out.println("  " + option);
        }
        System.out.println();
    }

    private static void printTaskList(String title, List<Task> taskList, String emptyMessage) {
        printSection(title);
        if (taskList == null || taskList.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }

        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            System.out.println((i + 1) + ". " + formatTask(task));
        }
    }

    private static String formatTask(Task task) {
        String projectName = task.getProject() != null ? task.getProject().getName() : "No project";
        String collaboratorName = task.getCollaborator() != null ? task.getCollaborator().getName() : "Unassigned";

        return task.getName()
                + " | " + task.getStatus()
                + " | " + task.getPriority()
                + " | due " + task.getDueDate()
                + " | " + projectName
                + " | " + collaboratorName;
    }

    private static void printMessage(String message) {
        System.out.println();
        System.out.println("> " + message);
    }

    private static String centerText(String text) {
        int padding = Math.max(0, (DIVIDER.length() - text.length()) / 2);
        return " ".repeat(padding) + text;
    }
}

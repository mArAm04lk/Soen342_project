import model.*;

import java.util.*;

public class Main {

    // In-memory storage
    public static List<Task> tasks = new ArrayList<>();
    public static Map<String, Project> projects = new HashMap<>();
    public static Map<String, Collaborator> collaborators = new HashMap<>();

    public static void main(String[] args) {

        // Test data
        Project project = new Project("School", "Assignments");
        Collaborator collab = new Collaborator("Alice", "Student");

        Task task = new Task(
                "Finish PoC",
                "Do assignment",
                "",
                "OPEN",
                "HIGH",
                null,
                project,
                collab
        );

        tasks.add(task);

        System.out.println(task);
    }
}
package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Project {
    private String name;
    private String description;
    private List<Task> tasks;

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
        this.tasks = new ArrayList<>();
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    // Iteration 3: Logic to find eligible tasks for iCal (must have due date)
    public List<Task> getExportableTasks() {
        return tasks.stream()
                    .filter(t -> t.getDueDate() != null)
                    .collect(Collectors.toList());
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<Task> getTasks() { return tasks; }
}
package model;

import java.time.LocalDate;

public class Task {
    private Long id;
    private String name;
    private String description;
    private String subtask;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private Project project;
    private Collaborator collaborator;
    public Task(String name, String description, String subtask,
                String status, String priority, LocalDate dueDate,
                Project project, Collaborator collaborator) {
        this(null, name, description, subtask, status, priority, dueDate, project, collaborator);
    }

    public Task(Long id, String name, String description, String subtask,
                String status, String priority, LocalDate dueDate,
                Project project, Collaborator collaborator) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.subtask = subtask;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.project = project;
        this.collaborator = collaborator;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSubtask() { return subtask; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public Project getProject() { return project; }
    public Collaborator getCollaborator() { return collaborator; }

    public void cancelTask() {
        this.status = "cancel";
    }

    @Override
    public String toString() {
        return name + " | " + status + " | " + dueDate;
    }
}

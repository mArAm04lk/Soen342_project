package model;

import java.time.LocalDate;

public class Task {
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

        this.name = name;
        this.description = description;
        this.subtask = subtask;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.project = project;
        this.collaborator = collaborator;
    }

    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public Project getProject() { return project; }
    public Collaborator getCollaborator() { return collaborator; }

    @Override
    public String toString() {
        return name + " | " + status + " | " + dueDate;
    }
}

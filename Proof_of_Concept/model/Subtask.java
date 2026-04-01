package model;

public class Subtask {
    private String title;
    private String status;
    private Task parentTask;
    private Collaborator assignedCollaborator; // Added for Iteration 2/3 collaborator logic

    public Subtask(String title, Task parentTask) {
        this.title = title;
        this.parentTask = parentTask;
        this.status = "open";
    }

    // Iteration 2 Requirement: Completing subtask only provides progress info
    public void completeSubtask() {
        this.status = "completed";
        // Logic would trigger a progress update in parentTask here
    }

    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public Task getParentTask() { return parentTask; }
    public Collaborator getAssignedCollaborator() { return assignedCollaborator; }
    public void setAssignedCollaborator(Collaborator c) { this.assignedCollaborator = c; }
}
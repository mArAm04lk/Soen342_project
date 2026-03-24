package service;

import model.Collaborator;
import model.Project;
import model.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class searchService {

    public static List<Task> searchByKeyword(List<Task> tasks, String keyword) {
        List<Task> matches = new ArrayList<>();

        if (tasks == null || keyword == null || keyword.trim().isEmpty()) {
            return matches;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();

        for (Task task : tasks) {
            if (task == null) {
                continue;
            }

            if (containsIgnoreCase(task.getName(), normalizedKeyword)) {
                matches.add(task);
            }
        }

        return matches;
    }

    public static List<Task> searchByStatus(List<Task> tasks, String status) {
        List<Task> matches = new ArrayList<>();

        if (tasks == null || status == null || status.trim().isEmpty()) {
            return matches;
        }

        String normalizedStatus = status.trim().toLowerCase();

        for (Task task : tasks) {
            if (task != null && containsIgnoreCase(task.getStatus(), normalizedStatus)) {
                matches.add(task);
            }
        }

        return matches;
    }

    public static List<Task> searchByPriority(List<Task> tasks, String priority) {
        List<Task> matches = new ArrayList<>();

        if (tasks == null || priority == null || priority.trim().isEmpty()) {
            return matches;
        }

        String normalizedPriority = priority.trim().toLowerCase();

        for (Task task : tasks) {
            if (task != null && containsIgnoreCase(task.getPriority(), normalizedPriority)) {
                matches.add(task);
            }
        }

        return matches;
    }

    public static List<Task> searchByProject(List<Task> tasks, String projectName) {
        List<Task> matches = new ArrayList<>();

        if (tasks == null || projectName == null || projectName.trim().isEmpty()) {
            return matches;
        }

        String normalizedProjectName = projectName.trim().toLowerCase();

        for (Task task : tasks) {
            if (task == null) {
                continue;
            }

            Project project = task.getProject();
            if (project != null && containsIgnoreCase(project.getName(), normalizedProjectName)) {
                matches.add(task);
            }
        }

        return matches;
    }

    public static List<Task> searchByCollaborator(List<Task> tasks, String collaboratorName) {
        List<Task> matches = new ArrayList<>();

        if (tasks == null || collaboratorName == null || collaboratorName.trim().isEmpty()) {
            return matches;
        }

        String normalizedCollaboratorName = collaboratorName.trim().toLowerCase();

        for (Task task : tasks) {
            if (task == null) {
                continue;
            }

            Collaborator collaborator = task.getCollaborator();
            if (collaborator != null && containsIgnoreCase(collaborator.getName(), normalizedCollaboratorName)) {
                matches.add(task);
            }
        }

        return matches;
    }

    public static List<Task> searchByDueDate(List<Task> tasks, LocalDate dueDate) {
        List<Task> matches = new ArrayList<>();

        if (tasks == null || dueDate == null) {
            return matches;
        }

        for (Task task : tasks) {
            if (task != null && dueDate.equals(task.getDueDate())) {
                matches.add(task);
            }
        }

        return matches;
    }

    private static boolean containsIgnoreCase(String value, String searchTerm) {
        return value != null && value.toLowerCase().contains(searchTerm);
    }
}

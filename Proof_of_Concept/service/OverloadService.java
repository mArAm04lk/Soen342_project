package service;

import model.Collaborator;
import model.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OverloadService {

    private static final int UPCOMING_DAYS_THRESHOLD = 7;
    private static final int ASSIGNMENT_THRESHOLD = 2;

    private OverloadService() {
    }

    public static List<CollaboratorLoad> findOverloadedCollaborators(List<Task> tasks) {
        Map<String, CollaboratorLoad> collaboratorLoads = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (Task task : tasks) {
            if (task == null || task.getCollaborator() == null || isCompleted(task)) {
                continue;
            }

            Collaborator collaborator = task.getCollaborator();
            String collaboratorName = collaborator.getName();
            if (collaboratorName == null || collaboratorName.isBlank()) {
                continue;
            }

            CollaboratorLoad load = collaboratorLoads.computeIfAbsent(
                collaboratorName,
                ignored -> new CollaboratorLoad(collaborator)
            );

            load.assignmentCount++;

            LocalDate dueDate = task.getDueDate();
            if (dueDate == null) {
                continue;
            }

            if (dueDate.isBefore(today)) {
                load.overdueCount++;
            } else if (!dueDate.isAfter(today.plusDays(UPCOMING_DAYS_THRESHOLD))) {
                load.upcomingCount++;
            }
        }

        List<CollaboratorLoad> overloadedCollaborators = new ArrayList<>();
        for (CollaboratorLoad load : collaboratorLoads.values()) {
            if (load.assignmentCount >= ASSIGNMENT_THRESHOLD && (load.overdueCount > 0 || load.upcomingCount > 0)) {
                overloadedCollaborators.add(load);
            }
        }

        overloadedCollaborators.sort(Comparator
            .comparingInt(CollaboratorLoad::getAssignmentCount).reversed()
            .thenComparingInt(CollaboratorLoad::getOverdueCount).reversed()
            .thenComparing(CollaboratorLoad::getName, String.CASE_INSENSITIVE_ORDER));

        return overloadedCollaborators;
    }

    public static List<Task> findTasksForOverloadedCollaborators(List<Task> tasks) {
        List<CollaboratorLoad> overloadedCollaborators = findOverloadedCollaborators(tasks);
        Set<String> overloadedNames = new LinkedHashSet<>();

        for (CollaboratorLoad load : overloadedCollaborators) {
            overloadedNames.add(load.getName().toLowerCase());
        }

        List<Task> overloadedTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task == null || task.getCollaborator() == null) {
                continue;
            }

            String collaboratorName = task.getCollaborator().getName();
            if (collaboratorName != null && overloadedNames.contains(collaboratorName.toLowerCase())) {
                overloadedTasks.add(task);
            }
        }

        return overloadedTasks;
    }

    private static boolean isCompleted(Task task) {
        String status = task.getStatus();
        if (status == null) {
            return false;
        }

        String normalizedStatus = status.trim().toLowerCase();
        return normalizedStatus.equals("done")
            || normalizedStatus.equals("completed")
            || normalizedStatus.equals("closed")
            || normalizedStatus.equals("cancel")
            || normalizedStatus.equals("canceled")
            || normalizedStatus.equals("cancelled");
    }

    public static class CollaboratorLoad {
        private final Collaborator collaborator;
        private int assignmentCount;
        private int overdueCount;
        private int upcomingCount;

        private CollaboratorLoad(Collaborator collaborator) {
            this.collaborator = collaborator;
        }

        public String getName() {
            return collaborator.getName();
        }

        public String getCategory() {
            String category = collaborator.getCategory();
            return category == null || category.isBlank() ? "Unknown" : category;
        }

        public int getAssignmentCount() {
            return assignmentCount;
        }

        public int getOverdueCount() {
            return overdueCount;
        }

        public int getUpcomingCount() {
            return upcomingCount;
        }
    }
}

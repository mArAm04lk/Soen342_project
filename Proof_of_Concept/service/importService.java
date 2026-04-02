package service;

import model.*;
import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class importService {

    public static void importTasks(String filePath,
                                   List<Task> tasks,
                                   Map<String, Project> projects,
                                   Map<String, Collaborator> collaborators) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header

                String[] fields = line.split(",", -1); // -1 to keep empty strings

                String taskName = fields[0].trim();
                String description = fields[1].trim();
                String subtask = fields[2].trim();
                String status = fields[3].trim();
                String priority = fields[4].trim();
                LocalDate dueDate = fields[5].trim().isEmpty() ? null : LocalDate.parse(fields[5].trim(), formatter);

                String projectName = fields[6].trim();
                String projectDesc = fields[7].trim();
                String collaboratorName = fields[8].trim();
                String collaboratorCategory = fields[9].trim();

                // Projects
                Project project = null;
                if (!projectName.isEmpty()) {
                    project = projects.get(projectName);
                    if (project == null) {
                        project = new Project(projectName, projectDesc);
                        projects.put(projectName, project);
                    }
                }

                // Collaborators
                Collaborator collaborator = null;
                if (!collaboratorName.isEmpty()) {
                    collaborator = collaborators.get(collaboratorName);
                    if (collaborator == null) {
                        collaborator = new Collaborator(collaboratorName, collaboratorCategory);
                        collaborators.put(collaboratorName, collaborator);
                    }
                }

                // Create task
                Task task = new Task(taskName, description, subtask, status, priority, dueDate, project, collaborator);
                if (project != null) {
                    project.addTask(task);
                }
                tasks.add(task);
            }

            System.out.println("CSV import completed. Total tasks: " + tasks.size());

        } catch (Exception e) {
            System.out.println("Error importing CSV: " + e.getMessage());
        }
    }
}
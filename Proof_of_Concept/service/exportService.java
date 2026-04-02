package service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Collaborator;
import model.Project;
import model.Task;

public class exportService {

	private static final String HEADER = "TaskName,Description,Subtask,Status,Priority,DueDate,ProjectName,ProjectDescription,Collaborator,CollaboratorCategory";

	public static void exportDatabase(String filePath,
									  List<Task> tasks,
									  Map<String, Project> projects,
									  Map<String, Collaborator> collaborators) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
			writer.println(HEADER);

			Set<String> linkedProjects = new HashSet<>();
			Set<String> linkedCollaborators = new HashSet<>();

			for (Task task : tasks) {
				Project project = task.getProject();
				Collaborator collaborator = task.getCollaborator();
				LocalDate dueDate = task.getDueDate();

				if (project != null) {
					linkedProjects.add(project.getName());
				}
				if (collaborator != null) {
					linkedCollaborators.add(collaborator.getName());
				}

				String[] columns = new String[] {
						safe(task.getName()),
						safe(task.getDescription()),
						safe(task.getSubtask()),
						safe(task.getStatus()),
						safe(task.getPriority()),
						dueDate == null ? "" : dueDate.toString(),
						project == null ? "" : safe(project.getName()),
						project == null ? "" : safe(project.getDescription()),
						collaborator == null ? "" : safe(collaborator.getName()),
						collaborator == null ? "" : safe(collaborator.getCategory())
				};

				writer.println(String.join(",", columns));
			}

			// Also export standalone projects not linked to any task.
			for (Project project : projects.values()) {
				if (!linkedProjects.contains(project.getName())) {
					String[] columns = new String[] {
							"",
							"",
							"",
							"",
							"",
							"",
							safe(project.getName()),
							safe(project.getDescription()),
							"",
							""
					};
					writer.println(String.join(",", columns));
				}
			}

			// Also export standalone collaborators not linked to any task.
			for (Collaborator collaborator : collaborators.values()) {
				if (!linkedCollaborators.contains(collaborator.getName())) {
					String[] columns = new String[] {
							"",
							"",
							"",
							"",
							"",
							"",
							"",
							"",
							safe(collaborator.getName()),
							safe(collaborator.getCategory())
					};
					writer.println(String.join(",", columns));
				}
			}

			System.out.println("CSV export completed. Entire in-memory database was exported.");
			System.out.println("Output file: " + filePath);
		} catch (IOException e) {
			System.out.println("Error exporting CSV: " + e.getMessage());
		}
	}

	private static String safe(String value) {
		if (value == null) {
			return "";
		}

		String escaped = value.replace("\"", "\"\"");
		return "\"" + escaped + "\"";
	}
}

package service;

import model.Collaborator;
import model.Project;
import model.Task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseService {

    private static final String JDBC_URL = "jdbc:h2:./task_planner_db";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    private DatabaseService() {
    }

    public static void initializeSchema() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS projects (
                        id IDENTITY PRIMARY KEY,
                        name VARCHAR(255) NOT NULL UNIQUE,
                        description CLOB
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS collaborators (
                        id IDENTITY PRIMARY KEY,
                        name VARCHAR(255) NOT NULL UNIQUE,
                        category VARCHAR(255)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS tasks (
                        id IDENTITY PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        description CLOB,
                        subtask VARCHAR(255),
                        status VARCHAR(100),
                        priority VARCHAR(100),
                        due_date DATE,
                        project_id BIGINT,
                        collaborator_id BIGINT,
                        CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id),
                        CONSTRAINT fk_tasks_collaborator FOREIGN KEY (collaborator_id) REFERENCES collaborators(id)
                    )
                    """);
        }
    }

    public static void replaceAllData(List<TaskRecord> records) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                clearAllTables(connection);

                for (TaskRecord record : records) {
                    Long projectId = upsertProject(connection, record.projectName(), record.projectDescription());
                    Long collaboratorId = upsertCollaborator(connection, record.collaboratorName(), record.collaboratorCategory());
                    insertTask(connection, record, projectId, collaboratorId);
                }

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static void loadAll(List<Task> tasks,
                               Map<String, Project> projects,
                               Map<String, Collaborator> collaborators) throws SQLException {
        tasks.clear();
        projects.clear();
        collaborators.clear();

        Map<Long, Project> projectsById = new HashMap<>();
        Map<Long, Collaborator> collaboratorsById = new HashMap<>();

        try (Connection connection = getConnection()) {
            loadProjects(connection, projectsById, projects);
            loadCollaborators(connection, collaboratorsById, collaborators);
            loadTasks(connection, tasks, projectsById, collaboratorsById);
        }
    }

    public static boolean updateTaskStatus(long taskId, String status) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE tasks SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setLong(2, taskId);
            return statement.executeUpdate() > 0;
        }
    }

    public static List<TaskRecord> readRecordsFromCsv(String filePath) {
        List<TaskRecord> records = new ArrayList<>();

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] fields = line.split(",", -1);

                String taskName = getField(fields, 0);
                String description = getField(fields, 1);
                String subtask = getField(fields, 2);
                String status = getField(fields, 3);
                String priority = getField(fields, 4);
                LocalDate dueDate = getField(fields, 5).isEmpty() ? null : LocalDate.parse(getField(fields, 5), formatter);

                String projectName = getField(fields, 6);
                String projectDescription = getField(fields, 7);
                String collaboratorName = getField(fields, 8);
                String collaboratorCategory = getField(fields, 9);

                records.add(new TaskRecord(
                        taskName,
                        description,
                        subtask,
                        status,
                        priority,
                        dueDate,
                        projectName,
                        projectDescription,
                        collaboratorName,
                        collaboratorCategory
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error importing CSV: " + e.getMessage(), e);
        }

        return records;
    }

    private static void clearAllTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM tasks");
            statement.executeUpdate("DELETE FROM projects");
            statement.executeUpdate("DELETE FROM collaborators");
        }
    }

    private static Long upsertProject(Connection connection, String name, String description) throws SQLException {
        if (name == null || name.isBlank()) {
            return null;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO projects (name, description) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException ex) {
            if (!isUniqueConstraintViolation(ex)) {
                throw ex;
            }
        }

        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM projects WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("id");
                }
            }
        }

        return null;
    }

    private static Long upsertCollaborator(Connection connection, String name, String category) throws SQLException {
        if (name == null || name.isBlank()) {
            return null;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO collaborators (name, category) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException ex) {
            if (!isUniqueConstraintViolation(ex)) {
                throw ex;
            }
        }

        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM collaborators WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("id");
                }
            }
        }

        return null;
    }

    private static void insertTask(Connection connection,
                                   TaskRecord record,
                                   Long projectId,
                                   Long collaboratorId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO tasks (name, description, subtask, status, priority, due_date, project_id, collaborator_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            statement.setString(1, record.taskName());
            statement.setString(2, record.description());
            statement.setString(3, record.subtask());
            statement.setString(4, record.status());
            statement.setString(5, record.priority());

            if (record.dueDate() == null) {
                statement.setDate(6, null);
            } else {
                statement.setDate(6, Date.valueOf(record.dueDate()));
            }

            if (projectId == null) {
                statement.setNull(7, Types.BIGINT);
            } else {
                statement.setLong(7, projectId);
            }

            if (collaboratorId == null) {
                statement.setNull(8, Types.BIGINT);
            } else {
                statement.setLong(8, collaboratorId);
            }

            statement.executeUpdate();
        }
    }

    private static void loadProjects(Connection connection,
                                     Map<Long, Project> projectsById,
                                     Map<String, Project> projectsByName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, description FROM projects");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                long id = result.getLong("id");
                Project project = new Project(result.getString("name"), result.getString("description"));
                projectsById.put(id, project);
                projectsByName.put(project.getName(), project);
            }
        }
    }

    private static void loadCollaborators(Connection connection,
                                          Map<Long, Collaborator> collaboratorsById,
                                          Map<String, Collaborator> collaboratorsByName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, category FROM collaborators");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                long id = result.getLong("id");
                Collaborator collaborator = new Collaborator(result.getString("name"), result.getString("category"));
                collaboratorsById.put(id, collaborator);
                collaboratorsByName.put(collaborator.getName(), collaborator);
            }
        }
    }

    private static void loadTasks(Connection connection,
                                  List<Task> tasks,
                                  Map<Long, Project> projectsById,
                                  Map<Long, Collaborator> collaboratorsById) throws SQLException {
        List<TaskWithOrder> orderedTasks = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT id, name, description, subtask, status, priority, due_date, project_id, collaborator_id
                FROM tasks
                """);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                long id = result.getLong("id");

                Long projectId = (Long) result.getObject("project_id");
                Long collaboratorId = (Long) result.getObject("collaborator_id");

                Project project = projectId == null ? null : projectsById.get(projectId);
                Collaborator collaborator = collaboratorId == null ? null : collaboratorsById.get(collaboratorId);

                Date dueDate = result.getDate("due_date");
                Task task = new Task(
                    id,
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("subtask"),
                        result.getString("status"),
                        result.getString("priority"),
                        dueDate == null ? null : dueDate.toLocalDate(),
                        project,
                        collaborator
                );

                if (project != null) {
                    project.addTask(task);
                }

                orderedTasks.add(new TaskWithOrder(id, task));
            }
        }

        orderedTasks.stream()
                .sorted(Comparator.comparingLong(TaskWithOrder::id))
                .forEach(item -> tasks.add(item.task()));
    }

    private static boolean isUniqueConstraintViolation(SQLException ex) {
        return ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unique");
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    private static String getField(String[] fields, int index) {
        if (index < 0 || index >= fields.length) {
            return "";
        }
        return fields[index].trim();
    }

    private record TaskWithOrder(long id, Task task) {
    }

    public record TaskRecord(
            String taskName,
            String description,
            String subtask,
            String status,
            String priority,
            LocalDate dueDate,
            String projectName,
            String projectDescription,
            String collaboratorName,
            String collaboratorCategory
    ) {
    }
}

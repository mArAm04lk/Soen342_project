package service;

import model.*;
import java.util.*;

public class importService {

    public static void importTasks(String filePath,
                                   List<Task> tasks,
                                   Map<String, Project> projects,
                                   Map<String, Collaborator> collaborators) {
        try {
            DatabaseService.initializeSchema();
            List<DatabaseService.TaskRecord> records = DatabaseService.readRecordsFromCsv(filePath);
            DatabaseService.replaceAllData(records);
            DatabaseService.loadAll(tasks, projects, collaborators);
            System.out.println("CSV import completed. Total tasks: " + tasks.size());
        } catch (Exception e) {
            System.out.println("Error importing CSV: " + e.getMessage());
        }
    }
}
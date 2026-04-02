package service;

import model.Task;
import java.util.List;

public interface CalendarExportGateway {
    void exportTasks(List<Task> tasks, String filePath);
}

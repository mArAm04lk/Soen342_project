package service;

import model.Task;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class ICal4jCalendarGateway implements CalendarExportGateway {

    @Override
    public void exportTasks(List<Task> tasks, String filePath) {
        PropertyList calendarProperties = new PropertyList(List.of(
            new ProdId("-//SOEN342 Task Planner//EN"),
            new Version(new ParameterList(), "2.0")
        ));

        List<CalendarComponent> calendarComponentItems = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getDueDate() == null) {
                continue;
            }

            LocalDateTime startDateTime = task.getDueDate().atStartOfDay();
            LocalDateTime endDateTime = startDateTime.plusHours(1);

                VEvent event = new VEvent(
                    startDateTime.atZone(ZoneId.systemDefault()),
                    endDateTime.atZone(ZoneId.systemDefault()),
                    task.getName()
                );
                PropertyList eventProperties = (PropertyList) event.getPropertyList().add(new Description(buildDescription(task)));
                event.setPropertyList(eventProperties);
                calendarComponentItems.add(event);
        }

            ComponentList<CalendarComponent> calendarComponents = new ComponentList<>(calendarComponentItems);
        Calendar calendar = new Calendar(calendarProperties, calendarComponents);

        try (FileOutputStream out = new FileOutputStream(filePath)) {
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write iCalendar file: " + filePath, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export tasks to iCalendar", e);
        }
    }

    private String buildDescription(Task task) {
        StringBuilder sb = new StringBuilder();

        if (task.getDescription() != null && !task.getDescription().isBlank()) {
            sb.append("Description: ").append(task.getDescription()).append("\n");
        }

        if (task.getStatus() != null && !task.getStatus().isBlank()) {
            sb.append("Status: ").append(task.getStatus()).append("\n");
        }

        if (task.getPriority() != null && !task.getPriority().isBlank()) {
            sb.append("Priority: ").append(task.getPriority()).append("\n");
        }

        if (task.getProject() != null) {
            sb.append("Project: ").append(task.getProject().getName()).append("\n");
        }

        if (task.getSubtask() != null && !task.getSubtask().isBlank()) {
            sb.append("Subtask: ").append(task.getSubtask()).append("\n");
        }

        if (task.getCollaborator() != null) {
            sb.append("Collaborator: ").append(task.getCollaborator().getName()).append("\n");
        }

        return sb.toString().trim();
    }
}

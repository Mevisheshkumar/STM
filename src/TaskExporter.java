import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskExporter {

    public static void exportToCSV(List<Task> tasks, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println("Title,Description,Priority,Deadline,Category,Status,Created");

            // Write tasks
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Task task : tasks) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        escapeCSV(task.getTitle()),
                        escapeCSV(task.getDescription()),
                        task.getPriority(),
                        task.getDeadline() != null ? task.getDeadline().format(formatter) : "",
                        escapeCSV(task.getCategory()),
                        task.isCompleted() ? "Completed" : "Pending",
                        task.getCreatedAt().format(formatter)
                );
            }
        }
    }

    public static void exportToHTML(List<Task> tasks, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html><head><title>Task Report</title>");
            writer.println("<style>");
            writer.println("table { border-collapse: collapse; width: 100%; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #f2f2f2; }");
            writer.println(".high { background-color: #ffcccc; }");
            writer.println(".medium { background-color: #ffffcc; }");
            writer.println(".low { background-color: #ccffcc; }");
            writer.println(".completed { text-decoration: line-through; opacity: 0.6; }");
            writer.println("</style></head><body>");
            writer.println("<h1>Task Report</h1>");
            writer.println("<table>");
            writer.println("<tr><th>Title</th><th>Priority</th><th>Deadline</th><th>Category</th><th>Status</th></tr>");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            for (Task task : tasks) {
                String rowClass = "";
                if (task.isCompleted()) {
                    rowClass = "completed";
                } else {
                    switch (task.getPriority()) {
                        case HIGH: rowClass = "high"; break;
                        case MEDIUM: rowClass = "medium"; break;
                        case LOW: rowClass = "low"; break;
                    }
                }

                writer.printf("<tr class=\"%s\">", rowClass);
                writer.printf("<td>%s</td>", escapeHTML(task.getTitle()));
                writer.printf("<td>%s</td>", task.getPriority());
                writer.printf("<td>%s</td>", task.getDeadline() != null ? task.getDeadline().format(formatter) : "No deadline");
                writer.printf("<td>%s</td>", escapeHTML(task.getCategory()));
                writer.printf("<td>%s</td>", task.isCompleted() ? "Completed" : "Pending");
                writer.println("</tr>");
            }

            writer.println("</table>");
            writer.println("</body></html>");
        }
    }

    private static String escapeCSV(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }

    private static String escapeHTML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
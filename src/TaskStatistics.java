import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskStatistics {
    private TaskManager taskManager;

    public TaskStatistics(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public int getTotalTasks() {
        return taskManager.getAllTasks().size();
    }

    public int getCompletedTasks() {
        return taskManager.getCompletedTasks().size();
    }

    public int getPendingTasks() {
        return taskManager.getPendingTasks().size();
    }

    public int getOverdueTasks() {
        return taskManager.getOverdueTasks().size();
    }

    public double getCompletionRate() {
        int total = getTotalTasks();
        if (total == 0) return 0.0;
        return (double) getCompletedTasks() / total * 100;
    }

    public Map<Task.Priority, Long> getTasksByPriorityCount() {
        return taskManager.getAllTasks().stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }

    public Map<String, Long> getTasksByCategoryCount() {
        return taskManager.getAllTasks().stream()
                .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));
    }

    public long getTasksDueInNextHours(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(hours);

        return taskManager.getPendingTasks().stream()
                .filter(task -> task.getDeadline() != null)
                .filter(task -> task.getDeadline().isAfter(now) && task.getDeadline().isBefore(future))
                .count();
    }

    public double getAverageTaskCompletionTime() {
        List<Task> completedTasks = taskManager.getCompletedTasks();
        if (completedTasks.isEmpty()) return 0.0;

        return completedTasks.stream()
                .filter(task -> task.getDeadline() != null)
                .mapToLong(task -> ChronoUnit.HOURS.between(task.getCreatedAt(), task.getDeadline()))
                .average()
                .orElse(0.0);
    }
}
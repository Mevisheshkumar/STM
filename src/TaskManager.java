import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class TaskManager {
    private PriorityBlockingQueue<Task> taskQueue;
    private List<TaskManagerListener> listeners;

    public interface TaskManagerListener {
        void onTaskAdded(Task task);
        void onTaskRemoved(Task task);
        void onTaskUpdated(Task task);
    }

    public TaskManager() {
        this.taskQueue = new PriorityBlockingQueue<>();
        this.listeners = new ArrayList<>();
    }

    public void addListener(TaskManagerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TaskManagerListener listener) {
        listeners.remove(listener);
    }

    public void addTask(Task task) {
        taskQueue.offer(task);
        notifyTaskAdded(task);
    }

    public boolean removeTask(Task task) {
        boolean removed = taskQueue.remove(task);
        if (removed) {
            notifyTaskRemoved(task);
        }
        return removed;
    }

    public void updateTask(Task task) {
        // Remove and re-add to maintain priority order
        taskQueue.remove(task);
        taskQueue.offer(task);
        notifyTaskUpdated(task);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(taskQueue);
    }

    public List<Task> getTasksByPriority(Task.Priority priority) {
        return taskQueue.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    public List<Task> getTodaysTasks() {
        return taskQueue.stream()
                .filter(Task::isDueToday)
                .collect(Collectors.toList());
    }

    public List<Task> getOverdueTasks() {
        return taskQueue.stream()
                .filter(Task::isOverdue)
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }

    public List<Task> getCompletedTasks() {
        return taskQueue.stream()
                .filter(Task::isCompleted)
                .collect(Collectors.toList());
    }

    public List<Task> getPendingTasks() {
        return taskQueue.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByCategory(String category) {
        return taskQueue.stream()
                .filter(task -> task.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public Set<String> getAllCategories() {
        return taskQueue.stream()
                .map(Task::getCategory)
                .collect(Collectors.toSet());
    }

    public void clearAllTasks() {
        taskQueue.clear();
    }

    private void notifyTaskAdded(Task task) {
        listeners.forEach(listener -> listener.onTaskAdded(task));
    }

    private void notifyTaskRemoved(Task task) {
        listeners.forEach(listener -> listener.onTaskRemoved(task));
    }

    private void notifyTaskUpdated(Task task) {
        listeners.forEach(listener -> listener.onTaskUpdated(task));
    }
}
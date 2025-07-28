import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;

public class Task implements Comparable<Task>, Serializable {
    private static final long serialVersionUID = 1L;

    public enum Priority {
        LOW(1), MEDIUM(2), HIGH(3), URGENT(4);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private String title;
    private String description;
    private Priority priority;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private boolean completed;
    private String category;

    public Task(String title, String description, Priority priority, LocalDateTime deadline, String category) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }

    @Override
    public int compareTo(Task other) {
        // First compare by priority (higher priority first)
        int priorityComparison = Integer.compare(other.priority.getValue(), this.priority.getValue());
        if (priorityComparison != 0) {
            return priorityComparison;
        }

        // If same priority, compare by deadline (earlier deadline first)
        if (this.deadline != null && other.deadline != null) {
            return this.deadline.compareTo(other.deadline);
        } else if (this.deadline != null) {
            return -1; // This task has deadline, other doesn't
        } else if (other.deadline != null) {
            return 1; // Other task has deadline, this doesn't
        }

        // If no deadlines, compare by creation time
        return this.createdAt.compareTo(other.createdAt);
    }

    public boolean isDueToday() {
        if (deadline == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return deadline.toLocalDate().equals(now.toLocalDate());
    }

    public boolean isOverdue() {
        if (deadline == null) return false;
        return deadline.isBefore(LocalDateTime.now());
    }

    public String getFormattedDeadline() {
        if (deadline == null) return "No deadline";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return deadline.format(formatter);
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s", priority, title, getFormattedDeadline());
    }
}
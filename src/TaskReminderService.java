import javax.swing.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

public class TaskReminderService {
    private Timer timer;
    private TaskManager taskManager;

    public TaskReminderService(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.timer = new Timer(true); // Daemon thread
        startReminderScheduler();
    }

    private void startReminderScheduler() {
        // Check for reminders every 5 minutes
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForReminders();
            }
        }, 0, 5 * 60 * 1000); // 5 minutes in milliseconds
    }

    private void checkForReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> pendingTasks = taskManager.getPendingTasks();

        for (Task task : pendingTasks) {
            if (task.getDeadline() != null) {
                long minutesUntilDeadline = ChronoUnit.MINUTES.between(now, task.getDeadline());

                // Show reminder for tasks due within 30 minutes
                if (minutesUntilDeadline > 0 && minutesUntilDeadline <= 30) {
                    showReminder(task, minutesUntilDeadline);
                }
                // Show overdue notification
                else if (minutesUntilDeadline < 0 && minutesUntilDeadline >= -60) {
                    showOverdueNotification(task);
                }
            }
        }
    }

    private void showReminder(Task task, long minutesUntilDeadline) {
        SwingUtilities.invokeLater(() -> {
            String message = String.format(
                    "Task '%s' is due in %d minutes!\nPriority: %s",
                    task.getTitle(), minutesUntilDeadline, task.getPriority()
            );

            JOptionPane.showMessageDialog(
                    null, message, "Task Reminder",
                    JOptionPane.WARNING_MESSAGE
            );
        });
    }

    private void showOverdueNotification(Task task) {
        SwingUtilities.invokeLater(() -> {
            String message = String.format(
                    "Task '%s' is overdue!\nDeadline was: %s",
                    task.getTitle(), task.getFormattedDeadline()
            );

            JOptionPane.showMessageDialog(
                    null, message, "Overdue Task",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }

    public void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
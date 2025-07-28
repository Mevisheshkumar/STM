import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class TaskTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Title", "Priority", "Deadline", "Category", "Status"};
    private List<Task> tasks = new ArrayList<>();

    public void setTasks(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return tasks.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = tasks.get(rowIndex);
        switch (columnIndex) {
            case 0: return task.getTitle();
            case 1: return task.getPriority();
            case 2: return task.getFormattedDeadline();
            case 3: return task.getCategory();
            case 4: return task.isCompleted() ? "Completed" :
                    (task.isOverdue() ? "Overdue" : "Pending");
            default: return null;
        }
    }

    public Task getTaskAt(int rowIndex) {
        return tasks.get(rowIndex);
    }
}
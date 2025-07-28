import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

public class SmartTaskSchedulerGUI extends JFrame implements TaskManager.TaskManagerListener {
    private TaskManager taskManager;
    private TaskPersistence persistence;
    private TaskReminderService reminderService;
    private TaskTableModel tableModel;
    private JTable taskTable;
    private JComboBox<String> filterCombo;
    private JLabel statusLabel;

    public SmartTaskSchedulerGUI() {
        taskManager = new TaskManager();
        persistence = new TaskPersistence();
        tableModel = new TaskTableModel();

        taskManager.addListener(this);

        initComponents();
        loadTasks();

        reminderService = new TaskReminderService(taskManager);

        // Save tasks on window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks();
                reminderService.shutdown();
                System.exit(0);
            }
        });
    }

    private void initComponents() {
        setTitle("Smart Task Scheduler");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton addButton = new JButton("Add Task");
        JButton editButton = new JButton("Edit Task");
        JButton deleteButton = new JButton("Delete Task");
        JButton completeButton = new JButton("Mark Complete");

        addButton.addActionListener(e -> addTask());
        editButton.addActionListener(e -> editTask());
        deleteButton.addActionListener(e -> deleteTask());
        completeButton.addActionListener(e -> toggleTaskCompletion());

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.addSeparator();
        toolBar.add(completeButton);

        add(toolBar, BorderLayout.NORTH);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));

        filterCombo = new JComboBox<>(new String[]{
                "All Tasks", "Pending", "Completed", "Today's Tasks",
                "Overdue", "High Priority", "Medium Priority", "Low Priority"
        });
        filterCombo.addActionListener(e -> applyFilter());
        filterPanel.add(filterCombo);

        mainPanel.add(filterPanel, BorderLayout.NORTH);

        // Table
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(25);

        // Set column widths
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Title
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Priority
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Deadline
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Category
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        // Menu bar
        createMenuBar();

        pack();
        setLocationRelativeTo(null);
        updateStatusLabel();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save Tasks");
        JMenuItem loadItem = new JMenuItem("Load Tasks");
        JMenuItem exitItem = new JMenuItem("Exit");

        saveItem.addActionListener(e -> saveTasks());
        loadItem.addActionListener(e -> loadTasks());
        exitItem.addActionListener(e -> {
            saveTasks();
            reminderService.shutdown();
            System.exit(0);
        });

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // View menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.addActionListener(e -> refreshTable());
        viewMenu.add(refreshItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    private void addTask() {
        TaskDialog dialog = new TaskDialog(this, null, "Add New Task");
        dialog.setVisible(true);

        if (dialog.isOkPressed()) {
            taskManager.addTask(dialog.getTask());
            refreshTable();
        }
    }

    private void editTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = tableModel.getTaskAt(selectedRow);
        TaskDialog dialog = new TaskDialog(this, task, "Edit Task");
        dialog.setVisible(true);

        if (dialog.isOkPressed()) {
            taskManager.updateTask(task);
            refreshTable();
        }
    }

    private void deleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = tableModel.getTaskAt(selectedRow);
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete task: " + task.getTitle() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            taskManager.removeTask(task);
            refreshTable();
        }
    }

    private void toggleTaskCompletion() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = tableModel.getTaskAt(selectedRow);
        task.setCompleted(!task.isCompleted());
        taskManager.updateTask(task);
        refreshTable();
    }

    private void applyFilter() {
        String filter = (String) filterCombo.getSelectedItem();
        List<Task> filteredTasks;

        switch (filter) {
            case "Pending":
                filteredTasks = taskManager.getPendingTasks();
                break;
            case "Completed":
                filteredTasks = taskManager.getCompletedTasks();
                break;
            case "Today's Tasks":
                filteredTasks = taskManager.getTodaysTasks();
                break;
            case "Overdue":
                filteredTasks = taskManager.getOverdueTasks();
                break;
            case "High Priority":
                filteredTasks = taskManager.getTasksByPriority(Task.Priority.HIGH);
                break;
            case "Medium Priority":
                filteredTasks = taskManager.getTasksByPriority(Task.Priority.MEDIUM);
                break;
            case "Low Priority":
                filteredTasks = taskManager.getTasksByPriority(Task.Priority.LOW);
                break;
            default:
                filteredTasks = taskManager.getAllTasks();
                break;
        }

        tableModel.setTasks(filteredTasks);
        updateStatusLabel();
    }

    private void refreshTable() {
        applyFilter(); // This will refresh based on current filter
    }

    private void updateStatusLabel() {
        int totalTasks = taskManager.getAllTasks().size();
        int pendingTasks = taskManager.getPendingTasks().size();
        int completedTasks = taskManager.getCompletedTasks().size();
        int overdueTasks = taskManager.getOverdueTasks().size();

        statusLabel.setText(String.format(
                "Total: %d | Pending: %d | Completed: %d | Overdue: %d",
                totalTasks, pendingTasks, completedTasks, overdueTasks
        ));
    }

    private void saveTasks() {
        try {
            persistence.saveTasks(taskManager.getAllTasks());
            JOptionPane.showMessageDialog(this, "Tasks saved successfully!",
                    "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving tasks: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        try {
            List<Task> tasks = persistence.loadTasks();
            taskManager.clearAllTasks();
            for (Task task : tasks) {
                taskManager.addTask(task);
            }
            refreshTable();
            JOptionPane.showMessageDialog(this, "Tasks loaded successfully!",
                    "Load Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // TaskManager.TaskManagerListener implementation
    @Override
    public void onTaskAdded(Task task) {
        updateStatusLabel();
    }

    @Override
    public void onTaskRemoved(Task task) {
        updateStatusLabel();
    }

    @Override
    public void onTaskUpdated(Task task) {
        updateStatusLabel();
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new SmartTaskSchedulerGUI().setVisible(true);
        });
    }
}
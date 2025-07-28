import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TaskDialog extends JDialog {
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Task.Priority> priorityCombo;
    private JTextField deadlineField;
    private JTextField categoryField;
    private JCheckBox completedCheckBox;

    private Task task;
    private boolean okPressed = false;

    public TaskDialog(Frame parent, Task task, String title) {
        super(parent, title, true);
        this.task = task;
        initComponents();
        if (task != null) {
            populateFields();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        titleField = new JTextField(20);
        formPanel.add(titleField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);

        // Priority
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        priorityCombo = new JComboBox<>(Task.Priority.values());
        formPanel.add(priorityCombo, gbc);

        // Deadline
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Deadline (yyyy-MM-dd HH:mm):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        deadlineField = new JTextField(20);
        formPanel.add(deadlineField, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        categoryField = new JTextField(20);
        formPanel.add(categoryField, gbc);

        // Completed checkbox
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        completedCheckBox = new JCheckBox("Completed");
        formPanel.add(completedCheckBox, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateAndSave()) {
                    okPressed = true;
                    dispose();
                }
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getParent());
    }

    private void populateFields() {
        titleField.setText(task.getTitle());
        descriptionArea.setText(task.getDescription());
        priorityCombo.setSelectedItem(task.getPriority());
        categoryField.setText(task.getCategory());
        completedCheckBox.setSelected(task.isCompleted());

        if (task.getDeadline() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            deadlineField.setText(task.getDeadline().format(formatter));
        }
    }

    private boolean validateAndSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        LocalDateTime deadline = null;
        String deadlineText = deadlineField.getText().trim();
        if (!deadlineText.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                deadline = LocalDateTime.parse(deadlineText, formatter);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid deadline format! Use yyyy-MM-dd HH:mm",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        String category = categoryField.getText().trim();
        if (category.isEmpty()) {
            category = "General";
        }

        if (task == null) {
            task = new Task(title, descriptionArea.getText().trim(),
                    (Task.Priority) priorityCombo.getSelectedItem(), deadline, category);
        } else {
            task.setTitle(title);
            task.setDescription(descriptionArea.getText().trim());
            task.setPriority((Task.Priority) priorityCombo.getSelectedItem());
            task.setDeadline(deadline);
            task.setCategory(category);
            task.setCompleted(completedCheckBox.isSelected());
        }

        return true;
    }

    public Task getTask() {
        return task;
    }

    public boolean isOkPressed() {
        return okPressed;
    }
}

package university.management.system;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class ExaminationDetails extends JFrame {

    private JComboBox<String> studentComboBox;
    private JComboBox<String> semesterComboBox;
    private JTable marksTable;
    private DefaultTableModel tableModel;
    private JButton saveButton;

    private JLabel lblGPAValue, lblCGPAValue;

    private String currentRollno = null;
    private String currentSemester = null;

    public ExaminationDetails() {
        setTitle("Examination Details");
        setSize(700, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.add(new JLabel("Select Student:"));
        studentComboBox = new JComboBox<>();
        topPanel.add(studentComboBox);

        topPanel.add(new JLabel("Select Semester:"));
        semesterComboBox = new JComboBox<>();
        topPanel.add(semesterComboBox);

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Subject", "Marks"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }

            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 1) ? Integer.class : String.class;
            }
        };
        marksTable = new JTable(tableModel);
        marksTable.setRowHeight(25);
        add(new JScrollPane(marksTable), BorderLayout.CENTER);

        // GPA + Save Panel
        JPanel southPanel = new JPanel(new BorderLayout());

        JPanel gpaPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        gpaPanel.setBorder(BorderFactory.createTitledBorder("Performance"));

        gpaPanel.add(new JLabel("GPA:"));
        lblGPAValue = new JLabel("0.00");
        gpaPanel.add(lblGPAValue);

        gpaPanel.add(new JLabel("CGPA:"));
        lblCGPAValue = new JLabel("0.00");
        gpaPanel.add(lblCGPAValue);

        southPanel.add(gpaPanel, BorderLayout.NORTH);

        saveButton = new JButton("Save Marks");
        saveButton.setEnabled(false);
        southPanel.add(saveButton, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        // Load students
        loadStudents();

        studentComboBox.addActionListener(e -> loadSemestersForStudent());
        semesterComboBox.addActionListener(e -> {
            if (semesterComboBox.isEnabled()) {
                showMarks();
            }
        });
        saveButton.addActionListener(e -> saveMarks());

        setVisible(true);
    }

    private void loadStudents() {
        try {
            Conn conn = new Conn();
            ResultSet rs = conn.s.executeQuery("SELECT rollno, name FROM student ORDER BY name");

            studentComboBox.removeAllItems();
            while (rs.next()) {
                String studentInfo = rs.getString("name") + " (" + rs.getString("rollno") + ")";
                studentComboBox.addItem(studentInfo);
            }

            if (studentComboBox.getItemCount() > 0) {
                studentComboBox.setSelectedIndex(0);
                loadSemestersForStudent();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    private void loadSemestersForStudent() {
        semesterComboBox.removeAllItems();
        tableModel.setRowCount(0);
        saveButton.setEnabled(false);
        semesterComboBox.setEnabled(true);

        String selectedStudent = (String) studentComboBox.getSelectedItem();
        if (selectedStudent == null) return;

        currentRollno = selectedStudent.substring(selectedStudent.indexOf('(') + 1, selectedStudent.indexOf(')'));

        try {
            Conn conn = new Conn();
            String query = "SELECT DISTINCT semester FROM marks WHERE rollno = ? ORDER BY semester";
            PreparedStatement pst = conn.c.prepareStatement(query);
            pst.setString(1, currentRollno);
            ResultSet rs = pst.executeQuery();

            int count = 0;
            while (rs.next()) {
                semesterComboBox.addItem(rs.getString("semester"));
                count++;
            }

            if (count == 0) {
                semesterComboBox.addItem("No semesters found");
                semesterComboBox.setEnabled(false);
                JOptionPane.showMessageDialog(this, "No examination details found for this student.");
            } else if (count == 1) {
                semesterComboBox.setSelectedIndex(0);
                showMarks();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading semesters: " + e.getMessage());
        }
    }

    private void showMarks() {
        tableModel.setRowCount(0);
        saveButton.setEnabled(false);

        String selectedSemester = (String) semesterComboBox.getSelectedItem();
        if (selectedSemester == null || selectedSemester.equals("No semesters found")) return;

        currentSemester = selectedSemester;

        try {
            Conn conn = new Conn();
            String query = "SELECT * FROM marks WHERE rollno = ? AND semester = ?";
            PreparedStatement pst = conn.c.prepareStatement(query);
            pst.setString(1, currentRollno);
            pst.setString(2, currentSemester);
            ResultSet rs = pst.executeQuery();

            int totalMarks = 0;
            int subjectCount = 0;

            if (rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();

                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String colName = meta.getColumnName(i).toLowerCase();
                    if (colName.startsWith("subject")) {
                        String subject = rs.getString(i);
                        if (subject != null && !subject.trim().isEmpty()) {
                            int marks = 0;
                            try {
                                int marksIndex = getColumnIndexByName(meta, "marks" + colName.replace("subject", ""));
                                marks = rs.getInt(marksIndex);
                                totalMarks += marks;
                                subjectCount++;
                            } catch (SQLException ex) {
                                marks = 0;
                            }
                            tableModel.addRow(new Object[]{subject, marks});
                        }
                    }
                }

                // GPA Calculation
                double gpa = (subjectCount > 0) ? ((double) totalMarks / (subjectCount * 100)) * 4.0 : 0.0;
                gpa = Math.round(gpa * 100.0) / 100.0;
                lblGPAValue.setText(String.valueOf(gpa));

                PreparedStatement updateGPA = conn.c.prepareStatement(
                        "UPDATE marks SET gpa = ? WHERE rollno = ? AND semester = ?");
                updateGPA.setDouble(1, gpa);
                updateGPA.setString(2, currentRollno);
                updateGPA.setString(3, currentSemester);
                updateGPA.executeUpdate();

                // CGPA Calculation
                PreparedStatement pstCGPA = conn.c.prepareStatement("SELECT gpa FROM marks WHERE rollno = ?");
                pstCGPA.setString(1, currentRollno);
                ResultSet rsCGPA = pstCGPA.executeQuery();

                double totalGPA = 0;
                int semCount = 0;
                while (rsCGPA.next()) {
                    totalGPA += rsCGPA.getDouble("gpa");
                    semCount++;
                }

                double cgpa = semCount > 0 ? Math.round((totalGPA / semCount) * 100.0) / 100.0 : 0.0;
                lblCGPAValue.setText(String.valueOf(cgpa));

                PreparedStatement updateCGPA = conn.c.prepareStatement(
                        "UPDATE marks SET cgpa = ? WHERE rollno = ? AND semester = ?");
                updateCGPA.setDouble(1, cgpa);
                updateCGPA.setString(2, currentRollno);
                updateCGPA.setString(3, currentSemester);
                updateCGPA.executeUpdate();

                saveButton.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "No marks data found for the selected semester.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading marks: " + e.getMessage());
        }
    }

    private void saveMarks() {
        if (currentRollno == null || currentSemester == null) return;

        try {
            Conn conn = new Conn();
            String query = "SELECT * FROM marks WHERE rollno = ? AND semester = ?";
            PreparedStatement pst = conn.c.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pst.setString(1, currentRollno);
            pst.setString(2, currentSemester);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "No record found to update.");
                return;
            }

            ArrayList<String> marksCols = new ArrayList<>();
            ArrayList<Integer> marksValues = new ArrayList<>();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object marksObj = tableModel.getValueAt(i, 1);
                int marks = 0;

                if (marksObj instanceof Integer) {
                    marks = (Integer) marksObj;
                } else {
                    try {
                        marks = Integer.parseInt(marksObj.toString());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Invalid marks at row " + (i + 1) + ". Please enter a number.");
                        return;
                    }
                }

                if (marks < 0 || marks > 100) {
                    JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100 at row " + (i + 1));
                    return;
                }

                marksCols.add("marks" + (i + 1));
                marksValues.add(marks);
            }

            StringBuilder sql = new StringBuilder("UPDATE marks SET ");
            for (int i = 0; i < marksCols.size(); i++) {
                sql.append(marksCols.get(i)).append(" = ?");
                if (i < marksCols.size() - 1) sql.append(", ");
            }
            sql.append(" WHERE rollno = ? AND semester = ?");

            PreparedStatement updatePst = conn.c.prepareStatement(sql.toString());
            for (int i = 0; i < marksValues.size(); i++) {
                updatePst.setInt(i + 1, marksValues.get(i));
            }
            updatePst.setString(marksValues.size() + 1, currentRollno);
            updatePst.setString(marksValues.size() + 2, currentSemester);

            int updatedRows = updatePst.executeUpdate();

            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Marks updated successfully!");
                showMarks(); // refresh GPA/CGPA
            } else {
                JOptionPane.showMessageDialog(this, "Marks update failed.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating marks: " + e.getMessage());
        }
    }

    private int getColumnIndexByName(ResultSetMetaData meta, String columnName) throws SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (meta.getColumnName(i).equalsIgnoreCase(columnName)) return i;
        }
        throw new SQLException("Column " + columnName + " not found");
    }

    public static void main(String[] args) {
        new ExaminationDetails();
    }
}

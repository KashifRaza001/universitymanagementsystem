package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EnterMarks extends JFrame {

    private JComboBox<String> studentComboBox;
    private JTextField[] subjectFields;
    private JTextField[] marksFields;
    private JButton submitButton;

    private String currentSemester = "";
    private String currentRollno = "";
    private String degree = "";

    public EnterMarks() {
        setTitle("Enter Marks");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.add(new JLabel("Select Student:"));

        studentComboBox = new JComboBox<>();
        topPanel.add(studentComboBox);
        add(topPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(6, 3, 10, 10));
        subjectFields = new JTextField[5];
        marksFields = new JTextField[5];

        inputPanel.add(new JLabel("Subject No."));
        inputPanel.add(new JLabel("Subject Name (Auto-Filled)"));
        inputPanel.add(new JLabel("Enter Marks (0–100)"));

        for (int i = 0; i < 5; i++) {
            inputPanel.add(new JLabel("Subject " + (i + 1) + ":"));

            subjectFields[i] = new JTextField();
            subjectFields[i].setEditable(false);
            inputPanel.add(subjectFields[i]);

            marksFields[i] = new JTextField();
            inputPanel.add(marksFields[i]);
        }

        add(inputPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        submitButton = new JButton("Submit Marks");
        bottomPanel.add(submitButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadStudents();

        studentComboBox.addActionListener(e -> loadStudentInfo());
        submitButton.addActionListener(e -> submitMarks());

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
                loadStudentInfo();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    private void loadStudentInfo() {
        String selectedStudent = (String) studentComboBox.getSelectedItem();
        if (selectedStudent == null) return;

        currentRollno = selectedStudent.split(" \\(")[1].replace(")", "");
        try {
            Conn conn = new Conn();
            PreparedStatement pst = conn.c.prepareStatement(
                    "SELECT degree, current_semester FROM student WHERE rollno = ?");
            pst.setString(1, currentRollno);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                degree = rs.getString("degree");
                currentSemester = rs.getString("current_semester");
                loadSubjectsForSemester(degree, currentSemester);
                updateTitle();
                clearMarksFields();
            } else {
                JOptionPane.showMessageDialog(this, "Student data not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading student info: " + e.getMessage());
        }
    }

    private void loadSubjectsForSemester(String degree, String semester) {
        try {
            Conn conn = new Conn();
            PreparedStatement pst = conn.c.prepareStatement(
                    "SELECT subject1, subject2, subject3, subject4, subject5 FROM subject WHERE template_id = ? AND semester = ?");
            pst.setString(1, degree);
            pst.setString(2, semester);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                for (int i = 0; i < 5; i++) {
                    subjectFields[i].setText(rs.getString("subject" + (i + 1)));
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    subjectFields[i].setText("N/A");
                }
                JOptionPane.showMessageDialog(this, "Subjects not found for this degree and semester.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + e.getMessage());
        }
    }

    private void submitMarks() {
        if (currentRollno.isEmpty() || currentSemester.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid student or semester selected.");
            return;
        }

        try {
            for (int i = 0; i < 5; i++) {
                if ("N/A".equals(subjectFields[i].getText())) {
                    JOptionPane.showMessageDialog(this, "Cannot submit marks. Subject data is invalid.");
                    return;
                }
            }

            int[] marks = new int[5];
            for (int i = 0; i < 5; i++) {
                String marksText = marksFields[i].getText().trim();
                if (marksText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter marks for all subjects.");
                    return;
                }
                try {
                    int m = Integer.parseInt(marksText);
                    if (m < 0 || m > 100) {
                        JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100.");
                        return;
                    }
                    marks[i] = m;
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Please enter numeric marks only.");
                    return;
                }
            }

            Conn conn = new Conn();

            PreparedStatement checkPst = conn.c.prepareStatement(
                    "SELECT * FROM marks WHERE rollno = ? AND semester = ?");
            checkPst.setString(1, currentRollno);
            checkPst.setString(2, currentSemester);
            ResultSet rs = checkPst.executeQuery();

            if (rs.next()) {
                PreparedStatement updatePst = conn.c.prepareStatement(
                        "UPDATE marks SET subject1=?, subject2=?, subject3=?, subject4=?, subject5=?," +
                                " marks1=?, marks2=?, marks3=?, marks4=?, marks5=? WHERE rollno=? AND semester=?");
                for (int i = 0; i < 5; i++) {
                    updatePst.setString(i + 1, subjectFields[i].getText());
                    updatePst.setInt(i + 6, marks[i]);
                }
                updatePst.setString(11, currentRollno);
                updatePst.setString(12, currentSemester);
                updatePst.executeUpdate();
            } else {
                PreparedStatement insertPst = conn.c.prepareStatement(
                        "INSERT INTO marks (rollno, semester, subject1, subject2, subject3, subject4, subject5," +
                                " marks1, marks2, marks3, marks4, marks5) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                insertPst.setString(1, currentRollno);
                insertPst.setString(2, currentSemester);
                for (int i = 0; i < 5; i++) {
                    insertPst.setString(i + 3, subjectFields[i].getText());
                    insertPst.setInt(i + 8, marks[i]);
                }
                insertPst.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Marks submitted successfully.");
            incrementSemester();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void incrementSemester() {
        try {
            Conn conn = new Conn();

            String digits = currentSemester.replaceAll("\\D+", "");
            if (digits.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Current semester is invalid for increment.");
                return;
            }
            int semNum = Integer.parseInt(digits);
            int nextSemNum = semNum + 1;
            String nextSemester = "Semester " + nextSemNum;

            // Check if next semester subjects exist
            PreparedStatement subjectCheck = conn.c.prepareStatement(
                    "SELECT COUNT(*) AS count FROM subject WHERE template_id = ? AND semester = ?");
            subjectCheck.setString(1, degree);
            subjectCheck.setString(2, nextSemester);
            ResultSet subjectRs = subjectCheck.executeQuery();

            if (subjectRs.next() && subjectRs.getInt("count") > 0) {
                // Also check if marks already exist for next semester
                PreparedStatement markCheck = conn.c.prepareStatement(
                        "SELECT * FROM marks WHERE rollno = ? AND semester = ?");
                markCheck.setString(1, currentRollno);
                markCheck.setString(2, nextSemester);
                ResultSet markRs = markCheck.executeQuery();

                if (!markRs.next()) {
                    // Promote to next semester
                    PreparedStatement updateStudent = conn.c.prepareStatement(
                            "UPDATE student SET current_semester = ? WHERE rollno = ?");
                    updateStudent.setString(1, nextSemester);
                    updateStudent.setString(2, currentRollno);
                    updateStudent.executeUpdate();

                    currentSemester = nextSemester;
                    loadSubjectsForSemester(degree, currentSemester);
                    clearMarksFields();
                    updateTitle();

                    JOptionPane.showMessageDialog(this, "Now you can enter marks for " + currentSemester);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Marks for all semesters have been entered.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error moving to next semester: " + e.getMessage());
        }
    }

    private void clearMarksFields() {
        for (JTextField marksField : marksFields) {
            marksField.setText("");
        }
    }

    private void updateTitle() {
        setTitle("Enter Marks - Student: " + currentRollno + " - " + currentSemester);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnterMarks::new);
    }
}

package university.management.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentFeeForm extends JFrame implements ActionListener {

    Choice rollnoChoice, installmentChoice;
    JLabel nameLabel, degreeLabel, campusLabel, semesterLabel, totalLabel, statusLabel;
    JButton payButton, printButton;
    JTable historyTable;
    DefaultTableModel historyModel;

    String currentRollno = null;
    String currentSemester = "";

    public StudentFeeForm() {
        setTitle("Student Fee Form");
        setSize(800, 600);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel rollnoLabel = new JLabel("Select Roll No:");
        rollnoLabel.setBounds(50, 30, 120, 20);
        add(rollnoLabel);

        rollnoChoice = new Choice();
        rollnoChoice.setBounds(180, 30, 200, 20);
        add(rollnoChoice);

        JButton searchButton = new JButton("Load Details");
        searchButton.setBounds(400, 30, 150, 25);
        searchButton.addActionListener(this);
        add(searchButton);

        nameLabel = new JLabel("Name: ");
        nameLabel.setBounds(50, 70, 400, 20);
        add(nameLabel);

        degreeLabel = new JLabel("Degree: ");
        degreeLabel.setBounds(50, 100, 400, 20);
        add(degreeLabel);

        campusLabel = new JLabel("Campus: ");
        campusLabel.setBounds(50, 130, 400, 20);
        add(campusLabel);

        semesterLabel = new JLabel("Semester: ");
        semesterLabel.setBounds(50, 160, 400, 20);
        add(semesterLabel);

        totalLabel = new JLabel("Fee: ");
        totalLabel.setBounds(50, 190, 400, 20);
        add(totalLabel);

        JLabel installmentLabel = new JLabel("Installment:");
        installmentLabel.setBounds(50, 220, 100, 20);
        add(installmentLabel);

        installmentChoice = new Choice();
        installmentChoice.add("Full");
        installmentChoice.add("Half");
        installmentChoice.setBounds(180, 220, 200, 20);
        add(installmentChoice);

        statusLabel = new JLabel("");
        statusLabel.setBounds(50, 250, 400, 20);
        statusLabel.setForeground(Color.BLUE);
        add(statusLabel);

        payButton = new JButton("Pay Fee");
        payButton.setBounds(50, 280, 120, 30);
        payButton.addActionListener(this);
        payButton.setEnabled(false);
        add(payButton);

        printButton = new JButton("Print Receipt");
        printButton.setBounds(180, 280, 150, 30);
        printButton.addActionListener(this);
        printButton.setEnabled(false);
        add(printButton);

        // Fee History Table
        historyModel = new DefaultTableModel(new String[]{"Semester", "Installment", "Amount", "Date"}, 0);
        historyTable = new JTable(historyModel);
        JScrollPane sp = new JScrollPane(historyTable);
        sp.setBounds(50, 330, 680, 200);
        add(sp);

        loadRollNumbers();
        setVisible(true);
    }

    public void loadRollNumbers() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT rollno FROM student");
            while (rs.next()) {
                rollnoChoice.add(rs.getString("rollno"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load roll numbers: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();

        if (cmd.equals("Load Details")) {
            currentRollno = rollnoChoice.getSelectedItem();
            String rollno = currentRollno;

            try {
                Conn c = new Conn();

                PreparedStatement ps = c.c.prepareStatement("SELECT name, degree, campus, current_semester FROM student WHERE rollno = ?");
                ps.setString(1, rollno);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String name = rs.getString("name");
                    String degree = rs.getString("degree");
                    String campus = rs.getString("campus");
                    currentSemester = rs.getString("current_semester");

                    nameLabel.setText("Name: " + name);
                    degreeLabel.setText("Degree: " + degree);
                    campusLabel.setText("Campus: " + campus);
                    semesterLabel.setText("Semester: " + currentSemester);
                } else {
                    JOptionPane.showMessageDialog(this, "Student not found.");
                    return;
                }

                // Check if fee already paid
                ps = c.c.prepareStatement("SELECT * FROM collegefee WHERE rollno = ? AND semester = ?");
                ps.setString(1, rollno);
                ps.setString(2, currentSemester);
                rs = ps.executeQuery();

                if (rs.next()) {
                    totalLabel.setText("Fee: -");
                    statusLabel.setText("Fee already submitted.");
                    payButton.setEnabled(false);
                    printButton.setEnabled(true);
                } else {
                    int semNum = extractSemesterNumber(currentSemester);
                    ps = c.c.prepareStatement("SELECT semester" + semNum + " FROM fee WHERE courses = ?");
                    ps.setString(1, degreeLabel.getText().replace("Degree: ", ""));
                    rs = ps.executeQuery();

                    int feeAmount = 0;
                    if (rs.next()) {
                        feeAmount = rs.getInt(1);
                    }

                    totalLabel.setText("Fee: " + feeAmount);
                    statusLabel.setText("Pending. You can proceed to pay.");
                    payButton.setEnabled(true);
                    printButton.setEnabled(false);
                }

                loadFeeHistory(rollno);

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading details: " + e.getMessage());
            }
        }

        if (cmd.equals("Pay Fee")) {
            if (currentRollno == null) {
                JOptionPane.showMessageDialog(this, "No student selected.");
                return;
            }

            try {
                Conn c = new Conn();
                String degree = degreeLabel.getText().replace("Degree: ", "");
                String campus = campusLabel.getText().replace("Campus: ", "");
                String installment = installmentChoice.getSelectedItem();
                int total = Integer.parseInt(totalLabel.getText().replace("Fee: ", ""));

                if (installment.equals("Half")) {
                    total = total / 2;
                }

                PreparedStatement ps = c.c.prepareStatement(
                        "INSERT INTO collegefee (rollno, degree, campus, semester, installment, total) VALUES (?, ?, ?, ?, ?, ?)");
                ps.setString(1, currentRollno);
                ps.setString(2, degree);
                ps.setString(3, campus);
                ps.setString(4, currentSemester);
                ps.setString(5, installment);
                ps.setInt(6, total);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Fee paid and saved!");
                statusLabel.setText("Fee submitted successfully.");
                payButton.setEnabled(false);
                printButton.setEnabled(true);
                loadFeeHistory(currentRollno);

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error while submitting fee: " + e.getMessage());
            }
        }

        if (cmd.equals("Print Receipt")) {
            StringBuilder receipt = new StringBuilder();
            receipt.append("------ FEE RECEIPT ------\n");
            receipt.append("Roll No: ").append(currentRollno).append("\n");
            receipt.append(semesterLabel.getText()).append("\n");
            receipt.append(degreeLabel.getText()).append("\n");
            receipt.append(campusLabel.getText()).append("\n");
            receipt.append("Installment: ").append(installmentChoice.getSelectedItem()).append("\n");
            receipt.append(totalLabel.getText()).append("\n");
            receipt.append("--------------------------\n");

            JTextArea receiptArea = new JTextArea(receipt.toString());
            try {
                receiptArea.print(); // opens system print dialog
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage());
            }
        }
    }

    private void loadFeeHistory(String rollno) {
        historyModel.setRowCount(0);
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT semester, installment, total, created_at FROM collegefee WHERE rollno = ? ORDER BY created_at DESC");
            ps.setString(1, rollno);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                historyModel.addRow(new Object[]{
                        rs.getString("semester"),
                        rs.getString("installment"),
                        rs.getInt("total"),
                        rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load fee history: " + e.getMessage());
        }
    }

    private int extractSemesterNumber(String semesterLabel) {
        try {
            return Integer.parseInt(semesterLabel.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return -1;
        }
    }

    public static void main(String[] args) {
        new StudentFeeForm();
    }
}

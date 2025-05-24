package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;
import com.toedter.calendar.JDateChooser;

public class AddStudent extends JFrame implements ActionListener {

    JTextField tfname, tffname, tfaddress, tfphone, tfemail, tfclassX, tfclassXII, tfnic;
    JComboBox<String> cbdegree, cbcampus;
    JLabel lblGeneratedRollno;
    JButton submit, cancel;
    JLabel lblSemValue;
    JDateChooser dcdob;

    private String generatedRollNo = "";

    public AddStudent() {
        setTitle("Add Student");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel heading = new JLabel("New Student Details");
        heading.setBounds(300, 30, 500, 50);
        heading.setFont(new Font("Tahoma", Font.BOLD, 30));
        add(heading);

        addLabel("Student Name", 50, 100);
        tfname = addTextField(200, 100);

        addLabel("Father's Name", 450, 100);
        tffname = addTextField(600, 100);

        addLabel("Generated Roll Number", 50, 150);
        lblGeneratedRollno = new JLabel("Will be auto-generated");
        lblGeneratedRollno.setBounds(200, 150, 300, 20);
        lblGeneratedRollno.setForeground(Color.BLUE);
        add(lblGeneratedRollno);

        addLabel("Date of Birth", 450, 150);
        dcdob = new JDateChooser();
        dcdob.setBounds(600, 150, 200, 20);
        add(dcdob);

        addLabel("Address", 50, 200);
        tfaddress = addTextField(200, 200);

        addLabel("Phone", 450, 200);
        tfphone = addTextField(600, 200);

        addLabel("Email", 50, 250);
        tfemail = addTextField(200, 250);

        addLabel("Class X (%)", 450, 250);
        tfclassX = addTextField(600, 250);

        addLabel("Class XII (%)", 50, 300);
        tfclassXII = addTextField(200, 300);

        addLabel("NIC", 450, 300);
        tfnic = addTextField(600, 300);

        addLabel("Degree", 50, 350);
        cbdegree = new JComboBox<>();
        cbdegree.setBounds(200, 350, 200, 20);
        add(cbdegree);

        addLabel("Campus", 450, 350);
        cbcampus = new JComboBox<>();
        cbcampus.setBounds(600, 350, 200, 20);
        add(cbcampus);

        addLabel("Current Semester", 50, 400);
        lblSemValue = new JLabel("Semester 1");
        lblSemValue.setBounds(200, 400, 200, 20);
        add(lblSemValue);

        submit = new JButton("Submit");
        submit.setBounds(250, 500, 120, 30);
        submit.addActionListener(this);
        add(submit);

        cancel = new JButton("Cancel");
        cancel.setBounds(450, 500, 120, 30);
        cancel.addActionListener(this);
        add(cancel);

        loadDegreeAndCampus();

        cbdegree.addActionListener(e -> {
            if (generatedRollNo.isEmpty() && cbdegree.getSelectedItem() != null) {
                try {
                    String degree = (String) cbdegree.getSelectedItem();
                    generatedRollNo = generateUniqueRollNo(degree);
                    lblGeneratedRollno.setText(generatedRollNo);
                } catch (Exception ex) {
                    lblGeneratedRollno.setText("Error generating roll no");
                }
            }
        });

        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }

    private void addLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(x, y, 150, 20);
        add(lbl);
    }

    private JTextField addTextField(int x, int y) {
        JTextField tf = new JTextField();
        tf.setBounds(x, y, 200, 20);
        add(tf);
        return tf;
    }

    private void loadDegreeAndCampus() {
        try {
            Conn c = new Conn();
            ResultSet rsDegree = c.s.executeQuery("SELECT degree_name FROM degree");
            while (rsDegree.next()) {
                cbdegree.addItem(rsDegree.getString("degree_name"));
            }

            ResultSet rsCampus = c.s.executeQuery("SELECT campus_name FROM campus");
            while (rsCampus.next()) {
                cbcampus.addItem(rsCampus.getString("campus_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading degree/campus: " + e.getMessage());
        }
    }

    private String generateUniqueRollNo(String degreePrefix) throws SQLException {
        Conn c = new Conn();
        Random rand = new Random();
        String rollno;
        while (true) {
            int num = 1000 + rand.nextInt(9000);
            rollno = degreePrefix + "_" + num;
            ResultSet rs = c.s.executeQuery("SELECT rollno FROM student WHERE rollno = '" + rollno + "'");
            if (!rs.next()) break;
        }
        return rollno;
    }

    private boolean validateFields() {
        if (tfname.getText().trim().isEmpty()) return showError("Student name is required.");
        if (tffname.getText().trim().isEmpty()) return showError("Father's name is required.");
        if (dcdob.getDate() == null) return showError("Date of birth is required.");
        if (tfaddress.getText().trim().isEmpty()) return showError("Address is required.");
        if (!tfphone.getText().matches("\\d{11}")) return showError("Phone number must be 11 digits.");
        if (!tfemail.getText().matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) return showError("Invalid email format.");
        if (tfclassX.getText().trim().isEmpty()) return showError("Class X % is required.");
        if (tfclassXII.getText().trim().isEmpty()) return showError("Class XII % is required.");

        try {
            double x = Double.parseDouble(tfclassX.getText());
            double xii = Double.parseDouble(tfclassXII.getText());
            if (x < 0 || x > 100 || xii < 0 || xii > 100) {
                return showError("Class X and XII % must be between 0 and 100.");
            }
        } catch (NumberFormatException e) {
            return showError("Class X and XII % must be valid numbers.");
        }

        if (!tfnic.getText().matches("\\d{5}-\\d{7}-\\d{1}"))
            return showError("NIC must be in format XXXXX-XXXXXXX-X.");
        return true;
    }

    private boolean showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == submit) {
            if (!validateFields()) return;

            String rollno = generatedRollNo;
            String name = tfname.getText();
            String fname = tffname.getText();
            String address = tfaddress.getText();
            java.sql.Date dob = new java.sql.Date(dcdob.getDate().getTime());
            String phone = tfphone.getText();
            String email = tfemail.getText();
            String classX = tfclassX.getText();
            String classXII = tfclassXII.getText();
            String nic = tfnic.getText();
            String degree = (String) cbdegree.getSelectedItem();
            String campus = (String) cbcampus.getSelectedItem();
            String semester = lblSemValue.getText();

            try {
                Conn c = new Conn();

                // Check for duplicates
                PreparedStatement check = c.c.prepareStatement("SELECT * FROM student WHERE nic = ? OR phone = ? OR email = ?");
                check.setString(1, nic);
                check.setString(2, phone);
                check.setString(3, email);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "NIC, Phone, or Email already exists.");
                    return;
                }

                String query = "INSERT INTO student (name, fname, rollno, current_semester, dob, address, phone, email, class_x, class_xii, nic, degree, campus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = c.c.prepareStatement(query);
                pst.setString(1, name);
                pst.setString(2, fname);
                pst.setString(3, rollno);
                pst.setString(4, semester);
                pst.setDate(5, dob);
                pst.setString(6, address);
                pst.setString(7, phone);
                pst.setString(8, email);
                pst.setString(9, classX);
                pst.setString(10, classXII);
                pst.setString(11, nic);
                pst.setString(12, degree);
                pst.setString(13, campus);

                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student Added Successfully\nRoll No: " + rollno);
                setVisible(false);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }

        } else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new AddStudent();
    }
}

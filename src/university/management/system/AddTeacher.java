package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;
import com.toedter.calendar.JDateChooser;

public class AddTeacher extends JFrame implements ActionListener {

    JTextField tfname, tffname, tfaddress, tfphone, tfemail, tfgpa, tfnic;
    JComboBox<String> cbeducation, cbcampus;
    JDateChooser dcdob;
    JLabel labelempId;
    JButton submit, cancel;

    public AddTeacher() {
        setTitle("Add Teacher");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel heading = new JLabel("New Teacher Details");
        heading.setBounds(300, 30, 500, 50);
        heading.setFont(new Font("Tahoma", Font.BOLD, 30));
        add(heading);

        JLabel lblname = new JLabel("Name");
        lblname.setBounds(50, 100, 150, 20);
        add(lblname);

        tfname = new JTextField();
        tfname.setBounds(200, 100, 200, 20);
        add(tfname);

        JLabel lblfname = new JLabel("Father's Name");
        lblfname.setBounds(450, 100, 200, 20);
        add(lblfname);

        tffname = new JTextField();
        tffname.setBounds(600, 100, 200, 20);
        add(tffname);

        JLabel lblemp = new JLabel("Generated Emp ID");
        lblemp.setBounds(50, 150, 200, 20);
        add(lblemp);

        labelempId = new JLabel(generateEmpId());
        labelempId.setBounds(200, 150, 300, 20);
        labelempId.setForeground(Color.BLUE);
        add(labelempId);

        JLabel lbldob = new JLabel("Date of Birth");
        lbldob.setBounds(450, 150, 200, 20);
        add(lbldob);

        dcdob = new JDateChooser();
        dcdob.setBounds(600, 150, 200, 20);
        add(dcdob);

        JLabel lbladdress = new JLabel("Address");
        lbladdress.setBounds(50, 200, 150, 20);
        add(lbladdress);

        tfaddress = new JTextField();
        tfaddress.setBounds(200, 200, 200, 20);
        add(tfaddress);

        JLabel lblphone = new JLabel("Phone");
        lblphone.setBounds(450, 200, 200, 20);
        add(lblphone);

        tfphone = new JTextField();
        tfphone.setBounds(600, 200, 200, 20);
        add(tfphone);

        JLabel lblemail = new JLabel("Email");
        lblemail.setBounds(50, 250, 150, 20);
        add(lblemail);

        tfemail = new JTextField();
        tfemail.setBounds(200, 250, 200, 20);
        add(tfemail);

        JLabel lblgpa = new JLabel("Graduation GPA");
        lblgpa.setBounds(450, 250, 200, 20);
        add(lblgpa);

        tfgpa = new JTextField();
        tfgpa.setBounds(600, 250, 200, 20);
        add(tfgpa);

        JLabel lblnic = new JLabel("NIC");
        lblnic.setBounds(50, 300, 150, 20);
        add(lblnic);

        tfnic = new JTextField();
        tfnic.setBounds(200, 300, 200, 20);
        add(tfnic);

        JLabel lbldegree = new JLabel("Education");
        lbldegree.setBounds(450, 300, 150, 20);
        add(lbldegree);

        cbeducation = new JComboBox<>(new String[]{"Graduated", "Masters", "PhD"});
        cbeducation.setBounds(600, 300, 200, 20);
        add(cbeducation);

        JLabel lblcampus = new JLabel("Campus");
        lblcampus.setBounds(50, 350, 150, 20);
        add(lblcampus);

        cbcampus = new JComboBox<>();
        cbcampus.setBounds(200, 350, 200, 20);
        add(cbcampus);

        loadCampus();

        submit = new JButton("Submit");
        submit.setBounds(250, 500, 120, 30);
        submit.addActionListener(this);
        add(submit);

        cancel = new JButton("Cancel");
        cancel.setBounds(450, 500, 120, 30);
        cancel.addActionListener(this);
        add(cancel);

        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }

    private void loadCampus() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT campus_name FROM campus");
            while (rs.next()) {
                cbcampus.addItem(rs.getString("campus_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading campus: " + e.getMessage());
        }
    }

    private String generateEmpId() {
        Random rand = new Random();
        int num = 1000 + rand.nextInt(9000);
        return "EMP_" + num;
    }

    private boolean validateFields() {
        if (tfname.getText().trim().isEmpty()) return showError("Name is required.");
        if (tffname.getText().trim().isEmpty()) return showError("Father's name is required.");
        if (dcdob.getDate() == null) return showError("Date of birth is required.");
        if (tfaddress.getText().trim().isEmpty()) return showError("Address is required.");
        if (!tfphone.getText().matches("\\d{11}")) return showError("Phone number must be 11 digits.");
        if (!tfemail.getText().matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) return showError("Invalid email format.");
        if (tfgpa.getText().trim().isEmpty()) return showError("GPA is required.");
        if (!tfnic.getText().matches("\\d{5}-\\d{7}-\\d{1}")) return showError("NIC must be in format XXXXX-XXXXXXX-X.");
        return true;
    }

    private boolean showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == submit) {
            if (!validateFields()) return;

            String empId = labelempId.getText();
            String name = tfname.getText();
            String fname = tffname.getText();
            java.sql.Date dob = new java.sql.Date(dcdob.getDate().getTime());
            String address = tfaddress.getText();
            String phone = tfphone.getText();
            String email = tfemail.getText();
            String education = (String) cbeducation.getSelectedItem();
            String gpa = tfgpa.getText();
            String nic = tfnic.getText();
            String campus = (String) cbcampus.getSelectedItem();

            try {
                Conn c = new Conn();
                String query = "INSERT INTO teacher (name, fname, empId, dob, address, phone, email, education, gpa, nic, campus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = c.c.prepareStatement(query);
                pst.setString(1, name);
                pst.setString(2, fname);
                pst.setString(3, empId);
                pst.setDate(4, dob);
                pst.setString(5, address);
                pst.setString(6, phone);
                pst.setString(7, email);
                pst.setString(8, education);
                pst.setString(9, gpa);
                pst.setString(10, nic);
                pst.setString(11, campus);

                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Teacher Added Successfully\nEmployee ID: " + empId);
                setVisible(false);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
            }
        } else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new AddTeacher();
    }
}

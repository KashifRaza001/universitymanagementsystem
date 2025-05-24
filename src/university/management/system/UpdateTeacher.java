package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UpdateTeacher extends JFrame implements ActionListener {
    JComboBox<String> cbEducation, cbCampus;
    JTextField tfaddress, tfphone, tfemail;
    JLabel labelEmpId, labelname, labelfname, labeldob, labelnic;
    JButton submit, cancel;
    Choice cEmpId;

    UpdateTeacher() {
        setSize(900, 650);
        setLocation(350, 50);
        setLayout(null);

        JLabel heading = new JLabel("Update Teacher Details");
        heading.setBounds(50, 10, 500, 50);
        heading.setFont(new Font("Tahoma", Font.ITALIC, 35));
        add(heading);

        JLabel lblrollnumber = new JLabel("Select Employee Id");
        lblrollnumber.setBounds(50, 100, 200, 20);
        lblrollnumber.setFont(new Font("serif", Font.PLAIN, 20));
        add(lblrollnumber);

        cEmpId = new Choice();
        cEmpId.setBounds(250, 100, 200, 20);
        add(cEmpId);

        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT empId FROM teacher");
            while (rs.next()) {
                cEmpId.add(rs.getString("empId"));
            }
            c.c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        addLabel("Name", 50, 150);
        labelname = addValueLabel(200, 150);

        addLabel("Father's Name", 400, 150);
        labelfname = addValueLabel(600, 150);

        addLabel("Employee Id", 50, 200);
        labelEmpId = addValueLabel(200, 200);

        addLabel("Date of Birth", 400, 200);
        labeldob = addValueLabel(600, 200);

        addLabel("Address", 50, 250);
        tfaddress = addTextField(200, 250);

        addLabel("Phone", 400, 250);
        tfphone = addTextField(600, 250);

        addLabel("Email Id", 50, 300);
        tfemail = addTextField(200, 300);

        addLabel("NIC Number", 400, 300);
        labelnic = addValueLabel(600, 300);

        addLabel("Education", 50, 350);
        cbEducation = new JComboBox<>(new String[]{"Graduated", "Masters", "PhD"});
        cbEducation.setBounds(200, 350, 150, 30);
        add(cbEducation);

        addLabel("Campus", 400, 350);
        cbCampus = new JComboBox<>();
        cbCampus.setBounds(600, 350, 150, 30);
        add(cbCampus);

        loadCampusesFromDB();
        loadTeacherDetails(cEmpId.getSelectedItem());

        cEmpId.addItemListener(e -> loadTeacherDetails(cEmpId.getSelectedItem()));

        submit = new JButton("Update");
        submit.setBounds(250, 500, 120, 30);
        submit.setBackground(Color.BLACK);
        submit.setForeground(Color.WHITE);
        submit.setFont(new Font("Tahoma", Font.BOLD, 15));
        submit.addActionListener(this);
        add(submit);

        cancel = new JButton("Cancel");
        cancel.setBounds(450, 500, 120, 30);
        cancel.setBackground(Color.BLACK);
        cancel.setForeground(Color.WHITE);
        cancel.setFont(new Font("Tahoma", Font.BOLD, 15));
        cancel.addActionListener(this);
        add(cancel);

        setVisible(true);
    }

    private void loadCampusesFromDB() {
        cbCampus.removeAllItems();
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT campus_name FROM campus");
            while (rs.next()) {
                cbCampus.addItem(rs.getString("campus_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading campuses: " + e.getMessage());
        }
    }

    private void loadTeacherDetails(String empId) {
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("SELECT * FROM teacher WHERE empId = ?");
            ps.setString(1, empId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                labelname.setText(rs.getString("name"));
                labelfname.setText(rs.getString("fname"));
                labeldob.setText(rs.getString("dob"));
                tfaddress.setText(rs.getString("address"));
                tfphone.setText(rs.getString("phone"));
                tfemail.setText(rs.getString("email"));
                labelnic.setText(rs.getString("nic"));
                labelEmpId.setText(rs.getString("empId"));
                cbEducation.setSelectedItem(rs.getString("education"));
                cbCampus.setSelectedItem(rs.getString("campus"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load teacher details: " + e.getMessage());
        }
    }

    private void addLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(x, y, 200, 30);
        lbl.setFont(new Font("serif", Font.BOLD, 20));
        add(lbl);
    }

    private JLabel addValueLabel(int x, int y) {
        JLabel label = new JLabel();
        label.setBounds(x, y, 200, 30);
        label.setFont(new Font("Tahoma", Font.PLAIN, 18));
        add(label);
        return label;
    }

    private JTextField addTextField(int x, int y) {
        JTextField tf = new JTextField();
        tf.setBounds(x, y, 150, 30);
        add(tf);
        return tf;
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == submit) {
            String empId = labelEmpId.getText();
            String address = tfaddress.getText();
            String phone = tfphone.getText();
            String email = tfemail.getText();
            String education = (String) cbEducation.getSelectedItem();
            String campus = (String) cbCampus.getSelectedItem();

            if (!phone.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(this, "Phone must be 11 digits.");
                return;
            }

            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
                JOptionPane.showMessageDialog(this, "Invalid email format.");
                return;
            }

            try {
                Conn con = new Conn();
                String query = "UPDATE teacher SET address = ?, phone = ?, email = ?, education = ?, campus = ? WHERE empId = ?";
                PreparedStatement ps = con.c.prepareStatement(query);
                ps.setString(1, address);
                ps.setString(2, phone);
                ps.setString(3, email);
                ps.setString(4, education);
                ps.setString(5, campus);
                ps.setString(6, empId);

                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Teacher Details Updated Successfully");
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this, "No update made.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error updating teacher: " + e.getMessage());
            }
        } else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new UpdateTeacher();
    }
}

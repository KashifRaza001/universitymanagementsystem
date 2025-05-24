package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UpdateStudent extends JFrame implements ActionListener {

    JTextField tfaddress, tfphone, tfemail;
    JComboBox<String> cbcampus;
    JLabel labelrollno, labelname, labelfname, labeldob, labelx, labelxii, labelnic, labeldegree;
    JButton submit, cancel;
    Choice crollno;

    UpdateStudent() {

        setSize(900, 650);
        setLocation(350, 50);
        setLayout(null);

        JLabel heading = new JLabel("Update Student Details");
        heading.setBounds(50, 10, 500, 50);
        heading.setFont(new Font("Tahoma", Font.ITALIC, 35));
        add(heading);

        JLabel lblrollnumber = new JLabel("Select Roll Number");
        lblrollnumber.setBounds(50, 100, 200, 20);
        lblrollnumber.setFont(new Font("serif", Font.PLAIN, 20));
        add(lblrollnumber);

        crollno = new Choice();
        crollno.setBounds(250, 100, 200, 20);
        add(crollno);

        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("select rollno from student");
            while (rs.next()) {
                crollno.add(rs.getString("rollno"));
            }
            c.c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        createLabel("Name", 50, 150);
        labelname = createDynamicLabel(200, 150);

        createLabel("Father's Name", 400, 150);
        labelfname = createDynamicLabel(600, 150);

        createLabel("Roll Number", 50, 200);
        labelrollno = createDynamicLabel(200, 200);

        createLabel("Date of Birth", 400, 200);
        labeldob = createDynamicLabel(600, 200);

        createLabel("Address", 50, 250);
        tfaddress = createTextField(200, 250);

        createLabel("Phone", 400, 250);
        tfphone = createTextField(600, 250);

        createLabel("Email Id", 50, 300);
        tfemail = createTextField(200, 300);

        createLabel("Class X (%)", 400, 300);
        labelx = createDynamicLabel(600, 300);

        createLabel("Class XII (%)", 50, 350);
        labelxii = createDynamicLabel(200, 350);

        createLabel("NIC Number", 400, 350);
        labelnic = createDynamicLabel(600, 350);

        createLabel("Degree", 50, 400);
        labeldegree = createDynamicLabel(200, 400); // now label not textfield

        createLabel("Campus", 400, 400);
        cbcampus = new JComboBox<>();
        cbcampus.setBounds(600, 400, 150, 30);
        add(cbcampus);
        loadCampusOptions();

        crollno.addItemListener(e -> loadStudentDetails());

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

    private JLabel createLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, 200, 30);
        label.setFont(new Font("serif", Font.BOLD, 20));
        add(label);
        return label;
    }

    private JLabel createDynamicLabel(int x, int y) {
        JLabel label = new JLabel();
        label.setBounds(x, y, 150, 30);
        label.setFont(new Font("Tahoma", Font.PLAIN, 18));
        add(label);
        return label;
    }

    private JTextField createTextField(int x, int y) {
        JTextField tf = new JTextField();
        tf.setBounds(x, y, 150, 30);
        add(tf);
        return tf;
    }

    private void loadCampusOptions() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT campus_name FROM campus");
            while (rs.next()) {
                cbcampus.addItem(rs.getString("campus_name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading campus list: " + e.getMessage());
        }
    }

    private void loadStudentDetails() {
        try {
            Conn c = new Conn();
            String query = "SELECT * FROM student WHERE rollno = ?";
            PreparedStatement pst = c.c.prepareStatement(query);
            pst.setString(1, crollno.getSelectedItem());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                labelname.setText(rs.getString("name"));
                labelfname.setText(rs.getString("fname"));
                labeldob.setText(rs.getString("dob"));
                tfaddress.setText(rs.getString("address"));
                tfphone.setText(rs.getString("phone"));
                tfemail.setText(rs.getString("email"));
                labelx.setText(rs.getString("class_x"));
                labelxii.setText(rs.getString("class_xii"));
                labelnic.setText(rs.getString("nic"));
                labelrollno.setText(rs.getString("rollno"));
                labeldegree.setText(rs.getString("degree"));

                String campus = rs.getString("campus");
                cbcampus.setSelectedItem(campus);
            }
            c.c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == submit) {
            String rollno = labelrollno.getText();
            String address = tfaddress.getText();
            String phone = tfphone.getText();
            String email = tfemail.getText();
            String campus = (String) cbcampus.getSelectedItem();

            try {
                Conn con = new Conn();
                String query = "UPDATE student SET address = ?, phone = ?, email = ?, campus = ? WHERE rollno = ?";
                PreparedStatement pst = con.c.prepareStatement(query);
                pst.setString(1, address);
                pst.setString(2, phone);
                pst.setString(3, email);
                pst.setString(4, campus);
                pst.setString(5, rollno);

                int updated = pst.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Student details updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "No changes were made.");
                }
                setVisible(false);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating record: " + e.getMessage());
            }
        } else {
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new UpdateStudent();
    }
}

package ui.auth;

import dao.EmployeeDAO;
import model.Employee;
import model.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);

    private final JButton btnLogin = new JButton("Đăng nhập");
    private final JButton btnExit  = new JButton("Thoát");

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    public LoginFrame() {
        setTitle("Hotel Management - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 260);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ KHÁCH SẠN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        root.add(form, BorderLayout.CENTER);

        // Username label
        GridBagConstraints c1 = new GridBagConstraints();
        c1.insets = new Insets(6, 6, 6, 6);
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.gridx = 0; c1.gridy = 0;
        form.add(new JLabel("Username:"), c1);

        // Username field
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets = new Insets(6, 6, 6, 6);
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.gridx = 1; c2.gridy = 0;
        form.add(txtUsername, c2);

        // Password label
        GridBagConstraints c3 = new GridBagConstraints();
        c3.insets = new Insets(6, 6, 6, 6);
        c3.fill = GridBagConstraints.HORIZONTAL;
        c3.gridx = 0; c3.gridy = 1;
        form.add(new JLabel("Password:"), c3);

        // Password field
        GridBagConstraints c4 = new GridBagConstraints();
        c4.insets = new Insets(6, 6, 6, 6);
        c4.fill = GridBagConstraints.HORIZONTAL;
        c4.gridx = 1; c4.gridy = 1;
        form.add(txtPassword, c4);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnLogin);
        buttons.add(btnExit);
        root.add(buttons, BorderLayout.SOUTH);

        btnExit.addActionListener(e -> System.exit(0));
        btnLogin.addActionListener(e -> doLogin());

        // Nhấn Enter để login
        getRootPane().setDefaultButton(btnLogin);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập username và password!");
            return;
        }

        try {
            Employee emp = employeeDAO.login(username, password);

            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Sai tài khoản/mật khẩu hoặc tài khoản bị khóa!");
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Đăng nhập thành công: " + emp.getFullName() + " (" + emp.getRole() + ")");

            // TODO: mở frame theo role
            if (emp.getRole() == Role.ADMIN) {
                // new ui.admin.AdminFrame(emp).setVisible(true);
            } else {
                // new ui.employee.EmployeeFrame(emp).setVisible(true);
            }
            // dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi DB/Driver. Xem Console để biết chi tiết!");
        }
    }
}
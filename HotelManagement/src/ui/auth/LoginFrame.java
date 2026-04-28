package ui.auth;

import dao.EmployeeDAO;
import model.Employee;
import model.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Path2D;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();

    private final JButton btnLogin = new JButton("Đăng nhập");
    private final JButton btnExit = new JButton("Thoát");

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    public LoginFrame() {
        setTitle("Hotel Management - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(Color.WHITE);
        setContentPane(root);

        // --- Left Panel ---
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(80, 50, 80, 50));

        JLabel lblTitle = new JLabel("Sign In");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(lblTitle);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Form Fields
        JPanel formPan = new JPanel();
        formPan.setLayout(new BoxLayout(formPan, BoxLayout.Y_AXIS));
        formPan.setBackground(Color.WHITE);
        
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        txtUsername.setMaximumSize(new Dimension(250, 40));
        txtUsername.setPreferredSize(new Dimension(250, 40));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        txtUsername.setBackground(new Color(245, 245, 245));
        
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        txtPassword.setMaximumSize(new Dimension(250, 40));
        txtPassword.setPreferredSize(new Dimension(250, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        txtPassword.setBackground(new Color(245, 245, 245));

        formPan.add(lblUser);
        formPan.add(Box.createRigidArea(new Dimension(0, 5)));
        formPan.add(txtUsername);
        formPan.add(Box.createRigidArea(new Dimension(0, 15)));
        formPan.add(lblPass);
        formPan.add(Box.createRigidArea(new Dimension(0, 5)));
        formPan.add(txtPassword);

        leftPanel.add(formPan);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(260, 40));

        btnLogin.setPreferredSize(new Dimension(120, 40));
        btnLogin.setBackground(new Color(88, 77, 184));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBorder(BorderFactory.createEmptyBorder());

        btnExit.setPreferredSize(new Dimension(120, 40));
        btnExit.setBackground(new Color(200, 60, 60)); // Red exit button
        btnExit.setForeground(Color.WHITE);
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setBorder(BorderFactory.createEmptyBorder());

        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);

        leftPanel.add(btnPanel);
        leftPanel.add(Box.createVerticalGlue());
        
        root.add(leftPanel);

        // --- Right Panel ---
        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                g2.setColor(new Color(88, 77, 184)); // Match the purple in the image
                Path2D path = new Path2D.Float();
                int curveRadius = 150;
                
                path.moveTo(w, 0);
                path.lineTo(curveRadius, 0);
                // Top-left curve
                path.quadTo(0, 0, 0, curveRadius);
                path.lineTo(0, h - curveRadius);
                // Bottom-left curve
                path.quadTo(0, h, curveRadius, h);
                path.lineTo(w, h);
                path.closePath();
                
                g2.fill(path);
            }
        };
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(0, 20, 0, 0)); // space for text not to hit the left curve

        rightPanel.add(Box.createVerticalGlue());
        
        JLabel lblWelcome = new JLabel("Welcome Hotel");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblWelcome);
        
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel lblSub = new JLabel("chúc bạn có 1 ngày tốt lành");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblSub.setForeground(Color.WHITE);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblSub);
        
        rightPanel.add(Box.createVerticalGlue());

        root.add(rightPanel);

        btnLogin.addActionListener(e -> doLogin());
        btnExit.addActionListener(e -> System.exit(0));
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

            if (emp.getRole() == Role.ADMIN) {
                new ui.admin.AdminFrame(emp).setVisible(true);
            } else {
                new ui.employee.StaffFrame(emp).setVisible(true);
            }
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi DB/Driver. Xem Console để biết chi tiết!");
        }
    }
}
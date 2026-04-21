package ui.auth;

import dao.EmployeeDAO;
import model.Employee;
import model.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.border.LineBorder;

public class LoginFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);

    private final JButton btnLogin = new JButton("Đăng nhập");
    private final JButton btnExit = new JButton("Thoát");

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    public LoginFrame() {
        setTitle("Hotel Management - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 260);
        setLocationRelativeTo(null);

        // Root panel with nicer background
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(new Color(245, 247, 250));
        setContentPane(root);

        // Title area
        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ KHÁCH SẠN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 60, 90));
        root.add(lblTitle, BorderLayout.NORTH);

        // Left icon panel
        JLabel bigIcon = new JLabel(createIcon(64, new Color(60, 130, 200), Color.WHITE, "person"));
        bigIcon.setBorder(new EmptyBorder(6, 6, 6, 12));
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(bigIcon, BorderLayout.NORTH);
        root.add(left, BorderLayout.WEST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        root.add(form, BorderLayout.CENTER);

        // Username label + field with small icon
        GridBagConstraints c1 = new GridBagConstraints();
        c1.insets = new Insets(6, 6, 6, 6);
        c1.anchor = GridBagConstraints.LINE_END;
        c1.gridx = 0;
        c1.gridy = 0;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setIcon(createIcon(18, new Color(70, 130, 180), Color.WHITE, "person"));
        form.add(lblUser, c1);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets = new Insets(6, 6, 6, 6);
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.weightx = 1.0;
        c2.gridx = 1;
        c2.gridy = 0;
        txtUsername.setBorder(new LineBorder(new Color(210, 210, 210), 1));
        form.add(txtUsername, c2);

        // Password label + field with small icon
        GridBagConstraints c3 = new GridBagConstraints();
        c3.insets = new Insets(6, 6, 6, 6);
        c3.anchor = GridBagConstraints.LINE_END;
        c3.gridx = 0;
        c3.gridy = 1;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setIcon(createIcon(18, new Color(100, 160, 90), Color.WHITE, "key"));
        form.add(lblPass, c3);

        GridBagConstraints c4 = new GridBagConstraints();
        c4.insets = new Insets(6, 6, 6, 6);
        c4.fill = GridBagConstraints.HORIZONTAL;
        c4.weightx = 1.0;
        c4.gridx = 1;
        c4.gridy = 1;
        txtPassword.setBorder(new LineBorder(new Color(210, 210, 210), 1));
        form.add(txtPassword, c4);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setOpaque(false);
        btnLogin.setBackground(new Color(60, 130, 200));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setIcon(createIcon(18, new Color(30, 90, 150), Color.WHITE, "power"));

        btnExit.setBackground(new Color(200, 60, 60));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        btnExit.setIcon(createIcon(16, new Color(160, 40, 40), Color.WHITE, "X"));

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

    // Simple helper to create a circular icon with a center letter.
    private static ImageIcon createIcon(int size, Color bg, Color fg, String text) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        // background circle
        g.setColor(bg);
        g.fillOval(0, 0, size, size);
        g.setColor(fg);

        if ("person".equalsIgnoreCase(text)) {
            // draw a simple person silhouette: head + body
            int headR = size / 3;
            int hx = size / 2 - headR / 2;
            int hy = size / 6;
            g.fillOval(hx, hy, headR, headR);
            int bodyW = size / 2;
            int bodyH = size / 3;
            int bx = size / 2 - bodyW / 2;
            int by = hy + headR + 2;
            g.fillRoundRect(bx, by, bodyW, bodyH, bodyW / 4, bodyW / 4);
        } else if ("key".equalsIgnoreCase(text)) {
            // draw a simple key
            int y = size / 2;
            g.fillRect(size / 6, y - 3, size / 2, 6);
            g.fillOval(size / 10, y - 6, size / 3, size / 3);
            // teeth
            int tx = size / 6 + size / 2;
            g.fillRect(tx, y - 6, 6, 6);
            g.fillRect(tx + 6, y - 2, 6, 6);
        } else if ("power".equalsIgnoreCase(text)) {
            // power icon: circle with vertical line
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(Math.max(2, size / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int pad = Math.max(2, size / 8);
            g.drawOval(pad, pad, size - 2 * pad, size - 2 * pad);
            g.drawLine(size / 2, pad / 2, size / 2, size / 2 + 2);
            g.setStroke(old);
        } else {
            // fallback: letter
            Font f = new Font("Dialog", Font.BOLD, Math.max(10, size / 2));
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            int tx = (size - fm.stringWidth(text)) / 2;
            int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, tx, ty);
        }

        g.dispose();
        return new ImageIcon(img);
    }
}
package ui.admin;

import model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AdminFrame extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final CardLayout card = new CardLayout();
    private final JPanel cardPanel = new JPanel(card);

    public AdminFrame(Employee me) {
        setTitle("Admin - Hotel Management | " + me.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(250, 251, 253));
        setContentPane(root);

        // Top bar
        JLabel top = new JLabel("ADMIN DASHBOARD  |  " + me.getFullName() + " (" + me.getRole() + ")");
        top.setFont(new Font("Tahoma", Font.BOLD, 14));
        top.setForeground(new Color(30,60,90));
        root.add(top, BorderLayout.NORTH);

        // Left menu
        JPanel menu = new JPanel();
        menu.setLayout(new GridLayout(0, 1, 0, 8));
        menu.setBorder(new EmptyBorder(0, 0, 0, 10));
        menu.setBackground(new Color(235,240,245));
        root.add(menu, BorderLayout.WEST);

        JButton btnEmployees = new JButton("Quản lý nhân viên");
        JButton btnRooms = new JButton("Quản lý phòng");
        JButton btnServices = new JButton("Quản lý dịch vụ");
        JButton btnRevenue = new JButton("Doanh thu / Hóa đơn");
        JButton btnSalary = new JButton("Tính lương");
        JButton btnLogout = new JButton("Đăng xuất");

        JButton[] buttons = new JButton[]{btnEmployees, btnRooms, btnServices, btnRevenue, btnSalary, btnLogout};
        Color btnBg = new Color(60,130,200);
        for (int i = 0; i < buttons.length; i++) {
            JButton b = buttons[i];
            b.setFocusPainted(false);
            b.setForeground(Color.WHITE);
            b.setBackground(i == buttons.length-1 ? new Color(200,60,60) : btnBg);
            String letter = "";
            switch (i) {
                case 0: letter = "E"; break;
                case 1: letter = "R"; break;
                case 2: letter = "S"; break;
                case 3: letter = "D"; break;
                case 4: letter = "$"; break;
                case 5: letter = "L"; break;
            }
            b.setIcon(createIcon(18, new Color(30,90,150), Color.WHITE, letter));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setPreferredSize(new Dimension(220, 60));
            menu.add(b);
        }

        // Cards
        cardPanel.add(new EmployeesPanel(me), "employees");
        cardPanel.add(new RoomsPanel(), "rooms");
        cardPanel.add(new ServicesPanel(), "services");
        cardPanel.add(new RevenuePanel(), "revenue");
        cardPanel.add(new SalaryPanel(), "salary");
        root.add(cardPanel, BorderLayout.CENTER);

        // Actions
        btnEmployees.addActionListener(e -> card.show(cardPanel, "employees"));
        btnRooms.addActionListener(e -> card.show(cardPanel, "rooms"));
        btnServices.addActionListener(e -> card.show(cardPanel, "services"));
        btnRevenue.addActionListener(e -> card.show(cardPanel, "revenue"));
        btnSalary.addActionListener(e -> card.show(cardPanel, "salary"));

        btnLogout.addActionListener(e -> {
            dispose();
            new ui.auth.LoginFrame().setVisible(true);
        });

        card.show(cardPanel, "employees");
    }

    private static ImageIcon createIcon(int size, Color bg, Color fg, String text) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(bg);
        g.fillOval(0, 0, size, size);
        g.setColor(fg);
        Font f = new Font("Dialog", Font.BOLD, Math.max(10, size/2));
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int tx = (size - fm.stringWidth(text)) / 2;
        int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
        g.dispose();
        return new ImageIcon(img);
    }
}
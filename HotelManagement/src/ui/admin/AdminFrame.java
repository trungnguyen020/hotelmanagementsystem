package ui.admin;

import model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AdminFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final CardLayout card = new CardLayout();
    private final JPanel cardPanel = new JPanel(card);

    private boolean isExpanded = true;
    private JPanel sidebar;
    private JLabel lblLogo;
    private JButton[] menuButtons;
    private String[] menuTexts;

    public AdminFrame(Employee me) {
        setTitle("Admin - Hotel Management | " + me.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(250, 251, 253));
        setContentPane(root);

        // --- Sidebar ---
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(21, 18, 31)); // Dark background
        sidebar.setPreferredSize(new Dimension(220, 0));
        root.add(sidebar, BorderLayout.WEST);

        // Sidebar Top
        JPanel sidebarTop = new JPanel(new BorderLayout());
        sidebarTop.setOpaque(false);
        sidebarTop.setBorder(new EmptyBorder(15, 10, 15, 10));

        lblLogo = new JLabel("HOTEL");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnToggle = new JButton("≡");
        btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnToggle.setForeground(Color.WHITE);
        btnToggle.setBackground(new Color(21, 18, 31));
        btnToggle.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sidebarTop.add(lblLogo, BorderLayout.CENTER);
        sidebarTop.add(btnToggle, BorderLayout.EAST);
        sidebar.add(sidebarTop, BorderLayout.NORTH);

        // Sidebar Menu
        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 0));
        menu.setOpaque(false);

        JPanel menuContainer = new JPanel(new BorderLayout());
        menuContainer.setOpaque(false);
        menuContainer.add(menu, BorderLayout.NORTH);
        sidebar.add(menuContainer, BorderLayout.CENTER);

        menuTexts = new String[] {
                " Quản lý nhân viên", " Quản lý phòng", " Quản lý dịch vụ",
                " Doanh thu / Hóa đơn", " Tính lương", " Đăng xuất"
        };
        menuButtons = new JButton[6];
        String[] letters = { "E", "R", "S", "D", "$", "L" };
        Color btnBg = new Color(21, 18, 31);
        Color hoverBg = new Color(40, 36, 56);

        for (int i = 0; i < menuButtons.length; i++) {
            JButton b = new JButton(menuTexts[i]);
            b.setFocusPainted(false);
            b.setForeground(Color.WHITE);
            b.setBackground(btnBg);
            b.setBorder(new EmptyBorder(15, 15, 15, 15));
            b.setIcon(createIcon(20, new Color(88, 77, 184), Color.WHITE, letters[i]));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Hover effect
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    b.setBackground(hoverBg);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    b.setBackground(btnBg);
                }
            });

            menuButtons[i] = b;
            menu.add(b);
        }

        // --- Main Area ---
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setOpaque(false);
        root.add(mainArea, BorderLayout.CENTER);

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel top = new JLabel("ADMIN DASHBOARD  |  " + me.getFullName() + " (" + me.getRole() + ")");
        top.setFont(new Font("Segoe UI", Font.BOLD, 14));
        top.setForeground(new Color(50, 50, 50));
        topBar.add(top, BorderLayout.WEST);
        mainArea.add(topBar, BorderLayout.NORTH);

        // Cards Container
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        cardPanel.add(new EmployeesPanel(me), "employees");
        cardPanel.add(new RoomsPanel(), "rooms");
        cardPanel.add(new ServicesPanel(), "services");
        cardPanel.add(new RevenuePanel(), "revenue");
        cardPanel.add(new SalaryPanel(), "salary");
        mainArea.add(cardPanel, BorderLayout.CENTER);

        // Actions
        btnToggle.addActionListener(e -> toggleSidebar());

        menuButtons[0].addActionListener(e -> card.show(cardPanel, "employees"));
        menuButtons[1].addActionListener(e -> card.show(cardPanel, "rooms"));
        menuButtons[2].addActionListener(e -> card.show(cardPanel, "services"));
        menuButtons[3].addActionListener(e -> card.show(cardPanel, "revenue"));
        menuButtons[4].addActionListener(e -> card.show(cardPanel, "salary"));
        menuButtons[5].addActionListener(e -> {
            dispose();
            new ui.auth.LoginFrame().setVisible(true);
        });

        card.show(cardPanel, "employees");
    }

    private void toggleSidebar() {
        isExpanded = !isExpanded;
        if (isExpanded) {
            sidebar.setPreferredSize(new Dimension(220, 0));
            lblLogo.setVisible(true);
            for (int i = 0; i < menuButtons.length; i++) {
                menuButtons[i].setText(menuTexts[i]);
            }
        } else {
            sidebar.setPreferredSize(new Dimension(60, 0));
            lblLogo.setVisible(false);
            for (int i = 0; i < menuButtons.length; i++) {
                menuButtons[i].setText("");
            }
        }
        sidebar.revalidate();
        sidebar.repaint();
    }

    private static ImageIcon createIcon(int size, Color bg, Color fg, String text) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(bg);
        g.fillRoundRect(0, 0, size, size, 8, 8); // slightly rounded icon
        g.setColor(fg);
        Font f = new Font("Segoe UI", Font.BOLD, Math.max(10, size / 2));
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int tx = (size - fm.stringWidth(text)) / 2;
        int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
        g.dispose();
        return new ImageIcon(img);
    }
}
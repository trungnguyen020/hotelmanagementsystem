package ui.admin;

import model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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
        setContentPane(root);

        // Top bar
        JLabel top = new JLabel("ADMIN DASHBOARD  |  " + me.getFullName() + " (" + me.getRole() + ")");
        top.setFont(new Font("Tahoma", Font.BOLD, 14));
        root.add(top, BorderLayout.NORTH);

        // Left menu
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(0, 0, 0, 10));
        root.add(menu, BorderLayout.WEST);

        JButton btnEmployees = new JButton("Quản lý nhân viên");
        JButton btnRooms = new JButton("Quản lý phòng");
        JButton btnServices = new JButton("Quản lý dịch vụ");
        JButton btnRevenue = new JButton("Doanh thu / Hóa đơn");
        JButton btnLogout = new JButton("Đăng xuất");

        Dimension btnSize = new Dimension(200, 40);
        for (JButton b : new JButton[]{btnEmployees, btnRooms, btnServices, btnRevenue, btnLogout}) {
            b.setMaximumSize(btnSize);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            menu.add(b);
            menu.add(Box.createVerticalStrut(8));
        }

        // Cards
        cardPanel.add(new EmployeesPanel(), "employees");
        cardPanel.add(new RoomsPanel(), "rooms");
        cardPanel.add(new ServicesPanel(), "services");
        cardPanel.add(new RevenuePanel(), "revenue");
        root.add(cardPanel, BorderLayout.CENTER);

        // Actions
        btnEmployees.addActionListener(e -> card.show(cardPanel, "employees"));
        btnRooms.addActionListener(e -> card.show(cardPanel, "rooms"));
        btnServices.addActionListener(e -> card.show(cardPanel, "services"));
        btnRevenue.addActionListener(e -> card.show(cardPanel, "revenue"));

        btnLogout.addActionListener(e -> {
            dispose();
            new ui.auth.LoginFrame().setVisible(true);
        });

        card.show(cardPanel, "employees");
    }
}
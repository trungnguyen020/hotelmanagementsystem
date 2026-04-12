package ui.employee;

import model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StaffFrame extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final CardLayout card = new CardLayout();
    private final JPanel cardPanel = new JPanel(card);

    public StaffFrame(Employee me) {
        setTitle("Nhân viên - Hotel Management | " + me.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        JLabel top = new JLabel("NHÂN VIÊN  |  " + me.getFullName() + " (" + me.getRole() + ")");
        top.setFont(new Font("Tahoma", Font.BOLD, 14));
        root.add(top, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(0, 0, 0, 10));
        root.add(menu, BorderLayout.WEST);

        JButton btnRooms = new JButton("Quản lý phòng");
        JButton btnServices = new JButton("Quản lý dịch vụ");
        JButton btnCheckin = new JButton("Check-in");
        JButton btnCheckout = new JButton("Check-out");
        JButton btnLogout = new JButton("Đăng xuất");

        Dimension btnSize = new Dimension(200, 40);
        for (JButton b : new JButton[]{btnRooms, btnServices, btnCheckin, btnCheckout, btnLogout}) {
            b.setMaximumSize(btnSize);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            menu.add(b);
            menu.add(Box.createVerticalStrut(8));
        }

        // Cards
        cardPanel.add(new RoomsPanel(), "rooms");
        cardPanel.add(new ServicesPanel(), "services");
        cardPanel.add(new CheckinPanel(), "checkin");
        cardPanel.add(new CheckoutPanel(), "checkout");
        root.add(cardPanel, BorderLayout.CENTER);

        // Actions
        btnRooms.addActionListener(e -> card.show(cardPanel, "rooms"));
        btnServices.addActionListener(e -> card.show(cardPanel, "services"));
        btnCheckin.addActionListener(e -> card.show(cardPanel, "checkin"));
        btnCheckout.addActionListener(e -> card.show(cardPanel, "checkout"));

        btnLogout.addActionListener(e -> {
            dispose();
            new ui.auth.LoginFrame().setVisible(true);
        });

        card.show(cardPanel, "rooms");
    }
}
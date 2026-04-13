package ui.employee;

import model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StaffFrame extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final CardLayout card = new CardLayout();
    private final JPanel cardPanel = new JPanel(card);

    private final RoomsPanel roomsPanel = new RoomsPanel();
    private final ServicesPanel servicesPanel = new ServicesPanel();
    private final CheckinPanel checkinPanel;
    private final CheckoutPanel checkoutPanel;

    public StaffFrame(Employee me) {
        setTitle("Nhân viên - Hotel Management | " + me.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(250, 251, 253));
        setContentPane(root);

        JLabel top = new JLabel("NHÂN VIÊN  |  " + me.getFullName() + " (" + me.getRole() + ")");
        top.setFont(new Font("Tahoma", Font.BOLD, 14));
        top.setForeground(new Color(30,60,90));
        root.add(top, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        // Use GridLayout so buttons stretch vertically to fill the left area
        menu.setLayout(new GridLayout(0, 1, 0, 8));
        menu.setBorder(new EmptyBorder(0, 0, 0, 10));
        menu.setBackground(new Color(235,240,245));
        root.add(menu, BorderLayout.WEST);

        JButton btnRooms = new JButton("Quản lý phòng");
        JButton btnServices = new JButton("Quản lý dịch vụ");
        JButton btnCheckin = new JButton("Check-in");
        JButton btnCheckout = new JButton("Check-out");
        JButton btnLogout = new JButton("Đăng xuất");

        // Style buttons: add icons, colors and larger size
        JButton[] buttons = new JButton[]{btnRooms, btnServices, btnCheckin, btnCheckout, btnLogout};
        Color btnBg = new Color(60,130,200);
        for (int i = 0; i < buttons.length; i++) {
            JButton b = buttons[i];
            b.setFocusPainted(false);
            b.setForeground(Color.WHITE);
            b.setBackground(i == buttons.length-1 ? new Color(200,60,60) : btnBg);
            // small icon letter
            String letter = "";
            switch (i) {
                case 0: letter = "R"; break;
                case 1: letter = "S"; break;
                case 2: letter = "I"; break;
                case 3: letter = "O"; break;
                case 4: letter = "L"; break;
            }
            b.setIcon(createIcon(18, new Color(30,90,150), Color.WHITE, letter));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setPreferredSize(new Dimension(220, 60));
            menu.add(b);
        }

        checkinPanel = new CheckinPanel(me, roomsPanel);
        checkoutPanel = new CheckoutPanel(me, roomsPanel);

        cardPanel.add(roomsPanel, "rooms");
        cardPanel.add(servicesPanel, "services");
        cardPanel.add(checkinPanel, "checkin");
        cardPanel.add(checkoutPanel, "checkout");
        root.add(cardPanel, BorderLayout.CENTER);

        btnRooms.addActionListener(e -> { roomsPanel.loadRooms(); card.show(cardPanel, "rooms"); });
        btnServices.addActionListener(e -> {
            servicesPanel.reload();
            card.show(cardPanel, "services");
        });
        btnCheckin.addActionListener(e -> card.show(cardPanel, "checkin"));
        btnCheckout.addActionListener(e -> { checkoutPanel.reload(); card.show(cardPanel, "checkout"); });

        btnLogout.addActionListener(e -> {
            dispose();
            new ui.auth.LoginFrame().setVisible(true);
        });

        card.show(cardPanel, "rooms");
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
package ui.employee;

import dao.ServiceUsageDAO;
import dao.StayDAO;
import model.ServiceUsageView;
import model.StayView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings({"serial", "this-escape"})
public class DashboardPanel extends JPanel {

    private final StayDAO stayDAO = new StayDAO();
    private final ServiceUsageDAO usageDAO = new ServiceUsageDAO();

    private final DefaultTableModel modelCheckout = new DefaultTableModel(
            new Object[]{"Phòng", "Khách hàng", "Dự kiến Check-out"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tblCheckout = new JTable(modelCheckout);

    private final DefaultTableModel modelServices = new DefaultTableModel(
            new Object[]{"Phòng", "Dịch vụ", "Số lượng", "Thời gian"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tblServices = new JTable(modelServices);
    
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public DashboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel title = new JLabel("THÔNG BÁO & LỊCH TRÌNH HÔM NAY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(44, 62, 80));
        topPanel.add(title, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> reload());
        topPanel.add(btnRefresh, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Checkout Panel
        JPanel checkoutPanel = createSection("Phòng dự kiến Check-out hôm nay", tblCheckout);
        // Services Panel
        JPanel servicesPanel = createSection("Dịch vụ được sử dụng hôm nay", tblServices);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, checkoutPanel, servicesPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // Styling tables
        styleTable(tblCheckout);
        styleTable(tblServices);

        reload();
    }

    private JPanel createSection(String titleStr, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTitle = new JLabel(titleStr);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(230, 126, 34)); // Orange accent
        panel.add(lblTitle, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionBackground(new Color(220, 235, 250));
        table.setShowVerticalLines(false);
    }

    public void reload() {
        try {
            modelCheckout.setRowCount(0);
            List<StayView> checkouts = stayDAO.findStaysCheckingOutToday();
            for (StayView s : checkouts) {
                String time = s.getExpectedCheckoutAt() != null ? s.getExpectedCheckoutAt().format(timeFormatter) : "";
                modelCheckout.addRow(new Object[]{
                        s.getRoomNumber(),
                        s.getCustomerName(),
                        time
                });
            }

            modelServices.setRowCount(0);
            List<ServiceUsageView> usages = usageDAO.getUsagesForToday();
            for (ServiceUsageView u : usages) {
                String time = u.getUsedAt() != null ? u.getUsedAt().format(timeFormatter) : "";
                modelServices.addRow(new Object[]{
                        u.getRoomNumber(),
                        u.getServiceName(),
                        u.getQuantity(),
                        time
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu thông báo!");
        }
    }
}

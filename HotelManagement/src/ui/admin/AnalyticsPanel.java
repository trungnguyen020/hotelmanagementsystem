package ui.admin;

import dao.AnalyticsDAO;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class AnalyticsPanel extends JPanel {
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public AnalyticsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 246, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("BẢNG PHÂN TÍCH VÀ THỐNG KÊ (ANALYTICS)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(2, 75, 141));
        titlePanel.add(titleLabel);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setBackground(new Color(60, 130, 200));
        btnRefresh.setForeground(Color.WHITE);
        titlePanel.add(btnRefresh);

        add(titlePanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        contentPanel.setOpaque(false);

        // Card 1: Room Usage
        JPanel pnlRoomUsage = createCard("Tỉ lệ đặt loại phòng");
        updateRoomUsageCard(pnlRoomUsage);
        contentPanel.add(pnlRoomUsage);

        // Card 2: Revenue
        JPanel pnlRevenue = createCard("Doanh thu theo loại phòng");
        updateRevenueCard(pnlRevenue);
        contentPanel.add(pnlRevenue);

        // Card 3: Top Customers
        JPanel pnlTopCustomers = createCard("Khách hàng trung thành (Top 5)");
        updateTopCustomersCard(pnlTopCustomers);
        contentPanel.add(pnlTopCustomers);

        add(contentPanel, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> {
            updateRoomUsageCard(pnlRoomUsage);
            updateRevenueCard(pnlRevenue);
            updateTopCustomersCard(pnlTopCustomers);
            revalidate();
            repaint();
        });
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        card.add(lblTitle, BorderLayout.NORTH);

        return card;
    }

    private void updateRoomUsageCard(JPanel card) {
        if (card.getComponentCount() > 1) {
            card.remove(1);
        }
        JPanel content = new JPanel(new GridLayout(0, 2, 10, 10));
        content.setOpaque(false);
        Map<String, Integer> data = analyticsDAO.getRoomTypeUsage();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            JLabel lblType = new JLabel(entry.getKey() + ":");
            lblType.setFont(new Font("Arial", Font.PLAIN, 14));
            JLabel lblVal = new JLabel(entry.getValue() + " lượt");
            lblVal.setFont(new Font("Arial", Font.BOLD, 14));
            lblVal.setForeground(new Color(60, 130, 200));
            content.add(lblType);
            content.add(lblVal);
        }
        card.add(content, BorderLayout.CENTER);
    }

    private void updateRevenueCard(JPanel card) {
        if (card.getComponentCount() > 1) {
            card.remove(1);
        }
        JPanel content = new JPanel(new GridLayout(0, 2, 10, 10));
        content.setOpaque(false);
        Map<String, Double> data = analyticsDAO.getRevenueByRoomType();
        double total = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            JLabel lblType = new JLabel(entry.getKey() + ":");
            lblType.setFont(new Font("Arial", Font.PLAIN, 14));
            JLabel lblVal = new JLabel(currencyFormatter.format(entry.getValue()));
            lblVal.setFont(new Font("Arial", Font.BOLD, 14));
            lblVal.setForeground(new Color(46, 204, 113)); // Green
            content.add(lblType);
            content.add(lblVal);
            total += entry.getValue();
        }
        
        JLabel lblTotalType = new JLabel("Tổng cộng:");
        lblTotalType.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel lblTotalVal = new JLabel(currencyFormatter.format(total));
        lblTotalVal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalVal.setForeground(Color.RED);
        content.add(lblTotalType);
        content.add(lblTotalVal);

        card.add(content, BorderLayout.CENTER);
    }

    private void updateTopCustomersCard(JPanel card) {
        if (card.getComponentCount() > 1) {
            card.remove(1);
        }
        JPanel content = new JPanel(new GridLayout(0, 2, 10, 10));
        content.setOpaque(false);
        Map<String, Integer> data = analyticsDAO.getTopCustomers(5);
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            JLabel lblName = new JLabel(entry.getKey());
            lblName.setFont(new Font("Arial", Font.PLAIN, 14));
            JLabel lblVal = new JLabel(entry.getValue() + " lần thuê");
            lblVal.setFont(new Font("Arial", Font.BOLD, 14));
            lblVal.setForeground(new Color(230, 126, 34)); // Orange
            content.add(lblName);
            content.add(lblVal);
        }
        card.add(content, BorderLayout.CENTER);
    }
}

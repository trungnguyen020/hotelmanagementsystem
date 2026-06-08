package ui.admin;

import dao.AnalyticsDAO;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

@SuppressWarnings({"serial", "this-escape"})
public class AnalyticsPanel extends JPanel {
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

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

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 20)); // Thay đổi thành 1 row, 2 cols
        contentPanel.setOpaque(false);

        // Card 1: Room Usage
        JPanel pnlRoomUsage = createCard("Tỉ lệ đặt loại phòng");
        updateRoomUsageCard(pnlRoomUsage);
        contentPanel.add(pnlRoomUsage);

        // Card 2: Revenue
        JPanel pnlRevenue = createCard("Doanh thu theo loại phòng");
        updateRevenueCard(pnlRevenue);
        contentPanel.add(pnlRevenue);

        add(contentPanel, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> {
            updateRoomUsageCard(pnlRoomUsage);
            updateRevenueCard(pnlRevenue);
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
        
        Map<String, Integer> data = analyticsDAO.getRoomTypeUsage();
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
                "Tỉ lệ đặt phòng", dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        
        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} lượt ({2})"));
        plot.setBackgroundPaint(Color.WHITE);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(300, 250));
        
        card.add(chartPanel, BorderLayout.CENTER);
    }

    private void updateRevenueCard(JPanel card) {
        if (card.getComponentCount() > 1) {
            card.remove(1);
        }
        Map<String, Double> data = analyticsDAO.getRevenueByRoomType();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Doanh thu theo loại phòng", "Loại phòng", "VNĐ", dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(300, 250));
        
        card.add(chartPanel, BorderLayout.CENTER);
    }

    // Đã xóa hàm updateTopCustomersCard
}

package ui.admin;

import dao.InvoiceDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RevenuePanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTable table;
    private final DefaultTableModel tableModel;
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final JLabel lblTotalRevenue;

    public RevenuePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Báo cáo doanh thu & Hóa đơn");
        title.setFont(new Font("Tahoma", Font.BOLD, 18));
        title.setForeground(new Color(30, 60, 90));
        topPanel.add(title, BorderLayout.WEST);

        lblTotalRevenue = new JLabel("Tổng doanh thu: 0 VNĐ");
        lblTotalRevenue.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblTotalRevenue.setForeground(new Color(200, 60, 60));
        topPanel.add(lblTotalRevenue, BorderLayout.EAST);
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Ngày:"));
        JComboBox<String> cbDay = new JComboBox<>();
        cbDay.addItem("Tất cả");
        for (int i=1; i<=31; i++) cbDay.addItem(String.valueOf(i));
        filterPanel.add(cbDay);
        
        filterPanel.add(new JLabel("Tháng:"));
        JComboBox<String> cbMonth = new JComboBox<>();
        cbMonth.addItem("Tất cả");
        for (int i=1; i<=12; i++) cbMonth.addItem(String.valueOf(i));
        filterPanel.add(cbMonth);
        
        filterPanel.add(new JLabel("Năm:"));
        JComboBox<String> cbYear = new JComboBox<>();
        cbYear.addItem("Tất cả");
        for (int i=2020; i<=2030; i++) cbYear.addItem(String.valueOf(i));
        filterPanel.add(cbYear);
        
        JButton btnFilter = new JButton("Lọc");
        btnFilter.setBackground(new Color(52, 152, 219));
        btnFilter.setForeground(Color.WHITE);
        btnFilter.setFocusPainted(false);
        filterPanel.add(btnFilter);

        topPanel.add(filterPanel, BorderLayout.SOUTH);
        
        btnFilter.addActionListener(e -> {
            Integer d = cbDay.getSelectedIndex() > 0 ? Integer.parseInt((String)cbDay.getSelectedItem()) : null;
            Integer m = cbMonth.getSelectedIndex() > 0 ? Integer.parseInt((String)cbMonth.getSelectedItem()) : null;
            Integer y = cbYear.getSelectedIndex() > 0 ? Integer.parseInt((String)cbYear.getSelectedItem()) : null;
            loadData(d, m, y);
        });

        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID Hóa đơn", "Stay ID", "Người thu", "Tạm tính", "Giảm giá", "Tổng cộng", "Thời gian thanh toán"};
        tableModel = new DefaultTableModel(cols, 0) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Làm mới");
        JButton btnExport = new JButton("Xuất Excel");
        
        btnRefresh.addActionListener(e -> {
            cbDay.setSelectedIndex(0);
            cbMonth.setSelectedIndex(0);
            cbYear.setSelectedIndex(0);
            loadData(null, null, null);
        });
        
        btnExport.addActionListener(e -> exportExcel());
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnExport);
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData(null, null, null);
    }

    private void exportExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file báo cáo");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if(!path.toLowerCase().endsWith(".csv")) path += ".csv";
            
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.File(path), "UTF-8")) {
                pw.write('\ufeff'); // BOM cho UTF-8
                pw.print("\"ID Hóa đơn\",\"Stay ID\",\"Người thu\",\"Tạm tính\",\"Giảm giá\",\"Tổng cộng\",\"Thời gian thanh toán\"\n");
                
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        String val = tableModel.getValueAt(i, j) == null ? "" : tableModel.getValueAt(i, j).toString().replace("\"", "\"\"");
                        pw.print("\"" + val + "\"");
                        if (j < tableModel.getColumnCount() - 1) pw.print(",");
                    }
                    pw.print("\n");
                }
                JOptionPane.showMessageDialog(this, "Xuất file thành công!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadData(Integer day, Integer month, Integer year) {
        tableModel.setRowCount(0);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try {
            List<InvoiceDAO.InvoiceView> invoices = invoiceDAO.getFilteredInvoices(day, month, year);
            for (InvoiceDAO.InvoiceView inv : invoices) {
                totalRevenue = totalRevenue.add(inv.total != null ? inv.total : BigDecimal.ZERO);
                tableModel.addRow(new Object[]{
                        inv.id,
                        inv.stayId,
                        inv.paidByName != null ? inv.paidByName : "N/A",
                        String.format("%,.0f", inv.subtotal),
                        String.format("%,.0f", inv.discountAmount),
                        String.format("%,.0f", inv.total),
                        inv.paidAt.format(dtf)
                });
            }
            lblTotalRevenue.setText("Tổng doanh thu: " + String.format("%,.0f VNĐ", totalRevenue));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải báo cáo: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
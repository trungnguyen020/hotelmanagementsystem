package ui.admin;

import dao.SalaryDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class SalaryPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final SalaryDAO salaryDAO = new SalaryDAO();

    public SalaryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Bảng Tính Lương Nhân Viên");
        title.setFont(new Font("Tahoma", Font.BOLD, 18));
        title.setForeground(new Color(30, 60, 90));
        topPanel.add(title, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Tháng:"));
        JComboBox<String> cbMonth = new JComboBox<>();
        int currentMonth = LocalDate.now().getMonthValue();
        for (int i=1; i<=12; i++) cbMonth.addItem(String.valueOf(i));
        cbMonth.setSelectedItem(String.valueOf(currentMonth));
        filterPanel.add(cbMonth);
        
        filterPanel.add(new JLabel("Năm:"));
        JComboBox<String> cbYear = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i=2020; i<=2030; i++) cbYear.addItem(String.valueOf(i));
        cbYear.setSelectedItem(String.valueOf(currentYear));
        filterPanel.add(cbYear);
        
        JButton btnFilter = new JButton("Tính Lương");
        btnFilter.setBackground(new Color(52, 152, 219));
        btnFilter.setForeground(Color.WHITE);
        btnFilter.setFocusPainted(false);
        filterPanel.add(btnFilter);

        topPanel.add(filterPanel, BorderLayout.SOUTH);
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Tài khoản", "Họ Tên", "Vai Trò", "Lương CB", "Doanh thu Check-in", "Doanh thu Check-out", "Hoa hồng (7-3)", "Thực lãnh"};
        tableModel = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        btnFilter.addActionListener(e -> {
            Integer m = Integer.parseInt((String)cbMonth.getSelectedItem());
            Integer y = Integer.parseInt((String)cbYear.getSelectedItem());
            loadData(m, y);
        });

        JButton btnExport = new JButton("Xuất Excel");
        btnExport.addActionListener(e -> exportExcel());
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnExport);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData(currentMonth, currentYear);
    }

    private void exportExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file báo cáo lương");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if(!path.toLowerCase().endsWith(".csv")) path += ".csv";
            
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.File(path), "UTF-8")) {
                pw.write('\ufeff'); // BOM cho UTF-8
                pw.print("\"ID\",\"Tài khoản\",\"Họ Tên\",\"Vai Trò\",\"Lương CB\",\"Doanh thu Check-in\",\"Doanh thu Check-out\",\"Hoa hồng (7-3)\",\"Thực lãnh\"\n");
                
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

    private void loadData(Integer month, Integer year) {
        tableModel.setRowCount(0);
        try {
            List<SalaryDAO.SalaryReportRow> list = salaryDAO.getSalaryReport(month, year);
            for (SalaryDAO.SalaryReportRow row : list) {
                tableModel.addRow(new Object[]{
                        row.empId,
                        row.username,
                        row.fullName,
                        row.role,
                        String.format("%,.0f", row.baseSalary),
                        String.format("%,.0f", row.revenueCheckin),
                        String.format("%,.0f", row.revenueCheckout),
                        String.format("%,.0f", row.commission),
                        String.format("%,.0f", row.totalSalary)
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tính lương: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

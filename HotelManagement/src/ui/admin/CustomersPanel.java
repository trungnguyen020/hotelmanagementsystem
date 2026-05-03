package ui.admin;

import dao.CustomerDAO;
import model.Customer;
import ui.components.PaginationPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CustomersPanel extends JPanel {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final DefaultTableModel model;
    private final JTable table;
    private final PaginationPanel paginationPanel;

    public CustomersPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 246, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        model = new DefaultTableModel(new Object[]{"ID", "Họ tên", "Số điện thoại", "CCCD", "Thao tác"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Chỉ cho phép click cột thao tác
            }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionBackground(new Color(220, 235, 250));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));

        // Column Thao tác
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        sp.getViewport().setBackground(Color.WHITE);

        paginationPanel = new PaginationPanel((offset, limit, keyword) -> loadData(offset, limit, keyword));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        
        JLabel title = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(2, 75, 141));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(paginationPanel.getSearchPanel(), BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(paginationPanel.getPagingPanel(), BorderLayout.SOUTH);

        loadData(paginationPanel.getOffset(), paginationPanel.getPageSize(), paginationPanel.getKeyword());
    }

    private void loadData(int offset, int limit, String keyword) {
        try {
            model.setRowCount(0);
            int totalCount = customerDAO.countTotal(keyword);
            List<Customer> list = customerDAO.findPaginated(keyword, offset, limit);
            for (Customer c : list) {
                model.addRow(new Object[]{
                        c.getId(), c.getFullName(), c.getPhone(), c.getIdNumber(), "Xem lịch sử"
                });
            }
            paginationPanel.updatePagination(totalCount);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu khách hàng!");
        }
    }

    private void showHistory(int row) {
        if (row < 0) return;
        int customerId = (Integer) model.getValueAt(row, 0);
        String customerName = (String) model.getValueAt(row, 1);
        
        CustomerHistoryDialog dialog = new CustomerHistoryDialog(SwingUtilities.getWindowAncestor(this), customerId, customerName);
        dialog.setVisible(true);
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(60, 130, 200));
            setForeground(Color.WHITE);
            setFocusPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Xem lịch sử" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(new Color(60, 130, 200));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "Xem lịch sử" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                showHistory(currentRow);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}

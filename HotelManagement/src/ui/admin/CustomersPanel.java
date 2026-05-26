package ui.admin;

import dao.CustomerDAO;
import model.Customer;
import ui.components.PaginationPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
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

        model = new DefaultTableModel(new Object[] { "ID", "Họ tên", "Số điện thoại", "CCCD", "Thao tác" }, 0) {
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

    private void editCustomerAt(int row) {
        if (row < 0)
            return;
        Customer c = new Customer();
        c.setId((Integer) model.getValueAt(row, 0));
        c.setFullName((String) model.getValueAt(row, 1));
        c.setPhone((String) model.getValueAt(row, 2));
        c.setIdNumber((String) model.getValueAt(row, 3));

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Sửa Thông Tin Khách Hàng",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField(c.getFullName());
        JTextField txtPhone = new JTextField(c.getPhone());
        JTextField txtId = new JTextField(c.getIdNumber());

        p.add(new JLabel("Họ và tên:"));
        p.add(txtName);
        p.add(new JLabel("Số điện thoại:"));
        p.add(txtPhone);
        p.add(new JLabel("CCCD/CMND:"));
        p.add(txtId);

        JButton btnSave = new JButton("Lưu Thay Đổi");
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);

        p.add(new JLabel());
        p.add(btnSave);

        dialog.add(p);

        btnSave.addActionListener(evt -> {
            String n = txtName.getText().trim();
            String ph = txtPhone.getText().trim();
            String idNo = txtId.getText().trim();

            if (n.isEmpty() || ph.isEmpty() || idNo.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            if (n.length() < 3 || n.matches(".*\\d.*") || n.matches("^[^a-zA-Z0-9]+$")) {
                JOptionPane.showMessageDialog(dialog, "Họ và tên không hợp lệ (ít nhất 3 ký tự, không chứa số)!");
                return;
            }

            c.setFullName(n);
            c.setPhone(ph);
            c.setIdNumber(idNo);

            try {
                customerDAO.update(c);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin khách hàng thành công!");
                paginationPanel.reload();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi cập nhật: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void loadData(int offset, int limit, String keyword) {
        try {
            model.setRowCount(0);
            int totalCount = customerDAO.countTotal(keyword);
            List<Customer> list = customerDAO.findPaginated(keyword, offset, limit);
            for (Customer c : list) {
                model.addRow(new Object[] {
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
        if (row < 0)
            return;
        int customerId = (Integer) model.getValueAt(row, 0);
        String customerName = (String) model.getValueAt(row, 1);

        CustomerHistoryDialog dialog = new CustomerHistoryDialog(SwingUtilities.getWindowAncestor(this), customerId,
                customerName);
        dialog.setVisible(true);
    }

    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton btnHistory = new JButton("Xem lịch sử");
        private JButton btnEdit = new JButton("Sửa");

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(true);
            setBackground(Color.WHITE);

            btnHistory.setBackground(new Color(60, 130, 200));
            btnHistory.setForeground(Color.WHITE);
            btnHistory.setFocusPainted(false);
            btnHistory.setFont(new Font("Tahoma", Font.PLAIN, 12));

            btnEdit.setBackground(new Color(230, 126, 34));
            btnEdit.setForeground(Color.WHITE);
            btnEdit.setFocusPainted(false);
            btnEdit.setFont(new Font("Tahoma", Font.PLAIN, 12));

            add(btnHistory);
            add(btnEdit);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnHistory;
        private JButton btnEdit;
        private int currentRow;
        private boolean isHistoryClicked;
        private boolean isEditClicked;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

            btnHistory = new JButton("Xem lịch sử");
            btnHistory.setBackground(new Color(60, 130, 200));
            btnHistory.setForeground(Color.WHITE);
            btnHistory.setFocusPainted(false);
            btnHistory.setFont(new Font("Tahoma", Font.PLAIN, 12));

            btnEdit = new JButton("Sửa");
            btnEdit.setBackground(new Color(230, 126, 34));
            btnEdit.setForeground(Color.WHITE);
            btnEdit.setFocusPainted(false);
            btnEdit.setFont(new Font("Tahoma", Font.PLAIN, 12));

            panel.add(btnHistory);
            panel.add(btnEdit);

            btnHistory.addActionListener(e -> {
                isHistoryClicked = true;
                fireEditingStopped();
            });

            btnEdit.addActionListener(e -> {
                isEditClicked = true;
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            currentRow = row;
            isHistoryClicked = false;
            isEditClicked = false;
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            if (isHistoryClicked) {
                showHistory(currentRow);
            } else if (isEditClicked) {
                editCustomerAt(currentRow);
            }
            isHistoryClicked = false;
            isEditClicked = false;
            return "";
        }

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }
}

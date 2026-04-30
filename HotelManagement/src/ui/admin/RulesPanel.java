package ui.admin;

import dao.DiscountRuleDAO;
import model.DiscountRule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class RulesPanel extends JPanel {
    private final DiscountRuleDAO ruleDAO = new DiscountRuleDAO();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Số ngày tối thiểu", "Phần trăm giảm (%)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField txtMinDays = new JTextField(10);
    private final JTextField txtDiscount = new JTextField(10);

    public RulesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 246, 250));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Số ngày tối thiểu:"));
        topPanel.add(txtMinDays);
        topPanel.add(new JLabel("Giảm giá (%):"));
        topPanel.add(txtDiscount);

        JButton btnAdd = new JButton("Thêm/Cập nhật");
        btnAdd.setBackground(new Color(60, 130, 200));
        btnAdd.setForeground(Color.WHITE);
        topPanel.add(btnAdd);

        JButton btnDelete = new JButton("Xóa");
        btnDelete.setBackground(new Color(200, 60, 60));
        btnDelete.setForeground(Color.WHITE);
        topPanel.add(btnDelete);

        add(topPanel, BorderLayout.NORTH);

        table.setRowHeight(35);
        table.getTableHeader().setBackground(Color.WHITE);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> saveRule());
        btnDelete.addActionListener(e -> deleteRule());

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtMinDays.setText(model.getValueAt(row, 1).toString());
                txtDiscount.setText(model.getValueAt(row, 2).toString());
            }
        });

        loadData();
    }

    private void loadData() {
        try {
            model.setRowCount(0);
            List<DiscountRule> list = ruleDAO.findAll();
            for (DiscountRule r : list) {
                model.addRow(new Object[]{r.getId(), r.getMinDays(), r.getDiscountPercent()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveRule() {
        try {
            int minDays = Integer.parseInt(txtMinDays.getText());
            BigDecimal discount = new BigDecimal(txtDiscount.getText());

            List<DiscountRule> existing = ruleDAO.findAll();
            DiscountRule ruleToUpdate = null;
            for (DiscountRule r : existing) {
                if (r.getMinDays() == minDays) {
                    ruleToUpdate = r;
                    break;
                }
            }

            if (ruleToUpdate != null) {
                ruleToUpdate.setDiscountPercent(discount);
                ruleDAO.update(ruleToUpdate);
                JOptionPane.showMessageDialog(this, "Đã cập nhật quy tắc thành công!");
            } else {
                ruleDAO.insert(new DiscountRule(minDays, discount));
                JOptionPane.showMessageDialog(this, "Đã thêm quy tắc thành công!");
            }
            loadData();
            txtMinDays.setText("");
            txtDiscount.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu quy tắc!");
        }
    }

    private void deleteRule() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn quy tắc để xóa!");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        try {
            ruleDAO.delete(id);
            JOptionPane.showMessageDialog(this, "Đã xóa quy tắc!");
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

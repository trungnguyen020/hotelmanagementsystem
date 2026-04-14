package ui.admin;

import dao.ServiceDAO;
import model.Service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ServicesPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ServiceDAO serviceDAO = new ServiceDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Tên dịch vụ", "Đơn giá", "Đơn vị tính", "Đang bán?"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    public ServicesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250)); // Nền sáng
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setOpaque(false);

        JButton btnAdd = createBtn("Thêm mới", new Color(46, 204, 113));
        JButton btnEdit = createBtn("Sửa thông tin", new Color(52, 152, 219));
        JButton btnDelete = createBtn("Xoá / Ẩn", new Color(231, 76, 60));
        JButton btnRefresh = createBtn("Làm mới", new Color(149, 165, 166));

        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);

        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        table.setFont(new Font("Tahoma", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setSelectionBackground(new Color(189, 195, 199));
        table.setShowGrid(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(223, 228, 234)));
        sp.getViewport().setBackground(Color.WHITE);

        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadData());
        btnAdd.addActionListener(e -> showForm(null));
        btnEdit.addActionListener(e -> {
            Service s = getSelectedService();
            if (s != null) showForm(s);
        });
        btnDelete.addActionListener(e -> deleteSelected());

        loadData();
    }

    private JButton createBtn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Tahoma", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void loadData() {
        try {
            model.setRowCount(0);
            List<Service> list = serviceDAO.findAllAdmin();
            for (Service s : list) {
                model.addRow(new Object[]{
                        s.getId(), s.getName(), s.getUnitPrice(), s.getUnit(), s.isActive() ? "Có" : "Không"
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu dịch vụ!");
        }
    }

    private Service getSelectedService() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dịch vụ trong bảng!");
            return null;
        }
        Service s = new Service();
        s.setId((Integer) model.getValueAt(row, 0));
        s.setName((String) model.getValueAt(row, 1));
        s.setUnitPrice((BigDecimal) model.getValueAt(row, 2));
        s.setUnit((String) model.getValueAt(row, 3));
        s.setActive("Có".equals(model.getValueAt(row, 4)));
        return s;
    }

    private void deleteSelected() {
        Service s = getSelectedService();
        if (s == null) return;

        int ans = JOptionPane.showConfirmDialog(this, "Bạn có muốn xóa dịch vụ '" + s.getName() + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                serviceDAO.deleteOrDeactivate(s.getId());
                JOptionPane.showMessageDialog(this, "Đã xóa dịch vụ thành công!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
                loadData();
            }
        }
    }

    private void showForm(Service s) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), s == null ? "Thêm Dịch Vụ" : "Sửa Dịch Vụ", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField(s != null ? s.getName() : "");
        JTextField txtPrice = new JTextField(s != null ? s.getUnitPrice().toString() : "");
        JTextField txtUnit = new JTextField(s != null ? s.getUnit() : "lần");
        JCheckBox chkActive = new JCheckBox("Đang bán");
        chkActive.setSelected(s == null || s.isActive());

        p.add(new JLabel("Tên dịch vụ:")); p.add(txtName);
        p.add(new JLabel("Đơn giá (VNĐ):")); p.add(txtPrice);
        p.add(new JLabel("Đơn vị tính:")); p.add(txtUnit);
        p.add(new JLabel("Trạng thái:")); p.add(chkActive);

        JButton btnSave = createBtn("Lưu", new Color(46, 204, 113));
        p.add(new JLabel()); // spacer
        p.add(btnSave);

        dialog.add(p);

        btnSave.addActionListener(evt -> {
            try {
                String n = txtName.getText().trim();
                String pStr = txtPrice.getText().trim();
                String u = txtUnit.getText().trim();

                if (n.isEmpty() || pStr.isEmpty() || u.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đủ thông tin!");
                    return;
                }

                BigDecimal price;
                try {
                    price = new BigDecimal(pStr);
                } catch (Exception e2) {
                    JOptionPane.showMessageDialog(dialog, "Đơn giá không hợp lệ!");
                    return;
                }

                Service sv = new Service();
                sv.setName(n);
                sv.setUnitPrice(price);
                sv.setUnit(u);
                sv.setActive(chkActive.isSelected());

                if (s == null) {
                    serviceDAO.insert(sv);
                } else {
                    sv.setId(s.getId());
                    serviceDAO.update(sv);
                }

                dialog.dispose();
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi lưu dữ liệu: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }
}
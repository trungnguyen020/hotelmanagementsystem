package ui.admin;

import dao.RoomDAO;
import model.RoomType;
import model.RoomView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final RoomDAO roomDAO = new RoomDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Số phòng", "Loại phòng", "Giá/đêm", "Trạng thái"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    public RoomsPanel() {
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
            RoomView r = getSelectedRoom();
            if (r != null) showForm(r);
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
            List<RoomView> list = roomDAO.findAll();
            for (RoomView r : list) {
                model.addRow(new Object[]{
                        r.getRoomId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerNight(), r.getStatus()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu phòng!");
        }
    }

    private RoomView getSelectedRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 phòng trong bảng!");
            return null;
        }
        RoomView r = new RoomView();
        r.setRoomId((Integer) model.getValueAt(row, 0));
        r.setRoomNumber((String) model.getValueAt(row, 1));
        r.setRoomType((String) model.getValueAt(row, 2));
        r.setStatus((String) model.getValueAt(row, 4));
        return r;
    }

    private void deleteSelected() {
        RoomView r = getSelectedRoom();
        if (r == null) return;

        int ans = JOptionPane.showConfirmDialog(this, "Bạn có muốn xóa phòng " + r.getRoomNumber() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                roomDAO.deleteOrHide(r.getRoomId());
                JOptionPane.showMessageDialog(this, "Đã xóa phòng thành công!");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
                loadData();
            }
        }
    }

    private void showForm(RoomView r) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), r == null ? "Thêm Phòng" : "Sửa Phòng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtNumber = new JTextField(r != null ? r.getRoomNumber() : "");
        JComboBox<RoomType> cbType = new JComboBox<>();
        try {
            List<RoomType> types = roomDAO.findAllRoomTypes();
            for (RoomType t : types) {
                cbType.addItem(t);
                if (r != null && t.getName().equals(r.getRoomType())) {
                    cbType.setSelectedItem(t);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"AVAILABLE", "OCCUPIED", "MAINTENANCE"});
        if (r != null) cbStatus.setSelectedItem(r.getStatus());
        JTextField txtNote = new JTextField(); // simplified for now

        p.add(new JLabel("Số phòng:")); p.add(txtNumber);
        p.add(new JLabel("Loại phòng:")); p.add(cbType);
        p.add(new JLabel("Trạng thái:")); p.add(cbStatus);
        p.add(new JLabel("Ghi chú:")); p.add(txtNote);

        JButton btnSave = createBtn("Lưu", new Color(46, 204, 113));
        p.add(new JLabel()); // spacer
        p.add(btnSave);

        dialog.add(p);

        btnSave.addActionListener(evt -> {
            try {
                String no = txtNumber.getText().trim();
                RoomType t = (RoomType) cbType.getSelectedItem();
                String stt = cbStatus.getSelectedItem().toString();
                String note = txtNote.getText().trim();

                if (no.isEmpty() || t == null) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đủ thông tin!");
                    return;
                }

                if (r == null) {
                    roomDAO.insert(no, t.getId(), stt, note);
                } else {
                    roomDAO.update(r.getRoomId(), no, t.getId(), stt, note);
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
package ui.admin;

import dao.RoomDAO;
import model.RoomType;
import model.RoomView;
import ui.components.PaginationPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings({"serial", "this-escape"})
public class RoomsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final RoomDAO roomDAO = new RoomDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Số phòng", "Loại phòng", "Giá/đêm", "Trạng thái", "Thao tác"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { 
            return column == 5; 
        }
    };

    private final JTable table = new JTable(model);
    private final PaginationPanel paginationPanel;

    public RoomsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setOpaque(false);

        JButton btnAdd = createBtn("Thêm mới", new Color(46, 204, 113));
        JButton btnRoomTypes = createBtn("Quản lý loại phòng", new Color(52, 152, 219));
        JButton btnRefresh = createBtn("Làm mới", new Color(149, 165, 166));

        top.add(btnAdd);
        top.add(btnRoomTypes);
        top.add(btnRefresh);

        table.setFillsViewportHeight(true);
        table.setRowHeight(40);
        table.setFont(new Font("Tahoma", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(250, 250, 250));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.setSelectionBackground(new Color(220, 235, 250));
        table.setSelectionForeground(new Color(30, 30, 30));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));

        ui.components.TableActionEvent event = new ui.components.TableActionEvent() {
            @Override
            public void onEdit(int row) {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                RoomView r = getRoomAt(row);
                if (r != null) showForm(r);
            }

            @Override
            public void onDelete(int row) {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                deleteRoomAt(row);
            }
        };
        table.getColumnModel().getColumn(5).setCellRenderer(new ui.components.TableActionCellRender());
        table.getColumnModel().getColumn(5).setCellEditor(new ui.components.TableActionCellEditor(event));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        sp.getViewport().setBackground(Color.WHITE);

        paginationPanel = new PaginationPanel((offset, limit, keyword) -> loadData(offset, limit, keyword));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(top, BorderLayout.CENTER);
        northPanel.add(paginationPanel.getSearchPanel(), BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(paginationPanel.getPagingPanel(), BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> paginationPanel.reload());
        btnAdd.addActionListener(e -> showForm(null));
        btnRoomTypes.addActionListener(e -> showRoomTypesDialog());

        loadData(paginationPanel.getOffset(), paginationPanel.getPageSize(), paginationPanel.getKeyword());
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

    private void loadData(int offset, int limit, String keyword) {
        try {
            model.setRowCount(0);
            List<RoomView> list = roomDAO.findPaginated(keyword, offset, limit);
            int totalCount = roomDAO.countTotal(keyword);
            for (RoomView r : list) {
                model.addRow(new Object[]{
                        r.getRoomId(), r.getRoomNumber(), r.getRoomType(), r.getPricePerNight(), r.getStatus(), ""
                });
            }
            paginationPanel.updatePagination(totalCount);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu phòng!");
        }
    }

    private RoomView getRoomAt(int row) {
        RoomView r = new RoomView();
        r.setRoomId((Integer) model.getValueAt(row, 0));
        r.setRoomNumber((String) model.getValueAt(row, 1));
        r.setRoomType((String) model.getValueAt(row, 2));
        r.setStatus((String) model.getValueAt(row, 4));
        return r;
    }

    private void deleteRoomAt(int row) {
        RoomView r = getRoomAt(row);
        if (r == null) return;

        int ans = JOptionPane.showConfirmDialog(this, "Bạn có muốn xóa phòng " + r.getRoomNumber() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                roomDAO.deleteOrHide(r.getRoomId());
                JOptionPane.showMessageDialog(this, "Đã xóa phòng thành công!");
                paginationPanel.reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
                paginationPanel.reload();
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
                paginationPanel.reload();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi lưu dữ liệu: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void showRoomTypesDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Quản lý loại phòng & giá", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(700, 450);
        dialog.setLocationRelativeTo(this);

        DefaultTableModel rtModel = new DefaultTableModel(
                new Object[]{"ID", "Tên loại", "Giá/ngày", "Giá/giờ", "Giá qua đêm", "Sức chứa"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable rtTable = new JTable(rtModel);
        rtTable.setRowHeight(35);
        rtTable.setSelectionBackground(new Color(220, 235, 250));
        rtTable.setSelectionForeground(new Color(30, 30, 30));

        Runnable loadRoomTypes = () -> {
            try {
                rtModel.setRowCount(0);
                List<RoomType> types = roomDAO.findAllRoomTypes();
                for (RoomType t : types) {
                    rtModel.addRow(new Object[]{
                            t.getId(), t.getName(),
                            String.format("%,.0f", t.getPricePerNight()),
                            String.format("%,.0f", t.getPricePerHour() != null ? t.getPricePerHour() : BigDecimal.ZERO),
                            String.format("%,.0f", t.getPriceOvernight() != null ? t.getPriceOvernight() : BigDecimal.ZERO),
                            t.getCapacity()
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        loadRoomTypes.run();

        JButton btnEditType = createBtn("Sửa giá loại phòng", new Color(230, 126, 34));
        btnEditType.addActionListener(e -> {
            int row = rtTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Chọn 1 loại phòng!");
                return;
            }
            int typeId = (Integer) rtModel.getValueAt(row, 0);
            try {
                List<RoomType> types = roomDAO.findAllRoomTypes();
                RoomType selected = null;
                for (RoomType t : types) {
                    if (t.getId() == typeId) { selected = t; break; }
                }
                if (selected == null) return;
                showRoomTypeForm(dialog, selected, loadRoomTypes);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(btnEditType);

        dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(rtTable), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void showRoomTypeForm(JDialog parent, RoomType rt, Runnable onSave) {
        JDialog dialog = new JDialog(parent, "Sửa loại phòng: " + rt.getName(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 350);
        dialog.setLocationRelativeTo(parent);

        JPanel p = new JPanel(new GridLayout(7, 2, 10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField(rt.getName());
        JTextField txtPriceNight = new JTextField(String.format("%.0f", rt.getPricePerNight()));
        JTextField txtPriceHour = new JTextField(String.format("%.0f", rt.getPricePerHour() != null ? rt.getPricePerHour() : BigDecimal.ZERO));
        JTextField txtPriceOvernight = new JTextField(String.format("%.0f", rt.getPriceOvernight() != null ? rt.getPriceOvernight() : BigDecimal.ZERO));
        JTextField txtCapacity = new JTextField(String.valueOf(rt.getCapacity()));
        JTextField txtDesc = new JTextField(rt.getDescription() != null ? rt.getDescription() : "");

        p.add(new JLabel("Tên loại:")); p.add(txtName);
        p.add(new JLabel("Giá/ngày (VNĐ):")); p.add(txtPriceNight);
        p.add(new JLabel("Giá/giờ (VNĐ):")); p.add(txtPriceHour);
        p.add(new JLabel("Giá qua đêm (VNĐ):")); p.add(txtPriceOvernight);
        p.add(new JLabel("Sức chứa:")); p.add(txtCapacity);
        p.add(new JLabel("Mô tả:")); p.add(txtDesc);

        JButton btnSave = createBtn("Lưu", new Color(46, 204, 113));
        p.add(new JLabel()); p.add(btnSave);

        dialog.add(p);

        btnSave.addActionListener(evt -> {
            try {
                String name = txtName.getText().trim();
                BigDecimal pNight = new BigDecimal(txtPriceNight.getText().trim());
                BigDecimal pHour = new BigDecimal(txtPriceHour.getText().trim());
                BigDecimal pOvernight = new BigDecimal(txtPriceOvernight.getText().trim());
                int capacity = Integer.parseInt(txtCapacity.getText().trim());
                String desc = txtDesc.getText().trim();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Tên không được trống!");
                    return;
                }

                // Cảnh báo giá không hợp lý
                StringBuilder warnings = new StringBuilder();
                if (pHour.multiply(new BigDecimal("24")).compareTo(pNight) <= 0) {
                    warnings.append("⚠ Giá/giờ × 24 nên > Giá/ngày (giá theo giờ nên đắt hơn ở ngày)\n");
                }
                if (pOvernight.compareTo(pNight) > 0) {
                    warnings.append("⚠ Giá qua đêm nên ≤ Giá/ngày\n");
                }
                if (warnings.length() > 0) {
                    int ans = JOptionPane.showConfirmDialog(dialog,
                            warnings.toString() + "\nBạn vẫn muốn lưu?",
                            "Cảnh báo giá", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (ans != JOptionPane.YES_OPTION) return;
                }

                roomDAO.updateRoomType(rt.getId(), name, pNight, pHour, pOvernight, capacity, desc);
                JOptionPane.showMessageDialog(dialog, "Đã cập nhật loại phòng thành công!");
                dialog.dispose();
                onSave.run();
                paginationPanel.reload(); // Refresh main room table too
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Giá hoặc sức chứa không hợp lệ!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }
}
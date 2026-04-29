package ui.admin;

import dao.RoomDAO;
import model.RoomType;
import model.RoomView;
import ui.components.PaginationPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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
        JButton btnRefresh = createBtn("Làm mới", new Color(149, 165, 166));

        top.add(btnAdd);
        top.add(btnRefresh);

        table.setFillsViewportHeight(true);
        table.setRowHeight(40);
        table.setFont(new Font("Tahoma", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(250, 250, 250));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.setSelectionBackground(new Color(235, 245, 255));
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

    private RoomView getSelectedRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 phòng trong bảng!");
            return null;
        }
        return getRoomAt(row);
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
}
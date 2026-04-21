package ui.employee;

import dao.RoomDAO;
import model.RoomView;
import ui.components.PaginationPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomsPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final RoomDAO roomDAO = new RoomDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Số phòng", "Loại phòng", "Giá/đêm", "Trạng thái"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);
    private final PaginationPanel paginationPanel;

    public RoomsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 251, 253));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setBackground(new Color(60,130,200));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        top.add(btnRefresh);

        // table style
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(200,230,255));
        table.setGridColor(new Color(230,230,230));

        paginationPanel = new PaginationPanel((offset, limit, keyword) -> loadRooms(offset, limit, keyword));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(top, BorderLayout.CENTER);
        northPanel.add(paginationPanel.getSearchPanel(), BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);
        add(paginationPanel.getPagingPanel(), BorderLayout.SOUTH);
        btnRefresh.addActionListener(e -> paginationPanel.reload());

        loadRooms(paginationPanel.getOffset(), paginationPanel.getPageSize(), paginationPanel.getKeyword());
    }

    public void reload() {
        if (paginationPanel != null) {
            paginationPanel.reload();
        }
    }

    public void loadRooms(int offset, int limit, String keyword) {
        try {
            model.setRowCount(0);
            List<RoomView> rooms = roomDAO.findPaginated(keyword, offset, limit);
            int totalCount = roomDAO.countTotal(keyword);
            for (RoomView r : rooms) {
                model.addRow(new Object[]{
                        r.getRoomId(),
                        r.getRoomNumber(),
                        r.getRoomType(),
                        r.getPricePerNight(),
                        r.getStatus()
                });
            }
            paginationPanel.updatePagination(totalCount);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi load phòng! Xem Console.");
        }
    }
    public Integer getSelectedRoomId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (Integer) model.getValueAt(row, 0);
    }
}
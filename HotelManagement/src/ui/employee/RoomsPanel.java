package ui.employee;

import dao.RoomDAO;
import model.RoomView;

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

    public RoomsPanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        top.add(btnRefresh);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadRooms());

        loadRooms();
    }

    public void loadRooms() {
        try {
            model.setRowCount(0);
            List<RoomView> rooms = roomDAO.findAll();
            for (RoomView r : rooms) {
                model.addRow(new Object[]{
                        r.getRoomId(),
                        r.getRoomNumber(),
                        r.getRoomType(),
                        r.getPricePerNight(),
                        r.getStatus()
                });
            }
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
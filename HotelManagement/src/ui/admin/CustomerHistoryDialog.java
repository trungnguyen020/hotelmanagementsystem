package ui.admin;

import dao.StayDAO;
import model.BookingHistoryItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CustomerHistoryDialog extends JDialog {

    private final StayDAO stayDAO = new StayDAO();
    private final DefaultTableModel model;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CustomerHistoryDialog(Window owner, int customerId, String customerName) {
        super(owner, "Lịch sử thuê phòng - " + customerName, ModalityType.APPLICATION_MODAL);
        setSize(700, 400);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        root.setBackground(Color.WHITE);
        setContentPane(root);

        JLabel lblTitle = new JLabel("Lịch sử thuê phòng của khách hàng: " + customerName);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(new Color(2, 75, 141));
        root.add(lblTitle, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Phòng", "Check-in", "Check-out", "Trạng thái", "Tổng thanh toán"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(new Color(245, 246, 250));

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        root.add(sp, BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnClose);
        root.add(bottom, BorderLayout.SOUTH);

        loadHistory(customerId);
    }

    private void loadHistory(int customerId) {
        try {
            List<BookingHistoryItem> history = stayDAO.getBookingHistory(customerId);
            for (BookingHistoryItem item : history) {
                String checkinStr = item.getCheckinAt() != null ? item.getCheckinAt().format(dateFormatter) : "";
                String checkoutStr = item.getCheckoutAt() != null ? item.getCheckoutAt().format(dateFormatter) : "";
                String totalStr = item.getTotalPaid() != null ? currencyFormatter.format(item.getTotalPaid()) : "-";
                
                model.addRow(new Object[]{
                    item.getRoomNumber(),
                    checkinStr,
                    checkoutStr,
                    item.getStatus(),
                    totalStr
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử!");
        }
    }
}

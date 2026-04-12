package ui.employee;

import dao.StayDAO;
import model.Employee;
import model.StayView;
import service.PricingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Employee me;
    private final RoomsPanel roomsPanel;

    private final StayDAO stayDAO = new StayDAO();
    private final PricingService pricing = new PricingService();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"StayID","Phòng","Khách","Check-in","Dự kiến checkout","Giá/đêm"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField txtExtraDiscount = new JTextField("0", 5);

    private final JLabel lblDays = new JLabel("-");
    private final JLabel lblHalf = new JLabel("-");
    private final JLabel lblSubtotal = new JLabel("-");
    private final JLabel lblAutoDiscount = new JLabel("-");
    private final JLabel lblTotalDiscount = new JLabel("-");
    private final JLabel lblTotal = new JLabel("-");

    public CheckoutPanel(Employee me, RoomsPanel roomsPanel) {
        this.me = me;
        this.roomsPanel = roomsPanel;

        setLayout(new BorderLayout(10,10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnReload = new JButton("Reload");
        JButton btnCalc = new JButton("Tính tiền");
        JButton btnCheckout = new JButton("Checkout");
        JButton btnExtend = new JButton("Gia hạn +1 ngày");

        top.add(btnReload);
        top.add(btnExtend);
        top.add(new JLabel("Giảm giá thêm (%):"));
        top.add(txtExtraDiscount);
        top.add(btnCalc);
        top.add(btnCheckout);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel info = new JPanel(new GridLayout(0,2,8,6));
        info.add(new JLabel("Số ngày:")); info.add(lblDays);
        info.add(new JLabel("Nửa ngày (>=07:00):")); info.add(lblHalf);
        info.add(new JLabel("Tạm tính (phòng):")); info.add(lblSubtotal);
        info.add(new JLabel("Giảm tự động (%):")); info.add(lblAutoDiscount);
        info.add(new JLabel("Tổng giảm (%):")); info.add(lblTotalDiscount);
        info.add(new JLabel("Tổng tiền:")); info.add(lblTotal);
        add(info, BorderLayout.SOUTH);

        btnReload.addActionListener(e -> reload());
        btnExtend.addActionListener(e -> extendSelected());
        btnCalc.addActionListener(e -> calcSelected());
        btnCheckout.addActionListener(e -> doCheckout());

        reload();
    }

    public void reload() {
        try {
            model.setRowCount(0);
            List<StayView> list = stayDAO.findCheckedInStays();
            for (StayView s : list) {
                model.addRow(new Object[]{
                        s.getStayId(),
                        s.getRoomNumber(),
                        s.getCustomerName(),
                        s.getCheckinAt(),
                        s.getExpectedCheckoutAt(),
                        s.getPricePerNight()
                });
            }
            clearSummary();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi load stays!");
        }
    }

    private void extendSelected() {
        Integer stayId = selectedStayId();
        if (stayId == null) {
            JOptionPane.showMessageDialog(this, "Chọn 1 stay để gia hạn!");
            return;
        }
        try {
            stayDAO.extendExpectedCheckout(stayId, 1);
            JOptionPane.showMessageDialog(this, "Gia hạn +1 ngày OK");
            reload();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gia hạn lỗi!");
        }
    }

    private void calcSelected() {
        StayView s = selectedStay();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Chọn 1 stay!");
            return;
        }

        LocalDateTime checkoutAt = LocalDateTime.now();

        PricingService.PriceResult r = pricing.calcRoom(s.getCheckinAt(), checkoutAt, s.getPricePerNight());

        BigDecimal extra = parsePercent(txtExtraDiscount.getText());
        BigDecimal totalDiscountPercent = r.autoDiscountPercent.add(extra);
        if (totalDiscountPercent.compareTo(new BigDecimal("100")) > 0) totalDiscountPercent = new BigDecimal("100");

        BigDecimal discountAmount = pricing.calcDiscountAmount(r.subtotal, totalDiscountPercent);
        BigDecimal total = r.subtotal.subtract(discountAmount);

        lblDays.setText(String.valueOf(r.fullDays));
        lblHalf.setText(r.halfDayFactor.toPlainString());
        lblSubtotal.setText(r.subtotal.toPlainString());
        lblAutoDiscount.setText(r.autoDiscountPercent.toPlainString());
        lblTotalDiscount.setText(totalDiscountPercent.toPlainString());
        lblTotal.setText(total.toPlainString());
    }

    private void doCheckout() {
        StayView s = selectedStay();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Chọn 1 stay!");
            return;
        }

        LocalDateTime checkoutAt = LocalDateTime.now();
        PricingService.PriceResult r = pricing.calcRoom(s.getCheckinAt(), checkoutAt, s.getPricePerNight());

        BigDecimal extra = parsePercent(txtExtraDiscount.getText());
        BigDecimal totalDiscountPercent = r.autoDiscountPercent.add(extra);
        if (totalDiscountPercent.compareTo(new BigDecimal("100")) > 0) totalDiscountPercent = new BigDecimal("100");

        BigDecimal discountAmount = pricing.calcDiscountAmount(r.subtotal, totalDiscountPercent);
        BigDecimal total = r.subtotal.subtract(discountAmount);

        int ok = JOptionPane.showConfirmDialog(this,
                "Xác nhận checkout?\n" +
                "Phòng: " + s.getRoomNumber() + "\n" +
                "Số ngày: " + r.fullDays + " + " + r.halfDayFactor + "\n" +
                "Tạm tính: " + r.subtotal + "\n" +
                "Giảm: " + totalDiscountPercent + "% (" + discountAmount + ")\n" +
                "Tổng: " + total,
                "Confirm", JOptionPane.OK_CANCEL_OPTION);

        if (ok != JOptionPane.OK_OPTION) return;

        try {
            stayDAO.checkout(
                    s.getStayId(),
                    checkoutAt,
                    r.subtotal,
                    totalDiscountPercent,
                    discountAmount,
                    total,
                    me.getId()
            );

            JOptionPane.showMessageDialog(this, "Checkout OK!");
            roomsPanel.loadRooms();
            reload();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Checkout lỗi! Xem Console.");
        }
    }

    private Integer selectedStayId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (Integer) model.getValueAt(row, 0);
    }

    private StayView selectedStay() {
        int row = table.getSelectedRow();
        if (row < 0) return null;

        StayView s = new StayView();
        s.setStayId((Integer) model.getValueAt(row, 0));
        s.setRoomNumber((String) model.getValueAt(row, 1));
        s.setCustomerName((String) model.getValueAt(row, 2));
        s.setCheckinAt((java.time.LocalDateTime) model.getValueAt(row, 3));
        s.setExpectedCheckoutAt((java.time.LocalDateTime) model.getValueAt(row, 4));
        s.setPricePerNight((BigDecimal) model.getValueAt(row, 5));
        return s;
    }

    private void clearSummary() {
        lblDays.setText("-");
        lblHalf.setText("-");
        lblSubtotal.setText("-");
        lblAutoDiscount.setText("-");
        lblTotalDiscount.setText("-");
        lblTotal.setText("-");
    }

    private BigDecimal parsePercent(String s) {
        try {
            BigDecimal v = new BigDecimal(s.trim());
            if (v.compareTo(BigDecimal.ZERO) < 0) v = BigDecimal.ZERO;
            if (v.compareTo(new BigDecimal("100")) > 0) v = new BigDecimal("100");
            return v;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
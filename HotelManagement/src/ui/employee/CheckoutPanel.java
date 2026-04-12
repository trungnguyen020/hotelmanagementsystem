package ui.employee;

import dao.ServiceUsageDAO;
import dao.StayDAO;
import model.Employee;
import model.StayView;
import service.PricingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Employee me;
    private final RoomsPanel roomsPanel;

    private final StayDAO stayDAO = new StayDAO();
    private final ServiceUsageDAO usageDAO = new ServiceUsageDAO();
    private final PricingService pricing = new PricingService();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"StayID","Phòng","Khách","Check-in","Dự kiến checkout","Giá/đêm"}, 0
    ) { @Override public boolean isCellEditable(int row, int col) { return false; } };

    private final JTable table = new JTable(model);

    private final JTextField txtExtraDiscount = new JTextField("0", 5);

    private final JLabel lblDays = new JLabel("-");
    private final JLabel lblHalf = new JLabel("-");
    private final JLabel lblRoomCharge = new JLabel("-");
    private final JLabel lblServiceTotal = new JLabel("-");
    private final JLabel lblSubtotal = new JLabel("-");
    private final JLabel lblAutoDiscount = new JLabel("-");
    private final JLabel lblTotalDiscount = new JLabel("-");
    private final JLabel lblDiscountAmount = new JLabel("-");
    private final JLabel lblTotal = new JLabel("-");

    public CheckoutPanel(Employee me, RoomsPanel roomsPanel) {
        this.me = me;
        this.roomsPanel = roomsPanel;

        setLayout(new BorderLayout(10,10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnReload = new JButton("Reload");
        JButton btnExtend = new JButton("Gia hạn +1 ngày");
        JButton btnCalc = new JButton("Tính tiền");
        JButton btnCheckout = new JButton("Checkout");

        top.add(btnReload);
        top.add(btnExtend);
        top.add(new JLabel("Giảm giá thêm (%):"));
        top.add(txtExtraDiscount);
        top.add(btnCalc);
        top.add(btnCheckout);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel info = new JPanel(new GridLayout(0,2,8,6));
        info.add(new JLabel("Số ngày (ngày tròn):")); info.add(lblDays);
        info.add(new JLabel("Nửa ngày (>=07:00):")); info.add(lblHalf);

        info.add(new JLabel("Tiền phòng:")); info.add(lblRoomCharge);
        info.add(new JLabel("Tiền dịch vụ:")); info.add(lblServiceTotal);
        info.add(new JLabel("Tạm tính (phòng + dịch vụ):")); info.add(lblSubtotal);

        info.add(new JLabel("Giảm tự động (%):")); info.add(lblAutoDiscount);
        info.add(new JLabel("Tổng giảm (%):")); info.add(lblTotalDiscount);
        info.add(new JLabel("Tiền giảm:")); info.add(lblDiscountAmount);

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

        try {
            LocalDateTime checkoutAt = LocalDateTime.now();

            PricingService.PriceResult r = pricing.calcRoom(s.getCheckinAt(), checkoutAt, s.getPricePerNight());
            BigDecimal roomCharge = r.roomCharge; // half-day chỉ nằm ở đây

            BigDecimal serviceTotal = usageDAO.sumServiceAmountByStay(s.getStayId());
            if (serviceTotal == null) serviceTotal = BigDecimal.ZERO;

            BigDecimal subtotal = roomCharge.add(serviceTotal).setScale(2, RoundingMode.HALF_UP);

            BigDecimal extra = parsePercent(txtExtraDiscount.getText());
            BigDecimal totalDiscountPercent = r.autoDiscountPercent.add(extra);
            if (totalDiscountPercent.compareTo(new BigDecimal("100")) > 0) totalDiscountPercent = new BigDecimal("100");

            BigDecimal discountAmount = pricing.calcDiscountAmount(subtotal, totalDiscountPercent);
            BigDecimal total = subtotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

            lblDays.setText(String.valueOf(r.fullDays));
            lblHalf.setText(r.halfDayFactor.toPlainString());

            lblRoomCharge.setText(roomCharge.toPlainString());
            lblServiceTotal.setText(serviceTotal.toPlainString());
            lblSubtotal.setText(subtotal.toPlainString());

            lblAutoDiscount.setText(r.autoDiscountPercent.toPlainString());
            lblTotalDiscount.setText(totalDiscountPercent.toPlainString());
            lblDiscountAmount.setText(discountAmount.toPlainString());
            lblTotal.setText(total.toPlainString());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tính tiền!");
        }
    }

    private void doCheckout() {
    StayView s = selectedStay();
    if (s == null) {
        JOptionPane.showMessageDialog(this, "Chọn 1 stay!");
        return;
    }

    try {
        LocalDateTime checkoutAt = LocalDateTime.now();

        PricingService.PriceResult r = pricing.calcRoom(s.getCheckinAt(), checkoutAt, s.getPricePerNight());
        BigDecimal roomCharge = r.roomCharge;

        BigDecimal serviceTotal = usageDAO.sumServiceAmountByStay(s.getStayId());
        if (serviceTotal == null) serviceTotal = BigDecimal.ZERO;

        BigDecimal subtotal = roomCharge.add(serviceTotal).setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal extra = parsePercent(txtExtraDiscount.getText());
        BigDecimal totalDiscountPercent = r.autoDiscountPercent.add(extra);
        if (totalDiscountPercent.compareTo(new BigDecimal("100")) > 0) totalDiscountPercent = new BigDecimal("100");

        BigDecimal discountAmount = pricing.calcDiscountAmount(subtotal, totalDiscountPercent);
        BigDecimal total = subtotal.subtract(discountAmount).setScale(2, java.math.RoundingMode.HALF_UP);

        // MỞ FORM HÓA ĐƠN
        InvoiceDialog dlg = new InvoiceDialog(
                SwingUtilities.getWindowAncestor(this),
                s.getStayId(),
                s.getRoomNumber(),
                s.getCustomerName(),
                s.getCheckinAt(),
                checkoutAt,
                r.fullDays,
                r.halfDayFactor,
                s.getPricePerNight(),
                roomCharge,
                serviceTotal,
                subtotal,
                totalDiscountPercent,
                discountAmount,
                total
        );
        dlg.setVisible(true);

        if (!dlg.isConfirmed()) return; // user bấm Hủy

        // LƯU DB sau khi xác nhận
        stayDAO.checkout(
                s.getStayId(),
                checkoutAt,
                subtotal,
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
        s.setCheckinAt((LocalDateTime) model.getValueAt(row, 3));
        s.setExpectedCheckoutAt((LocalDateTime) model.getValueAt(row, 4));
        s.setPricePerNight((BigDecimal) model.getValueAt(row, 5));
        return s;
    }

    private void clearSummary() {
        lblDays.setText("-");
        lblHalf.setText("-");
        lblRoomCharge.setText("-");
        lblServiceTotal.setText("-");
        lblSubtotal.setText("-");
        lblAutoDiscount.setText("-");
        lblTotalDiscount.setText("-");
        lblDiscountAmount.setText("-");
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
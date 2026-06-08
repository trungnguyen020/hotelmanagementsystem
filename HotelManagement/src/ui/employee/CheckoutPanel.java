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
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings({"serial", "this-escape"})
public class CheckoutPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Employee me;
    private final RoomsPanel roomsPanel;

    private final StayDAO stayDAO = new StayDAO();
    private final ServiceUsageDAO usageDAO = new ServiceUsageDAO();
    private final PricingService pricing = new PricingService();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"StayID","Phòng","Khách","Hình thức","Check-in","Dự kiến checkout","Giá đơn vị"}, 0
    ) { @Override public boolean isCellEditable(int row, int col) { return false; } };

    private final JTable table = new JTable(model);

    private final JTextField txtExtraDiscount = new JTextField("0", 5);

    private final JLabel lblPricingType = new JLabel("-");
    private final JLabel lblQuantity = new JLabel("-");
    private final JLabel lblUnitPrice = new JLabel("-");
    private final JLabel lblRoomCharge = new JLabel("-");
    private final JLabel lblLateFee = new JLabel("-");
    private final JLabel lblTotalRoom = new JLabel("-");
    private final JLabel lblServiceTotal = new JLabel("-");
    private final JLabel lblSubtotal = new JLabel("-");
    private final JLabel lblAutoDiscount = new JLabel("-");
    private final JLabel lblTotalDiscount = new JLabel("-");
    private final JLabel lblDiscountAmount = new JLabel("-");
    private final JLabel lblTotal = new JLabel("-");

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public CheckoutPanel(Employee me, RoomsPanel roomsPanel) {
        this.me = me;
        this.roomsPanel = roomsPanel;
        setLayout(new BorderLayout(10,10));
        setBackground(Color.WHITE);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        JButton btnReload = new JButton("Tải lại"); btnReload.setBackground(new Color(60,130,200)); btnReload.setForeground(Color.WHITE); btnReload.setFocusPainted(false);
        JButton btnExtend = new JButton("Gia hạn"); btnExtend.setBackground(new Color(100,160,100)); btnExtend.setForeground(Color.WHITE); btnExtend.setFocusPainted(false);
        JButton btnCalc = new JButton("Tính tiền"); btnCalc.setBackground(new Color(80,120,200)); btnCalc.setForeground(Color.WHITE); btnCalc.setFocusPainted(false);
        JButton btnCheckout = new JButton("Checkout"); btnCheckout.setBackground(new Color(200,80,60)); btnCheckout.setForeground(Color.WHITE); btnCheckout.setFocusPainted(false);

        top.add(btnReload);
        top.add(btnExtend);
        top.add(new JLabel("Giảm giá thêm (%):"));
        top.add(txtExtraDiscount);
        top.add(btnCalc);
        top.add(btnCheckout);

        add(top, BorderLayout.NORTH);

        table.setSelectionBackground(new Color(220, 235, 250));
        table.setSelectionForeground(new Color(30, 30, 30));
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);

        JPanel info = new JPanel(new GridLayout(0,2,8,6));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        info.add(new JLabel("Hình thức:")); info.add(lblPricingType);
        info.add(new JLabel("Số lượng (giờ/đêm/ngày):")); info.add(lblQuantity);
        info.add(new JLabel("Đơn giá:")); info.add(lblUnitPrice);
        info.add(new JLabel("Tiền phòng cơ bản:")); info.add(lblRoomCharge);
        info.add(new JLabel("Phạt trễ giờ:")); info.add(lblLateFee);
        info.add(new JLabel("Tổng tiền phòng:")); info.add(lblTotalRoom);
        info.add(new JLabel("Tiền dịch vụ:")); info.add(lblServiceTotal);
        info.add(new JLabel("Tạm tính (phòng + DV):")); info.add(lblSubtotal);
        info.add(new JLabel("Giảm tự động (%):")); info.add(lblAutoDiscount);
        info.add(new JLabel("Tổng giảm (%):")); info.add(lblTotalDiscount);
        info.add(new JLabel("Tiền giảm:")); info.add(lblDiscountAmount);

        JLabel lblTotalLabel = new JLabel("TỔNG THANH TOÁN:");
        lblTotalLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTotal.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTotal.setForeground(new Color(200, 60, 60));
        info.add(lblTotalLabel); info.add(lblTotal);

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
                String typeLabel = getTypeLabel(s.getPricingType());
                BigDecimal unitPrice = getUnitPrice(s);
                model.addRow(new Object[]{
                        s.getStayId(),
                        s.getRoomNumber(),
                        s.getCustomerName(),
                        typeLabel,
                        s.getCheckinAt() != null ? s.getCheckinAt().format(DTF) : "",
                        s.getExpectedCheckoutAt() != null ? s.getExpectedCheckoutAt().format(DTF) : "",
                        String.format("%,.0f", unitPrice)
                });
            }
            clearSummary();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi load stays!");
        }
    }

    private String getTypeLabel(String pricingType) {
        if (pricingType == null) return "Theo ngày";
        switch (pricingType) {
            case "HOURLY": return "Theo giờ";
            case "OVERNIGHT": return "Qua đêm";
            case "DAILY": return "Theo ngày";
            default: return pricingType;
        }
    }

    private BigDecimal getUnitPrice(StayView s) {
        if (s.getPricingType() == null) return s.getPricePerNight();
        switch (s.getPricingType()) {
            case "HOURLY": return s.getPricePerHour() != null ? s.getPricePerHour() : BigDecimal.ZERO;
            case "OVERNIGHT": return s.getPriceOvernight() != null ? s.getPriceOvernight() : BigDecimal.ZERO;
            default: return s.getPricePerNight();
        }
    }

    private void extendSelected() {
        StayView s = selectedStay();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Chọn 1 stay để gia hạn!");
            return;
        }
        try {
            String pricingType = s.getPricingType() != null ? s.getPricingType() : "DAILY";
            if ("HOURLY".equals(pricingType)) {
                String input = JOptionPane.showInputDialog(this, "Gia hạn thêm mấy giờ?", "1");
                if (input == null || input.trim().isEmpty()) return;
                int hours = Integer.parseInt(input.trim());
                stayDAO.extendExpectedCheckoutByHours(s.getStayId(), hours);
                JOptionPane.showMessageDialog(this, "Gia hạn +" + hours + " giờ OK");
            } else if ("OVERNIGHT".equals(pricingType)) {
                JOptionPane.showMessageDialog(this, "Qua đêm không thể gia hạn. Nếu cần ở thêm, hãy check-in lại theo hình thức khác.");
                return;
            } else {
                String input = JOptionPane.showInputDialog(this, "Gia hạn thêm mấy ngày?", "1");
                if (input == null || input.trim().isEmpty()) return;
                int days = Integer.parseInt(input.trim());
                stayDAO.extendExpectedCheckout(s.getStayId(), days);
                JOptionPane.showMessageDialog(this, "Gia hạn +" + days + " ngày OK");
            }
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
            String pricingType = s.getPricingType() != null ? s.getPricingType() : "DAILY";

            PricingService.PriceResult r = pricing.calcRoom(
                    pricingType,
                    s.getCheckinAt(), checkoutAt, s.getExpectedCheckoutAt(),
                    s.getPricePerNight(),
                    s.getPricePerHour() != null ? s.getPricePerHour() : BigDecimal.ZERO,
                    s.getPriceOvernight() != null ? s.getPriceOvernight() : BigDecimal.ZERO
            );

            BigDecimal serviceTotal = usageDAO.sumServiceAmountByStay(s.getStayId());
            if (serviceTotal == null) serviceTotal = BigDecimal.ZERO;

            BigDecimal subtotal = r.totalRoom.add(serviceTotal).setScale(0, RoundingMode.HALF_UP);

            BigDecimal extra = parsePercent(txtExtraDiscount.getText());
            BigDecimal totalDiscountPercent = r.autoDiscountPercent.add(extra);
            if (totalDiscountPercent.compareTo(new BigDecimal("100")) > 0) totalDiscountPercent = new BigDecimal("100");

            BigDecimal discountAmount = pricing.calcDiscountAmount(subtotal, totalDiscountPercent);
            BigDecimal total = subtotal.subtract(discountAmount).setScale(0, RoundingMode.HALF_UP);

            lblPricingType.setText(r.pricingLabel);
            lblQuantity.setText(String.format("%.0f %s", r.quantity, r.quantityLabel));
            lblUnitPrice.setText(String.format("%,.0f VNĐ/%s", r.unitPrice, r.quantityLabel));
            lblRoomCharge.setText(String.format("%,.0f VNĐ", r.roomCharge));

            if (r.lateHours > 0) {
                lblLateFee.setText(String.format("%,.0f VNĐ (trễ %.0f giờ × %,.0f)", r.lateFee, r.lateHours, s.getPricePerHour()));
                lblLateFee.setForeground(new Color(200, 60, 60));
            } else {
                lblLateFee.setText("0 (đúng giờ)");
                lblLateFee.setForeground(new Color(60, 140, 60));
            }

            lblTotalRoom.setText(String.format("%,.0f VNĐ", r.totalRoom));
            lblServiceTotal.setText(String.format("%,.0f VNĐ", serviceTotal));
            lblSubtotal.setText(String.format("%,.0f VNĐ", subtotal));

            lblAutoDiscount.setText(r.autoDiscountPercent.toPlainString() + "%");
            lblTotalDiscount.setText(totalDiscountPercent.toPlainString() + "%");
            lblDiscountAmount.setText(String.format("%,.0f VNĐ", discountAmount));
            lblTotal.setText(String.format("%,.0f VNĐ", total));

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
            String pricingType = s.getPricingType() != null ? s.getPricingType() : "DAILY";

            PricingService.PriceResult r = pricing.calcRoom(
                    pricingType,
                    s.getCheckinAt(), checkoutAt, s.getExpectedCheckoutAt(),
                    s.getPricePerNight(),
                    s.getPricePerHour() != null ? s.getPricePerHour() : BigDecimal.ZERO,
                    s.getPriceOvernight() != null ? s.getPriceOvernight() : BigDecimal.ZERO
            );

            BigDecimal serviceTotal = usageDAO.sumServiceAmountByStay(s.getStayId());
            if (serviceTotal == null) serviceTotal = BigDecimal.ZERO;

            BigDecimal subtotal = r.totalRoom.add(serviceTotal).setScale(0, RoundingMode.HALF_UP);

            BigDecimal extra = parsePercent(txtExtraDiscount.getText());
            BigDecimal totalDiscountPercent = r.autoDiscountPercent.add(extra);
            if (totalDiscountPercent.compareTo(new BigDecimal("100")) > 0) totalDiscountPercent = new BigDecimal("100");

            BigDecimal discountAmount = pricing.calcDiscountAmount(subtotal, totalDiscountPercent);
            BigDecimal total = subtotal.subtract(discountAmount).setScale(0, RoundingMode.HALF_UP);

            // MỞ FORM HÓA ĐƠN
            InvoiceDialog dlg = new InvoiceDialog(
                    SwingUtilities.getWindowAncestor(this),
                    s.getStayId(),
                    s.getRoomNumber(),
                    s.getCustomerName(),
                    s.getCheckinAt(),
                    checkoutAt,
                    r,
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
            roomsPanel.reload();
            reload();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Checkout lỗi! Xem Console.");
        }
    }

    private StayView selectedStay() {
        int row = table.getSelectedRow();
        if (row < 0) return null;

        try {
            // Reload from DB to get full StayView with all pricing fields
            int stayId = (Integer) model.getValueAt(row, 0);
            List<StayView> stays = stayDAO.findCheckedInStays();
            for (StayView s : stays) {
                if (s.getStayId() == stayId) return s;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void clearSummary() {
        lblPricingType.setText("-");
        lblQuantity.setText("-");
        lblUnitPrice.setText("-");
        lblRoomCharge.setText("-");
        lblLateFee.setText("-");
        lblLateFee.setForeground(Color.BLACK);
        lblTotalRoom.setText("-");
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
package ui.employee;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceDialog extends JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean confirmed = false;

    public InvoiceDialog(Window owner,
                         int stayId,
                         String roomNumber,
                         String customerName,
                         LocalDateTime checkinAt,
                         LocalDateTime checkoutAt,
                         long fullDays,
                         BigDecimal halfDayFactor,
                         BigDecimal pricePerNight,
                         BigDecimal roomCharge,
                         BigDecimal serviceTotal,
                         BigDecimal subtotal,
                         BigDecimal discountPercent,
                         BigDecimal discountAmount,
                         BigDecimal total) {

        super(owner, "Hóa đơn checkout - StayID " + stayId, ModalityType.APPLICATION_MODAL);
        setSize(520, 520);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setContentPane(root);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        String text =
                "===== HÓA ĐƠN THANH TOÁN =====\n" +
                "StayID: " + stayId + "\n" +
                "Phòng: " + roomNumber + "\n" +
                "Khách: " + customerName + "\n" +
                "Check-in:  " + checkinAt + "\n" +
                "Check-out: " + checkoutAt + "\n" +
                "------------------------------\n" +
                "Giá/đêm: " + pricePerNight + "\n" +
                "Số ngày tròn: " + fullDays + "\n" +
                "Nửa ngày (>=07:00): " + halfDayFactor + "\n" +
                "Tiền phòng: " + roomCharge + "\n" +
                "Tiền dịch vụ: " + serviceTotal + "\n" +
                "TẠM TÍNH: " + subtotal + "\n" +
                "------------------------------\n" +
                "Giảm giá (%): " + discountPercent + "\n" +
                "Tiền giảm: " + discountAmount + "\n" +
                "TỔNG THANH TOÁN: " + total + "\n" +
                "==============================\n";

        area.setText(text);
        root.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnConfirm = new JButton("Xác nhận thanh toán");
        JButton btnCancel = new JButton("Hủy");
        buttons.add(btnCancel);
        buttons.add(btnConfirm);
        root.add(buttons, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        btnConfirm.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        getRootPane().setDefaultButton(btnConfirm);
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
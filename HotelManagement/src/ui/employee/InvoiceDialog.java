package ui.employee;

import service.PricingService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;

@SuppressWarnings({"serial", "this-escape"})
public class InvoiceDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private boolean confirmed = false;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public InvoiceDialog(Window owner,
                         int stayId,
                         String roomNumber,
                         String customerName,
                         LocalDateTime checkinAt,
                         LocalDateTime checkoutAt,
                         PricingService.PriceResult r,
                         BigDecimal serviceTotal,
                         BigDecimal subtotal,
                         BigDecimal discountPercent,
                         BigDecimal discountAmount,
                         BigDecimal total) {

        super(owner, "Hóa đơn checkout - StayID " + stayId, ModalityType.APPLICATION_MODAL);
        setSize(560, 580);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setContentPane(root);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        StringBuilder sb = new StringBuilder();
        sb.append("========== HÓA ĐƠN THANH TOÁN ==========\n");
        sb.append("StayID:     ").append(stayId).append("\n");
        sb.append("Phòng:      ").append(roomNumber).append("\n");
        sb.append("Khách:      ").append(customerName).append("\n");
        sb.append("Check-in:   ").append(checkinAt.format(DTF)).append("\n");
        sb.append("Check-out:  ").append(checkoutAt.format(DTF)).append("\n");
        sb.append("------------------------------------------\n");
        sb.append("Hình thức:  ").append(r.pricingLabel).append("\n");
        sb.append(String.format("Đơn giá:    %,.0f VNĐ/%s\n", r.unitPrice, r.quantityLabel));
        sb.append(String.format("Số lượng:   %.0f %s\n", r.quantity, r.quantityLabel));
        sb.append(String.format("Tiền phòng: %,.0f VNĐ\n", r.roomCharge));

        if (r.lateHours > 0) {
            sb.append("------------------------------------------\n");
            sb.append(String.format("⚠ PHẠT TRỄ GIỜ: %.0f giờ\n", r.lateHours));
            sb.append(String.format("  Phí phạt:  %,.0f VNĐ\n", r.lateFee));
        }

        sb.append("------------------------------------------\n");
        sb.append(String.format("Tổng phòng:   %,.0f VNĐ\n", r.totalRoom));
        sb.append(String.format("Tiền dịch vụ: %,.0f VNĐ\n", serviceTotal));
        sb.append(String.format("TẠM TÍNH:     %,.0f VNĐ\n", subtotal));
        sb.append("------------------------------------------\n");
        sb.append(String.format("Giảm giá (%%): %s%%\n", discountPercent.toPlainString()));
        sb.append(String.format("Tiền giảm:    %,.0f VNĐ\n", discountAmount));
        sb.append(String.format("TỔNG THANH TOÁN: %,.0f VNĐ\n", total));
        sb.append("==========================================\n");

        area.setText(sb.toString());
        root.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExport = new JButton("Xuất PDF");
        JButton btnConfirm = new JButton("Xác nhận thanh toán");
        JButton btnCancel = new JButton("Hủy");
        buttons.add(btnExport);
        buttons.add(btnCancel);
        buttons.add(btnConfirm);
        root.add(buttons, BorderLayout.SOUTH);

        btnExport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu hóa đơn PDF");
            fileChooser.setSelectedFile(new File("HoaDon_" + stayId + ".pdf"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                    document.open();
                    
                    com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
                    com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
                    com.itextpdf.text.Font warnFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.RED);
                    
                    document.add(new Paragraph("===== HOA DON THANH TOAN =====", titleFont));
                    document.add(new Paragraph("StayID: " + stayId, normalFont));
                    document.add(new Paragraph("Phong: " + roomNumber, normalFont));
                    document.add(new Paragraph("Khach: " + customerName, normalFont));
                    document.add(new Paragraph("Check-in:  " + checkinAt.format(DTF), normalFont));
                    document.add(new Paragraph("Check-out: " + checkoutAt.format(DTF), normalFont));
                    document.add(new Paragraph("------------------------------", normalFont));
                    document.add(new Paragraph("Hinh thuc: " + r.pricingLabel, normalFont));
                    document.add(new Paragraph(String.format("Don gia: %,.0f VND/%s", r.unitPrice, r.quantityLabel), normalFont));
                    document.add(new Paragraph(String.format("So luong: %.0f %s", r.quantity, r.quantityLabel), normalFont));
                    document.add(new Paragraph(String.format("Tien phong: %,.0f VND", r.roomCharge), normalFont));

                    if (r.lateHours > 0) {
                        document.add(new Paragraph(String.format("PHAT TRE GIO: %.0f gio = %,.0f VND", r.lateHours, r.lateFee), warnFont));
                    }

                    document.add(new Paragraph("------------------------------", normalFont));
                    document.add(new Paragraph(String.format("Tong phong: %,.0f VND", r.totalRoom), normalFont));
                    document.add(new Paragraph(String.format("Tien dich vu: %,.0f VND", serviceTotal), normalFont));
                    document.add(new Paragraph(String.format("TAM TINH: %,.0f VND", subtotal), normalFont));
                    document.add(new Paragraph("------------------------------", normalFont));
                    document.add(new Paragraph(String.format("Giam gia: %s%%", discountPercent.toPlainString()), normalFont));
                    document.add(new Paragraph(String.format("Tien giam: %,.0f VND", discountAmount), normalFont));
                    document.add(new Paragraph(String.format("TONG THANH TOAN: %,.0f VND", total), titleFont));
                    document.add(new Paragraph("==============================", normalFont));
                    
                    document.close();
                    JOptionPane.showMessageDialog(this, "Xuất PDF thành công tại:\n" + fileToSave.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi xuất PDF: " + ex.getMessage());
                }
            }
        });

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
package ui.employee;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;

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
                    
                    document.add(new Paragraph("===== HOA DON THANH TOAN =====", titleFont));
                    document.add(new Paragraph("StayID: " + stayId, normalFont));
                    document.add(new Paragraph("Phong: " + roomNumber, normalFont));
                    document.add(new Paragraph("Khach: " + customerName, normalFont));
                    document.add(new Paragraph("Check-in:  " + checkinAt, normalFont));
                    document.add(new Paragraph("Check-out: " + checkoutAt, normalFont));
                    document.add(new Paragraph("------------------------------", normalFont));
                    document.add(new Paragraph("Gia/dem: " + pricePerNight, normalFont));
                    document.add(new Paragraph("So ngay tron: " + fullDays, normalFont));
                    document.add(new Paragraph("Nua ngay (>=07:00): " + halfDayFactor, normalFont));
                    document.add(new Paragraph("Tien phong: " + roomCharge, normalFont));
                    document.add(new Paragraph("Tien dich vu: " + serviceTotal, normalFont));
                    document.add(new Paragraph("TAM TINH: " + subtotal, normalFont));
                    document.add(new Paragraph("------------------------------", normalFont));
                    document.add(new Paragraph("Giam gia (%): " + discountPercent, normalFont));
                    document.add(new Paragraph("Tien giam: " + discountAmount, normalFont));
                    document.add(new Paragraph("TONG THANH TOAN: " + total, titleFont));
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
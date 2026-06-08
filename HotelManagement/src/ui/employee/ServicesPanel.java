package ui.employee;

import dao.ServiceDAO;
import dao.ServiceUsageDAO;
import dao.StayDAO;
import model.Service;
import model.StayView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"serial", "this-escape"})
public class ServicesPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final ServiceUsageDAO usageDAO = new ServiceUsageDAO();
    private final StayDAO stayDAO = new StayDAO();

    private final DefaultTableModel modelServices = new DefaultTableModel(
            new Object[]{"ServiceID","Tên dịch vụ","Đơn giá","Đơn vị"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tblServices = new JTable(modelServices);

    private final DefaultTableModel modelStays = new DefaultTableModel(
            new Object[]{"StayID","Phòng","Khách"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tblStays = new JTable(modelStays);

    private final DefaultTableModel modelBooked = new DefaultTableModel(
            new Object[]{"Dịch vụ", "Số lượng", "Đơn giá", "Thời gian", "Ghi chú"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable tblBooked = new JTable(modelBooked);

    private final JSpinner spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTextField txtNote = new JTextField(15);
    
    private final SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
    private final JSpinner spnTime = new JSpinner(dateModel);

    public ServicesPanel() {
        setLayout(new BorderLayout(10,10));
        setBackground(Color.WHITE);
        
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spnTime, "dd/MM/yyyy HH:mm");
        spnTime.setEditor(timeEditor);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        JButton btnReload = new JButton("Tải lại");
        btnReload.setBackground(new Color(60,130,200)); btnReload.setForeground(Color.WHITE); btnReload.setFocusPainted(false);
        JButton btnAdd = new JButton("Thêm dịch vụ vào stay");
        btnAdd.setBackground(new Color(80,160,110)); btnAdd.setForeground(Color.WHITE); btnAdd.setFocusPainted(false);
        top.add(btnReload);
        top.add(new JLabel("Số lượng:"));
        top.add(spnQty);
        top.add(new JLabel("Thời gian:"));
        top.add(spnTime);
        top.add(new JLabel("Ghi chú:"));
        top.add(txtNote);
        top.add(btnAdd);

        add(top, BorderLayout.NORTH);

        tblStays.setSelectionBackground(new Color(200,230,255));
        tblServices.setSelectionBackground(new Color(200,230,255));
        tblBooked.setSelectionBackground(new Color(200,230,255));

        JPanel staysPanel = createTitledPanel("1. Chọn phòng đang ở", tblStays);
        JPanel servicesPanel = createTitledPanel("2. Chọn dịch vụ để thêm", tblServices);
        JPanel bookedPanel = createTitledPanel("3. Các dịch vụ đã đặt của phòng này", tblBooked);

        JSplitPane splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT, servicesPanel, bookedPanel);
        splitRight.setResizeWeight(0.5);

        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, staysPanel, splitRight);
        splitMain.setResizeWeight(0.35);
        add(splitMain, BorderLayout.CENTER);

        tblStays.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadBookedServices();
            }
        });

        btnReload.addActionListener(e -> reload());
        btnAdd.addActionListener(e -> addServiceToStay());

        reload();
    }

    public void reload() {
        try {
            // stays checked in
            modelStays.setRowCount(0);
            List<StayView> stays = stayDAO.findCheckedInStays();
            for (StayView s : stays) {
                modelStays.addRow(new Object[]{s.getStayId(), s.getRoomNumber(), s.getCustomerName()});
            }

            // services active
            modelServices.setRowCount(0);
            List<Service> services = serviceDAO.findActive();
            for (Service sv : services) {
                modelServices.addRow(new Object[]{sv.getId(), sv.getName(), sv.getUnitPrice(), sv.getUnit()});
            }
            
            loadBookedServices();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi reload dịch vụ/stay!");
        }
    }

    private void loadBookedServices() {
        Integer stayId = selectedStayId();
        modelBooked.setRowCount(0);
        if (stayId == null) return;
        
        try {
            List<model.ServiceUsageView> usages = usageDAO.getUsagesByStay(stayId);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (model.ServiceUsageView u : usages) {
                String time = u.getUsedAt() != null ? u.getUsedAt().format(formatter) : "";
                modelBooked.addRow(new Object[]{
                        u.getServiceName(),
                        u.getQuantity(),
                        String.format("%,.0f", u.getUnitPrice()),
                        time,
                        u.getNote()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addServiceToStay() {
        Integer stayId = selectedStayId();
        Integer serviceId = selectedServiceId();
        if (stayId == null) {
            JOptionPane.showMessageDialog(this, "Chọn 1 stay (đang ở) phía trên!");
            return;
        }
        if (serviceId == null) {
            JOptionPane.showMessageDialog(this, "Chọn 1 dịch vụ phía dưới!");
            return;
        }

        int qty = (Integer) spnQty.getValue();
        Date d = (Date) spnTime.getValue();
        LocalDateTime usedAt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
        String note = txtNote.getText().trim();

        try {
            usageDAO.addUsage(stayId, serviceId, qty, usedAt, note);
            JOptionPane.showMessageDialog(this, "Thêm dịch vụ OK!");
            txtNote.setText("");
            spnQty.setValue(1);
            loadBookedServices();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi thêm dịch vụ!");
        }
    }

    private JPanel createTitledPanel(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(lbl, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private Integer selectedStayId() {
        int row = tblStays.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelStays.getValueAt(row, 0);
    }

    private Integer selectedServiceId() {
        int row = tblServices.getSelectedRow();
        if (row < 0) return null;
        return (Integer) modelServices.getValueAt(row, 0);
    }
}
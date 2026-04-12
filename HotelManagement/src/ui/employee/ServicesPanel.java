package ui.employee;

import dao.ServiceDAO;
import dao.ServiceUsageDAO;
import dao.StayDAO;
import model.Service;
import model.StayView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ServicesPanel extends JPanel {

    /**
	 * 
	 */
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

    private final JSpinner spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTextField txtNote = new JTextField(20);

    public ServicesPanel() {
        setLayout(new BorderLayout(10,10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnReload = new JButton("Reload");
        JButton btnAdd = new JButton("Thêm dịch vụ vào stay");
        top.add(btnReload);
        top.add(new JLabel("Số lượng:"));
        top.add(spnQty);
        top.add(new JLabel("Ghi chú:"));
        top.add(txtNote);
        top.add(btnAdd);

        add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tblStays),
                new JScrollPane(tblServices)
        );
        split.setResizeWeight(0.35);
        add(split, BorderLayout.CENTER);

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
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi reload dịch vụ/stay!");
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
        String note = txtNote.getText().trim();

        try {
            usageDAO.addUsage(stayId, serviceId, qty, note);
            JOptionPane.showMessageDialog(this, "Thêm dịch vụ OK!");
            txtNote.setText("");
            spnQty.setValue(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi thêm dịch vụ!");
        }
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
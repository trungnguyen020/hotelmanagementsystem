package ui.employee;

import dao.CustomerDAO;
import dao.RoomDAO;
import dao.StayDAO;
import model.Customer;
import model.Employee;
import model.RoomView;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@SuppressWarnings({"serial", "this-escape"})
public class CheckinPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final Employee me;
    private final RoomsPanel roomsPanel;

    private final RoomDAO roomDAO = new RoomDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final StayDAO stayDAO = new StayDAO();

    private final JComboBox<RoomItem> cboRooms = new JComboBox<>();

    // Search customer
    private final JTextField txtSearch = new JTextField(18);
    private final JButton btnSearch = new JButton("Tìm");
    private final JComboBox<CustomerItem> cboCustomers = new JComboBox<>();
    private final JButton btnNewCustomer = new JButton("Thêm mới KH");

    // Customer fields
    private final JTextField txtName = new JTextField(20);
    private final JTextField txtPhone = new JTextField(20);
    private final JTextField txtIdNo = new JTextField(20);

    // Pricing type selection
    private final JComboBox<String> cboPricingType = new JComboBox<>(new String[]{"Theo giờ", "Qua đêm", "Theo ngày"});
    private final JSpinner spnDuration = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
    private final JLabel lblDurationUnit = new JLabel("ngày");
    private final JLabel lblPricePreview = new JLabel("");

    private Customer selectedCustomer = null;

    public CheckinPanel(Employee me, RoomsPanel roomsPanel) {
        this.me = me;
        this.roomsPanel = roomsPanel;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        add(form, BorderLayout.NORTH);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Phòng trống:"), c);
        c.gridx = 1; c.gridy = y; form.add(cboRooms, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Tìm KH (Tên/CCCD/Phone):"), c);
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchRow.add(txtSearch);
        searchRow.add(btnSearch);
        searchRow.add(cboCustomers);
        searchRow.add(btnNewCustomer);
        c.gridx = 1; c.gridy = y; form.add(searchRow, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Họ tên khách:"), c);
        c.gridx = 1; c.gridy = y; form.add(txtName, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("SĐT:"), c);
        c.gridx = 1; c.gridy = y; form.add(txtPhone, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("CCCD/ID:"), c);
        c.gridx = 1; c.gridy = y; form.add(txtIdNo, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Hình thức:"), c);
        c.gridx = 1; c.gridy = y; form.add(cboPricingType, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Thời lượng:"), c);
        JPanel durationRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        durationRow.add(spnDuration);
        durationRow.add(lblDurationUnit);
        durationRow.add(Box.createHorizontalStrut(20));
        lblPricePreview.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblPricePreview.setForeground(new Color(200, 60, 60));
        durationRow.add(lblPricePreview);
        c.gridx = 1; c.gridy = y; form.add(durationRow, c); y++;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        JButton btnReloadRooms = new JButton("Load phòng trống");
        btnReloadRooms.setBackground(new Color(60,130,200)); btnReloadRooms.setForeground(Color.WHITE); btnReloadRooms.setFocusPainted(false);
        JButton btnCheckin = new JButton("Check-in");
        btnCheckin.setBackground(new Color(80,160,110)); btnCheckin.setForeground(Color.WHITE); btnCheckin.setFocusPainted(false);
        JButton btnClear = new JButton("Xóa form");
        btnClear.setBackground(new Color(180,180,180)); btnClear.setForeground(Color.WHITE); btnClear.setFocusPainted(false);
        buttons.add(btnReloadRooms);
        buttons.add(btnCheckin);
        buttons.add(btnClear);
        add(buttons, BorderLayout.CENTER);

        // Events
        btnReloadRooms.addActionListener(e -> loadAvailableRooms());
        btnCheckin.addActionListener(e -> doCheckin());
        btnClear.addActionListener(e -> clearForm());

        btnSearch.addActionListener(e -> doSearchCustomer());
        txtSearch.addActionListener(e -> doSearchCustomer());
        cboCustomers.addActionListener(e -> onSelectCustomer());
        btnNewCustomer.addActionListener(e -> openCreateCustomer());

        cboPricingType.addActionListener(e -> onPricingTypeChanged());
        spnDuration.addChangeListener(e -> updatePricePreview());
        cboRooms.addActionListener(e -> updatePricePreview());

        // Init state
        cboPricingType.setSelectedIndex(2); // Default: Theo ngày
        onPricingTypeChanged();
        loadAvailableRooms();
    }

    private void onPricingTypeChanged() {
        int idx = cboPricingType.getSelectedIndex();
        if (idx == 0) { // Theo giờ
            lblDurationUnit.setText("giờ");
            spnDuration.setModel(new SpinnerNumberModel(2, 1, 72, 1));
            spnDuration.setEnabled(true);
        } else if (idx == 1) { // Qua đêm
            lblDurationUnit.setText("đêm (23h→7h)");
            spnDuration.setModel(new SpinnerNumberModel(1, 1, 1, 1));
            spnDuration.setEnabled(false); // cố định 1 đêm
        } else { // Theo ngày
            lblDurationUnit.setText("ngày");
            spnDuration.setModel(new SpinnerNumberModel(1, 1, 365, 1));
            spnDuration.setEnabled(true);
        }
        updatePricePreview();
    }

    private void updatePricePreview() {
        RoomItem room = (RoomItem) cboRooms.getSelectedItem();
        if (room == null) {
            lblPricePreview.setText("");
            return;
        }

        try {
            int idx = cboPricingType.getSelectedIndex();
            int duration = (Integer) spnDuration.getValue();
            
            if (idx == 0) {
                lblPricePreview.setText("Tạm tính: " + duration + " giờ × giá/giờ");
            } else if (idx == 1) {
                lblPricePreview.setText("Tạm tính: 1 đêm × giá qua đêm");
            } else {
                lblPricePreview.setText("Tạm tính: " + duration + " ngày × giá/ngày");
            }
        } catch (Exception ex) {
            lblPricePreview.setText("");
        }
    }

    private String getPricingTypeCode() {
        int idx = cboPricingType.getSelectedIndex();
        switch (idx) {
            case 0: return "HOURLY";
            case 1: return "OVERNIGHT";
            case 2: return "DAILY";
            default: return "DAILY";
        }
    }

    private void loadAvailableRooms() {
        try {
            cboRooms.removeAllItems();
            List<RoomView> list = roomDAO.findAll();
            for (RoomView r : list) {
                if ("AVAILABLE".equalsIgnoreCase(r.getStatus())) {
                    cboRooms.addItem(new RoomItem(r.getRoomId(), r.getRoomNumber()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi load phòng trống!");
        }
    }

    private void doSearchCustomer() {
        try {
            String kw = txtSearch.getText().trim();
            cboCustomers.removeAllItems();
            selectedCustomer = null;

            if (kw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập tên/CCCD/phone để tìm!");
                return;
            }

            List<Customer> list = customerDAO.search(kw);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách. Bấm 'Thêm mới KH' để tạo.");
                return;
            }

            for (Customer cs : list) cboCustomers.addItem(new CustomerItem(cs));
            cboCustomers.setSelectedIndex(0); // auto fill
            onSelectCustomer();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tìm khách!");
        }
    }

    private void onSelectCustomer() {
        Object o = cboCustomers.getSelectedItem();
        if (!(o instanceof CustomerItem)) return;

        Customer cs = ((CustomerItem) o).customer;
        selectedCustomer = cs;

        txtName.setText(nvl(cs.getFullName()));
        txtPhone.setText(nvl(cs.getPhone()));
        txtIdNo.setText(nvl(cs.getIdNumber()));
    }

    private void openCreateCustomer() {
        Customer preset = new Customer();
        preset.setFullName(txtName.getText().trim());
        preset.setPhone(txtPhone.getText().trim());
        preset.setIdNumber(txtIdNo.getText().trim());

        CustomerCreateDialog dlg = new CustomerCreateDialog(SwingUtilities.getWindowAncestor(this), preset);
        dlg.setVisible(true);

        Customer created = dlg.getResult();
        if (created == null) return;

        try {
            Customer saved = customerDAO.insert(created);
            selectedCustomer = saved;

            txtName.setText(nvl(saved.getFullName()));
            txtPhone.setText(nvl(saved.getPhone()));
            txtIdNo.setText(nvl(saved.getIdNumber()));

            cboCustomers.removeAllItems();
            cboCustomers.addItem(new CustomerItem(saved));
            cboCustomers.setSelectedIndex(0);

            JOptionPane.showMessageDialog(this, "Tạo khách hàng OK!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu khách hàng!");
        }
    }

    private void doCheckin() {
        RoomItem room = (RoomItem) cboRooms.getSelectedItem();
        if (room == null) {
            JOptionPane.showMessageDialog(this, "Không còn phòng trống!");
            return;
        }

        // BẮT BUỘC: phải chọn khách có trong DB (từ search hoặc vừa tạo mới)
        if (selectedCustomer == null) {
            JOptionPane.showMessageDialog(this,
                    "Không có khách hàng này trong hệ thống.\n" +
                    "Hãy tìm kiếm và chọn khách, hoặc bấm 'Thêm mới KH' để tạo.",
                    "Thiếu khách hàng", JOptionPane.WARNING_MESSAGE);

            // "đá ra" theo ý bạn = xoá sạch form check-in
            clearForm();
            return;
        }

        try {
            String pricingType = getPricingTypeCode();
            int duration = (Integer) spnDuration.getValue();
            LocalDateTime checkinAt = LocalDateTime.now();
            LocalDateTime expected;

            if ("HOURLY".equals(pricingType)) {
                expected = checkinAt.plusHours(duration);
            } else if ("OVERNIGHT".equals(pricingType)) {
                // Qua đêm: check-in từ 23h, trả phòng 7h sáng hôm sau
                LocalDateTime tonight23 = checkinAt.toLocalDate().atTime(LocalTime.of(23, 0));
                if (checkinAt.isAfter(tonight23)) {
                    // Đã quá 23h → trả phòng 7h sáng ngày mai
                    expected = checkinAt.toLocalDate().plusDays(1).atTime(LocalTime.of(7, 0));
                } else {
                    // Chưa 23h → trả phòng 7h sáng ngày mai
                    expected = checkinAt.toLocalDate().plusDays(1).atTime(LocalTime.of(7, 0));
                }
            } else {
                // DAILY
                expected = checkinAt.plusDays(duration);
            }

            int stayId = stayDAO.checkin(selectedCustomer.getId(), room.id, checkinAt, expected, me.getId(), pricingType);

            String typeLabel = cboPricingType.getSelectedItem().toString();
            JOptionPane.showMessageDialog(this, 
                "Check-in OK! StayID=" + stayId + 
                "\nHình thức: " + typeLabel +
                "\nDự kiến trả phòng: " + expected.toString().replace("T", " "));

            roomsPanel.reload();
            loadAvailableRooms();

            clearForm();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Check-in lỗi! Xem Console.");
        }
    }

    private void clearForm() {
        txtSearch.setText("");
        cboCustomers.removeAllItems();
        selectedCustomer = null;

        txtName.setText("");
        txtPhone.setText("");
        txtIdNo.setText("");
        cboPricingType.setSelectedIndex(2); // Default: Theo ngày
        spnDuration.setValue(1);
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private static class RoomItem {
        final int id;
        final String number;
        RoomItem(int id, String number) { this.id = id; this.number = number; }
        @Override public String toString() { return number; }
    }

    private static class CustomerItem {
        final Customer customer;
        CustomerItem(Customer customer) { this.customer = customer; }

        @Override public String toString() {
            String phone = customer.getPhone() == null ? "" : customer.getPhone();
            String idno  = customer.getIdNumber() == null ? "" : customer.getIdNumber();
            return customer.getFullName() + " | " + phone + " | " + idno;
        }
    }
}
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
import java.util.List;

public class CheckinPanel extends JPanel {

    /**
	 * 
	 */
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

    private final JSpinner spnDays = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));

    private Customer selectedCustomer = null;

    public CheckinPanel(Employee me, RoomsPanel roomsPanel) {
        this.me = me;
        this.roomsPanel = roomsPanel;

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250,250,252));

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

        c.gridx = 0; c.gridy = y; form.add(new JLabel("Số ngày dự kiến:"), c);
        c.gridx = 1; c.gridy = y; form.add(spnDays, c); y++;

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

        btnReloadRooms.addActionListener(e -> loadAvailableRooms());
        btnCheckin.addActionListener(e -> doCheckin());
        btnClear.addActionListener(e -> clearForm());

        btnSearch.addActionListener(e -> doSearchCustomer());
        txtSearch.addActionListener(e -> doSearchCustomer());
        cboCustomers.addActionListener(e -> onSelectCustomer());
        btnNewCustomer.addActionListener(e -> openCreateCustomer());

        loadAvailableRooms();
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
            int days = (Integer) spnDays.getValue();
            LocalDateTime checkinAt = LocalDateTime.now();
            LocalDateTime expected = checkinAt.plusDays(days);

            int stayId = stayDAO.checkin(selectedCustomer.getId(), room.id, checkinAt, expected, me.getId());

            JOptionPane.showMessageDialog(this, "Check-in OK. StayID=" + stayId);

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
        spnDays.setValue(1);
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
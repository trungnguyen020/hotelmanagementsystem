package ui.employee;

import model.Customer;

import javax.swing.*;
import java.awt.*;

public class CustomerCreateDialog extends JDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTextField txtName = new JTextField(20);
    private final JTextField txtPhone = new JTextField(20);
    private final JTextField txtIdNo = new JTextField(20);

    private Customer result; // null nếu cancel

    public CustomerCreateDialog(Window owner, Customer preset) {
        super(owner, "Tạo khách hàng mới", ModalityType.APPLICATION_MODAL);
        setSize(420, 220);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        JPanel form = new JPanel(new GridBagLayout());
        root.add(form, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        c.gridx = 0; c.gridy = y; form.add(new JLabel("Họ tên:"), c);
        c.gridx = 1; c.gridy = y; form.add(txtName, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("SĐT:"), c);
        c.gridx = 1; c.gridy = y; form.add(txtPhone, c); y++;

        c.gridx = 0; c.gridy = y; form.add(new JLabel("CCCD/ID:"), c);
        c.gridx = 1; c.gridy = y; form.add(txtIdNo, c); y++;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        buttons.add(btnOk);
        buttons.add(btnCancel);
        root.add(buttons, BorderLayout.SOUTH);

        if (preset != null) {
            txtName.setText(nvl(preset.getFullName()));
            txtPhone.setText(nvl(preset.getPhone()));
            txtIdNo.setText(nvl(preset.getIdNumber()));
        }

        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> { result = null; dispose(); });

        getRootPane().setDefaultButton(btnOk);
    }

    private void onOk() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên khách!");
            return;
        }

        Customer c = new Customer();
        c.setFullName(name);
        c.setPhone(txtPhone.getText().trim());
        c.setIdNumber(txtIdNo.getText().trim());

        this.result = c;
        dispose();
    }

    public Customer getResult() { return result; }

    private String nvl(String s) { return s == null ? "" : s; }
}
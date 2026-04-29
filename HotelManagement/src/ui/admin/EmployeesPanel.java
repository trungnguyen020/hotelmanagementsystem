package ui.admin;

import dao.EmployeeDAO;
import model.Employee;
import model.Role;
import ui.components.PaginationPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeesPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Username", "Họ tên", "Vai trò", "Trạng thái", "Thao tác"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { 
            return column == 5; // Only action column is editable
        }
    };

    private final JTable table = new JTable(model);
    private final Employee currentAdmin;
    private final PaginationPanel paginationPanel;

    public EmployeesPanel(Employee currentAdmin) {
        this.currentAdmin = currentAdmin;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setOpaque(false);

        JButton btnAdd = createBtn("Thêm mới", new Color(46, 204, 113));
        JButton btnResign = createBtn("Nghỉ việc", new Color(230, 126, 34)); // Orange for deactivate
        JButton btnActivate = createBtn("Làm việc lại", new Color(155, 89, 182)); // Purple for activate
        JButton btnRefresh = createBtn("Làm mới", new Color(149, 165, 166));

        top.add(btnAdd);
        top.add(btnResign);
        top.add(btnActivate);
        top.add(btnRefresh);

        // Styling the table
        // Styling the table
        table.setFillsViewportHeight(true);
        table.setRowHeight(40);
        table.setFont(new Font("Tahoma", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(250, 250, 250));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.setSelectionBackground(new Color(235, 245, 255));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));

        // Action column
        ui.components.TableActionEvent event = new ui.components.TableActionEvent() {
            @Override
            public void onEdit(int row) {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                Employee emp = getEmployeeAt(row);
                if (emp != null) showForm(emp);
            }

            @Override
            public void onDelete(int row) {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                deleteEmployeeAt(row);
            }
        };
        table.getColumnModel().getColumn(5).setCellRenderer(new ui.components.TableActionCellRender());
        table.getColumnModel().getColumn(5).setCellEditor(new ui.components.TableActionCellEditor(event));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        sp.getViewport().setBackground(Color.WHITE);

        paginationPanel = new PaginationPanel((offset, limit, keyword) -> loadData(offset, limit, keyword));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(top, BorderLayout.CENTER);
        northPanel.add(paginationPanel.getSearchPanel(), BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(paginationPanel.getPagingPanel(), BorderLayout.SOUTH);

        // Actions
        // Actions
        btnRefresh.addActionListener(e -> paginationPanel.reload());
        btnAdd.addActionListener(e -> showForm(null));
        btnResign.addActionListener(e -> resignSelected());
        btnActivate.addActionListener(e -> activateSelected());

        loadData(paginationPanel.getOffset(), paginationPanel.getPageSize(), paginationPanel.getKeyword());
    }

    private JButton createBtn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Tahoma", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void loadData(int offset, int limit, String keyword) {
        try {
            model.setRowCount(0);
            List<Employee> list = employeeDAO.findPaginatedAdmin(keyword, offset, limit);
            int totalCount = employeeDAO.countTotalAdmin(keyword);
            for (Employee e : list) {
                model.addRow(new Object[]{
                        e.getId(), e.getUsername(), e.getFullName(), e.getRole().name(), e.getStatus(), ""
                });
            }
            paginationPanel.updatePagination(totalCount);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu nhân viên!");
        }
    }

    private Employee getSelectedEmployee() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 nhân viên trong bảng!");
            return null;
        }
        return getEmployeeAt(row);
    }

    private Employee getEmployeeAt(int row) {
        Employee e = new Employee();
        e.setId((Integer) model.getValueAt(row, 0));
        e.setUsername((String) model.getValueAt(row, 1));
        e.setFullName((String) model.getValueAt(row, 2));
        e.setRole(Role.valueOf((String) model.getValueAt(row, 3)));
        e.setStatus((String) model.getValueAt(row, 4));
        return e;
    }

    private void deleteEmployeeAt(int row) {
        Employee e = getEmployeeAt(row);
        if (e == null) return;
        
        if (e.getId() == currentAdmin.getId()) {
             JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản mà bạn đang đăng nhập.");
             return;
        }

        if ("admin".equalsIgnoreCase(e.getUsername())) {
             JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản admin gốc.");
             return;
        }

        int ans = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa hẳn nhân viên '" + e.getFullName() + "' khỏi hệ thống?", "Xác nhận xóa cứng", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                employeeDAO.deleteOrDeactivate(e.getId());
                JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                paginationPanel.reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
                paginationPanel.reload(); 
            }
        }
    }

    private void resignSelected() {
        Employee e = getSelectedEmployee();
        if (e == null) return;

        if (e.getId() == currentAdmin.getId()) {
             JOptionPane.showMessageDialog(this, "Không thể cho tài khoản bạn đang đăng nhập nghỉ việc.");
             return;
        }

        if ("admin".equalsIgnoreCase(e.getUsername())) {
             JOptionPane.showMessageDialog(this, "Không thể cho tài khoản admin gốc nghỉ việc.");
             return;
        }

        if ("INACTIVE".equals(e.getStatus())) {
             JOptionPane.showMessageDialog(this, "Nhân viên này đã nghỉ việc rổi.");
             return;
        }

        int ans = JOptionPane.showConfirmDialog(this, "Xác nhận cho nhân viên '" + e.getFullName() + "' nghỉ việc (đổi trạng thái thành INACTIVE)?", "Cho nghỉ việc", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                employeeDAO.deactivate(e.getId());
                JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái nghỉ việc thành công!");
                paginationPanel.reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void activateSelected() {
        Employee e = getSelectedEmployee();
        if (e == null) return;

        if ("ACTIVE".equals(e.getStatus())) {
             JOptionPane.showMessageDialog(this, "Nhân viên này đang ở trạng thái làm việc rồi.");
             return;
        }

        int ans = JOptionPane.showConfirmDialog(this, "Xác nhận cho nhân viên '" + e.getFullName() + "' đi làm lại (đổi trạng thái thành ACTIVE)?", "Làm việc lại", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                employeeDAO.activate(e.getId());
                JOptionPane.showMessageDialog(this, "Đã khôi phục trạng thái đi làm lại thành công!");
                paginationPanel.reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showForm(Employee e) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), e == null ? "Thêm Nhân Viên" : "Sửa Nhân Viên", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtUser = new JTextField(e != null ? e.getUsername() : "");
        if (e != null) txtUser.setEnabled(false); // Can't change username
        JPasswordField txtPass = new JPasswordField();
        JTextField txtName = new JTextField(e != null ? e.getFullName() : "");
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"ADMIN", "STAFF"});
        if (e != null) {
            cbRole.setSelectedItem(e.getRole().name());
            if (e.getId() == currentAdmin.getId()) {
                cbRole.setEnabled(false);
            }
        }
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        if (e != null) {
            cbStatus.setSelectedItem(e.getStatus());
            if (e.getId() == currentAdmin.getId()) {
                cbStatus.setEnabled(false);
            }
        }

        p.add(new JLabel("Tên đăng nhập:")); p.add(txtUser);
        p.add(new JLabel("Mật khẩu" + (e != null ? " (Để trống=Không đổi):" : ":"))); p.add(txtPass);
        p.add(new JLabel("Họ và tên:")); p.add(txtName);
        p.add(new JLabel("Vai trò:")); p.add(cbRole);
        p.add(new JLabel("Trạng thái:")); 
        if (e == null) {
            JLabel lblStt = new JLabel("ACTIVE (Mặc định)");
            p.add(lblStt);
        } else {
            p.add(cbStatus);
        }

        JButton btnSave = createBtn("Lưu", new Color(46, 204, 113));
        p.add(new JLabel()); // spacer
        p.add(btnSave);

        dialog.add(p);

        btnSave.addActionListener(evt -> {
            try {
                String u = txtUser.getText().trim();
                String pass = new String(txtPass.getPassword()).trim();
                String n = txtName.getText().trim();
                if (u.isEmpty() || n.isEmpty() || (e == null && pass.isEmpty())) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng điền đủ thông tin!");
                    return;
                }

                Employee emp = new Employee();
                emp.setUsername(u);
                emp.setFullName(n);
                emp.setRole(Role.valueOf(cbRole.getSelectedItem().toString()));

                if (e == null) {
                    employeeDAO.insert(emp, pass);
                } else {
                    emp.setId(e.getId());
                    emp.setStatus(cbStatus.getSelectedItem().toString());
                    employeeDAO.update(emp, pass);
                }

                dialog.dispose();
                paginationPanel.reload();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi lưu dữ liệu: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }
}
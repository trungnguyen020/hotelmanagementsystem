package ui.admin;

import javax.swing.*;
import java.awt.*;

public class EmployeesPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EmployeesPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Quản lý nhân viên (Admin) - TODO", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
package ui.employee;

import javax.swing.*;
import java.awt.*;

public class ServicesPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServicesPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Quản lý dịch vụ (Nhân viên) - TODO", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
package ui.employee;

import javax.swing.*;
import java.awt.*;

public class RoomsPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RoomsPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Quản lý phòng (Nhân viên) - TODO", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
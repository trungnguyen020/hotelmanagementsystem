package ui.employee;

import javax.swing.*;
import java.awt.*;

public class CheckinPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CheckinPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Check-in - TODO", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
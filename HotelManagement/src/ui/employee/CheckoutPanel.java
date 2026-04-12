package ui.employee;

import javax.swing.*;
import java.awt.*;

public class CheckoutPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CheckoutPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Check-out - TODO", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
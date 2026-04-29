package ui.components;

import javax.swing.*;
import java.awt.*;

public class TableActionPanel extends JPanel {

    private JButton btnEdit;
    private JButton btnDelete;

    public TableActionPanel() {
        initComponents();
    }

    public void initEvent(TableActionEvent event, int row) {
        btnEdit.addActionListener(e -> event.onEdit(row));
        btnDelete.addActionListener(e -> event.onDelete(row));
    }

    private void initComponents() {
        btnEdit = new JButton("Sửa");
        btnEdit.setBackground(new Color(52, 152, 219)); // Blue
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFocusPainted(false);
        btnEdit.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnDelete = new JButton("Xóa");
        btnDelete.setBackground(new Color(231, 76, 60)); // Red
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));

        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        setBackground(Color.WHITE);
        add(btnEdit);
        add(btnDelete);
    }
}

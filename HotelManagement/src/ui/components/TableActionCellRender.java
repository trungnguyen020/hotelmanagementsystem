package ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

@SuppressWarnings({"serial", "this-escape"})
public class TableActionCellRender extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        TableActionPanel action = new TableActionPanel();
        if (isSelected) {
            action.setBackground(table.getSelectionBackground());
        } else if (row % 2 == 0) {
            action.setBackground(Color.WHITE);
        } else {
            action.setBackground(table.getBackground());
        }
        return action;
    }
}

package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PaginationPanel {

    private int currentPage = 1;
    private int pageSize = 20;
    private int totalPages = 1;

    private JTextField txtSearch;
    private JButton btnSearch;

    private JButton btnFirst;
    private JButton btnPrev;
    private JLabel lblPageInfo;
    private JButton btnNext;
    private JButton btnLast;

    private JPanel pnlSearch;
    private JPanel pnlPaging;

    private PaginationListener listener;

    public interface PaginationListener {
        void onPageChange(int offset, int limit, String keyword);
    }

    public PaginationPanel(PaginationListener listener) {
        this.listener = listener;

        // --- Search bar ---
        pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlSearch.setOpaque(false);
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnSearch = createBtn("Tìm kiếm", new Color(52, 152, 219));
        
        pnlSearch.add(new JLabel("Tìm kiếm: "));
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);

        // --- Pagination controls ---
        pnlPaging = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlPaging.setOpaque(false);
        btnFirst = createBtn("<<", new Color(52, 152, 219));
        btnPrev = createBtn("<", new Color(52, 152, 219));
        lblPageInfo = new JLabel("Trang 1 / 1");
        lblPageInfo.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblPageInfo.setBorder(new EmptyBorder(0, 10, 0, 10));
        btnNext = createBtn(">", new Color(52, 152, 219));
        btnLast = createBtn(">>", new Color(52, 152, 219));

        pnlPaging.add(btnFirst);
        pnlPaging.add(btnPrev);
        pnlPaging.add(lblPageInfo);
        pnlPaging.add(btnNext);
        pnlPaging.add(btnLast);

        // --- Events ---
        btnSearch.addActionListener(e -> {
            currentPage = 1; 
            triggerEvent();
        });

        txtSearch.addActionListener(e -> { 
            currentPage = 1;
            triggerEvent();
        });

        btnFirst.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage = 1;
                triggerEvent();
            }
        });

        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                triggerEvent();
            }
        });

        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                triggerEvent();
            }
        });

        btnLast.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage = totalPages;
                triggerEvent();
            }
        });
        
        updateControls();
    }

    public JPanel getSearchPanel() {
        return pnlSearch;
    }

    public JPanel getPagingPanel() {
        return pnlPaging;
    }

    private JButton createBtn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Tahoma", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public void updatePagination(int totalRecords) {
        this.totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (this.totalPages == 0) this.totalPages = 1;
        if (this.currentPage > this.totalPages) {
            this.currentPage = this.totalPages;
        }
        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        updateControls();
    }

    private void updateControls() {
        boolean hasPrev = currentPage > 1;
        boolean hasNext = currentPage < totalPages;
        
        btnFirst.setEnabled(hasPrev);
        btnPrev.setEnabled(hasPrev);
        btnNext.setEnabled(hasNext);
        btnLast.setEnabled(hasNext);

        Color cActive = new Color(52, 152, 219);
        Color cInactive = new Color(200, 200, 200);
        
        btnFirst.setBackground(hasPrev ? cActive : cInactive);
        btnPrev.setBackground(hasPrev ? cActive : cInactive);
        btnNext.setBackground(hasNext ? cActive : cInactive);
        btnLast.setBackground(hasNext ? cActive : cInactive);
    }

    private void triggerEvent() {
        int offset = (currentPage - 1) * pageSize;
        String kw = txtSearch.getText().trim();
        if (listener != null) {
            listener.onPageChange(offset, pageSize, kw);
        }
    }
    
    public void reload() {
        triggerEvent();
    }

    public String getKeyword() {
        return txtSearch.getText().trim();
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getOffset() {
        return (currentPage - 1) * pageSize;
    }
}

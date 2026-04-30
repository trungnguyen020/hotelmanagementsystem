package model;

import java.math.BigDecimal;

public class DiscountRule {
    private int id;
    private int minDays;
    private BigDecimal discountPercent;

    public DiscountRule() {}

    public DiscountRule(int minDays, BigDecimal discountPercent) {
        this.minDays = minDays;
        this.discountPercent = discountPercent;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMinDays() { return minDays; }
    public void setMinDays(int minDays) { this.minDays = minDays; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
}

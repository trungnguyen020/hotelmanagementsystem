package model;

import java.math.BigDecimal;

public class Service {
    private int id;
    private String name;
    private BigDecimal unitPrice;
    private String unit;
    private boolean active;

    public int getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getUnit() { return unit; }
    public boolean isActive() { return active; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setActive(boolean active) { this.active = active; }
}
package model;

import java.math.BigDecimal;

public class RoomType {
    private int id;
    private String name;
    private BigDecimal pricePerNight;
    private BigDecimal pricePerHour;
    private BigDecimal priceOvernight;
    private int capacity;
    private String description;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }

    public BigDecimal getPriceOvernight() { return priceOvernight; }
    public void setPriceOvernight(BigDecimal priceOvernight) { this.priceOvernight = priceOvernight; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name; // To display nicely in ComboBox
    }
}

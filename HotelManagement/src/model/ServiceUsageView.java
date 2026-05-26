package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceUsageView {
    private int id;
    private String serviceName;
    private int quantity;
    private BigDecimal unitPrice;
    private LocalDateTime usedAt;
    private String note;
    private String roomNumber;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}

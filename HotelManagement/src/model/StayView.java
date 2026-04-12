package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StayView {
    private int stayId;
    private int roomId;
    private String roomNumber;
    private String customerName;
    private LocalDateTime checkinAt;
    private LocalDateTime expectedCheckoutAt;
    private BigDecimal pricePerNight;

    public int getStayId() { return stayId; }
    public int getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getCheckinAt() { return checkinAt; }
    public LocalDateTime getExpectedCheckoutAt() { return expectedCheckoutAt; }
    public BigDecimal getPricePerNight() { return pricePerNight; }

    public void setStayId(int stayId) { this.stayId = stayId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCheckinAt(LocalDateTime checkinAt) { this.checkinAt = checkinAt; }
    public void setExpectedCheckoutAt(LocalDateTime expectedCheckoutAt) { this.expectedCheckoutAt = expectedCheckoutAt; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
}
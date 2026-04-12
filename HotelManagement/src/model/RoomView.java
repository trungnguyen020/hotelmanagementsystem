package model;

import java.math.BigDecimal;

public class RoomView {
    private int roomId;
    private String roomNumber;
    private String roomType;
    private BigDecimal pricePerNight;
    private String status; // AVAILABLE/OCCUPIED/MAINTENANCE

    public int getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public String getStatus() { return status; }

    public void setRoomId(int roomId) { this.roomId = roomId; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public void setStatus(String status) { this.status = status; }
}
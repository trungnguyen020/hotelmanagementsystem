package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingHistoryItem {
    private String roomNumber;
    private LocalDateTime checkinAt;
    private LocalDateTime checkoutAt;
    private String status;
    private BigDecimal totalPaid;

    public BookingHistoryItem() {}

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public LocalDateTime getCheckinAt() { return checkinAt; }
    public void setCheckinAt(LocalDateTime checkinAt) { this.checkinAt = checkinAt; }

    public LocalDateTime getCheckoutAt() { return checkoutAt; }
    public void setCheckoutAt(LocalDateTime checkoutAt) { this.checkoutAt = checkoutAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
}

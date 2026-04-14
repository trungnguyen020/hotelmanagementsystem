package service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PricingService {

	public static class PriceResult {
		public long fullDays; // số ngày tròn
		public BigDecimal halfDayFactor; // 0 hoặc 0.5
		public BigDecimal roomCharge; // tiền phòng (fullDays + halfDay)*price
		public BigDecimal subtotal; // hiện tại = roomCharge (chưa dịch vụ)
		public BigDecimal autoDiscountPercent; // 0/10/20
		public java.math.BigDecimal serviceTotal;  // tiền dịch vụ
		public java.math.BigDecimal totalBeforeDiscount; // phòng + dịch vụ
	}

	// checkOut < 07:00 => không tính ngày đó (chỉ tính đến ngày trước)
	// checkOut >= 07:00 => tính thêm 0.5 ngày (chỉ phòng)
	public PriceResult calcRoom(java.time.LocalDateTime checkin, java.time.LocalDateTime checkout,
			java.math.BigDecimal pricePerNight) {
		if (checkout.isBefore(checkin))
			throw new IllegalArgumentException("checkout < checkin");

		java.time.LocalDate inDate = checkin.toLocalDate();
		java.time.LocalDate outDate = checkout.toLocalDate();

		long baseDays = java.time.temporal.ChronoUnit.DAYS.between(inDate, outDate); // 0 nếu cùng ngày

		java.math.BigDecimal half = java.math.BigDecimal.ZERO;
		if (checkout.getHour() >= 7) {
			half = new java.math.BigDecimal("0.5");
		}

		// Tổng số "ngày tính tiền" = baseDays (+ nửa ngày nếu checkout >=07:00)
		java.math.BigDecimal billDays = java.math.BigDecimal.valueOf(baseDays).add(half);

		// Nếu bạn muốn tối thiểu vẫn phải trả 0.5 ngày khi billDays=0 (checkin & checkout trước 07:00 cùng ngày)
		// thì mở dòng dưới:
		// if (billDays.compareTo(java.math.BigDecimal.ZERO) == 0) billDays = new java.math.BigDecimal("0.5");

		java.math.BigDecimal roomCharge = pricePerNight.multiply(billDays).setScale(2, java.math.RoundingMode.HALF_UP);

		// Giảm giá theo SỐ NGÀY TRÒN (baseDays), không tính nửa ngày
		java.math.BigDecimal autoDiscount = java.math.BigDecimal.ZERO;
		if (baseDays >= 20)
			autoDiscount = new java.math.BigDecimal("20");
		else if (baseDays >= 10)
			autoDiscount = new java.math.BigDecimal("10");

		PriceResult r = new PriceResult();
		r.fullDays = baseDays;
		r.halfDayFactor = half;
		r.roomCharge = roomCharge;
		r.subtotal = roomCharge;
		r.autoDiscountPercent = autoDiscount;
		r.serviceTotal = java.math.BigDecimal.ZERO;
		r.totalBeforeDiscount = r.roomCharge;
		return r;
	}

	public BigDecimal calcDiscountAmount(BigDecimal subtotal, BigDecimal discountPercent) {
		if (discountPercent.compareTo(BigDecimal.ZERO) < 0)
			discountPercent = BigDecimal.ZERO;
		if (discountPercent.compareTo(new BigDecimal("100")) > 0)
			discountPercent = new BigDecimal("100");

		return subtotal.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
	}
}
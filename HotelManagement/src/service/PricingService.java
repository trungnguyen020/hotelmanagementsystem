package service;

import dao.DiscountRuleDAO;
import model.DiscountRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class PricingService {

    /** Ân hạn (phút) trước khi tính phạt trễ */
    public static final int GRACE_MINUTES = 30;

    private final DiscountRuleDAO ruleDAO = new DiscountRuleDAO();

    public static class PriceResult {
        public String pricingType;          // HOURLY, OVERNIGHT, DAILY
        public String pricingLabel;         // "Theo giờ", "Qua đêm", "Theo ngày"
        public BigDecimal unitPrice;        // giá đơn vị được dùng (giờ/đêm/ngày)
        public double quantity;             // số giờ/đêm/ngày tính tiền
        public String quantityLabel;        // "giờ" / "đêm" / "ngày"
        public BigDecimal roomCharge;       // tiền phòng cơ bản (trước phạt)
        public double lateHours;            // số giờ trễ (đã trừ grace, 0 nếu không trễ)
        public BigDecimal lateFee;          // tiền phạt trễ
        public BigDecimal totalRoom;        // roomCharge + lateFee
        public BigDecimal autoDiscountPercent; // giảm giá tự động
        public BigDecimal serviceTotal;     // tiền dịch vụ
        public BigDecimal totalBeforeDiscount; // totalRoom + serviceTotal
    }

    /**
     * Tính tiền phòng theo 3 hình thức.
     * 
     * @param pricingType    HOURLY, OVERNIGHT, DAILY
     * @param checkin        thời điểm checkin
     * @param actualCheckout thời điểm checkout thực tế (NOW)
     * @param expectedCheckout thời điểm checkout dự kiến (khách đã chọn)
     * @param pricePerNight  giá/ngày
     * @param pricePerHour   giá/giờ
     * @param priceOvernight giá qua đêm
     */
    public PriceResult calcRoom(String pricingType,
                                LocalDateTime checkin,
                                LocalDateTime actualCheckout,
                                LocalDateTime expectedCheckout,
                                BigDecimal pricePerNight,
                                BigDecimal pricePerHour,
                                BigDecimal priceOvernight) {

        if (actualCheckout.isBefore(checkin))
            throw new IllegalArgumentException("checkout < checkin");

        PriceResult r = new PriceResult();
        r.pricingType = pricingType;
        r.serviceTotal = BigDecimal.ZERO;

        // ===== Tính tiền phòng cơ bản =====
        switch (pricingType) {
            case "HOURLY":
                r.pricingLabel = "Theo giờ";
                r.quantityLabel = "giờ";
                r.unitPrice = pricePerHour;
                calcHourly(r, checkin, expectedCheckout, pricePerHour, pricePerNight);
                break;

            case "OVERNIGHT":
                r.pricingLabel = "Qua đêm";
                r.quantityLabel = "đêm";
                r.unitPrice = priceOvernight;
                r.quantity = 1; // Luôn 1 đêm (23h → 7h)
                r.roomCharge = priceOvernight;
                // Cap: qua đêm không được vượt giá ngày
                if (r.roomCharge.compareTo(pricePerNight) > 0) {
                    r.roomCharge = pricePerNight;
                }
                break;

            case "DAILY":
            default:
                r.pricingLabel = "Theo ngày";
                r.quantityLabel = "ngày";
                r.unitPrice = pricePerNight;
                calcDaily(r, checkin, expectedCheckout, pricePerNight);
                break;
        }

        // ===== Tính phạt trễ =====
        calcLateFee(r, actualCheckout, expectedCheckout, pricePerHour);

        // ===== Tổng phòng =====
        r.totalRoom = r.roomCharge.add(r.lateFee).setScale(0, RoundingMode.HALF_UP);

        // ===== Giảm giá tự động =====
        r.autoDiscountPercent = BigDecimal.ZERO;
        if ("DAILY".equals(pricingType)) {
            try {
                long baseDays = (long) r.quantity;
                List<DiscountRule> rules = ruleDAO.findAll();
                for (DiscountRule rule : rules) {
                    if (baseDays >= rule.getMinDays()) {
                        r.autoDiscountPercent = rule.getDiscountPercent();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        r.totalBeforeDiscount = r.totalRoom;
        return r;
    }

    /** Tính theo giờ: ceil(hours) * pricePerHour, cap tại pricePerNight cho mỗi 24h */
    private void calcHourly(PriceResult r, LocalDateTime checkin, LocalDateTime expectedCheckout,
                            BigDecimal pricePerHour, BigDecimal pricePerNight) {
        long minutes = Duration.between(checkin, expectedCheckout).toMinutes();
        if (minutes < 60) minutes = 60; // tối thiểu 1 giờ
        double hours = Math.ceil(minutes / 60.0);
        r.quantity = hours;

        BigDecimal rawCharge = pricePerHour.multiply(BigDecimal.valueOf((long) hours));

        // Cap: nếu vượt giá ngày thì tính theo ngày
        double days = hours / 24.0;
        if (days >= 1) {
            long fullDays = (long) days;
            double remainHours = hours - fullDays * 24;
            BigDecimal dayCharge = pricePerNight.multiply(BigDecimal.valueOf(fullDays));
            BigDecimal remainCharge = pricePerHour.multiply(BigDecimal.valueOf((long) Math.ceil(remainHours)));
            // Cap phần lẻ nếu vượt 1 ngày
            if (remainCharge.compareTo(pricePerNight) > 0) {
                remainCharge = pricePerNight;
            }
            BigDecimal cappedCharge = dayCharge.add(remainCharge);
            rawCharge = rawCharge.min(cappedCharge);
        }

        r.roomCharge = rawCharge.setScale(0, RoundingMode.HALF_UP);
    }

    /** Tính theo ngày: ceil(days) * pricePerNight, tối thiểu 1 ngày */
    private void calcDaily(PriceResult r, LocalDateTime checkin, LocalDateTime expectedCheckout,
                           BigDecimal pricePerNight) {
        long minutes = Duration.between(checkin, expectedCheckout).toMinutes();
        double days = Math.ceil(minutes / (24.0 * 60));
        if (days < 1) days = 1;
        r.quantity = days;
        r.roomCharge = pricePerNight.multiply(BigDecimal.valueOf((long) days)).setScale(0, RoundingMode.HALF_UP);
    }

    /** Tính phạt trễ: ceil(late hours - grace) * pricePerHour */
    private void calcLateFee(PriceResult r, LocalDateTime actualCheckout, LocalDateTime expectedCheckout,
                             BigDecimal pricePerHour) {
        r.lateFee = BigDecimal.ZERO;
        r.lateHours = 0;

        if (actualCheckout.isAfter(expectedCheckout)) {
            long lateMinutes = Duration.between(expectedCheckout, actualCheckout).toMinutes();
            lateMinutes -= GRACE_MINUTES; // trừ ân hạn
            if (lateMinutes > 0) {
                double lateH = Math.ceil(lateMinutes / 60.0);
                r.lateHours = lateH;
                r.lateFee = pricePerHour.multiply(BigDecimal.valueOf((long) lateH)).setScale(0, RoundingMode.HALF_UP);
            }
        }
    }

    public BigDecimal calcDiscountAmount(BigDecimal subtotal, BigDecimal discountPercent) {
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0)
            discountPercent = BigDecimal.ZERO;
        if (discountPercent.compareTo(new BigDecimal("100")) > 0)
            discountPercent = new BigDecimal("100");

        return subtotal.multiply(discountPercent).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
    }
}
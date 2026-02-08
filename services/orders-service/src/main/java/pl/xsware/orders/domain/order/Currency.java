package pl.xsware.orders.domain.order;

public enum Currency {
    PLN,
    EUR,
    USD;

    public static Currency from(String value) {
        try {
            return Currency.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported currency: " + value);
        }
    }
}

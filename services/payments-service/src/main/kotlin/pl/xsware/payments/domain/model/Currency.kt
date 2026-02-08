package pl.xsware.payments.domain.model

enum class Currency {
    PLN,
    EUR,
    USD;

    companion object {
        fun from(value: String): Currency =
            try {
                valueOf(value)
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException("Unsupported currency: $value")
            }
    }
}

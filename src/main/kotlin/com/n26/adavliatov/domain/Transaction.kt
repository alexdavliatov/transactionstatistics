package com.n26.adavliatov.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.time.Instant
import java.time.Instant.now

data class Transaction @JsonCreator constructor(
    @JsonProperty("amount") val amount: BigDecimal,
    @JsonProperty("timestamp") val timestamp: Instant
) {
    init {
        require(amount >= ZERO)
        require(timestamp <= now())
    }
}

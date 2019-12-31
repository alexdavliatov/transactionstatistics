package com.n26.adavliatov.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonIgnore
import com.n26.adavliatov.config.Config.TTL
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.time.Instant

data class Stats(
    @JsonFormat(shape= STRING)
    val sum: BigDecimal,
    @JsonFormat(shape= STRING)
    val max: BigDecimal,
    @JsonFormat(shape= STRING)
    val min: BigDecimal,
    val count: Int,
    @JsonIgnore val lastUpdatedAt: Instant,
    @JsonFormat(shape= STRING)
    val avg: BigDecimal = sum / BigDecimal(count)
) {
    //todo < vs <=
    fun active(now: Instant) = now <= lastUpdatedAt.plus(TTL)

    fun plus(other: Stats): Stats = Stats(
        sum + other.sum,
        maxOf(max, other.max),
        minOf(min, other.min),
        count + other.count,
        maxOf(lastUpdatedAt, other.lastUpdatedAt)
    )

    fun plus(tnx: Transaction): Stats =
        if (!active(tnx.timestamp))
            Stats(tnx.amount, tnx.amount, tnx.amount, 1, tnx.timestamp)
        else
            Stats(
                sum + tnx.amount,
                maxOf(max, tnx.amount),
                minOf(min, tnx.amount),
                count + 1,
                maxOf(lastUpdatedAt, tnx.timestamp)
            )

    companion object {
        val zero = Stats(ZERO, ZERO, ZERO, 0, Instant.MIN, ZERO)
    }
}

package com.n26.adavliatov.config

import java.time.Duration
import java.time.Duration.ofMinutes

object Config {
    val TTL: Duration = ofMinutes(1)
}

object StatisticsConfig {
    const val chunksPerSecond: Int = 1

    init {
        require(chunksPerSecond > 0) { "chunksPerSecond should be greater than 0" }
        require(1000 % chunksPerSecond == 0) { "1000 % chunksPerSecond should equal 0" }
    }
}

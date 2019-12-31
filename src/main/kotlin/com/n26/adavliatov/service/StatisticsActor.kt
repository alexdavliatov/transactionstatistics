package com.n26.adavliatov.service

import akka.actor.Props
import akka.actor.UntypedAbstractActor
import akka.event.Logging
import com.n26.adavliatov.api.RegisterTransaction
import com.n26.adavliatov.api.ReplyStats
import com.n26.adavliatov.api.RequestStats
import com.n26.adavliatov.config.StatisticsConfig
import com.n26.adavliatov.domain.Stats
import java.time.Instant
import java.time.ZoneOffset

internal class StatisticsActor(config: StatisticsConfig) : UntypedAbstractActor() {
    private val log = Logging.getLogger(context().system(), this)

    private val millisInChunk = 1000 / config.chunksPerSecond
    private val chunksNum = config.chunksPerSecond * 60

    private var last: Stats = Stats.zero

    private val stats: Array<Stats> = Array(chunksNum) { Stats.zero }

    override fun onReceive(message: Any?) = when (message) {
        is RegisterTransaction -> {
            val tnx = message.tnx
            val chunkId = chunkId(tnx.timestamp)
            stats[chunkId] = stats[chunkId].plus(tnx)

            log.info("{}", tnx)
            log.info("{}", stats.mapIndexed { i, s -> i to s }.toMap().filterValues { it != Stats.zero })
        }
        is RequestStats -> {
            val now = Instant.now().atZone(ZoneOffset.UTC).toInstant()
            last = aggregateStats(now)

            log.info(stats.mapIndexed { i, s -> i to s }.toMap().filterValues { it != Stats.zero }.toString())

            sender().tell(ReplyStats(last), self())
        }
        else -> log.error("Invalid message: $message")
    }

    private fun aggregateStats(now: Instant): Stats = stats
        .filter { it.active(now) }
        .fold(Stats.zero) { acc, stat -> acc.plus(stat) }

    private fun chunkId(timestamp: Instant) =
        ((timestamp.toEpochMilli() / millisInChunk) % chunksNum).toInt()


    companion object {
        fun props(config: StatisticsConfig): Props =
            Props.create(StatisticsActor::class.java) { StatisticsActor(config) }

    }
}

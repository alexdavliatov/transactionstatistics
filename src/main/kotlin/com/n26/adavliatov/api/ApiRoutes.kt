package com.n26.adavliatov.api

import akka.actor.ActorRef
import akka.actor.ActorRef.noSender
import akka.http.javadsl.marshallers.jackson.Jackson.marshaller
import akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller
import akka.http.javadsl.model.StatusCodes.*
import akka.http.javadsl.server.Directives.*
import akka.http.javadsl.server.Route
import akka.pattern.Patterns
import akka.pattern.PatternsCS
import akka.util.Timeout
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.n26.adavliatov.config.Config.TTL
import com.n26.adavliatov.domain.Stats
import com.n26.adavliatov.domain.Transaction
import scala.concurrent.duration.FiniteDuration
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.util.concurrent.TimeUnit.SECONDS

object ApiRoutes {
    internal val mapper: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())

    fun createRoute(statisticsActor: ActorRef): Route {
        return concat(
            path("transactions") {
                post {
                    entity(unmarshaller(mapper, Transaction::class.java)) { tnx ->
                        val now = Instant.now().atZone(UTC).toInstant()
                        if (tnx.timestamp.plus(TTL) < now) return@entity complete(NO_CONTENT)
                        if (tnx.timestamp > now) return@entity complete(UNPROCESSABLE_ENTITY)

                        statisticsActor.tell(RegisterTransaction(tnx), noSender())

                        complete(CREATED)
                    }
                }
            },
            path("statistics") {
                get {
                    val timeout = Duration.ofSeconds(5)
                    // query the actor for the current auction state
                    val stats = Patterns.ask(statisticsActor, RequestStats(), timeout)
                        .thenApply { it as ReplyStats }
                        .thenApply { it.stats }

                    completeOKWithFuture(stats, marshaller())
                }
            }
        )
    }
}

internal class RegisterTransaction(val tnx: Transaction)
internal class RequestStats
internal class ReplyStats(val stats: Stats)

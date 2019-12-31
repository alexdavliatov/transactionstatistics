package com.n26.adavliatov

import akka.actor.ActorSystem
import akka.actor.TypedActor.context
import akka.event.Logging
import akka.http.javadsl.ConnectHttp.toHost
import akka.http.javadsl.Http
import akka.http.javadsl.ServerBinding
import akka.stream.ActorMaterializer
import com.n26.adavliatov.api.ApiRoutes
import com.n26.adavliatov.config.StatisticsConfig
import com.n26.adavliatov.service.StatisticsActor
import java.util.concurrent.CompletionStage

class App {
    val system = ActorSystem.create("routes")
    val materializer = ActorMaterializer.create(system)

    private val log = Logging.getLogger(system, this)

    private lateinit var binding: CompletionStage<ServerBinding>

    fun start() {

        val http = Http.get(system)

        //In order to access all directives we need an instance where the routes are define.
        val statisticsActor = system.actorOf(StatisticsActor.props(StatisticsConfig), "auction")

        val routeFlow = ApiRoutes.createRoute(statisticsActor).flow(system, materializer)

        binding = http.bindAndHandle(
            routeFlow,
            toHost("localhost", 8080), materializer
        )
        log.info("Listening for http://localhost:8080")
    }

    fun stop() {
        binding
            .thenCompose(ServerBinding::unbind)
            .thenAccept { system.terminate() }
    }
}

fun main() {
    App().start()
}
//Notes:
//1. timezone
//2. special error instead of http status
//3. configuration
//4. cache & recalculate
//5. старые не протухли, а новые уже записываются
//6. stats without tnxs
//7. status +/-
//8. use interval tree

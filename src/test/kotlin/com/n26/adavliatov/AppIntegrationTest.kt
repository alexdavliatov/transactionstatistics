package com.n26.adavliatov

import akka.http.javadsl.Http
import akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller
import akka.http.javadsl.model.HttpRequest.GET
import akka.http.javadsl.model.StatusCodes.OK
import com.n26.adavliatov.api.ApiRoutes
import com.n26.adavliatov.domain.Stats
import org.spekframework.spek2.Spek
import kotlin.test.assertEquals

object AppIntegrationTest : Spek({
    lateinit var app: App
    lateinit var api: Http

    beforeGroup {
        app = App().apply { start() }
        api = Http.get(app.system)
    }

    group("should return") {
        test("empty statistics on 1 request") {
            api
                .singleRequest(GET("http://localhost:8080/statistics"))
                .handle { response, _ ->
                    {
                        unmarshaller(ApiRoutes.mapper, Stats::class.java)
                            .unmarshal(response.entity(), app.materializer)
                            .thenApply { stats -> assertEquals(stats, Stats.zero) }
                    }
                }
        }
    }

    afterGroup {
        app.stop()
    }
})

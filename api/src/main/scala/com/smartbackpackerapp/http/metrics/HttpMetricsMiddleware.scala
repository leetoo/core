/*
 * Copyright 2017 Smart Backpacker App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbackpackerapp.http.metrics

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.codahale.metrics._
import com.smartbackpackerapp.common.Log
import org.http4s.AuthedService

object HttpMetricsMiddleware {

  def apply[F[_]](registry: MetricRegistry,
                  service: AuthedService[String, F])
                 (implicit F: Sync[F], L: Log[F]): AuthedService[String, F] = {

    val requestsCount   = registry.meter("http-requests")
    val responseSuccess = registry.meter("http-response-success")
    val responseFailure = registry.meter("http-response-failure")
    val responseTime    = registry.histogram("http-response-time")

    Kleisli { req =>
      OptionT.liftF(F.delay(System.nanoTime())).flatMap { start =>
        service(req).semiflatMap { response =>
          for {
            _    <- F.delay(requestsCount.mark())
            _    <- if (response.status.isSuccess) F.delay(responseSuccess.mark())
                    else F.delay(responseFailure.mark())
            time <- F.delay((System.nanoTime() - start) / 1000000)
            _    <- F.delay(responseTime.update(time))
            _    <- L.info(s"HTTP Response Time: $time ms")
          } yield response
        }
      }
    }
  }

}

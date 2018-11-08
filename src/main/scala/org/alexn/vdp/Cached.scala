/*
 * Copyright (c) 2018 Alexandru Nedelcu.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alexn.vdp

import cats.effect.concurrent.Deferred
import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.atomic.Atomic
import org.slf4j.LoggerFactory

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

final class Cached[A] private () {
  private type State = Map[String, (Deferred[Task, A], Long)]
  private[this] val state = Atomic(Map.empty : State)

  /**
    * Fetches the current cached value, or initiates a task evaluation to
    * update the current value.
    */
  def getOrUpdate(key: String, exp: FiniteDuration, task: Task[A]): Task[A] = {
    def update(current: State, sc: Scheduler) = {
      val promise = Deferred.unsafe[Task, A]
      val expiresAt = sc.clockMonotonic(MILLISECONDS) + exp.toMillis
      val update = current.updated(key, (promise, expiresAt))
      if (state.compareAndSet(current, update)) {
        task.bracket(Task.now)(promise.complete)
      } else {
        Task.suspend(loop(state.get(), sc))
      }
    }

    def loop(current: State, sc: Scheduler): Task[A] = {
      val current = state.get()

      current.get(key) match {
        case None => update(current, sc)
        case Some((p, expiresAt)) =>
          val now = sc.clockMonotonic(MILLISECONDS)
          if (now < expiresAt) {
            logger.info("Cache hit: " + key)
            p.get
          } else {
            update(current, sc)
          }
      }
    }

    Task.deferAction { sc => loop(state.get(), sc) }
  }

  private[this] val logger = LoggerFactory.getLogger(getClass)
}

object Cached {
  /**
    * Builds a [[Cached]] instance.
    */
  def apply[A]: Task[Cached[A]] =
    Task(unsafe())

  def unsafe[A](): Cached[A] =
    new Cached[A]()
}

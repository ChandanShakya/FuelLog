package com.chandanshakya.fuellog.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class MoneyTest {

    @Test
    fun roundToCents_halfUp() {
        assertEquals(12.35, Money.roundToCents(12.345), 0.0)
        assertEquals(12.34, Money.roundToCents(12.344), 0.0)
        assertEquals(0.0, Money.roundToCents(0.0), 0.0)
    }

    @Test
    fun sumCents_avoidsFloatingPointDrift() {
        val total = Money.sumCents(listOf(0.1, 0.2))
        assertEquals(0.3, total, 0.0)
    }

    @Test
    fun sumCents_manyFillUps() {
        val amounts = List(100) { 10.10 }
        assertEquals(1010.0, Money.sumCents(amounts), 0.0)
    }

    @Test
    fun rate_and_cost_roundTrip() {
        val cost = Money.cost(volume = 40.0, rate = 1.234)
        assertEquals(49.36, cost, 0.0)
        val rate = Money.rate(volume = 40.0, cost = 49.36)
        assertEquals(1.23, rate!!, 0.0)
    }

    @Test
    fun rate_zeroVolume_returnsNull() {
        assertNull(Money.rate(volume = 0.0, cost = 10.0))
    }

    @Test
    fun formatCurrency_concurrentCalls_areConsistent() {
        val threads = 8
        val iterations = 64
        val pool = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)
        val failure = AtomicReference<String?>(null)

        repeat(threads) {
            pool.execute {
                try {
                    repeat(iterations) {
                        val result = CurrencyFormatter.formatCurrency(123.456, "USD")
                        if (result != "$123.46") {
                            failure.compareAndSet(null, "got $result")
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        pool.shutdownNow()
        assertEquals(null, failure.get())
    }
}

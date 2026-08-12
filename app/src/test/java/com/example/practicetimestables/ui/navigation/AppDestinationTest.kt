package com.example.practicetimestables.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDestinationTest {
    @Test
    fun startDestinationIsRegisteredAndHasAValidRoute() {
        val startDestination = AppDestination.startDestination

        assertTrue(startDestination.route.isNotBlank())
        assertTrue(startDestination in AppDestination.all)
        assertEquals(startDestination, AppDestination.fromRoute(startDestination.route))
    }

    @Test
    fun everyDestinationIsInitializedWithAUniqueValidRoute() {
        val destinations = AppDestination.all
        val routes = destinations.map(AppDestination::route)

        assertTrue(destinations.isNotEmpty())
        assertTrue(routes.all(String::isNotBlank))
        assertEquals(routes.size, routes.distinct().size)
        destinations.forEach { destination ->
            assertNotNull(destination)
            assertEquals(destination, AppDestination.fromRoute(destination.route))
        }
    }

    @Test
    fun unknownRouteDoesNotResolveToAnArbitraryDestination() {
        assertNull(AppDestination.fromRoute("not-a-route"))
    }
}

package io.github.rsookram.soon

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.rsookram.soon.tasks.Tasks

/**
 * The entry point into the UI, implemented in compose.
 */
@Composable
fun Main() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "tasks") {
        composable("tasks") {
            Tasks(navController)
        }
    }
}

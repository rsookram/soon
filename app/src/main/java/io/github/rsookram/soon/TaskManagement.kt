package io.github.rsookram.soon

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.rsookram.soon.taskdetails.TaskDetails
import io.github.rsookram.soon.tasks.Tasks

/**
 * The entry point into the UI which allows the user to manage their tasks / schedule.
 */
@Composable
fun TaskManagement() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "tasks") {
        composable("tasks") {
            Tasks(navController)
        }

        composable("task") { TaskDetails(navController) }

        composable(
            "task/{base64}",
            listOf(navArgument("base64") { type = NavType.StringType }),
        ) { TaskDetails(navController) }
    }
}

fun NavController.navigateToTaskDetails(task: Task? = null) {
    if (task == null) {
        navigate("task")
    } else {
        val base64 = Task.ADAPTER.encodeByteString(task).base64()
        navigate("task/$base64")
    }
}

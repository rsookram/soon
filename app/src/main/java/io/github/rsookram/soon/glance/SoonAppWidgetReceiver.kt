package io.github.rsookram.soon.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.rsookram.soon.ApplicationScope
import io.github.rsookram.soon.TasksActivity
import io.github.rsookram.soon.Todo
import io.github.rsookram.soon.data.Repository
import io.github.rsookram.soon.data.toLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class SoonAppWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject lateinit var widgetFactory: Provider<SoonWidget>
    @Inject lateinit var repository: Repository
    @Inject @ApplicationScope
    lateinit var scope: CoroutineScope

    override val glanceAppWidget: GlanceAppWidget
        get() = widgetFactory.get()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Handle updating the tasks displayed for the day when this is called on a new day.
        scope.launch {
            repository.refreshAgenda()
        }
    }
}

private val toggledTodoIdKey = ActionParameters.Key<ByteArray>("ToggledTodoIdKey")

class SoonWidget @Inject constructor(private val repository: Repository) : GlanceAppWidget() {

    @Composable
    override fun Content() {
        val agenda = runBlocking { repository.agenda.first() }

        // Hack to force action parameters to be updated
        // https://issuetracker.google.com/issues/213861535#comment2
        Text("")

        // TODO: Reuse AppTheme
        val isDarkTheme = isWidgetInDarkTheme()
        LazyColumn(
            GlanceModifier
                .fillMaxSize()
                .background(if (isDarkTheme) Color(0xFF212121) else Color.White)
                .appWidgetBackground()
                .appWidgetBackgroundRadius()
                .padding(16.dp)
        ) {
            item {
                val date = agenda.date.toLocalDate()
                val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                Text(
                    "✏️ ${date.format(formatter)}",
                    GlanceModifier.clickable(actionStartActivity(TasksActivity::class.java)),
                )
            }

            item {
                Spacer(GlanceModifier.height(8.dp))
            }

            items(agenda.todos) { todo ->
                CheckBox(
                    checked = todo.isComplete,
                    onCheckedChange = actionRunCallback<CheckBoxClickAction>(
                        actionParametersOf(
                            toggledTodoIdKey to Todo.ADAPTER.encode(todo)
                        ),
                    ),
                    modifier = GlanceModifier.fillMaxWidth(),
                    text = todo.task!!.name,
                    colors = CheckBoxColors(
                        checkedColor = if (isDarkTheme) Color.White else Color.Black,
                        uncheckedColor = if (isDarkTheme) Color.White else Color(0xFF121212),
                    ),
                )
            }
        }
    }
}

class CheckBoxClickAction : ActionCallback {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface Dependencies {
        fun repository(): Repository
    }

    override suspend fun onRun(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val todo = Todo.ADAPTER.decode(parameters[toggledTodoIdKey]!!)

        val entryPoint = EntryPointAccessors.fromApplication<Dependencies>(context)

        entryPoint.repository().toggleComplete(todo)
    }
}

// TODO: Check if this actually works
@Composable
private fun GlanceModifier.appWidgetBackgroundRadius(): GlanceModifier =
    if (Build.VERSION.SDK_INT >= 31) {
        cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        cornerRadius(16.dp)
    }

@Composable
private fun isWidgetInDarkTheme(): Boolean {
    val uiMode = LocalContext.current.resources.configuration.uiMode
    return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

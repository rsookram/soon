package io.github.rsookram.soon.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.rsookram.soon.ApplicationScope
import io.github.rsookram.soon.Todo
import io.github.rsookram.soon.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

        LazyColumn(GlanceModifier.fillMaxSize().padding(16.dp)) {
            items(agenda.todos) { todo ->
                CheckBox(
                    checked = todo.isComplete,
                    onCheckedChange = actionRunCallback<CheckBoxClickAction>(
                        actionParametersOf(
                            toggledTodoIdKey to Todo.ADAPTER.encode(todo)
                        ),
                    ),
                    text = todo.task!!.name,
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

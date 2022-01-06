package io.github.rsookram.soon.glance

import androidx.compose.runtime.Composable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class SoonAppWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget
        get() = SoonWidget()
}

class SoonWidget : GlanceAppWidget() {

    @Composable
    override fun Content() {
    }
}

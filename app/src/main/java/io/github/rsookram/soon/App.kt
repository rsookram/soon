package io.github.rsookram.soon

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.glance.appwidget.updateAll
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.rsookram.soon.data.DataSerializer
import io.github.rsookram.soon.data.Repository
import io.github.rsookram.soon.glance.SoonWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier annotation used to identify the [CoroutineScope] which is scoped to the application's
 * lifetime.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope

/**
 * The entry point into the app.
 */
@HiltAndroidApp
class App : Application() {

    @Inject lateinit var repository: Repository
    @Inject lateinit var widget: SoonWidget
    @Inject @ApplicationScope
    lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        scope.launch {
            repository.agenda.collect {
                widget.updateAll(this@App)
            }
        }
    }
}

/**
 * Hilt module which provides singleton-scoped dependencies that can't be provided through @Inject.
 */
@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Singleton
    @ApplicationScope
    @Provides
    fun providesCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Singleton
    @Provides
    fun providesDataStore(@ApplicationContext context: Context): DataStore<Data> =
        DataStoreFactory.create(
            DataSerializer(),
            produceFile = { context.dataStoreFile("soon.pb") }
        )

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}

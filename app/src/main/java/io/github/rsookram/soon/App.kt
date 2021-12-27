package io.github.rsookram.soon

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
class App : Application()

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
}

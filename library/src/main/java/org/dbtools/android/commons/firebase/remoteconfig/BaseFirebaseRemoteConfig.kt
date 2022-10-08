@file:Suppress("MemberVisibilityCanBePrivate")

package org.dbtools.android.commons.firebase.remoteconfig

import androidx.annotation.XmlRes
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import timber.log.Timber
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

abstract class BaseFirebaseRemoteConfig {
    private val firebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        val instance = Firebase.remoteConfig

        // config
        val firebaseSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(getMinimumFetchIntervalInSeconds()) // default is 12 hours... for testing/debug set to 0L
            .setFetchTimeoutInSeconds(getFetchTimeoutInSeconds())
            .build()

        instance.setConfigSettingsAsync(firebaseSettings)

        // set defaults
        instance.setDefaultsAsync(getDefaults())

        return@lazy instance
    }

    @XmlRes
    abstract fun getDefaults(): Int

    fun getMinimumFetchIntervalInSeconds(): Long = TimeUnit.HOURS.toSeconds(12)

    fun getFetchTimeoutInSeconds(): Long = DEFAULT_TIMEOUT_FETCH_SECONDS_LONG

    @Suppress("ForbiddenVoid") // calling Java
    fun fetch(now: Boolean = false): Task<Void> {
        Timber.d("RemoteConfig: fetch  now=$now")

        return if (now) {
            // Starts fetching configs, adhering to the specified (0L) minimum fetch interval (fetch NOW)
            // LIMIT: 5 calls per hour
            firebaseRemoteConfig.fetch(0L)
        } else {
            // Starts fetching configs, adhering to the default minimum fetch interval.
            firebaseRemoteConfig.fetch()
        }
    }

    fun activate(): Task<Boolean> {
        Timber.d("RemoteConfig: activate")
        return firebaseRemoteConfig.activate()
    }

    @Suppress("unused")
    fun fetchAndActivateAsync(now: Boolean = false, onFailureBlock: () -> Unit = {}, onSuccessBlock: () -> Unit = {}) {
        Timber.d("RemoteConfig: fetchAndActivateAsync")

        val fetchTask = if (now) {
            // Starts fetching configs, adhering to the specified (0L) minimum fetch interval (fetch NOW)
            firebaseRemoteConfig.fetch(0L)
        } else {
            // Starts fetching configs, adhering to the default minimum fetch interval.
            firebaseRemoteConfig.fetch()
        }

        fetchTask.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    firebaseRemoteConfig.activate()
                    onSuccessBlock()
                }
                else -> {
                    Timber.w("Failed to sync/fetch RemoteConfig")
                    onFailureBlock()
                }
            }
        }
    }

    /**
     * Fetch and Activate synchronously.... if there is a timeout issue, then don't error
     * @param timeoutSeconds How long the fetch should be allowed to take before timeout will occur
     * @return true if the fetch was successful and we could apply the changes; false if there was an error fetching and activating
     */
    fun fetchAndActivateNow(timeoutSeconds: Long = DEFAULT_TIMEOUT_FETCH_SECONDS_SHORT): Boolean {
        Timber.d("RemoteConfig: fetchAndActivateNow")

        // Starts fetching configs, adhering to the specified (0L) minimum fetch interval (fetch NOW)
        val fetchTask = firebaseRemoteConfig.fetch(0L)

        // Await fetch, then activate right away if fetch was successful
        try {
            Tasks.await(fetchTask, timeoutSeconds, TimeUnit.SECONDS)
            if (fetchTask.isSuccessful) {
                firebaseRemoteConfig.activate()
                return true
            }
        } catch (_: TimeoutException) {
            Timber.w("fetchAndActivateNow timeout occurred")
        } catch (expected: Exception) {
            Timber.e(expected,"Failed to FetchAndActivate")
        }

        return false
    }

    @Suppress("unused")
    fun getStatusDetails(): String {
        val info = firebaseRemoteConfig.info
        return "Last Fetch Status: [${getLastFetchStatus()}]  " +
                "Fetch: [${Instant.ofEpochMilli(info.fetchTimeMillis)}]  Min Fetch Interval: [${info.configSettings.minimumFetchIntervalInSeconds}s] " +
                "Fetch Timeout: [${info.configSettings.fetchTimeoutInSeconds}s]"
    }

    fun getLastFetchStatus(): String {
        return when (firebaseRemoteConfig.info.lastFetchStatus) {
            FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS -> "Success"
            FirebaseRemoteConfig.LAST_FETCH_STATUS_FAILURE -> "Failure"
            FirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET -> "No Fetch Yet"
            FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED -> "Throttled"
            else -> "Unknown"
        }
    }

    protected fun getLong(key: String) = firebaseRemoteConfig[key].asLong()
    protected fun getBoolean(key: String) = firebaseRemoteConfig[key].asBoolean()
    protected fun getString(key: String) = firebaseRemoteConfig[key].asString()
    protected fun getDouble(key: String) = firebaseRemoteConfig[key].asDouble()

    companion object {
        const val DEFAULT_TIMEOUT_FETCH_SECONDS_SHORT: Long = 10
        const val DEFAULT_TIMEOUT_FETCH_SECONDS_LONG: Long = 60
    }
}
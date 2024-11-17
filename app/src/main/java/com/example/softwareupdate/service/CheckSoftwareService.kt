package com.example.softwareupdate.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.example.softwareupdate.data.PackageInfoEntity
import com.example.softwareupdate.di.repositories.NotificationRepository
import com.example.softwareupdate.di.usecase.CheckAppUpdateUseCase
import com.example.softwareupdate.utils.AppConstants.DURATION_MILLIS
import com.example.softwareupdate.utils.AppConstants.IF_FIRST_TIME_OPEN_APP
import com.example.softwareupdate.utils.drawableToByteArray
import com.example.softwareupdate.utils.event.UpdateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
class CheckSoftwareService : Service(), CoroutineScope {
    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var checkAppUpdateUseCase: CheckAppUpdateUseCase
    private var updateEvent: UpdateEvent? = null
    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = IO + job
    private val serviceScope = CoroutineScope(IO)
    private var timer: Timer? = null

    companion object {
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        val notificationBuilder = notificationRepository.buildNotification()
        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= 34) {
                ServiceCompat.startForeground(
                    this@CheckSoftwareService,
                    1,
                    notification,
                    FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
                )
            } else {
                startForeground(1, notification)
            }
        } else {
            startForeground(1, notification)
        }
        startTimer()
        isRunning = true
    }

    private fun startTimer() {
        updateEvent = UpdateEvent(
            0,
            currentAppCheckIn = 0,
            totalAppSize = 0,
            isAllAppCheckFinished = false
        )

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                stopSelf()
            }
        }, DURATION_MILLIS)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            checkAppUpdateUseCase.invokeCheckAppUpdateCount(
                callback = { currentAppCheckIn, _, progressValue, totalAppCount, appInfo ->
                    updateEvent?.count = currentAppCheckIn
                    updateEvent?.currentAppCheckIn = progressValue
                    updateEvent?.totalAppSize = totalAppCount
                    updateEvent?.isAllAppCheckFinished = false
                    launch {
                        appInfo.icon?.drawableToByteArray()?.let {
                            PackageInfoEntity(
                                appName = appInfo.appName,
                                pName = appInfo.pName,
                                versionName = appInfo.versionName,
                                versionCode = appInfo.versionCode,
                                iconByteArray = it,
                                installationDate = appInfo.installationDate
                            )
                        }?.let {

                            checkAppUpdateUseCase.insertUpdatedApp(
                                it
                            )
                        }
                    }
                    notificationRepository.updateNotification(currentAppCheckIn)
                    EventBus.getDefault().post(updateEvent)
                },
                callbackFinished = {
                    updateEvent?.isAllAppCheckFinished = true
                    EventBus.getDefault().post(updateEvent)
                    if (it) stopSelf()
                }
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        timer?.cancel()
        job.cancel()
        timer = null
        IF_FIRST_TIME_OPEN_APP = false
        isRunning = false
    }

    override fun onBind(intent: Intent): IBinder? = null
}
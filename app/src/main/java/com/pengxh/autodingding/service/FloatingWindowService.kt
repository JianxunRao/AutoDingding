package com.pengxh.autodingding.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.pengxh.autodingding.R
import com.pengxh.autodingding.extensions.openApplication
import com.pengxh.autodingding.extensions.wakeScreen
import com.pengxh.autodingding.utils.Constant
import com.pengxh.kt.lite.extensions.getSystemService
import com.pengxh.kt.lite.extensions.show
import kotlinx.coroutines.*
import kotlin.time.Duration


class FloatingWindowService : Service() {

    private val kTag = "FloatingWindowService"
    private val windowManager by lazy { getSystemService<WindowManager>() }
    private val layoutInflater by lazy { LayoutInflater.from(this) }
    private var floatView: View? = null
    private lateinit var mFixedTimeJob: Job
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        floatView = layoutInflater.inflate(R.layout.window_floating, null)
        startFixedTimeJob()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (floatView == null) {
            floatView = layoutInflater.inflate(R.layout.window_floating, null)
        }
        initFloatingView(floatView)
        return START_STICKY
    }

    private fun startFixedTimeJob() {
        mFixedTimeJob = coroutineScope.launch {
            while (isActive) {
                delay(Duration.parse("50s"))
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                if ((hour == 8 && minute == 30)
                    || (hour == 12 && minute == 0)
                    || (hour == 2 && minute == 30)
                    || (hour == 5 && minute == 30)
                ) {
                    this@FloatingWindowService.wakeScreen()
                    openApplication(Constant.DING_DING)
                } else {
                    openApplication(Constant.DING_DING)
                    Log.d(kTag, "startFixedTimeJob: not on time: $hour:$minute")
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingView(view: View?) {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_TOAST
        }
        val floatLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager?.addView(view, floatLayoutParams)

            var lastX = 0
            var lastY = 0
            var paramX = 0
            var paramY = 0

            view?.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()
                        paramX = floatLayoutParams.x
                        paramY = floatLayoutParams.y
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX.toInt() - lastX
                        val dy = event.rawY.toInt() - lastY
                        floatLayoutParams.x = paramX + dx
                        floatLayoutParams.y = paramY + dy
                        // 更新悬浮窗位置
                        windowManager?.updateViewLayout(view, floatLayoutParams)
                    }
                }
                false
            }
            view?.setOnClickListener {
//                "无实际功能，仅为绕过Android 10+系统打卡之后无法回到桌面的问题".show(this)
                "Trojx ❤️ Mandy".show(this)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
}
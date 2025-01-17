package com.pengxh.autodingding.utils

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.extensions.createMail
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CountDownTimerManager private constructor() : LifecycleOwner {

    private val kTag = "CountDownTimerManager"
    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    companion object {
        val get by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CountDownTimerManager()
        }
    }

    private var timer: CountDownTimer? = null

    fun startTimer(context: Context, millisInFuture: Long, countDownInterval: Long) {
        Log.d(kTag, "startTimer: 打开钉钉并开始倒计时")
        timer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(kTag, "onTick: ${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
                if (emailAddress.isEmpty()) {
                    "邮箱地址为空".show(context)
                    return
                }

                //如果倒计时结束，那么表明没有收到打卡成功的通知，需要将异常日志保存
                lifecycleScope.launch(Dispatchers.Main) {
                    "未监听到打卡成功通知，即将发送异常日志邮件，请注意查收".show(context)
                    delay(3000)
                    withContext(Dispatchers.IO) {
                        "未监听到打卡成功通知，请手动检查".createMail(emailAddress).sendTextMail()
                    }
                }
            }
        }.start()
    }

    fun cancelTimer() {
        timer?.cancel()
        Log.d(kTag, "cancelTimer: 取消超时定时器")
    }
}
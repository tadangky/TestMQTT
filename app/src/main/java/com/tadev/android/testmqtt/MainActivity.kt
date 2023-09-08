package com.tadev.android.testmqtt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.tadev.android.testmqtt.MyApplication.Companion.serverURI
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var statusTxt: TextView
    private lateinit var messageTxt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTxt = findViewById(R.id.status)
        messageTxt = findViewById(R.id.message)

        val addressTxt = findViewById<TextView>(R.id.address)
        addressTxt.text = serverURI
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ConnectLost?) {
        statusTxt.text = "ConnectLost"
        messageTxt.text = ""
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ConnectSuccess?) {
        statusTxt.text = "ConnectSuccess"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ConnectFailed?) {
        statusTxt.text = "ConnectFailed"
        messageTxt.text = ""
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GotMessage?) {
        messageTxt.text = event?.mess
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SubscribeToTopicsSuccess?) {
        statusTxt.text = "SubscribeToTopicsSuccess"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SubscribeToTopicsFailed?) {
        statusTxt.text = "SubscribeToTopicsFailed"
    }
}
package com.tadev.android.testmqtt

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.widget.Toast
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import timber.log.Timber.DebugTree

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        Timber.plant(DebugTree())

        closeMQTT()
        setupMQTT()

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Timber.e(e)
        }
    }

    companion object {
        @JvmStatic
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set

        @SuppressLint("StaticFieldLeak")
        private var client: MqttAndroidClient? = null

        const val serverURI = "tcp://10.124.71.7:1883"

        fun closeMQTT() {
            try {
                client?.unregisterResources()
                client?.close()
                client?.disconnect()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        fun setupMQTT() {
            val ip = getIP()
            val clientID = ip.replace(".", "")
            Timber.d("MQTT clientID $clientID $serverURI")

            client = MqttAndroidClient(context, serverURI, clientID, MemoryPersistence())
            val options = MqttConnectOptions()
            options.isCleanSession = true
            options.keepAliveInterval = 5
            options.isAutomaticReconnect = true
            options.maxReconnectDelay = 5000

            client?.setCallback(object : MqttCallbackExtended {
                override fun connectionLost(cause: Throwable?) {
                    Timber.d(cause, "MQTT connectionLost")
                    Toast.makeText(context, "MQTT connectionLost", Toast.LENGTH_SHORT).show()
                    EventBus.getDefault().post(ConnectLost())
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Timber.e("MQTT messageArrived $topic, $message")
//                    handleMessage(message)
                    Toast.makeText(context, "MQTT messageArrived $message", Toast.LENGTH_SHORT)
                        .show()
                    EventBus.getDefault().post(GotMessage(message.toString()))
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Timber.d("MQTT deliveryComplete")
                }

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Timber.d("MQTT connectComplete reconnect=$reconnect")
                    if (client?.isConnected == true) {
                        subscribeToTopics()
                    } else {
                        Timber.e("MQTT connectComplete client isConnected = false")
                    }
                }
            })

            try {
                val token = client?.connect(options)
                token?.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Timber.d("MQTT connect onSuccess $serverURI")
                        Toast.makeText(context, "MQTT connect onSuccess", Toast.LENGTH_SHORT).show()
                        EventBus.getDefault().post(ConnectSuccess())
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Timber.e(exception, "MQTT onFailure")
                        Toast.makeText(context, "MQTT connect onFailure", Toast.LENGTH_SHORT).show()
                        EventBus.getDefault().post(ConnectFailed())
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

        }

        private fun subscribeToTopics() {
            val topic = "test"
            val subToken = client?.subscribe(topic, 2)
            subToken?.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Timber.d("MQTT subscribe onSuccess $topic")
                    Toast.makeText(context, "MQTT subscribe onSuccess $topic", Toast.LENGTH_SHORT)
                        .show()
                    EventBus.getDefault().post(SubscribeToTopicsSuccess())
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Timber.e(exception, "MQTT subscribe onFailure")
                    Toast.makeText(context, "MQTT subscribe onFailure $topic", Toast.LENGTH_SHORT)
                        .show()
                    EventBus.getDefault().post(SubscribeToTopicsFailed())
                }
            }
        }

    }
}
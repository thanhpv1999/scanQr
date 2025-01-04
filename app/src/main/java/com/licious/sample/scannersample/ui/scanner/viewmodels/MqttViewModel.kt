package com.licious.sample.scannersample.ui.scanner.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.exceptions.MqttClientStateException
import java.util.concurrent.TimeUnit
import java.nio.charset.StandardCharsets

class MqttViewModel : ViewModel() {
    val TAG = "thanhpv2499"
    private val mqttClient = MqttClient.builder()
        .useMqttVersion5()
        .serverHost("mqtt-dashboard.com") // Thay bằng địa chỉ MQTT broker của bạn
        .serverPort(1883)               // Thay bằng cổng MQTT broker (mặc định 1883)
        .automaticReconnect()
        .initialDelay(1, TimeUnit.SECONDS)
        .maxDelay(10, TimeUnit.SECONDS)
        .applyAutomaticReconnect()
        .addDisconnectedListener { context ->
            Log.d(TAG, "Disconnected: ${context.cause.message}")
            _isConnected.postValue(false)
        }
        .addConnectedListener {
            Log.d(TAG, "Reconnected to MQTT broker.")
            onReconnect()
        }
        .buildAsync()

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> get() = _isConnected
    var idHomeMqtt = ""

    private val _receivedMessage = MutableLiveData<String>()
    val receivedMessage: LiveData<String> get() = _receivedMessage

    @RequiresApi(Build.VERSION_CODES.N)
    fun connect() {
        mqttClient.connect()
            .whenComplete { _, throwable ->
                if (throwable == null) {
                    _isConnected.postValue(true)
                    listenForMessages()
                } else {
                    _isConnected.postValue(false)
                    throwable.printStackTrace()
                }
            }
    }

    private fun onReconnect() {
        Log.d(TAG, "Performing actions after reconnection...")
        _isConnected.postValue(true)
        listenForMessages()
    }

    private fun listenForMessages() {
        mqttClient.publishes(MqttGlobalPublishFilter.ALL) { publish ->
            val topic = publish.topic.toString()
            val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
            if(topic == "${idHomeMqtt}/STATUS"){
                _receivedMessage.postValue("$message")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun subscribe(topic: String) {
        mqttClient.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    throwable.printStackTrace()
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun publish(topic: String, message: String) {
        mqttClient.publishWith()
            .topic(topic)
            .payload(message.toByteArray(StandardCharsets.UTF_8))
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    throwable.printStackTrace()
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun disconnect() {
        mqttClient.disconnect()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    throwable.printStackTrace()
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}

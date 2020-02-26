package com.rnfanbotwebapp.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.rnfanbotwebapp.service.redis.IRedisListener
import com.rnfanbotwebapp.service.redis.RedisBucket

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import org.redisson.Redisson
import org.redisson.api.RScript
import org.redisson.api.RedissonClient
import org.redisson.client.codec.StringCodec
import org.redisson.config.Config
import org.redisson.connection.ConnectionListener

import java.net.InetSocketAddress

class RedisService : Service() {
    private var client: RedissonClient? = null
    private var redisIp = REDIS_IP_DEV


    private var iRedisListener:IRedisListener?=null
    private var keyResultMap:HashMap<String,String?> = HashMap()
    private var errorKeyMap:HashMap<String?,Int> = HashMap()

    override fun onBind(intent: Intent): IBinder? {
        return MyBinder()
    }

    override fun onCreate() {
        super.onCreate()
//        val isBot = resources.getBoolean(R.bool.is_bot)
//        if (isBot) {
//            redisIp = REDIS_IP_FANBOT
//        }
//        redisThread = RedisThread()
//        redisThread?.start()
        redisLaunch()
    }


    @Suppress("UNUSED_PARAMETER")
    private fun <T>getRedisValue(key: String, clazz: Class<T>): T? {
        return try {
            val result = client?.getBucket<T>(key)?.get()
            if (result == null) {
                Log.w(TAG,"redis get failed, key = [$key]")
            } else {
                Log.i(TAG,"redis get success,key = [$key], result = [$result]")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG,"redis get error, key = [$key]", e)
            null
        }

    }

    @Suppress("UNUSED_PARAMETER")
    private fun getReJsonValue(key: String, path: String): String? {
        return try {
            val result = client?.script?.evalAsync<String>(
                RScript.Mode.READ_ONLY,
                "return redis.call('JSON.GET','$key','NOESCAPE','$path')",
                RScript.ReturnType.VALUE
            )?.get()

            if (keyResultMap[path]!=result){
                keyResultMap[path]=result
                Log.i(TAG,"ReJson get success,key = [$key],path = [$path], result = [$result]")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG,"ReJson get error, key = [$key]")
            if (errorKeyMap[key]==null) {
                errorKeyMap.put(key, 1)
                Log.e(TAG,"ReJson get error, key = [$key], value = [$path]", e)
                false
            }
            errorKeyMap[key]?.let {
                if (it%30==0){
                    Log.e(TAG,"ReJson get error, key = [$key], value = [$path]", e)
                }
                errorKeyMap[key] = it+1
            }
            null
        }

    }

    private fun setRedisValue(key: String?, value: Any?): Boolean {
        return try {
            val result = client?.getBucket<Any>(key)?.let {
                it.set(value)
                true
            } ?: false
            if (result) {
                Log.i(TAG,"redis set success, key = [$key], value = [$value]")
            } else {
                Log.w(TAG,"redis set failed, key = [$key], value = [$value]")
            }
            result
        } catch (e: Exception) {
            if (errorKeyMap[key]==null){
                errorKeyMap.put(key,1)
                Log.e(TAG,"redis set error, key = [$key], value = [$value]", e)
                false
            }
            errorKeyMap[key]?.let {
                if (it%30==0){
                    Log.e(TAG,"redis set error, key = [$key], value = [$value]", e)
                }
            }
            false
        }
    }

    private fun setReJsonValue(key: String?, path: String?, value: Any?, isStringValue: Boolean?): Boolean {
        return try {
            val script =
                if (isStringValue == true) "return redis.call('JSON.SET','$key','$path','\"${(value as String).replace("\"", "\\\\\"")}\"')"
                else "return redis.call('JSON.SET','$key','$path','$value')"
            val result = client?.script?.evalAsync<String>(
                RScript.Mode.READ_WRITE,
                script,
                RScript.ReturnType.STATUS
            )?.get()
            if (result?.equals("OK") == true) {
                Log.i(TAG,"ReJson set success, key = [$key],path = [$path], value = [$value], script = [ $script ]")
                true
            } else {
                Log.w(TAG,"ReJson set failed, key = [$key],path = [$path], value = [$value]")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG,"ReJson set error, key = [$key],path = [$path], value = [$value]", e)
            false
        }
    }

    private fun publishTopic(topic: String?, message: String?): Long {
        return try {
            val result = client?.getTopic(topic)?.publish(message)
            if (result == null) {
                Log.w( TAG,"redis publish failed, topic = [$topic], message = [$message]")
                -1
            } else {
                Log.i(
                    TAG,"redis publish success, topic = [$topic], message = [$message], [ $result ] client received it")
                result
            }
        } catch (e: Exception) {
            Log.e(TAG,"redis publish error, topic = [$topic], message = [$message]", e)
            -1
        }
    }

    private fun subscribeTopic(topic: String?): Boolean {
        return try {
            if (topic == null) {
                Log.w(TAG,"subscribe topic [ $topic ] failed, topic is null")
                return false
            }
            val result = client?.getTopic(topic)?.let {
                if (it.countListeners() <= 0) {
                    it.addListener(Any::class.java) { channel, msg -> receivedTopic(channel.toString(), msg) }
                }
                true
            }
            if (result == true) {
                Log.i(TAG,"subscribe topic [ $topic ] success")
                true
            } else {
                Log.e(TAG,"subscribe topic [ $topic ] failed")
                false
            }
        } catch (e: Exception) {
            Log.e( TAG,"subscribe topic [ $topic ] error", e)
            false
        }
    }

    private fun receivedTopic(topic: String?, message: Any?) {
        val redisBucket = RedisBucket()
        redisBucket.topic = topic
        redisBucket.msg = message?.toString()
        iRedisListener?.notify(redisBucket)
    }


    fun setIRedisListener(iRedisListener: IRedisListener){
        this.iRedisListener=iRedisListener
    }

    private fun unsubscribe(topic: String) {
        Log.w(TAG,"unsubscribe topic [$topic]")
        client?.getTopic(topic)?.removeAllListeners()
    }


    fun redisLaunch(){
        GlobalScope.launch {
            val config = Config()
            config.codec = StringCodec()
            config.useSingleServer()
                    .run {
                        address = "redis://$redisIp:$port"
                        connectionPoolSize = 500
                        idleConnectionTimeout = 700
                        connectTimeout = 1000
                        timeout = 2000
                        @Suppress("DEPRECATION")
                        pingTimeout = 3000
                        pingConnectionInterval = 3000
                    }
            var s: Long = 0
            var time: Long = 0
            var total: Long = 0
            var max: Long = Long.MIN_VALUE
            var min: Long = Long.MAX_VALUE

            while (this.isActive) {
                try {
                    time++
                    s = System.currentTimeMillis()
                    client = Redisson.create(config)
                    val end = System.currentTimeMillis()
                    Log.e(TAG, "redis connect success in ${end - s}ms")

                    client?.nodesGroup?.addConnectionListener(object : ConnectionListener {
                        override fun onConnect(addr: InetSocketAddress?) {
                            Log.i(TAG,"redis connect:${addr?.hostName}: ${addr?.port}")
                        }

                        override fun onDisconnect(addr: InetSocketAddress?) {
                            Log.e(TAG,"redis disconnect:${addr?.hostName}: ${addr?.port}")
                        }

                    })
                    break
                } catch (e: Exception) {
                    val end = System.currentTimeMillis()
                    val h = end - s
                    total += h
                    if (max < h) {
                        max = h
                    }
                    if (min > h) {
                        min = h
                    }

                    Log.i(TAG,
                            "redis connect failed, 【 第 $time 次失败 failed，当前耗时： $h ms，平均耗时：${total / time} ms，最大耗时： $max ms，最小耗时： $min ms 】",
                            e
                    )
                }
            }
        }
    }



    inner class MyBinder: Binder() {

        fun pubTopic(topic: String?,message: String?){
            publishTopic(topic,message)
        }

        fun subtopic(topic: String?):Boolean{
           return subscribeTopic(topic)
        }
        fun getRejson(key: String,path: String):String?{
            return getReJsonValue(key,path)
        }
        fun setredisListener(iRedisListener: IRedisListener){
            setIRedisListener(iRedisListener)
        }
    }



    companion object {
        private const val TAG = "RedisService"
        private const val REDIS_IP_FANBOT: String = "192.168.199.38"
        private const val REDIS_IP_DEV: String = "192.168.0.99"
//        private const val REDIS_IP_FANBOT: String = "192.168.0.155"
//        private const val REDIS_IP_DEV: String = "192.168.0.163"
        private const val port: Int = 6379
    }
}

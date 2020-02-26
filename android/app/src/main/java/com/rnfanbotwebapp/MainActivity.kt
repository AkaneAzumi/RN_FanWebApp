package com.rnfanbotwebapp

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.SparseBooleanArray
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

import com.rnfanbotwebapp.fragment.BaseFragment
import com.rnfanbotwebapp.service.RedisService
import com.rnfanbotwebapp.service.redis.IRedisListener
import com.rnfanbotwebapp.service.redis.RedisBucket
import kotlinx.android.synthetic.main.main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(),IRedisListener {
    override fun notify(redisBucket: RedisBucket) {
//        Log.d("TAG","${redisBucket.msg}")
    }

    private var redisBind:RedisService.MyBinder?=null
    private var s:String=""


    private var plugin:HashMap<Int,FrameLayout> = HashMap()


    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        var intent=Intent(this,RedisService::class.java)
        bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE)
        initTimer()
        StatusClass.app=application
        StatusClass.activity=this
    }

    private var serviceConnection=object :ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            redisBind=service as RedisService.MyBinder
            redisBind?.setredisListener(this@MainActivity)
            StatusClass.serviceBinder=redisBind
            SubscribeThread().start()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }

    }


    @SuppressLint("ResourceType")
    private fun initTimer(){
        GlobalScope.launch {
            while (true){

                redisBind?.let {
                    it.getRejson("testplugin",".")?.let {

                        if (it!=s){
                            var g= Gson()
                            var dataBean=g.fromJson(it,DataBean::class.java)
                            if (dataBean.delete!=null){
                                removeView(dataBean.delete)
                            }
                            dataBean.add.plugin?.let {
                                it.forEach {
                                    t:Plugin->
                                    var frameLayout3=FrameLayout(this@MainActivity)
                                    frameLayout3.id=System.currentTimeMillis().toInt()
                                    var layout=FrameLayout.LayoutParams((480*t.mWidth),(270*t.mHeight))
                                    layout.leftMargin=480*t.mLeft
                                    layout.topMargin=270*t.mTop
                                    frameLayout3.layoutParams=layout
                                    GlobalScope.launch (Dispatchers.Main){
                                        test.addView(frameLayout3)
                                        Log.d("TAG","${t.name}")
                                        var baseFragment=BaseFragment(application,t.name)
                                        var fragmentmanager=supportFragmentManager
                                        fragmentmanager.beginTransaction().add(frameLayout3.id,baseFragment).commit()
                                    }

                                    plugin.put(t.id,frameLayout3)
                                }

                            }
                            s=it
                        }

                    }
                }
                delay(1000)
            }
        }
    }

    private fun removeView(deleteList:List<Int>){
        deleteList.forEach {
            GlobalScope.launch(Dispatchers.Main) {
                test.removeView(plugin[it])
            }
        }
    }



    internal inner class SubscribeThread : Thread() {
        override fun run() {
            super.run()
            var resultSub = false
            val topics = arrayOf("main", ".robot.schedule", "sys:voice:asr:current:state", "sys:voice:asr:current:result")
            val result = SparseBooleanArray()
            do {
                if (redisBind != null) {
                    for (i in topics.indices) {
                        if (!result.get(i)) {
                            if (redisBind!!.subtopic(topics[i])) {
                                result.put(i, true)
                                Log.i("TAGSUB", "subscribe ( " + topics[i] + " ) succeed")
                            } else {
                                Log.i("TAGSUB", "subscribe ( " + topics[i] + " ) failed")
                            }
                        }
                    }
                    resultSub = topics.size == result.size() && result.indexOfValue(false) < 0
                }
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            } while (!Thread.interrupted() && !resultSub)
        }
    }

}

package com.rnfanbotwebapp.briage

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.rnfanbotwebapp.MainApplication
import com.rnfanbotwebapp.StatusClass
import com.rnfanbotwebapp.fragment.TestBottomSheetDialog

/**
 *     author : wangli
 *     time   : 2020/02/25
 *     desc   :
 *     version: 1.0
 */
class ReactToAndroid(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {


    override fun getName(): String {
        return  "JsBridgeMoudle"
    }



    @ReactMethod
    fun publishTopic(channl:String,message:String){
        Log.d("TAG","dianjil")
        StatusClass.serviceBinder?.pubTopic(channl,message)
    }


    @ReactMethod
    fun showBottomFragment(model:String){
        var test=TestBottomSheetDialog(StatusClass.app!!,model)
        test.show(StatusClass.activity?.supportFragmentManager,"ceshi")
    }

}
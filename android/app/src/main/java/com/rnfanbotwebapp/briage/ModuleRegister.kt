package com.rnfanbotwebapp.briage



import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager
import java.util.*
import kotlin.collections.ArrayList


/**
 *     author : wangli
 *     time   : 2020/02/25
 *     desc   :
 *     version: 1.0
 */

class ModuleRegister:ReactPackage{


    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        var module=ArrayList<NativeModule>()
        module.add(ReactToAndroid(reactContext))
        return module
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<View, ReactShadowNode<*>>> {
        return emptyList()
    }

}
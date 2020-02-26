package com.rnfanbotwebapp.fragment

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.react.BuildConfig
import com.facebook.react.ReactDelegate
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.shell.MainReactPackage
import com.rnfanbotwebapp.briage.ModuleRegister

/**
 *     author : wangli
 *     time   : 2020/02/26
 *     desc   :
 *     version: 1.0
 */
class TestBottomSheetDialog(app: Application, fileName:String):BaseFullScreenDialogFragment(){

    private var app=app
    private var fileName=fileName
    private lateinit var reactDelegate: ReactDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            reactDelegate=ReactDelegate(it,getReactNativeHost(),fileName,null)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        reactDelegate.loadApp()
        return reactDelegate.reactRootView
    }


    private fun getReactNativeHost(): ReactNativeHost {
        return object : ReactNativeHost(app){
            override fun getPackages(): List<ReactPackage> {
                return  listOf(MainReactPackage(), ModuleRegister())
            }

            override fun getJSBundleFile(): String? {
                return  "/storage/emulated/0/$fileName.bundle"
            }

            override fun getUseDeveloperSupport(): Boolean {
                return BuildConfig.DEBUG
            }

        }
    }
}
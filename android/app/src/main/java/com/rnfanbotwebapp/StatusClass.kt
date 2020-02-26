package com.rnfanbotwebapp

import android.app.Application
import com.rnfanbotwebapp.service.RedisService

/**
 *     author : wangli
 *     time   : 2020/02/25
 *     desc   :
 *     version: 1.0
 */
class StatusClass {
    companion object{
        var serviceBinder:RedisService.MyBinder?=null
        var app:Application?=null
        var activity:MainActivity?=null
    }
}
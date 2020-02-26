package com.rnfanbotwebapp

/**
 *     author : wangli
 *     time   : 2020/02/25
 *     desc   :
 *     version: 1.0
 */
data class DataBean(
    val add: Add,
    val delete: List<Int>
)

data class Add(
    val plugin: List<Plugin>
)

data class Plugin(
        val id:Int,
    val mHeight: Int,
    val mLeft: Int,
    val mTop: Int,
    val mWidth: Int,
    val name: String
)
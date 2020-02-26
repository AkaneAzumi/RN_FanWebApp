package com.rnfanbotwebapp.service.redis

/**
 *     author : wangli
 *     time   : 2020/02/25
 *     desc   :
 *     version: 1.0
 */
interface IRedisListener {
    fun notify(redisBucket: RedisBucket)
}
package com.toywake.data.repository

import com.toywake.data.remote.ApiClient
import com.toywake.data.remote.PlayEndRequest
import com.toywake.data.remote.PlayNextRequest
import com.toywake.data.remote.PlayStartRequest
import com.toywake.data.remote.ScanRequest
import com.toywake.data.remote.ToyCreateRequest
import com.toywake.data.remote.ToyWakeApi

class ToyWakeRepository(private val api: ToyWakeApi) {

    suspend fun health() = api.health()

    suspend fun scan(tagUid: String) = api.scan(ScanRequest(tag_uid = tagUid))

    suspend fun createToy(
        tagUid: String,
        name: String,
        toyType: String,
        trait: String? = null,
        childSetting: String? = null,
    ) = api.createToy(
        ToyCreateRequest(
            tag_uid = tagUid,
            name = name,
            toy_type = toyType,
            trait = trait,
            child_setting = childSetting,
        )
    )

    suspend fun playStart(toyId: Int) = api.playStart(PlayStartRequest(toy_id = toyId))

    suspend fun playNext(sessionId: String, parentContext: String? = null) =
        api.playNext(PlayNextRequest(session_id = sessionId, parent_context = parentContext))

    suspend fun playEnd(sessionId: String, parentContext: String? = null) =
        api.playEnd(PlayEndRequest(session_id = sessionId, parent_context = parentContext))
}

object ToyWakeClient {
    fun repository(baseUrl: String): ToyWakeRepository =
        ToyWakeRepository(ApiClient.create(baseUrl))
}

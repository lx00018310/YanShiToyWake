package com.toywake.data.remote

import kotlinx.serialization.Serializable

// 后端统一前缀 /api/v1；DTO 属性名直接对应后端 snake_case 字段。

@Serializable
data class HealthDto(
    val status: String,
    val database: String,
    val ai_mode: String,
)

@Serializable
data class ScanRequest(val tag_uid: String)

@Serializable
data class ToyBriefDto(
    val id: Int,
    val name: String,
    val toy_type: String,
    val trait: String? = null,
    val child_setting: String? = null,
)

@Serializable
data class ScanResponseDto(
    val status: String,
    val tag_uid: String? = null,
    val toy: ToyBriefDto? = null,
)

@Serializable
data class ToyCreateRequest(
    val tag_uid: String,
    val name: String,
    val toy_type: String,
    val trait: String? = null,
    val child_setting: String? = null,
)

@Serializable
data class ToyCreateResponseDto(val status: String, val toy: ToyDto)

@Serializable
data class ToyDto(
    val id: Int,
    val name: String,
)

@Serializable
data class SparkDto(
    val child_speech: String,
    val parent_hint: String,
    val source: String,
    val memory_used: String? = null,
)

@Serializable
data class PlayStartRequest(val toy_id: Int)

@Serializable
data class PlayStartResponseDto(
    val session_id: String,
    val wake_phrase: String,
    val spark: SparkDto,
)

@Serializable
data class PlayNextRequest(
    val session_id: String,
    val parent_context: String? = null,
)

@Serializable
data class PlayNextResponseDto(val spark: SparkDto)

@Serializable
data class PlayEndRequest(
    val session_id: String,
    val parent_context: String? = null,
)

@Serializable
data class PlayEndResponseDto(
    val ending_speech: String,
    val memory_candidate: String? = null,
)

package cn.lifay.lerverhc.model

import kotlinx.serialization.Serializable

@Serializable
data class Header(var key: String, var value: String)

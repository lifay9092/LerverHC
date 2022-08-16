package model

import cn.hutool.http.ContentType
import cn.hutool.http.Method

data class HttpItemDTO(
    var id: String,
    var parentId: String,
    var addrId: String,
    var name: String,
    var type: String,
    var body: String,
    /* datas */
    var method: Method,
    var url: String,
    var isBatch: Boolean = false,
    var isSync: Boolean = false,
    var contentType: ContentType,
    var authorization: String,
    var batchFileName: String,
    var batchDataFilePath: String,

    )

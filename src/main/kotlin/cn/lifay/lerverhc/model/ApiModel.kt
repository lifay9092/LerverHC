package cn.lifay.lerverhc.model

class ApiModel {

    var swagger: String? = null
    var info: InfoDTO? = null
    var host: String? = null
    var basePath: String? = null
    var tags: List<TagsDTO>? = null
    var paths: LinkedHashMap<String, Map<String, HttpDTO>>? = null
        get
        set

    /*-----------/info------------*/
    class InfoDTO {
        var description: String? = null
        var version: String? = null
        var title: String? = null
        var termsOfService: String? = null
        var contact: ContactDTO? = null
    }

    class ContactDTO {
        var name: String? = null
        var url: String? = null
    }
    /*-----------info/------------*/

    /*-----------/tags------------*/
    class TagsDTO {
        var name: String? = null
        var description: String? = null
    }
    /*-----------tags/------------*/

    /*-----------/paths------------*/
    class HttpDTO {
        var tags: List<String>? = null
        var summary: String? = null
        var description: String? = null
        var operationId: String? = null
        var consumes: List<String>? = null
        var produces: List<String>? = null
        var parameters: List<ParametersDTO>? = null
        var deprecated: Boolean? = null
    }

    class ParametersDTO {
        var name: String? = null
        var `in`: String? = null
        var description: String? = null
        var required: Boolean? = null
        var type: String? = null
        var default: String? = null
    }
    /*-----------paths/------------*/

}

package model

/**
 * @ClassName HttpModel
 * @Description TODO
 * @Author lifay
 * @Date 2022/1/19 11:29
 */
class HttpModel {

    /*目录信息*/
    var info: InfoDTO? = null
    /*元素列表*/
    var item: List<ItemDTO>? = null

    class InfoDTO {
        var postmanId: String? = null
        var name: String? = null
        var schema: String? = null
    }

    class ItemDTO {
        /*名称*/
        var name: String? = null
        /*元素列表*/
        var item: List<ItemDTO>? = null
        /*请求信息*/
        var request: RequestDTO? = null

        class RequestDTO {
            var method: String? = null
            var header: List<HeaderDTO>? = null
            var body: BodyDTO? = null
            var url: UrlDTO? = null

            class BodyDTO {
                /*
                * - raw
                * {\n  \"filterByVisible\": true,\n  \"onlyNode\": false,\n  \"queryType\": \"all\",\n  \"powerType\": \"COMMON\"\n}
                *
                * - urlencoded
                * "key": "dataPath",
							"value": "D:\\hdf5\\GLASS02B05.V04.A1981169.2018062.hdf",
							"type": "text"
                * */
                var mode: String? = null//raw 或 urlencoded:
                var raw: String? = null
                var urlencoded: String? = null
            }

            class UrlDTO {
                var raw: String? = null//http://localhost:8083/spGisManLederNode/manNodeTree
                var protocol: String? = null//http
                var host: List<String>? = null//localhost 或 [127 0 0 1]
                var port: String? = null//8083
                var path: List<String>? = null//[spGisManLederNode,manNodeTree]
            }

            class HeaderDTO {
                var key: String? = null//Content-Type
                var name: String? = null//Content-Type
                var value: String? = null//text
                var type: String? = null//application/json application/x-www-form-urlencoded
            }
        }

    }
}

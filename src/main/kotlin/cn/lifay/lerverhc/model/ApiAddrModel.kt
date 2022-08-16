package cn.lifay.lerverhc.model

data class ApiAddrModel(val name: String, val addr: String) {
    override fun toString(): String {
        return name
    }
}

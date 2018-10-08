package miji.com.feedfit.Model

class RSS() {

    var link: String? = null
    var title: String? = null
    var summary: String? = null
    var id: Int? = null
    var type: Int? = null
    var postDate: Long? = null

    companion object {
        val ARTICLE_TYPE = 0
        val DATE_GROUP_TYPE = 1
    }

    constructor(link: String?, title: String?, summary: String?, id: Int?) : this() {
        this.link = link
        this.title = title
        this.summary = summary
        this.id = id
    }

    constructor(type: Int) : this() {
        this.type = type
    }


}
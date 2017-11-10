package zinus.feh

/**
 * Created by macbookair on 11/9/17.
 */
import org.jsoup.nodes.Element

val header = "https://feheroes.gamepedia.com"

class HeroBean {
    lateinit var pageUrl: String
    lateinit var name: String
    var wpnType: Int = 0
    var mvType: Int = 0
    var id: Int = 0

    var basehp: Int = 0
    var baseatk: Int = 0
    var basespd: Int = 0
    var basedef: Int = 0
    var baseres: Int = 0

    var hpgrowth: Int = 0
    var atkgrowth: Int = 0
    var spdgrowth: Int = 0
    var defgrowth: Int = 0
    var resgrowth: Int = 0

    var minrarity: Int = 0
    var color: Int = 0

    fun initFromHTML(html: Element) {
        val elements = html.getElementsByTag("td")

        val nameTD = elements[1].getElementsByTag("a").first()
        name = nameTD.attr("title")
        pageUrl = header + nameTD.attr("href")

        wpnType = elements[4].attr("data-sort-value").toInt()
        mvType = elements[5].attr("data-sort-value").toInt()

        grabFromPage()
    }

    fun grabFromPage() {
        val htmlRaw = Helper.fetch_url(pageUrl)
    }

    override fun toString(): String {
        return "HeroBean(pageUrl='$pageUrl', name='$name', wpnType=$wpnType, mvType=$mvType, id=$id, basehp=$basehp, baseatk=$baseatk, basespd=$basespd, basedef=$basedef, baseres=$baseres, hpgrowth=$hpgrowth, atkgrowth=$atkgrowth, spdgrowth=$spdgrowth, defgrowth=$defgrowth, resgrowth=$resgrowth, minrarity=$minrarity, color=$color)"
    }

}
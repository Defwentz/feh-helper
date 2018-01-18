package zinus.feh.bean

/**
 * Created by macbookair on 11/9/17.
 */
import android.util.Log
import org.json.JSONObject
import org.jsoup.Jsoup
import zinus.feh.Helper
import java.io.Serializable

val header = "https://feheroes.gamepedia.com"

class HeroBean : Serializable {
    companion object {
        val TABLE_NAME = "Heroes"
        val COL_ID = "id"
        val COL_NAME = "name"
        val COL_PAGE = "pageUrl"
        val COL_WPN = "wpnType"
        val COL_MV = "mvType"
        val COL_MINRAR = "minrarity"
        val COL_COLOR = "color"
        val COL_RELDT = "releaseDate"
        val COL_BHP = "basehp"
        val COL_BATK = "baseatk"
        val COL_BSPD = "basespd"
        val COL_BDEF = "basedef"
        val COL_BRES = "baseres"

        val COL_GHP = "hpgrowth"
        val COL_GATK = "atkgrowth"
        val COL_GSPD = "spdgrowth"
        val COL_GDEF = "defgrowth"
        val COL_GRES = "resgrowth"
    }

    lateinit var pageUrl: String
    lateinit var name: String
    var wpnType: Int = 0
    var mvType: Int = 0
    var id: Long = 0
    var minrarity: Int = 0
    var color: Int = 0
    var releaseDate: Int = 0

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

    fun initFromJSON(_id: Long, json: JSONObject) {
        id = _id
        name = json.getString("title")
        pageUrl = header + "/" + name.replace(" ", "_")
        grabFromPage()
    }

    fun grabFromPage() {

//        wpnType = elements[2].attr("data-sort-value").toInt()
//        color = (wpnType-1) / 3
//        mvType = elements[3].attr("data-sort-value").toInt()
//
//        releaseDate = 0


        val htmlRaw = Helper.fetch_url(pageUrl)
        val htmlContent = Jsoup.parse(htmlRaw).getElementById("mw-content-text")

        val htmlTables = htmlContent.getElementsByTag("table")


        val baseTable = htmlTables[2]
        val growthTable = htmlTables[4]

        val baseTRs = baseTable.getElementsByTag("tr")
        val growthTRs = growthTable.getElementsByTag("tr")

        val baseTDs = baseTRs[baseTRs.size-1].getElementsByTag("td")
        val growthTDs = growthTRs[1].getElementsByTag("td")

        minrarity = 7 - baseTRs.size
        Log.d("abc", name )
        if (baseTDs.size > 1) {
            if (baseTDs[1].text().contains('/')) { // summonable
                for (i in baseTDs.indices) {
                    when (i) {
                        1 -> {
                            basehp = baseTDs[i].text().split("/")[1].toInt()
                        }
                        2 -> baseatk = baseTDs[i].text().split("/")[1].toInt()
                        3 -> basespd = baseTDs[i].text().split("/")[1].toInt()
                        4 -> basedef = baseTDs[i].text().split("/")[1].toInt()
                        5 -> baseres = baseTDs[i].text().split("/")[1].toInt()
                        else -> {

                        }
                    }
                }
            } else { // nonsummonable
                for (i in baseTDs.indices) {
                    when (i) {
                        1 -> {
                            basehp = baseTDs[i].text().toInt()
                        }
                        2 -> baseatk = baseTDs[i].text().toInt()
                        3 -> basespd = baseTDs[i].text().toInt()
                        4 -> basedef = baseTDs[i].text().toInt()
                        5 -> baseres = baseTDs[i].text().toInt()
                        else -> {

                        }
                    }
                }
            }
            for (i in growthTDs.indices) {
                when (i) {
                    1 -> hpgrowth = growthTDs[i].text().toInt()
                    2 -> atkgrowth = growthTDs[i].text().toInt()
                    3 -> spdgrowth = growthTDs[i].text().toInt()
                    4 -> defgrowth = growthTDs[i].text().toInt()
                    5 -> resgrowth = growthTDs[i].text().toInt()
                    else -> {

                    }
                }
            }
        } else { // bruno
            minrarity = 5
        }
    }

    fun initFromDB(columns: Array<Any?>) {
        id = columns[0] as Long
        name = columns[1] as String
        pageUrl = columns[2] as String
        wpnType = (columns[3] as Long).toInt()
        mvType = (columns[4] as Long).toInt()
        minrarity = (columns[5] as Long).toInt()
        color = (columns[6] as Long).toInt()
        releaseDate = (columns[7] as Long).toInt()

        basehp = (columns[8] as Long).toInt()
        baseatk = (columns[9] as Long).toInt()
        basespd = (columns[10] as Long).toInt()
        basedef = (columns[11] as Long).toInt()
        baseres = (columns[12] as Long).toInt()

        hpgrowth = (columns[13] as Long).toInt()
        atkgrowth = (columns[14] as Long).toInt()
        spdgrowth = (columns[15] as Long).toInt()
        defgrowth = (columns[16] as Long).toInt()
        resgrowth = (columns[17] as Long).toInt()
    }

    override fun toString(): String {
        return "HeroBean(pageUrl='$pageUrl', name='$name', wpnType=$wpnType, mvType=$mvType, id=$id, minrarity=$minrarity, color=$color, releaseDate=$releaseDate, basehp=$basehp, baseatk=$baseatk, basespd=$basespd, basedef=$basedef, baseres=$baseres, hpgrowth=$hpgrowth, atkgrowth=$atkgrowth, spdgrowth=$spdgrowth, defgrowth=$defgrowth, resgrowth=$resgrowth)"
    }
}
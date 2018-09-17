package zinus.feh.bean

/**
 * Class for storing hero information like stats.
 *
 * Created by macbookair on 11/9/17.
 */
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import org.json.JSONObject
import org.jsoup.Jsoup
import zinus.feh.Config
import zinus.feh.GameLogic
import zinus.feh.GameLogic.MrgOrder
import zinus.feh.GameLogic.statColToInt
import zinus.feh.Helper
import java.io.Serializable
import java.util.*

class HeroBean : Serializable {
    companion object {
        val TABLE_NAME = "Heroes"
        val COL_ID = "id"
        val COL_NAME = "name"
        val COL_PAGE = "pageUrl"
        val COL_WPN = "wpnType"
        val COL_MV = "mvType"
        val COL_MINRAR = "minrarity"
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

        val COL_PORTRAIT = "portrait"
    }

    lateinit var pageUrl: String
    lateinit var name: String
    var wpnType: Int = 0
    var mvType: Int = 0
    var id: Long = 0
    var minrarity: Int = 0
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

    var portrait: Bitmap? = null

    /**
     * init hero data given their page id and technically just name from query on gamepedia
     *
     * @see grabFromPage
     * @param[_id] their page id on gamepedia
     * @param[json] query result {pageid:, title:,...}
     */
    fun initFromJSON(_id: Long, json: JSONObject) {
        id = _id
        name = json.getString("title")
        pageUrl = Config.encodeUrl(name)
        grabFromPage()
        grabPortraitFromUrl()
    }

    /**
     * grab and parse a gamepedia webpage and init the rest of data of a hero
     *
     * @see initFromJSON
     */
    fun grabFromPage() {

        val htmlRaw = Helper.fetch_url_text(pageUrl)
        val htmlContent = Jsoup.parse(htmlRaw).getElementById("mw-content-text")

        val heroInfoBox = htmlContent.getElementsByClass("hero-infobox")[0]
        var baseTableHead: org.jsoup.nodes.Element? = htmlContent.getElementById("Level_1_Stats")
        if (baseTableHead == null) {
            baseTableHead = htmlContent.getElementById("Level_1_stats")
        }
        val baseTable = baseTableHead!!.parent().nextElementSibling()
        val growthTable = htmlContent.getElementById("Growth_Rates").parent().nextElementSibling().nextElementSibling()


        val imgElements = heroInfoBox.getElementsByTag("img")
        if (imgElements.size < 4) { // doesn't even have the 4 portraits
            return
        }
        var wpnIdx = 4
        for (i in wpnIdx..(imgElements.size-1)) {
            val altStr = imgElements[i].attr("alt").toString()
            if (altStr.equals("★") == false && altStr.contains("Legendary") == false) {
                wpnIdx = i
                break
            }
        }
        val weaponEle = imgElements[wpnIdx]
        val mvEle = imgElements[wpnIdx+1]
        wpnType = Helper.wpnTypeStringToInt(weaponEle.attr("alt").toString())
        mvType = Helper.mvTypeStringToInt(mvEle.attr("alt").toString())
        Log.d("abc", "wpn: " + wpnType + ", mv: " + mvType)
//        releaseDate = 0

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
                    1 -> hpgrowth = growthTDs[i].text().replace("%", "").toInt()
                    2 -> atkgrowth = growthTDs[i].text().replace("%", "").toInt()
                    3 -> spdgrowth = growthTDs[i].text().replace("%", "").toInt()
                    4 -> defgrowth = growthTDs[i].text().replace("%", "").toInt()
                    5 -> resgrowth = growthTDs[i].text().replace("%", "").toInt()
                    else -> {

                    }
                }
            }
        } else {
            minrarity = 5
        }
    }

    fun grabPortraitFromUrl() {
        var fname: String = ""
        if (name.contains("?")) {
            fname = "Masked Knight Face FC.png".replace(" ", "_")
        } else {
            fname = (name + " Face FC.png").replace(":", "").replace("\'", "").replace("\"", "").replace(" ", "_")
        }
        val portUrl = Config.GAMEPEDIA_HEADER + "/File:" + fname
        val htmlRaw = Helper.fetch_url_text(portUrl)
        val portEle = Jsoup.parse(htmlRaw)
        val url = portEle.getElementsByClass("fullMedia")[0].child(0).attr("href")
        portrait = Helper.fetch_url_image(url)
    }

    /**
     * compute lv. 1 stats for all rarity
     *
     * @return example:
     * [["5", "12", "42", "123", "123"],
     *  ["4", ...]
     * ]
     */
    fun getBaseStats(): ArrayList<ArrayList<String>> {

        var ret = ArrayList<ArrayList<String>>()

        // temp holder for stat
        val stat = GameLogic.statMap(get_r5_max_stat())

        // base stat in 5 rarity
        ret.add(arrayListOf("5",
                stat[GameLogic.STAT[0]].toString(),
                stat[GameLogic.STAT[1]].toString(),
                stat[GameLogic.STAT[2]].toString(),
                stat[GameLogic.STAT[3]].toString(),
                stat[GameLogic.STAT[4]].toString()))

        var rar = 4
        while (rar >= minrarity) {

            val order = GameLogic.RarOrder(stat)
            when(rar) {
                4 -> {
                    stat.set(GameLogic.STAT[0], stat[GameLogic.STAT[0]]!! - 1)
                    stat.set(order[0], stat[order[0]]!! - 1)
                    stat.set(order[1], stat[order[1]]!! - 1)
                }
                3 -> {
                    stat.set(order[2], stat[order[2]]!! - 1)
                    stat.set(order[3], stat[order[3]]!! - 1)
                }
                2 -> {
                    stat.set(GameLogic.STAT[0], stat[GameLogic.STAT[0]]!! - 1)
                    stat.set(order[0], stat[order[0]]!! - 1)
                    stat.set(order[1], stat[order[1]]!! - 1)
                }
                1 -> {
                    stat.set(order[2], stat[order[2]]!! - 1)
                    stat.set(order[3], stat[order[3]]!! - 1)
                }
                else -> {

                }
            }

            ret.add(arrayListOf(rar.toString(),
                    stat[GameLogic.STAT[0]].toString(),
                    stat[GameLogic.STAT[1]].toString(),
                    stat[GameLogic.STAT[2]].toString(),
                    stat[GameLogic.STAT[3]].toString(),
                    stat[GameLogic.STAT[4]].toString()))
            rar--
        }

        return ret
    }

    /**
     * get 5* lv. 40 stat
     */
    fun get_r5_max_stat(): List<Int> {
        return listOf(
                basehp,
                baseatk,
                basespd,
                basedef,
                baseres)
    }

    /**
     * compute a particular hero's lv. 40 stat
     *
     * @param[boon] boon in string, example: "atk"
     * @param[bane] same as boon
     * @param[hero] MHeroBean instance, containing rarity and merge info and stuff
     * @param[baseStats] return value from getBaseStats
     * @return example:
     */
    fun getHeroStat(boon: String, bane: String, hero: MHeroBean, baseStats: ArrayList<ArrayList<String>>): MutableMap<String, Int> {

        // get 5* stat with iv modification
        val gvMod = GameLogic.getGVMod(boon, bane)
        val stdBaseStatMap = GameLogic.statMap(get_r5_max_stat())
        for (k in stdBaseStatMap.keys) {    // new base stat considering boon and bane
            stdBaseStatMap[k] = stdBaseStatMap[k]!! + gvMod[statColToInt(k)]
        }

        // apparently this merge order is based on 5* stat with iv modification,
        // not the actual rarity of the hero
        val order = MrgOrder(stdBaseStatMap)

        val baseStat = baseStats[6-hero.rarity]
        val ret = GameLogic.statMap(listOf(
                baseStat[1].toInt(),
                baseStat[2].toInt(),
                baseStat[3].toInt(),
                baseStat[4].toInt(),
                baseStat[5].toInt()))
        // new base stats with boon and bane
        for (k in ret.keys) {
            ret[k] = ret[k]!! + gvMod[statColToInt(k)]
        }

        Log.e("abc", gvMod.toString())
        val growth_val = GameLogic.get_growth_val(hero.rarity, get_mod_growths(gvMod))

        // new max level stats
        for (k in ret.keys) {
            ret[k] = ret[k]!! + growth_val[statColToInt(k)]
        }

        var m = 0
        while(m < hero.merge) {
            when(m) {
                0,5 -> {
                    ret.set(order[0], ret[order[0]]!!+1)
                    ret.set(order[1], ret[order[1]]!!+1)
                }
                1,6 -> {
                    ret.set(order[2], ret[order[2]]!!+1)
                    ret.set(order[3], ret[order[3]]!!+1)
                }
                2,7 -> {
                    ret.set(order[4], ret[order[4]]!!+1)
                    ret.set(order[0], ret[order[0]]!!+1)
                }
                3,8 -> {
                    ret.set(order[1], ret[order[1]]!!+1)
                    ret.set(order[2], ret[order[2]]!!+1)
                }
                4,9 -> {
                    ret.set(order[3], ret[order[3]]!!+1)
                    ret.set(order[4], ret[order[4]]!!+1)
                }
            }
            m++
        }

        return ret
    }

    fun initFromDB(columns: Array<Any?>) {
        id = columns[0] as Long
        name = columns[1] as String
        pageUrl = columns[2] as String
        wpnType = (columns[3] as Long).toInt()
        mvType = (columns[4] as Long).toInt()
        minrarity = (columns[5] as Long).toInt()
        releaseDate = (columns[6] as Long).toInt()

        basehp = (columns[7] as Long).toInt()
        baseatk = (columns[8] as Long).toInt()
        basespd = (columns[9] as Long).toInt()
        basedef = (columns[10] as Long).toInt()
        baseres = (columns[11] as Long).toInt()

        hpgrowth = (columns[12] as Long).toInt()
        atkgrowth = (columns[13] as Long).toInt()
        spdgrowth = (columns[14] as Long).toInt()
        defgrowth = (columns[15] as Long).toInt()
        resgrowth = (columns[16] as Long).toInt()

        portrait = Helper.bytesarray2bmp(columns[17] as ByteArray)
    }

    fun get_growths(): List<Int> {
        return get_mod_growths(listOf(0,0,0,0,0))
    }
    fun get_mod_growths(mod: Int): List<Int> {
        return get_mod_growths(listOf(mod,mod,mod,mod,mod))
    }
    fun get_mod_growths(mod: List<Int>): List<Int> {
        return listOf(hpgrowth + mod[0]*5, atkgrowth + mod[1]*5, spdgrowth + mod[2]*5, defgrowth + mod[3]*5, resgrowth + mod[4]*5)
    }


    override fun toString(): String {
        return "HeroBean(pageUrl='$pageUrl', name='$name', wpnType=$wpnType, mvType=$mvType, id=$id, minrarity=$minrarity, releaseDate=$releaseDate, basehp=$basehp, baseatk=$baseatk, basespd=$basespd, basedef=$basedef, baseres=$baseres, hpgrowth=$hpgrowth, atkgrowth=$atkgrowth, spdgrowth=$spdgrowth, defgrowth=$defgrowth, resgrowth=$resgrowth)"
    }
}
package zinus.feh

import android.util.Log
import org.jetbrains.anko.db.RowParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jsoup.Jsoup
import zinus.feh.bean.HeroBean

/**
 * Created by macbookair on 11/10/17.
 */

object DataOp {
    fun nuUnits(): Boolean {
        return true
    }

    fun clearLocal(database: DBHelper) {
        database.onUpgrade(database.writableDatabase, 0, 1)
    }

    fun fetchHeroNames(heroes: List<HeroBean>): List<String> {
        val names = List<String>(heroes.size,{i -> heroes[i].name})
//        Log.e("abc", names.toString())
        return names
    }

    fun fetchFromGamepedia(updateLocal: (List<HeroBean>) -> Any) {

        var heroes: MutableList<HeroBean> = mutableListOf<HeroBean>()

        val htmlRaw = Helper.fetch_url("https://feheroes.gamepedia.com/Hero_List")
        val htmlContent = Jsoup.parse(htmlRaw).getElementById("bodyContent")
        val htmlTable = htmlContent.getElementsByClass("hero-filter-element")

        Log.d("abc", htmlTable.size.toString() )

        for (i in htmlTable.indices) {

            var hero = HeroBean()
            hero.initFromHTML(i.toLong(), htmlTable[i])
            // Log.d("abc", hero.toString() )
            heroes.add(hero)
        }

        updateLocal(heroes)
    }

    fun fetchFromLocal(database: DBHelper, updateLocal: (List<HeroBean>) -> Any) {
        database.use {
            select(HeroBean.TABLE_NAME).exec {
                val rowParser = object : RowParser<HeroBean> {
                    override fun parseRow(columns: Array<Any?>): HeroBean {
                        var hero = HeroBean()
                        hero.initFromDB(columns)
                        return hero
                    }
                }
                val result = this.parseList(rowParser)
                Log.e("abc", result.size.toString())

                updateLocal(result)
            }
        }
    }

    fun saveToLocal(database: DBHelper, heroes: List<HeroBean>) {
        database.use {
            for (hero in heroes) {
                insert(HeroBean.TABLE_NAME,
                        HeroBean.COL_ID to hero.id,
                        HeroBean.COL_NAME to hero.name,
                        HeroBean.COL_PAGE to hero.pageUrl,
                        HeroBean.COL_WPN to hero.wpnType,
                        HeroBean.COL_MV to hero.mvType,
                        HeroBean.COL_MINRAR to hero.minrarity,
                        HeroBean.COL_COLOR to hero.color,
                        HeroBean.COL_RELDT to hero.releaseDate,

                        HeroBean.COL_BHP to hero.basehp,
                        HeroBean.COL_BATK to hero.baseatk,
                        HeroBean.COL_BSPD to hero.basespd,
                        HeroBean.COL_BDEF to hero.basedef,
                        HeroBean.COL_BRES to hero.baseres,

                        HeroBean.COL_GHP to hero.hpgrowth,
                        HeroBean.COL_GATK to hero.atkgrowth,
                        HeroBean.COL_GSPD to hero.spdgrowth,
                        HeroBean.COL_GDEF to hero.defgrowth,
                        HeroBean.COL_GRES to hero.resgrowth)
            }
        }
    }
}
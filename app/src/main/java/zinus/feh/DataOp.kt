package zinus.feh

import android.content.Context
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import org.jetbrains.anko.db.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jsoup.Jsoup
import zinus.feh.bean.HeroBean
import zinus.feh.bean.MHeroBean



/**
 * Created by macbookair on 11/10/17.
 */

object DataOp {
    var heroes: List<HeroBean>? = null

    fun nuUnits(): Boolean {
        return false
    }

    fun clearLocal(database: DBHelper) {
        database.upgradeTb(database.writableDatabase, HeroBean.TABLE_NAME)
    }

    fun clearNation(database: DBHelper) {
        database.upgradeTb(database.writableDatabase, MHeroBean.TABLE_NAME)
    }

    fun fetchHeroNames(heroes: List<HeroBean>): List<String> {
        val names = List<String>(heroes.size,{i -> heroes[i].name})
//        Log.e("abc", names.toString())
        return names
    }

    fun fetchFromGamepedia(ctxt: Context, updateLocal: (List<HeroBean>) -> Any) {

        var heroes: MutableList<HeroBean> = mutableListOf<HeroBean>()

        val htmlRaw = Helper.fetch_url("https://feheroes.gamepedia.com/Stats_Table")
        val htmlContent = Jsoup.parse(htmlRaw).getElementById("bodyContent")
        val htmlTable = htmlContent.getElementsByClass("hero-filter-element")

        ctxt.runOnUiThread {
            val showMinMax = true
            MaterialDialog.Builder(ctxt)
                    .title(R.string.title_progress)
                    .content(R.string.content_progress)
                    .progress(false, htmlTable.size, showMinMax)
                    .cancelable(false)
                    .showListener {
                        dialogInterface ->
                        val dialog = dialogInterface as MaterialDialog
                        doAsync {
                            for (i in htmlTable.indices) {

                                var hero = HeroBean()
                                hero.initFromHTML(i.toLong(), htmlTable[i])
                                // Log.d("abc", hero.toString() )
                                heroes.add(hero)
                                dialog.incrementProgress(1)
                            }
                            DataOp.heroes = heroes
                            runOnUiThread {
                                dialog.setContent(R.string.done)
                                dialog.dismiss()
                            }
                            updateLocal(heroes)
                        }
                    }
                    .show()
        }
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

                DataOp.heroes = result
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

    fun fetchNationFromLocal(database: DBHelper, updateLocal: (List<MHeroBean>) -> Any) {
        database.use {
            select(MHeroBean.TABLE_NAME).exec {
                val rowParser = object : RowParser<MHeroBean> {
                    override fun parseRow(columns: Array<Any?>): MHeroBean {
                        return MHeroBean.initFromDB(columns)
                    }
                }
                val result = this.parseList(rowParser)

                updateLocal(result)
            }
        }
    }

    fun fetchMyHeroesFromLocal(database: DBHelper, heroName: String, updateLocal: (List<MHeroBean>) -> Any) {
        database.use {
            select(MHeroBean.TABLE_NAME).
                    whereArgs("(${MHeroBean.COL_NAME} = {heroName})",
                    "heroName" to heroName).
                    exec {
                val rowParser = object : RowParser<MHeroBean> {
                    override fun parseRow(columns: Array<Any?>): MHeroBean {
                        return MHeroBean.initFromDB(columns)
                    }
                }
                val result = this.parseList(rowParser)

                updateLocal(result)
            }
        }
    }

    fun rmFromNation(database: DBHelper, id: Long, update: () -> Any) {
        database.use {
            delete(MHeroBean.TABLE_NAME,
                    "${MHeroBean.COL_ID} = {mid}",
                    "mid" to id)
        }
    }
}
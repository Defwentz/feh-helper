package zinus.feh

import android.content.Context
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import org.jetbrains.anko.db.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.json.JSONObject
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

        val retJsonStr = Helper.fetch_url("http://feheroes.gamepedia.com/api.php?action=query&format=json&prop=&list=categorymembers&meta=&titles=&cmtitle=+Category%3A+Heroes&cmlimit=max")
        val retJson = JSONObject(retJsonStr)
        val arrayJson = retJson.getJSONObject("query").getJSONArray("categorymembers")

        for (i in (arrayJson.length() - 1) downTo 0) {
            val json: JSONObject = arrayJson[i] as JSONObject
            if(json.getInt("ns") == 0) {

            } else {
                arrayJson.remove(i)

            }
        }
        ctxt.runOnUiThread {
            val showMinMax = true
            MaterialDialog.Builder(ctxt)
                    .title(R.string.title_progress)
                    .content(R.string.content_progress)
                    .progress(false, arrayJson.length(), showMinMax)
                    .cancelable(false)
                    .showListener {
                        dialogInterface ->
                        val dialog = dialogInterface as MaterialDialog
                        doAsync {
                            for (i in 0..(arrayJson.length() - 1)) {
                                val json: JSONObject = arrayJson[i] as JSONObject
                                var hero = HeroBean()
                                hero.initFromJSON(i.toLong(), json)
                                // Log.d("abc", hero.toString() )
                                heroes.add(hero)
                                dialog.incrementProgress(1)
                            }

                            if(heroes.size != 0) {
                                DataOp.heroes = heroes
                                runOnUiThread {
                                    dialog.setContent(R.string.done)
                                    dialog.dismiss()
                                }
                                updateLocal(heroes)
                            } else {
                                runOnUiThread {
                                    dialog.setContent("something is wrong, fetched nothing")
                                    dialog.dismiss()
                                }
                            }
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
            val result = delete(MHeroBean.TABLE_NAME,
                    "${MHeroBean.COL_ID} = {mid}",
                    "mid" to id) > 0
            if (result) {
                update()
            } else{
                Log.e("abc", "weird")
            }
        }
    }
}
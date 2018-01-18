package zinus.feh.adapter

import android.content.Context
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onFocusChange
import zinus.feh.*
import zinus.feh.GameLogic.GVs
import zinus.feh.GameLogic.MrgOrder
import zinus.feh.GameLogic.RarOrder
import zinus.feh.GameLogic.STAT
import zinus.feh.GameLogic.getGVMod
import zinus.feh.GameLogic.statColToInt
import zinus.feh.GameLogic.statMap
import zinus.feh.bean.HeroBean
import zinus.feh.bean.MHeroBean
import java.util.*


/**
 * Created by macbookair on 11/10/17.
 */

class CheckStatAdapter(contxt: Context, hero: HeroBean?): BaseExpandableListAdapter() {
    val header = arrayOf<String>("???", "base stats", "max stats", "mine")
    val cheader = arrayListOf<String>("rarity", "hp", "atk", "spd", "def", "res")

    var selHero: MHeroBean? = null
    val select: ArrayList<String> = ArrayList<String>()
    val baseStats: ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()
    val maxStats: ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()
    var hero: HeroBean? = hero
    var mHeroes: ArrayList<MHeroBean> = ArrayList<MHeroBean>()

    var nickEt: EditText? = null
    var rarSpinner: Spinner? = null
    var mrgSpinner: Spinner? = null
    var boonSpinner: Spinner? = null
    var baneSpinner: Spinner? = null

    val inflater: LayoutInflater? = contxt.layoutInflater
    val ctxt: Context = contxt
    val raritys: ArrayList<String> = arrayListOf("5")
    val rarAdapter = ArrayAdapter<String>(ctxt,
            android.R.layout.simple_spinner_item,
            raritys)

    var prevRar = 5
    var prevMrg = 0
    var prevBoon = "hp"
    var prevBane = "hp"

    init {
        rarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    fun computeBaseStats() {
        baseStats.add(cheader)

        val stats = statMap(listOf(hero!!.basehp,
                hero!!.baseatk,
                hero!!.basespd,
                hero!!.basedef,
                hero!!.baseres))

        baseStats.add(arrayListOf("5",
                stats[STAT[0]].toString(),
                stats[STAT[1]].toString(),
                stats[STAT[2]].toString(),
                stats[STAT[3]].toString(),
                stats[STAT[4]].toString()))

        var i = 4
        while (i >= hero!!.minrarity) {

            val order = RarOrder(stats)
            Log.e("abc", hero.toString())
            when(i) {
                4 -> {
                    stats.set(STAT[0], stats[STAT[0]]!! - 1)
                    stats.set(order[0], stats[order[0]]!! - 1)
                    stats.set(order[1], stats[order[1]]!! - 1)
                }
                3 -> {
                    stats.set(order[2], stats[order[2]]!! - 1)
                    stats.set(order[3], stats[order[3]]!! - 1)
                }
                2 -> {
                    stats.set(STAT[0], stats[STAT[0]]!! - 1)
                    stats.set(order[0], stats[order[0]]!! - 1)
                    stats.set(order[1], stats[order[1]]!! - 1)
                }
                1 -> {
                    stats.set(order[2], stats[order[2]]!! - 1)
                    stats.set(order[3], stats[order[3]]!! - 1)
                }
                else -> {

                }
            }

            baseStats.add(arrayListOf(i.toString(),
                    stats[STAT[0]].toString(),
                    stats[STAT[1]].toString(),
                    stats[STAT[2]].toString(),
                    stats[STAT[3]].toString(),
                    stats[STAT[4]].toString()))
            i--
        }
    }

    fun computeMaxStats() {
        maxStats.add(cheader)

        var i = 5
        while (i >= hero!!.minrarity) {
            val baseStat = baseStats[6-i].clone() as ArrayList<String>
            val gvs = listOf<Int>(GVs[i-1][hero!!.hpgrowth],
                    GVs[i-1][hero!!.atkgrowth],
                    GVs[i-1][hero!!.spdgrowth],
                    GVs[i-1][hero!!.defgrowth],
                    GVs[i-1][hero!!.resgrowth])
            for (j in baseStat.indices) {
                if(j!=0) {
                    baseStat[j] = (baseStat[j].toInt() + gvs[j-1]).toString()
                }
            }

            if (i == 5) {
                val unchangedbaseStat = baseStats[6-i].clone() as ArrayList<String>

                val baneGvs = listOf<Int>(GameLogic.getGVs(i-1,hero!!.hpgrowth-1),
                        GameLogic.getGVs(i-1,hero!!.atkgrowth-1),
                        GameLogic.getGVs(i-1,hero!!.spdgrowth-1),
                        GameLogic.getGVs(i-1,hero!!.defgrowth-1),
                        GameLogic.getGVs(i-1,hero!!.resgrowth-1))
                val boonGvs = listOf<Int>(GameLogic.getGVs(i-1,hero!!.hpgrowth+1),
                        GameLogic.getGVs(i-1,hero!!.atkgrowth+1),
                        GameLogic.getGVs(i-1,hero!!.spdgrowth+1),
                        GameLogic.getGVs(i-1,hero!!.defgrowth+1),
                        GameLogic.getGVs(i-1,hero!!.resgrowth+1))

                for (j in baseStat.indices) {
                    if(j!=0) {
                        val baneStatInt = (unchangedbaseStat[j].toInt() + baneGvs[j-1]-1)
                        val boonStatInt = (unchangedbaseStat[j].toInt() + boonGvs[j-1]+1)
                        val baseStatInt = baseStat[j].toInt()
                        if(baseStatInt - baneStatInt == 4) {
                            baseStat[j] =  "<font color=#ff0000>$baneStatInt</font><font color=#000000>/${baseStat[j]}/</font>"
                        } else {
                            baseStat[j] = "<font color=#000000>${baneStatInt.toString() + "/" + baseStat[j] + "/"}</font>"
                        }

                        if(boonStatInt - baseStatInt == 4) {
                            baseStat[j] = baseStat[j] + "<font color=#00ff00>$boonStatInt</font>"
                        } else {
                            baseStat[j] = baseStat[j] + "<font color=#000000>$boonStatInt</font>"
                        }
                    }
                }
            }


            maxStats.add(baseStat)
            i--
        }
    }

    fun spinnerSelect(rarSpinner: Spinner,
                      mrgSpinner: Spinner,
                      boonSpinner: Spinner,
                      baneSpinner: Spinner,
                      nick: EditText) {
        val rarStr = rarSpinner.selectedItem as String
        val rar = rarStr.toInt()

        val mrgStr = mrgSpinner.selectedItem as String
        val mrg = mrgStr.toInt()

        val boon = boonSpinner.selectedItem as String

        val bane = baneSpinner.selectedItem as String

        computeSelect(rar, mrg, boon, bane, nick.text.toString())
        this.notifyDataSetChanged()
    }

    fun computeSelect(rar: Int, mrg: Int, boon: String, bane: String, nick: String) {
        if(maxStats.size < 2) {}
        else {
            select.clear()

            selHero = MHeroBean(0,hero!!.name, nick,
                    rar,
                    mrg,
                    Helper.getDateInt(),
                    statColToInt(boon),
                    statColToInt(bane))


            val _baseStat = baseStats[6-rar].clone() as ArrayList<String>

            val baseStat = statMap(listOf(_baseStat[1].toInt(),
                    _baseStat[2].toInt(),
                    _baseStat[3].toInt(),
                    _baseStat[4].toInt(),
                    _baseStat[5].toInt()))
            val gvMod = getGVMod(boon, bane)

            // new base stats with boon and bane
            for (k in baseStat.keys) {
                baseStat[k] = baseStat[k]!! + gvMod[statColToInt(k)]
            }
            val order = MrgOrder(baseStat)

            Log.e("abc", gvMod.toString())
            val gvs = listOf<Int>(GVs[rar-1][hero!!.hpgrowth + gvMod[0]],
                    GVs[rar-1][hero!!.atkgrowth + gvMod[1]],
                    GVs[rar-1][hero!!.spdgrowth + gvMod[2]],
                    GVs[rar-1][hero!!.defgrowth + gvMod[3]],
                    GVs[rar-1][hero!!.resgrowth + gvMod[4]])

            // new max level stats
            for (k in baseStat.keys) {
                baseStat[k] = baseStat[k]!! + gvs[statColToInt(k)]
            }

            var m = 0
            while(m < mrg) {
                when(m) {
                    0,5 -> {
                        baseStat.set(order[0], baseStat[order[0]]!!+1)
                        baseStat.set(order[1], baseStat[order[1]]!!+1)
                    }
                    1,6 -> {
                        baseStat.set(order[2], baseStat[order[2]]!!+1)
                        baseStat.set(order[3], baseStat[order[3]]!!+1)
                    }
                    2,7 -> {
                        baseStat.set(order[4], baseStat[order[4]]!!+1)
                        baseStat.set(order[0], baseStat[order[0]]!!+1)
                    }
                    3,8 -> {
                        baseStat.set(order[1], baseStat[order[1]]!!+1)
                        baseStat.set(order[2], baseStat[order[2]]!!+1)
                    }
                    4,9 -> {
                        baseStat.set(order[3], baseStat[order[3]]!!+1)
                        baseStat.set(order[4], baseStat[order[4]]!!+1)
                    }
                }
                m++
            }
			
			var rarStr = rar.toString()
			if (mrg == 0) {
			
			} else {
                rarStr += "(+" + mrg.toString() + ")"
			}

            select.addAll(listOf(rarStr,
                    baseStat[STAT[0]].toString(),
                    baseStat[STAT[1]].toString(),
                    baseStat[STAT[2]].toString(),
                    baseStat[STAT[3]].toString(),
                    baseStat[STAT[4]].toString()))
        }
    }

    fun clearSelect() {
        prevRar = 5
        prevMrg = 0
        prevBoon = "hp"
        prevBane = "hp"
        rarSpinner!!.setSelection(0)
        mrgSpinner!!.setSelection(0)
        boonSpinner!!.setSelection(0)
        baneSpinner!!.setSelection(0)
        nickEt!!.text.clear()
    }

    fun updateData(hero: HeroBean) {
        this.hero = hero
        header[0] = hero.name

        mHeroes.clear()
        DataOp.fetchMyHeroesFromLocal(ctxt.database, hero.name, { heroes ->
            mHeroes.addAll(heroes)
        })

        baseStats.clear()
        maxStats.clear()
        clearSelect()
        // order can't change
        computeBaseStats()
        computeMaxStats()
        computeSelect(prevRar,prevMrg,prevBoon,prevBane, "")

        var r = 5
        raritys.clear()
        while (r >= hero.minrarity) {
            raritys.add(r.toString())
            r--
        }
        rarAdapter.notifyDataSetChanged()

    }

    override fun getGroup(groupPosition: Int): Any {
        return header[groupPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return header.size
    }

    override fun getGroupView(groupPosition: Int,
                              isExpanded: Boolean,
                              convertView: View?,
                              parent: ViewGroup?): View {
        val header = getGroup(groupPosition)

        val retView: View?
        if (convertView == null) {
            when(groupPosition) {
                0 -> {
                    retView = inflater?.inflate(R.layout.group_select, null)
                }
                else -> {
                    retView = inflater?.inflate(R.layout.group_check_stat, null)
                }
            }
        } else {
            retView = convertView
        }

        val tv = retView!!.findViewById<TextView>(R.id.textView)
        tv!!.setText(header.toString())

        when(groupPosition) {
            0 -> {
                nickEt = retView.findViewById<EditText>(R.id.et_nick)
                nickEt!!.onFocusChange { v, hasFocus ->
                    Log.e("abc", "focus lost")
                }

                rarSpinner = retView.findViewById<Spinner>(R.id.rar_spinner)
                mrgSpinner = retView.findViewById<Spinner>(R.id.mrg_spinner)
                boonSpinner = retView.findViewById<Spinner>(R.id.boon_spinner)
                baneSpinner = retView.findViewById<Spinner>(R.id.bane_spinner)
                rarSpinner!!.setAdapter(rarAdapter)
                rarSpinner!!.setSelection(5-prevRar)

                rarSpinner!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val sel = rarSpinner!!.selectedItem as String
                        if(sel.equals(prevRar.toString())) {

                        } else {
                            spinnerSelect(rarSpinner!!, mrgSpinner!!, boonSpinner!!, baneSpinner!!, nickEt!!)
                            prevRar = sel.toInt()
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        Log.e("abc", "nothign select")
                    }
                }
                mrgSpinner!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val sel = mrgSpinner!!.selectedItem as String
                        if(sel.equals(prevMrg)) {

                        } else {
                            spinnerSelect(rarSpinner!!, mrgSpinner!!, boonSpinner!!, baneSpinner!!, nickEt!!)
                            prevMrg = sel.toInt()
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        Log.e("abc", "nothign select")
                    }
                }
                boonSpinner!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val sel = boonSpinner!!.selectedItem as String
                        if(sel.equals(prevBoon)) {

                        } else {
                            spinnerSelect(rarSpinner!!, mrgSpinner!!, boonSpinner!!, baneSpinner!!, nickEt!!)
                            prevBoon = sel
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        Log.e("abc", "nothign select")
                    }
                }
                baneSpinner!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val sel = baneSpinner!!.selectedItem as String
                        if(sel.equals(prevBane)) {

                        } else {
                            spinnerSelect(rarSpinner!!, mrgSpinner!!, boonSpinner!!, baneSpinner!!, nickEt!!)
                            prevBane = sel
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        Log.e("abc", "nothign select")
                    }
                }

                val saveBtn = retView?.findViewById<ImageView>(R.id.btn_save)
                saveBtn.onClick {

                    if(nickEt!!.text.toString().isNotBlank()) {
                        selHero!!.nickname = nickEt!!.text.toString()
                    } else {

                    }
                    selHero!!.saveIntoDB(ctxt.database)
                    Helper.toaster(ctxt, "saved" +
                        "")

                    clearSelect()

                    mHeroes!!.add(selHero!!)
                    this@CheckStatAdapter.notifyDataSetChanged()
                }

            }
            else -> {

            }
        }

        return retView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        when(groupPosition) {
            0 -> {
                when(childPosition) {
                    0 -> return cheader
                    1 -> return select
                    else -> return 0
                }
            }
            1 -> return baseStats[childPosition]
            2 -> return maxStats[childPosition]
            3 -> return mHeroes[childPosition]
            else -> return 0
        }
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        when(groupPosition) {
            0 -> {
                if (select.isEmpty() || select.size < 6) {
                    return 0
                } else {
                    return 2
                }
            }
            1 -> return baseStats.size
            2 -> return maxStats.size
            3 -> return mHeroes.size
            else -> return 0
        }
    }


    override fun getChildView(groupPosition: Int,
                              childPosition: Int,
                              isLastChild: Boolean,
                              convertView: View?,
                              parent: ViewGroup?): View {
        var stat: ArrayList<String>?
        when (groupPosition) {
            0,1,2 -> {
                stat = getChild(groupPosition, childPosition) as ArrayList<String>
            }
            3 -> {
                stat = ArrayList<String>()
                val mHero = getChild(groupPosition, childPosition) as MHeroBean
                stat.add(mHero.toDes())
            }
            else -> return convertView!!
        }
        var retView: View?
        if (convertView == null) {
            if(groupPosition == 3) {
                retView = inflater!!.inflate(R.layout.item_nation, null)
            } else {
                retView = inflater!!.inflate(R.layout.child_base_stat, null)
            }
            retView.setTag(groupPosition)
        } else {
            if(convertView.tag == groupPosition) {
                retView = convertView
            } else if ((convertView.tag as Int) < 3 && groupPosition < 3) {
                retView = convertView
            } else {
                if(groupPosition == 3) {
                    retView = inflater!!.inflate(R.layout.item_nation, null)
                } else {
                    retView = inflater!!.inflate(R.layout.child_base_stat, null)
                }
                retView.setTag(groupPosition)
            }
        }
        if(groupPosition == 3) {
            val destv = retView!!.findViewById<TextView>(R.id.tv_des)
            val delBtn = retView?.findViewById<Button>(R.id.btn_delete)
            val mHero = getChild(groupPosition, childPosition) as MHeroBean

            retView!!.setOnClickListener {
                ctxt.runOnUiThread { Helper.toaster(ctxt, "what?") }
            }
            destv?.setText(stat[0])
            delBtn?.onClick {
                MaterialDialog.Builder(ctxt)
                        .title(R.string.title_wipe_nation)
                        .content(R.string.summ_wipe_nation)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .onPositive { dialog, which ->
                            doAsync {
                                mHeroes!!.removeAt(childPosition)
                                DataOp.rmFromNation(ctxt.database, mHero.id, {
                                    Helper.toaster(ctxt, mHero.namepls() + " went home.")
                                })
                                ctxt.runOnUiThread { this@CheckStatAdapter.notifyDataSetChanged() }
                            }
                        }
                        .show()
            }
            return retView!!
        }

        val rartv = retView!!.findViewById<TextView>(R.id.txt_rar)
        val hptv = retView!!.findViewById<TextView>(R.id.txt_hp)
        val atktv = retView!!.findViewById<TextView>(R.id.txt_atk)
        val spdtv = retView!!.findViewById<TextView>(R.id.txt_spd)
        val deftv = retView!!.findViewById<TextView>(R.id.txt_def)
        val restv = retView!!.findViewById<TextView>(R.id.txt_res)
        if(groupPosition == 2 && childPosition == 1) {
            rartv!!.setText(Html.fromHtml(stat[0]))
            hptv!!.setText(Html.fromHtml(stat[1]))
            atktv!!.setText(Html.fromHtml(stat[2]))
            spdtv!!.setText(Html.fromHtml(stat[3]))
            deftv!!.setText(Html.fromHtml(stat[4]))
            restv!!.setText(Html.fromHtml(stat[5]))
        } else {
            rartv!!.setText(stat[0])
            hptv!!.setText(stat[1])
            atktv!!.setText(stat[2])
            spdtv!!.setText(stat[3])
            deftv!!.setText(stat[4])
            restv!!.setText(stat[5])
        }
        return retView!!
    }

    override fun hasStableIds(): Boolean {
        return true
    }

}
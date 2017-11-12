package zinus.feh.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk25.coroutines.onClick
import zinus.feh.GameLogic.GVs
import zinus.feh.GameLogic.MrgOrder
import zinus.feh.GameLogic.RarOrder
import zinus.feh.GameLogic.STAT
import zinus.feh.GameLogic.getGVMod
import zinus.feh.GameLogic.statColToInt
import zinus.feh.GameLogic.statMap
import zinus.feh.Helper
import zinus.feh.R
import zinus.feh.bean.HeroBean
import zinus.feh.bean.MHeroBean
import zinus.feh.database
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
            maxStats.add(baseStat)
            i--
        }
    }

    fun spinnerSelect(rarSpinner: Spinner,
                      mrgSpinner: Spinner,
                      boonSpinner: Spinner,
                      baneSpinner: Spinner) {
        val rarStr = rarSpinner.selectedItem as String
        val rar = rarStr.toInt()

        val mrgStr = mrgSpinner.selectedItem as String
        val mrg = mrgStr.toInt()

        val boon = boonSpinner.selectedItem as String

        val bane = baneSpinner.selectedItem as String

        computeSelect(rar, mrg, boon, bane)
        this.notifyDataSetChanged()
    }

    fun computeSelect(rar: Int, mrg: Int, boon: String, bane: String) {
        if(maxStats.size < 2) {}
        else {
            select.clear()
            selHero = MHeroBean(0,hero!!.name,"giraffe",
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

            select.addAll(listOf(rar.toString(),
                    baseStat[STAT[0]].toString(),
                    baseStat[STAT[1]].toString(),
                    baseStat[STAT[2]].toString(),
                    baseStat[STAT[3]].toString(),
                    baseStat[STAT[4]].toString()))
        }
    }

    fun updateData(hero: HeroBean) {
        this.hero = hero
        header[0] = hero.name

        baseStats.clear()
        maxStats.clear()
        // order can't change
        computeBaseStats()
        computeMaxStats()
        computeSelect(prevRar,prevMrg,prevBoon,prevBane)
        
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
                val rarSpinner = retView!!.findViewById<Spinner>(R.id.rar_spinner)
                val mrgSpinner = retView!!.findViewById<Spinner>(R.id.mrg_spinner)
                val boonSpinner = retView!!.findViewById<Spinner>(R.id.boon_spinner)
                val baneSpinner = retView!!.findViewById<Spinner>(R.id.bane_spinner)
                rarSpinner.setAdapter(rarAdapter)
                rarSpinner.setSelection(5-prevRar)

                rarSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val sel = rarSpinner.selectedItem as String
                        if(sel.equals(prevRar.toString())) {

                        } else {
                            spinnerSelect(rarSpinner, mrgSpinner, boonSpinner, baneSpinner)
                            prevRar = sel.toInt()
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        Log.e("abc", "nothign select")
                    }
                }
                mrgSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val sel = mrgSpinner.selectedItem as String
                        if(sel.equals(prevMrg)) {

                        } else {
                            spinnerSelect(rarSpinner, mrgSpinner, boonSpinner, baneSpinner)
                            prevMrg = sel.toInt()
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        Log.e("abc", "nothign select")
                    }
                }
                boonSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val sel = boonSpinner.selectedItem as String
                        if(sel.equals(prevBoon)) {

                        } else {
                            spinnerSelect(rarSpinner, mrgSpinner, boonSpinner, baneSpinner)
                            prevBoon = sel
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        Log.e("abc", "nothign select")
                    }
                }
                baneSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val sel = baneSpinner.selectedItem as String
                        if(sel.equals(prevBane)) {

                        } else {
                            spinnerSelect(rarSpinner, mrgSpinner, boonSpinner, baneSpinner)
                            prevBane = sel
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        Log.e("abc", "nothign select")
                    }
                }

                val saveBtn = retView!!.findViewById<ImageView>(R.id.btn_save)
                saveBtn.onClick {
                    selHero?.saveIntoDB(ctxt.database)
                }

            }
            else -> {

            }
        }

        return retView!!
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
            else -> return 0
        }
    }


    override fun getChildView(groupPosition: Int,
                              childPosition: Int,
                              isLastChild: Boolean,
                              convertView: View?,
                              parent: ViewGroup?): View {
        var stat: ArrayList<String>? = null
        when (groupPosition) {
            0,1,2 -> {
                stat = getChild(groupPosition, childPosition) as ArrayList<String>
            }
            else -> return convertView!!
        }
        var retView: View?
        if (convertView == null) {
            retView = inflater?.inflate(R.layout.child_base_stat, null)
        } else {
            retView = convertView
        }

        val rartv = retView?.findViewById<TextView>(R.id.txt_rar)
        val hptv = retView?.findViewById<TextView>(R.id.txt_hp)
        val atktv = retView?.findViewById<TextView>(R.id.txt_atk)
        val spdtv = retView?.findViewById<TextView>(R.id.txt_spd)
        val deftv = retView?.findViewById<TextView>(R.id.txt_def)
        val restv = retView?.findViewById<TextView>(R.id.txt_res)
        rartv!!.setText(stat[0])
        hptv!!.setText(stat[1])
        atktv!!.setText(stat[2])
        spdtv!!.setText(stat[3])
        deftv!!.setText(stat[4])
        restv!!.setText(stat[5])
        return retView!!
    }

    override fun hasStableIds(): Boolean {
        return true
    }

}
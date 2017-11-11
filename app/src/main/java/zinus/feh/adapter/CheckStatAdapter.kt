package zinus.feh.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import org.jetbrains.anko.layoutInflater
import zinus.feh.GameLogic.RarOrder
import zinus.feh.GameLogic.STAT
import zinus.feh.R
import zinus.feh.bean.HeroBean
import java.util.*


/**
 * Created by macbookair on 11/10/17.
 */

class CheckStatAdapter(contxt: Context, hero: HeroBean?): BaseExpandableListAdapter() {
    val header = arrayOf<String>("???", "base stats", "max stats", "mine")
    val baseStats: ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()
    val maxStats: ArrayList<ArrayList<String>> = ArrayList<ArrayList<String>>()
    var hero: HeroBean? = hero
    val inflater: LayoutInflater? = contxt.layoutInflater

    fun computeBaseStats() {
        baseStats.add(arrayListOf("rarity", "hp", "atk", "spd", "def", "res"))
        val stats = mutableMapOf<String, Int>(
                STAT[0] to hero!!.basehp,
                STAT[1] to hero!!.baseatk,
                STAT[2] to hero!!.basespd,
                STAT[3] to hero!!.basedef,
                STAT[4] to hero!!.baseres)
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

    fun updateData(hero: HeroBean) {
        this.hero = hero
        header[0] = hero.name

        baseStats.clear()
        maxStats.clear()
        computeBaseStats()
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

        var retView: View?
        if (convertView == null) {
            retView = inflater?.inflate(R.layout.group_check_stat, null)
        } else {
            retView = convertView
        }

        val tv = retView?.findViewById<TextView>(R.id.textView)
        tv?.setText(header.toString())

        return retView!!
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        when(groupPosition) {
            1 -> return baseStats[childPosition]
            else -> return 0
        }
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        when(groupPosition) {
            1 -> return baseStats.size
            else -> return 0
        }
    }


    override fun getChildView(groupPosition: Int,
                              childPosition: Int,
                              isLastChild: Boolean,
                              convertView: View?,
                              parent: ViewGroup?): View {
        when (groupPosition) {
            1 -> {
                val stat = getChild(groupPosition, childPosition) as ArrayList<String>
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
            else -> return convertView!!
        }
    }

    override fun hasStableIds(): Boolean {
        return true
    }

}
package zinus.feh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.sdk25.coroutines.onClick
import zinus.feh.DataOp
import zinus.feh.Helper
import zinus.feh.R
import zinus.feh.bean.MHeroBean
import zinus.feh.database
import java.util.*

/**
 * Created by macbookair on 11/12/17.
 */

class NationAdapter(contxt: Context) : BaseAdapter() {

    val heroes = arrayListOf<MHeroBean>()
    val inflater: LayoutInflater? = contxt.layoutInflater
    val ctxt: Context = contxt

    fun clear() {
        heroes.clear()
    }

    fun addAll(_heroes: List<MHeroBean>?) {
        heroes.addAll(_heroes!!)
    }

    override fun getCount(): Int {
        return heroes.size
    }

    override fun getItem(p0: Int): Any {
        return heroes[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val hero = getItem(position) as MHeroBean

        var retView : View?
        if(convertView == null) {
            retView = inflater?.inflate(R.layout.item_nation, null)
        } else {
            retView = convertView
        }

        val rarAdapter = getRarAdapter(hero.name)
        if(rarAdapter == null) {

        } else {
            val infoLayout = retView!!.findViewById<LinearLayout>(R.id.ly_info)

            infoLayout.onClick {
                Helper.initEditHero(ctxt, hero, rarAdapter, { mhero, dialog ->
                    DataOp.updateToNation(ctxt.database, hero,
                            {
                                Helper.toaster(ctxt, hero.namepls() + "'s record updated.")
                                ctxt.runOnUiThread { this@NationAdapter.notifyDataSetChanged() }
                            })
                })
            }
        }

        Helper.setupHeroInfo(hero, retView!!)

        val delBtn = retView!!.findViewById<Button>(R.id.btn_delete)
        delBtn!!.onClick {
            Helper.initDelHero(ctxt, { dialog, which ->
                DataOp.rmFromNation(ctxt.database, hero, {
                    heroes!!.removeAt(position)
                    Helper.toaster(ctxt, hero.namepls() + " went home.")
                    ctxt.runOnUiThread { this@NationAdapter.notifyDataSetChanged() }
                })
            })
        }
        return retView!!
    }

    fun getRarAdapter(name: String): ArrayAdapter<String>?  {
        val hero = DataOp.getHeroData(name)

        if(hero == null) {
            return null
        } else {
            val raritys: ArrayList<String> = arrayListOf("5")
            var r = 5
            raritys.clear()
            while (r >= hero.minrarity) {
                raritys.add(r.toString())
                r--
            }

            val rarAdapter = ArrayAdapter<String>(ctxt,
                    android.R.layout.simple_spinner_item,
                    raritys)
            rarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            return rarAdapter
        }
    }
}
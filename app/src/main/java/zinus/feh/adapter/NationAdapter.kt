package zinus.feh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.sdk25.coroutines.onClick
import zinus.feh.DataOp
import zinus.feh.Helper
import zinus.feh.R
import zinus.feh.bean.MHeroBean
import zinus.feh.database

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

        val tv = retView?.findViewById<TextView>(R.id.tv_des)
        val delBtn = retView?.findViewById<Button>(R.id.btn_delete)

        tv?.setText(hero.toDes())
        delBtn?.onClick {
            MaterialDialog.Builder(ctxt)
                    .title(R.string.title_wipe_nation)
                    .content(R.string.summ_wipe_nation)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onPositive { dialog, which ->
                        doAsync {
                            heroes!!.removeAt(position)
                            DataOp.rmFromNation(ctxt.database, hero.id, {
                                Helper.toaster(ctxt, hero.namepls() + " went home.")
                                ctxt.runOnUiThread { this@NationAdapter.notifyDataSetChanged() }
                            })
                        }
                    }
                    .show()
        }
        return retView!!
    }
}
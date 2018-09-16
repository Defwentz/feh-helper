package zinus.feh

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.daimajia.swipe.SwipeLayout
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import zinus.feh.bean.MHeroBean
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by macbookair on 11/10/17.
 */
object Helper {
    fun isNetworkConnected(contxt: Context): Boolean {
        val connectivityManager = contxt.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun fetch_url(url: String): String {
        return URL(url).readText()
    }

    val df: DateFormat = SimpleDateFormat("yyyyMMdd") as DateFormat

    fun getDateInt(): Int {
        val today = Calendar.getInstance().time
        return df.format(today).toInt()
    }

    fun toaster(ctxt: Context, msg: String) {
        ctxt.runOnUiThread { toast(msg) }
    }

    fun setupHeroInfo(hero: MHeroBean, view: View) {
        val nametv = view.findViewById<TextView>(R.id.tv_name)
        val bbtv = view.findViewById<TextView>(R.id.tv_bb)

        nametv.text = hero.toNameTV() + "  [" + hero.toRarMrgTV() + "]"
        bbtv.text = hero.toBBTV()

        val swipeLayout = view.findViewById<SwipeLayout>(R.id.ly_swipe)
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut)
    }

    fun initDelHero(ctxt: Context,
                    onPositive: (MaterialDialog, DialogAction) -> Any ) {
        MaterialDialog.Builder(ctxt)
                .title(R.string.title_send_home)
                .content(R.string.summ_send_home)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive { dialog, which ->
                    onPositive(dialog, which)
                }
                .show()
    }

    fun initEditHero(ctxt: Context,
                     hero: MHeroBean,
                     rarAdapter: ArrayAdapter<String>,
                     update: (MHeroBean, MaterialDialog) -> Any) {
        Log.e("abc", "init edit")
        //Helper.toaster(ctxt, "what?" + hero.id + ";" + childPosition)
        val dialog = MaterialDialog.Builder(ctxt)
                .title(R.string.title_edit_hero)
                .customView(R.layout.group_select, false)
                .positiveText(R.string.confirm)
                .onPositive { dialog, which ->
                    updateToUI(hero, dialog, update)
                }
                .build()
        val view = dialog.customView!!
        val nkEt = view.findViewById<EditText>(R.id.et_nick)
        val rarSpnr = view.findViewById<Spinner>(R.id.rar_spinner)
        val mrgSpnr = view.findViewById<Spinner>(R.id.mrg_spinner)
        val boonSpnr = view.findViewById<Spinner>(R.id.boon_spinner)
        val baneSpnr = view.findViewById<Spinner>(R.id.bane_spinner)
        val saveBtn = view.findViewById<ImageView>(R.id.btn_save)
        val nameTv  = view.findViewById<TextView>(R.id.textView)

        saveBtn.visibility = View.INVISIBLE
        nkEt.setText(hero.nickname)

        rarSpnr.setAdapter(rarAdapter)
        rarSpnr.setSelection(5 - hero.rarity)
        mrgSpnr.setSelection(hero.merge)
        boonSpnr.setSelection(hero.boon)
        baneSpnr.setSelection(hero.bane)

        nameTv.setText(hero.namepls())

        dialog.show()
    }

    fun updateToUI(hero: MHeroBean, dialog: MaterialDialog, update: (MHeroBean, MaterialDialog) -> Any) {
        val view = dialog.customView!!
        val nkEt = view.findViewById<EditText>(R.id.et_nick)
        val rarSpnr = view.findViewById<Spinner>(R.id.rar_spinner)
        val mrgSpnr = view.findViewById<Spinner>(R.id.mrg_spinner)
        val boonSpnr = view.findViewById<Spinner>(R.id.boon_spinner)
        val baneSpnr = view.findViewById<Spinner>(R.id.bane_spinner)

        val rarStr = rarSpnr.selectedItem as String
        hero.rarity = rarStr.toInt()

        val mrgStr = mrgSpnr.selectedItem as String
        hero.merge = mrgStr.toInt()

        hero.boon = GameLogic.statColToInt(boonSpnr.selectedItem as String)

        hero.bane = GameLogic.statColToInt(baneSpnr.selectedItem as String)

        hero.nickname = nkEt.text.toString()

        update(hero, dialog)
    }

    val WPNCOLORSTRINGS = arrayOf("Red", "Blue", "Green", "Colorless")
    val WPNTYPESTRINGS = listOf<List<String>>(
            listOf("Sword", "Bow", "Tome", "Breath", "Dagger", "Staff"),
            listOf("Lance", "Bow", "Tome", "Breath", "Dagger", "Staff"),
            listOf("Axe", "Bow", "Tome", "Breath", "Dagger", "Staff"),
            listOf("_", "Bow", "Tome", "Breath", "Dagger", "Staff"))
    fun wpnTypeStringToInt(str: String): Int {
        var ret: Int = 4    // default is red bow

        val strs = str.split(' ')
        if (strs.size != 2) {
            return ret
        }

        for (i in WPNCOLORSTRINGS.indices) {    // determine color
            if (WPNCOLORSTRINGS[i].equals(strs[0])) {
                ret = i
            }
        }

        if (ret == 4) { // if color can't be determined
            return ret
        }

        for (i in WPNTYPESTRINGS[0].indices) {  // all list in WPNTYPESTRINGS has the same size so
            if (WPNTYPESTRINGS[ret][i].equals(strs[1])) {
                return ret + i*4
            }
        }
        return -1
    }

    val MVTYPESTRINGS = arrayOf("Infantry", "Armored", "Cavalry", "Flying")
    fun mvTypeStringToInt(str: String): Int {
        for (i in MVTYPESTRINGS.indices) {
            if (str.contains(MVTYPESTRINGS[i])) {
                return i
            }
        }
        return 0    // default is infantry
    }

    val WPNIMGS = arrayOf(
        R.drawable.icon_class_red_sword,
        R.drawable.icon_class_blue_lance,
        R.drawable.icon_class_green_axe,
        R.drawable.icon_class_colorless_dagger, // colorless _
        R.drawable.icon_class_red_bow,
        R.drawable.icon_class_blue_bow,
        R.drawable.icon_class_green_bow,
        R.drawable.icon_class_colorless_bow,
        R.drawable.icon_class_red_tome,
        R.drawable.icon_class_blue_tome,
        R.drawable.icon_class_green_tome,
        R.drawable.icon_class_colorless_dagger, // colorless tome
        R.drawable.icon_class_red_breath,
        R.drawable.icon_class_blue_breath,
        R.drawable.icon_class_green_breath,
        R.drawable.icon_class_colorless_breath,
        R.drawable.icon_class_red_dagger,
        R.drawable.icon_class_blue_dagger,
        R.drawable.icon_class_green_dagger,
        R.drawable.icon_class_colorless_dagger,
        R.drawable.icon_class_colorless_staff,  // red staff
        R.drawable.icon_class_colorless_staff,  // blue staff
        R.drawable.icon_class_colorless_staff,  // green staff
        R.drawable.icon_class_colorless_staff)
    val MVIMGS = arrayOf(
        R.drawable.icon_move_infantry,
        R.drawable.icon_move_armored,
        R.drawable.icon_move_cavalry,
        R.drawable.icon_move_flying)
}
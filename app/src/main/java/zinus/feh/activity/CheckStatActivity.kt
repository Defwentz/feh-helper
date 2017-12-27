package zinus.feh.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ExpandableListView
import android.widget.TextView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import zinus.feh.DataOp
import zinus.feh.DataOp.clearLocal
import zinus.feh.DataOp.fetchFromGamepedia
import zinus.feh.DataOp.fetchFromLocal
import zinus.feh.DataOp.fetchHeroNames
import zinus.feh.DataOp.heroes
import zinus.feh.DataOp.nuUnits
import zinus.feh.DataOp.saveToLocal
import zinus.feh.Helper
import zinus.feh.R
import zinus.feh.adapter.CheckStatAdapter
import zinus.feh.bean.HeroBean
import zinus.feh.database


class CheckStatActivity : AppCompatActivity() {

    companion object {
        val REQ_DB = 1
        val KEY_H = "heroes"
    }
    var autoCmpAdapter : ArrayAdapter<String>? = null
    var searchTxtView : AutoCompleteTextView? = null

    var contentAdapter: CheckStatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_stat)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)

        initMenuSearch()

        val expListView = findViewById<ExpandableListView>(R.id.main_content)
        expListView.setGroupIndicator(null)
        contentAdapter = CheckStatAdapter(this, null)
        expListView.setAdapter(contentAdapter)
        for (i in contentAdapter?.header!!.indices) {
            expListView.expandGroup(i)
        }
        expListView.setOnGroupClickListener(object: ExpandableListView.OnGroupClickListener {
            override fun onGroupClick(p0: ExpandableListView?, p1: View?, p2: Int, p3: Long): Boolean {
                return true
            }
        })
        database.onCreate(database.writableDatabase)

        doAsync {
            if (Helper.isNetworkConnected(this@CheckStatActivity) && nuUnits()) {
                fetchFromGamepedia(this@CheckStatActivity, { heroes ->
                    clearLocal(database)
                    saveToLocal(database, heroes)
                    updateAutoCmp(heroes)
                })
            } else {
                fetchFromLocal(database, { heroes ->
                    updateAutoCmp(heroes)
                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_check_stat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id) {
            R.id.action_settings -> {
                val intent: Intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, REQ_DB)
            }
            R.id.action_nation -> {
                val intent: Intent = Intent(this, NationActivity::class.java)
                startActivity(intent)
            }
            R.id.action_clear -> {
                searchTxtView?.setText("")
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchTxtView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQ_DB -> {
                doAsync {
                    updateAutoCmp(DataOp.heroes!!)
                }
            }
            else -> {

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun initMenuSearch() {
        val action = supportActionBar //get the actionbar
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        action!!.setDisplayShowCustomEnabled(true) //enable it to display a
        // custom view in the action bar.
        action.setCustomView(R.layout.single_autocomplete)//add the custom view
        action.setDisplayShowTitleEnabled(false) //hide the title

        searchTxtView = action.customView.findViewById<AutoCompleteTextView>(R.id.txtview_search)
        autoCmpAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                arrayListOf<String>())
        searchTxtView?.setAdapter<ArrayAdapter<String>>(autoCmpAdapter)
        searchTxtView?.setOnItemClickListener { adapterView, view, i, l ->
            val v = view as TextView
            toast(v.text)
            imm.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
            initContent(v.text.toString())
        }

        searchTxtView?.requestFocus()

        //open the keyboard focused in the edtSearch
        imm.showSoftInput(searchTxtView, InputMethodManager.SHOW_IMPLICIT)
    }

    fun initContent(name: String) {
        for(hero in heroes!!) {
            if(name.equals(hero.name)) {
                contentAdapter?.updateData(hero)
                contentAdapter?.notifyDataSetChanged()
            }
        }
    }

    fun updateAutoCmp(heroes: List<HeroBean>) {
        autoCmpAdapter?.clear()
        autoCmpAdapter?.addAll(fetchHeroNames(heroes))
        autoCmpAdapter?.notifyDataSetChanged()
    }
}

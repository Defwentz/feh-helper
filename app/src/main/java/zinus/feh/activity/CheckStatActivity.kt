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
import zinus.feh.DataOp.clearLocal
import zinus.feh.DataOp.fetchFromGamepedia
import zinus.feh.DataOp.fetchFromLocal
import zinus.feh.DataOp.fetchHeroNames
import zinus.feh.DataOp.nuUnits
import zinus.feh.DataOp.saveToLocal
import zinus.feh.Helper
import zinus.feh.R
import zinus.feh.adapter.CheckStatAdapter
import zinus.feh.bean.HeroBean
import zinus.feh.database


class CheckStatActivity : AppCompatActivity() {

    var heroes: List<HeroBean>? = null
    var names:  MutableList<String> = mutableListOf<String>()
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
                fetchFromGamepedia({ heroes ->
                    runOnUiThread { toast("fetch from Internet complete.") }
                    this@CheckStatActivity.heroes = heroes
                    clearLocal(database)
                    saveToLocal(database, heroes)
                    updateAutoCmp(heroes)
                })
            } else {
                fetchFromLocal(database, { heroes ->
                    runOnUiThread { toast("fetch from local db complete.") }
                    this@CheckStatActivity.heroes = heroes
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
            R.id.action_settings -> 0
            R.id.action_nation -> {
                val intent: Intent = Intent(this, NationActivity::class.java)
                startActivity(intent)
            }
            R.id.action_clear -> {
                searchTxtView?.setText("")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun initMenuSearch() {
        val action = supportActionBar //get the actionbar
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        action!!.setDisplayShowCustomEnabled(true) //enable it to display a
        // custom view in the action bar.
        action.setCustomView(R.layout.single_autocomplete)//add the custom view
        action.setDisplayShowTitleEnabled(false) //hide the title

        searchTxtView = action.customView.findViewById<AutoCompleteTextView>(R.id.txtview_search)
        autoCmpAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, names)
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
        names.clear()
        names.addAll(fetchHeroNames(heroes))
        autoCmpAdapter?.notifyDataSetChanged()
    }
}

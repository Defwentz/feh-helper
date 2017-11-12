package zinus.feh.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import org.jetbrains.anko.doAsync
import zinus.feh.DataOp
import zinus.feh.R
import zinus.feh.bean.MHeroBean
import zinus.feh.database


class NationActivity : AppCompatActivity() {
    var heroes: List<MHeroBean>? = null
    var items:  MutableList<String> = mutableListOf<String>()
    var nationAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nation)

        val listView = findViewById<ListView>(R.id.main_content)
        nationAdapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                items)
        listView.adapter = nationAdapter

        doAsync {
            DataOp.fetchNationFromLocal(database, { heroes ->
                this@NationActivity.heroes = heroes
                updateList(heroes)
            })
        }
    }

    fun fetchItems(heroes: List<MHeroBean>): ArrayList<String> {
        val ret = arrayListOf<String>()
        for(i in heroes.indices) {
            Log.e("abc", heroes[i].toDes())
            ret.add(heroes[i].toDes())
        }
        return ret
    }

    fun updateList(heroes: List<MHeroBean>) {
        items.clear()
        items.addAll(fetchItems(heroes))
        nationAdapter?.notifyDataSetChanged()
    }

}

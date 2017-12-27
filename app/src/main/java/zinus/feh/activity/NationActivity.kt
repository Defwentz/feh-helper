package zinus.feh.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import org.jetbrains.anko.doAsync
import zinus.feh.DataOp
import zinus.feh.R
import zinus.feh.adapter.NationAdapter
import zinus.feh.bean.MHeroBean
import zinus.feh.database


class NationActivity : AppCompatActivity() {
    var heroes: List<MHeroBean>? = null
    var nationAdapter: NationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nation)

        val listView = findViewById<ListView>(R.id.main_content)
        nationAdapter = NationAdapter(this)
        listView.adapter = nationAdapter

        doAsync {
            DataOp.fetchNationFromLocal(database, { heroes ->
                this@NationActivity.heroes = heroes
                updateList(heroes)
            })
        }
    }

    fun updateList(heroes: List<MHeroBean>) {
        nationAdapter?.clear()
        nationAdapter?.addAll(heroes)
        nationAdapter?.notifyDataSetChanged()
    }

}

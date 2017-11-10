package zinus.feh

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.AutoCompleteTextView
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup

class CheckStatActivity : AppCompatActivity() {

    var heroes: MutableList<HeroBean> = mutableListOf<HeroBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_stat)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)

        var searchTxtView = toolbar.findViewById<AutoCompleteTextView>(R.id.txtview_search)

        doAsync {
            if (Helper.isNetworkConnected(this@CheckStatActivity)) {
                fetchFromGamepedia()
            }
        }
    }

    fun check4NuUnits() {

    }

    fun fetchFromGamepedia() {
        val htmlRaw = Helper.fetch_url("https://feheroes.gamepedia.com/Hero_List")
        val htmlContent = Jsoup.parse(htmlRaw).getElementById("bodyContent")
        val htmlTable = htmlContent.getElementsByClass("hero-filter-element")

        Log.d("abc", htmlTable.size.toString() )

        for (htmlItem in htmlTable) {
            var hero = HeroBean()
            hero.initFromHTML(htmlItem)
            Log.d("abc", hero.toString() )
            heroes.add(hero)
        }
    }
}

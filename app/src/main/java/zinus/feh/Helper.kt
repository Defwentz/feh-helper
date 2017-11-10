package zinus.feh

import android.content.Context
import android.net.ConnectivityManager
import java.net.URL

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
}
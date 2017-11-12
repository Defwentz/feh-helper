package zinus.feh

import android.content.Context
import android.net.ConnectivityManager
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


    val df: DateFormat = SimpleDateFormat("yyyyMMdd")

    fun getDateInt(): Int {
        val today = Calendar.getInstance().time
        return df.format(today).toInt()
    }
}
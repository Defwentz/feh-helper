package zinus.feh

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.jetbrains.anko.db.*
import zinus.feh.bean.HeroBean
import zinus.feh.bean.MHeroBean

/**
 * Created by macbookair on 11/10/17.
 */
class DBHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "HeroesDB", null, 1) {
    companion object {
        private var instance: DBHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): DBHelper {
            //Log.e("abc", "getinstance")
            if (instance == null) {
                instance = DBHelper(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.e("abc", "oncreate")
        // Here you create tables
        db.createTable(HeroBean.TABLE_NAME, true,
                HeroBean.COL_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                HeroBean.COL_NAME to TEXT,
                HeroBean.COL_PAGE to TEXT,
                HeroBean.COL_WPN to INTEGER,
                HeroBean.COL_MV to INTEGER,
                HeroBean.COL_MINRAR to INTEGER,
                HeroBean.COL_COLOR to INTEGER,
                HeroBean.COL_RELDT to INTEGER,

                HeroBean.COL_BHP to INTEGER,
                HeroBean.COL_BATK to INTEGER,
                HeroBean.COL_BSPD to INTEGER,
                HeroBean.COL_BDEF to INTEGER,
                HeroBean.COL_BRES to INTEGER,

                HeroBean.COL_GHP to INTEGER,
                HeroBean.COL_GATK to INTEGER,
                HeroBean.COL_GSPD to INTEGER,
                HeroBean.COL_GDEF to INTEGER,
                HeroBean.COL_GRES to INTEGER)

        db.createTable(MHeroBean.TABLE_NAME, true,
                MHeroBean.COL_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                MHeroBean.COL_NAME to TEXT,
                MHeroBean.COL_NICK to TEXT,
                MHeroBean.COL_RAR to INTEGER,
                MHeroBean.COL_MRG to INTEGER,
                MHeroBean.COL_DATE to INTEGER,
                MHeroBean.COL_BOON to INTEGER,
                MHeroBean.COL_BANE to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        // Here you can upgrade tables, as usual
//        db.dropTable(HeroBean.TABLE_NAME, true)
//        // db.dropTable(MHeroBean.TABLE_NAME, true)
//        onCreate(db)
    }

    fun upgradeTb(db: SQLiteDatabase, table_name: String) {
        db.dropTable(table_name, true)
        onCreate(db)
    }
}

// Access property for Context
val Context.database: DBHelper
    get() = DBHelper.getInstance(getApplicationContext())
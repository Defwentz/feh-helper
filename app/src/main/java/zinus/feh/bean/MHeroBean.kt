package zinus.feh.bean

import org.jetbrains.anko.db.insert
import zinus.feh.DBHelper
import zinus.feh.GameLogic

/**
 * Created by macbookair on 11/10/17.
 */

class MHeroBean(id:Long, name:String, nick:String, rar:Int, mrg:Int, date:Int, boon:Int, bane:Int) {
    companion object {
        val TABLE_NAME = "MyHeroes"
        val COL_ID = "id"
        val COL_NAME = "name"
        val COL_NICK = "nickname"
        val COL_RAR = "rarity"
        val COL_MRG = "merge"
        val COL_DATE = "inputdate"

        val COL_BOON = "boon"
        val COL_BANE = "bane"

        fun initFromDB(columns: Array<Any?>): MHeroBean {
            val id = columns[0] as Long
            val name = columns[1] as String
            val nick = columns[2] as String
            val rar = (columns[3] as Long).toInt()
            val mrg = (columns[4] as Long).toInt()
            val date = (columns[5] as Long).toInt()
            val boon = (columns[6] as Long).toInt()
            val bane = (columns[7] as Long).toInt()
            return MHeroBean(id, name, nick, rar, mrg, date, boon, bane)
        }
    }

    var name: String = name
    var nickname: String = nick
    var id: Long = id
    var rarity: Int = rar
    var merge: Int = mrg
    var inputDate: Int = date

    var boon: Int = boon
    var bane: Int = bane

    override fun toString(): String {
        return "MHeroBean(name='$name', nickname='$nickname', id=$id, rarity=$rarity, merge=$merge, inputDate=$inputDate, boon=$boon, bane=$bane)"
    }

    fun namepls(): String {
        if(nickname.isNotBlank()) {
            return nickname
        } else {
            return name
        }
    }

    fun toDes(): String {
        var nickStr: String
        if(nickname.isNotBlank()) {
            nickStr = " aka $nickname"
        } else {
            nickStr = ""
        }

        var mrgStr: String
        if(merge == 0) {
            mrgStr = ""
        } else {
            mrgStr = "(+$merge)"
        }

        var bbStr: String
        if(boon == bane) {
            bbStr = "netural"
        } else {
            bbStr = "+${GameLogic.STAT[boon]}/-${GameLogic.STAT[bane]}"
        }
        return "$name$nickStr, $rarity*$mrgStr, $bbStr"
    }

    fun saveIntoDB(database: DBHelper, ret: (MHeroBean) -> Any) {
        database.use {
            this@MHeroBean.id = insert(TABLE_NAME,
                    COL_NAME to name,
                    COL_NICK to nickname,
                    COL_RAR to rarity,
                    COL_MRG to merge,
                    COL_DATE to inputDate,
                    COL_BOON to boon,
                    COL_BANE to bane)
            ret(this@MHeroBean)
        }
    }
}

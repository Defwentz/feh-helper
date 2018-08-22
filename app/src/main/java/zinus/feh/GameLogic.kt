package zinus.feh

/**
 * Class for game logic related computation.
 *
 * Created by macbookair on 11/10/17.
 */
object GameLogic {
    val GVs = listOf<List<Int>>(listOf(6,8,9,11,13,14,16,18,19,21,23,24,26),
                                listOf(7,8,10,12,14,15,17,19,21,23,25,26,28),
                                listOf(7,9,11,13,15,17,19,21,23,25,27,29,31,33),
                                listOf(8,10,12,14,16,18,20,22,24,26,28,31,33,35),
                                listOf(8,10,13,15,17,19,22,24,26,28,30,33,35,37))

    fun getGVs(rar: Int, grw: Int): Int {
        if(grw >= GVs[rar].size) {
            return GVs[rar][GVs[rar].size-1]
        }
        if(grw < 0) {
            return GVs[rar][0]
        }
        return return GVs[rar][grw]
    }
    val STAT = arrayListOf("hp", "atk", "spd", "def", "res")
    fun statColToInt(col: String): Int {
        for(i in STAT.indices) {
            if(col.equals(STAT[i])) {
                return i
            }
        }
        return 0
    }

    // convert stat to a map with header and stat
    fun statMap(stat: List<Int>): MutableMap<String, Int> {
        return mutableMapOf<String, Int>(
                STAT[0] to stat[0],
                STAT[1] to stat[1],
                STAT[2] to stat[2],
                STAT[3] to stat[3],
                STAT[4] to stat[4])
    }

    fun getGVMod(boon: String, bane: String): ArrayList<Int> {
        val boonI = statColToInt(boon)
        val baneI = statColToInt(bane)
        val GVMod = arrayListOf<Int>(0,0,0,0,0)
        GVMod[boonI]++
        GVMod[baneI]--
        return GVMod
    }

    fun getTotalGVMod(isBoon: Boolean): ArrayList<Int> {
        val GVMod = arrayListOf<Int>(0,0,0,0,0)
        return GVMod
    }

    val boostPriority = mutableMapOf<String, Int>(
            STAT[0] to 4,
            STAT[1] to 3,
            STAT[2] to 2,
            STAT[3] to 1,
            STAT[4] to 0)

    fun RarOrder(stats: MutableMap<String, Int>): ArrayList<String> {
        val rarityBaseOrder = arrayListOf<String>(STAT[1], STAT[2], STAT[3], STAT[4])

        rarityBaseOrder.sortWith(Comparator { x, y ->
            val basex = stats[x]!!
            val basey = stats[y]!!
            val prix = boostPriority[x]!!
            val priy = boostPriority[y]!!
            if(basex > basey) {
                1
            } else if (basex < basey) {
                -1
            } else {
                if (prix > priy) {
                    1
                } else {
                    -1
                }
            }
        })

        return rarityBaseOrder
    }

    fun MrgOrder(stats: MutableMap<String, Int>): ArrayList<String> {
        val mrgBaseOrder = arrayListOf<String>(STAT[0], STAT[1], STAT[2], STAT[3], STAT[4])

        mrgBaseOrder.sortWith(Comparator { x, y ->
            val basex = stats[x]!!
            val basey = stats[y]!!
            val prix = boostPriority[x]!!
            val priy = boostPriority[y]!!
            if(basex > basey) {
                -1
            } else if (basex < basey) {
                1
            } else {
                if (prix > priy) {
                    -1
                } else {
                    1
                }
            }
        })

        return mrgBaseOrder
    }
}
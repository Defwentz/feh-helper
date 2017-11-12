package zinus.feh

/**
 * Created by macbookair on 11/10/17.
 */
object GameLogic {
    val GVs = listOf<List<Int>>(listOf(6,8,9,11,13,14,16,18,19,21,23,24),
                                listOf(7,8,10,12,14,15,17,19,21,23,25,26),
                                listOf(7,9,11,13,15,17,19,21,23,25,27,29),
                                listOf(8,10,12,14,16,18,20,22,24,26,28,31),
                                listOf(8,10,13,15,17,19,22,24,26,28,30,33,35))

    val STAT = arrayListOf("hp", "atk", "spd", "def", "res")
    fun statColToInt(col: String): Int {
        for(i in STAT.indices) {
            if(col.equals(STAT[i])) {
                return i
            }
        }
        return 0
    }
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
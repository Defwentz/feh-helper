package zinus.feh

/**
 * Created by wxzhu on 2018/9/1.
 */
object Config {
    val GAMEPEDIA_HEADER = "https://feheroes.gamepedia.com"
    val GAMEPEDIA_HEROES_REQ_URL = GAMEPEDIA_HEADER + "/api.php?action=query&format=json&prop=&list=categorymembers&meta=&titles=&cmtitle=+Category%3A+Heroes&cmlimit=max"
    val GAMEPEDIA_PORT_REQ_URL = GAMEPEDIA_HEADER + "/api.php?action=query&format=json&list=categorymembers&cmtitle=Category%3AIcon+Portrait+files&cmlimit=max"
    /**
     * turn hero name into their page url
     *
     * @return example: https://feheroes.gamepedia.com/%3F%3F%3F:_Masked_Knight
     */
    fun encodeUrl(title: String): String {
        return Config.GAMEPEDIA_HEADER + "/" + title.replace(" ", "_").replace("?", "%3F")
    }
}
import java.lang.IllegalArgumentException
import java.util.*

val osuEditorTitleFormat: Regex = Regex("^osu! {2}- ([^-]*) - ([^\\(]*) \\(([^\\)]*)\\) \\[(.*)\\]\\.osu$")
val osuTitleFormat: Regex = Regex("^osu! {2}- ([^-]*) - ([^\\[]*) \\[(.*)\\]\\$")

class OsuTitle(osuTitle: String) {
    private val artistName: String?
    private val songName: String?
    private val diffName: String?

    private val mapperName: String?
    private val isInEditor: Boolean

    init {
        if (!osuTitle.startsWith("osu!"))
            throw IllegalArgumentException("osu! title \"$osuTitle\" did not match expected format!")

        val editorMatch: MatchResult? = osuEditorTitleFormat.matchEntire(osuTitle)
        if (editorMatch != null) {
            artistName = editorMatch.groupValues[1]
            songName   = editorMatch.groupValues[2]
            mapperName = editorMatch.groupValues[3]
            diffName   = editorMatch.groupValues[4]
            isInEditor = true
        }
        else {
            val playingMatch: MatchResult? = osuTitleFormat.matchEntire(osuTitle)
            if (playingMatch != null) {
                artistName = playingMatch.groupValues[1]
                songName   = playingMatch.groupValues[2]
                diffName   = playingMatch.groupValues[3]
                mapperName = null
                isInEditor = false
            }
            else {
                artistName = "none"
                songName   = "none"
                diffName   = "none"
                mapperName = null
                isInEditor = false
            }
        }
    }

    fun isPracticeDiff(map: OsuTitle): Boolean {
        return this.artistName.equals(map.artistName)
                && this.songName.equals(map.songName)
                && !this.diffName.equals(map.diffName)
    }

    override fun toString(): String {
        if (artistName == "none" && diffName == "none")
            return "no map open"
        return "$artistName - $songName [$diffName]" +
                if (mapperName != null) ", mapset by $mapperName" else {""} +
                if (isInEditor) " -- editing" else ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OsuTitle) return false

        return Objects.equals(this.artistName, other.artistName)
                && Objects.equals(this.songName, other.songName)
                && Objects.equals(this.diffName, other.diffName)
                && Objects.equals(this.mapperName, other.mapperName)
    }

    override fun hashCode(): Int {
        var result = artistName?.hashCode() ?: 0
        result = 31 * result + (songName?.hashCode() ?: 0)
        result = 31 * result + (diffName?.hashCode() ?: 0)
        result = 31 * result + (mapperName?.hashCode() ?: 0)
        result = 31 * result + isInEditor.hashCode()
        return result
    }

}
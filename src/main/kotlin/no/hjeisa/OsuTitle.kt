package no.hjeisa

import com.sun.org.apache.xpath.internal.operations.Bool
import java.lang.IllegalArgumentException
import java.util.*

// group 1: version number (' ' for stable, heh)
val osuEditorTitleFormat: Regex = Regex("^osu!([^-]*) - ([^-]*) - ([^\\(]*) \\(([^\\)]*)\\) \\[(.*)\\]\\.osu$")
val osuTitleFormat: Regex = Regex("^osu!([^-]*) - ([^-]*) - ([^\\[]*) \\[(.*)\\]$")

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
            artistName = editorMatch.groupValues[2]
            songName   = editorMatch.groupValues[3]
            mapperName = editorMatch.groupValues[4]
            diffName   = editorMatch.groupValues[5]
            isInEditor = true
        }
        else {
            val playingMatch: MatchResult? = osuTitleFormat.matchEntire(osuTitle)
            if (playingMatch != null) {
                artistName = playingMatch.groupValues[2]
                songName   = playingMatch.groupValues[3]
                diffName   = playingMatch.groupValues[4]
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

    fun isPracticeDiffOf(map: OsuTitle?): Boolean {
        if (map == null) return false;
        return this.artistName.equals(map.artistName)
                && this.songName.equals(map.songName)
                && !this.diffName.equals(map.diffName)
    }

    /**
     * also returns true if the player is editing a practice difficulty of the currently attempted map
     */
    fun isEditing(attemptedMap: OsuTitle?): Boolean {
        return if (attemptedMap == null) isInEditor
            else isInEditor && (this == attemptedMap || isPracticeDiffOf(attemptedMap))
    }

    override fun toString(): String {
        if (artistName == "none" && diffName == "none")
            return "No map open"
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
    }

    override fun hashCode(): Int {
        var result = artistName?.hashCode() ?: 0
        result = 31 * result + (songName?.hashCode() ?: 0)
        result = 31 * result + (diffName?.hashCode() ?: 0)
        result = 31 * result + (mapperName?.hashCode() ?: 0)
        result = 31 * result + isInEditor.hashCode()
        return result
    }

    // static fields for equality check
    companion object {
        val idle = OsuTitle("osu!")
    }
}

package no.hjeisa

import java.io.*

const val UPDATE_PERIOD_MS = 1000L

class OsuMonitorTask(val statusFileDirectoryPath: String) : Runnable {
    // one file per status allows a different visual configuration per status in OBS
    val idleStatusFile: File = File("$statusFileDirectoryPath/idle.txt")
    val practiceStatusFile: File = File("$statusFileDirectoryPath/practice.txt")
    val attemptsStatusFile: File = File("$statusFileDirectoryPath/attempts.txt")
    val studyingStatusFile: File = File("$statusFileDirectoryPath/studying.txt")
    val fcStatusFile: File = File("$statusFileDirectoryPath/fc.txt")

    var currentStatusWriter: FileWriter = FileWriter(idleStatusFile)

    var previousFile: File? = null
    var previousOsuTitle: OsuTitle? = null

    var attemptedMap: OsuTitle? = null

    fun getOsuTitle(): String? {
        return try {
            previousOsuTitle?.toString() ?: fetchOsuTitle()?.toString()
        } catch (e: Exception) {
            "error given: $e.message"
        }
    }

    fun updateAttemptedMap() {
        attemptedMap = fetchOsuTitle()
    }

    override fun run() {
        try {
            // task can be force closed by command input thread
            while (true) {
                val osuTitle = fetchOsuTitle()
                if (osuTitle != previousOsuTitle) {
                    previousOsuTitle = osuTitle
                    writeToFile(idleStatusFile, osuTitle.toString())
                }

                // TODO: FIND THE DELAY FUNCTION REEEEEE
                // (UPDATE_PERIOD_MS)
            }
        }
        catch (nsee: NoSuchElementException) {
            println("osu! closed - shutting down. Input anything to save configuration and exit...")
        }
        catch (e: Exception) {
            println("Unhandled exception occurred.")
            e.printStackTrace()
        }
        finally {
            currentStatusWriter.close()
        }
    }

    // overwrites text in file
    private fun writeToFile(file: File, content: String) {
        if (previousFile != null) {
            currentStatusWriter = FileWriter(previousFile!!)
            currentStatusWriter.use {}
        }
        currentStatusWriter = FileWriter(file)
        currentStatusWriter.use {
            it.write(content)
        }
    }

    fun fetchOsuTitle(): OsuTitle? {
        if (knownOsuPid < 0) {
            val process = Runtime.getRuntime().exec("tasklist /FI \"imagename eq osu!.exe\" /FO list /V")
            val pidString = readTasklist(process, "PID:")
                ?: throw NoSuchElementException("osu! has not been started.")

            knownOsuPid = Integer.parseInt(pidString.trim())
        }

        val process = Runtime.getRuntime().exec("tasklist /FI \"pid eq $knownOsuPid\" /FO list /V")
        val titleString = readTasklist(process, "Window Title: ")

        return OsuTitle(titleString!!)
    }

    fun readTasklist(process: Process, prefix: String): String? {
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var result: String? = null
        reader.forEachLine {
            if (result == null && it.startsWith(prefix))
                result = it.substring(prefix.length)
        }
        reader.close()
        return result
    }
}

fun main() {
    val nowPlaying = OsuMonitorTask(".").fetchOsuTitle()
    println(nowPlaying)
}
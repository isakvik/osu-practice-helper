package no.hjeisa

import java.io.*

const val UPDATE_PERIOD_MS = 1000L

class OsuMonitorTask(statusFileDirectoryPath: String): Runnable {
    // one file per status allows a different visual configuration per status in OBS
    val idleStatusFile: File = File("$statusFileDirectoryPath/idle.txt")
    val practiceStatusFile: File = File("$statusFileDirectoryPath/practice.txt")
    val attemptsStatusFile: File = File("$statusFileDirectoryPath/attempts.txt")
    val studyingStatusFile: File = File("$statusFileDirectoryPath/studying.txt")
    val otherStatusFile: File = File("$statusFileDirectoryPath/other.txt")
    val fcStatusFile: File = File("$statusFileDirectoryPath/fc.txt")
    val nowPlayingFile: File = File("$statusFileDirectoryPath/now_playing.txt")

    var currentStatusWriter: FileWriter = FileWriter(idleStatusFile)

    var previousFile: File? = null
    var previousOsuTitle: OsuTitle? = null

    var attemptedMap: OsuTitle? = null

    init {
        writeToFile(practiceStatusFile, "")
        writeToFile(attemptsStatusFile, "")
        writeToFile(studyingStatusFile, "")
        writeToFile(otherStatusFile, "")
        writeToFile(fcStatusFile, "")
        // write stuff to idle while we wait for map selection
        writeToFile(idleStatusFile, "...")
        writeToFile(nowPlayingFile, "...")
    }

    fun getOsuTitle(): String? {
        return try {
            previousOsuTitle?.toString() ?: fetchOsuTitle()?.toString()
        } catch (e: Exception) {
            "error given: $e.message"
        }
    }

    fun updateAttemptedMap() {
        attemptedMap = fetchOsuTitle()
        previousOsuTitle = null
    }

    override fun run() {
        try {
            // task can be force closed by command input thread
            while (!Thread.interrupted()) {
                try {
                    if (attemptedMap != null) {
                        val osuTitle = fetchOsuTitle()
                        if (osuTitle != previousOsuTitle) {
                            previousOsuTitle = osuTitle
                            runFileWriteLoop(osuTitle)
                        }
                    }
                    // always write current playing map to this file
                    writeToFile(nowPlayingFile, getOsuTitle())
                }
                catch (re: RuntimeException) {
                    println("\nunhandled exception occurred.")
                    re.printStackTrace()
                }
                Thread.sleep(UPDATE_PERIOD_MS)
            }
        }
        catch (nsee: NoSuchElementException) {
            print("\nosu! closed; ")
        }
        catch (ie: InterruptedException) {
            print("\npractice helper closed; ")
        }
        catch (e: Exception) {
            println("\nunhandled exception occurred; ")
            e.printStackTrace()
        }
        finally {
            println("shutting down polling thread.")
            currentStatusWriter.close()
        }
    }

    private fun runFileWriteLoop(osuTitle: OsuTitle?) {
        when {
            osuTitle == null -> return
            osuTitle == OsuTitle.idle -> {
                // TODO: check fc status
                writeToFile(idleStatusFile, config.idleStatusMessage)
            }
            osuTitle.isEditing(attemptedMap) -> {
                writeToFile(studyingStatusFile, config.studyingStatusMessage)
            }
            osuTitle.isPracticeDiffOf(attemptedMap) -> {
                writeToFile(practiceStatusFile, config.practicingStatusMessage)
            }
            osuTitle == attemptedMap -> {
                writeToFile(attemptsStatusFile, config.attemptingStatusMessage)
            }
            else -> {
                writeToFile(otherStatusFile, config.otherStatusMessage)
            }
        }
    }

    // overwrites text in file
    private fun writeToFile(file: File, content: String?) {
        if (content == null) return

        if (previousFile != null) {
            currentStatusWriter = FileWriter(previousFile!!)
            currentStatusWriter.write("")
        }
        previousFile = file

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
            ?: throw NoSuchElementException("osu! has not been started.")

        return OsuTitle(titleString)
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

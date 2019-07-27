package no.hjeisa

import java.io.File
import java.io.PrintWriter

const val CONFIG_PATH = "./osuPracticeConf.txt"

class Configuration {

    private val configMap = HashMap<String, String>()

    var lastPathUsed: String? = null
    var logFilePath: String? = null // UNUSED

    var idleStatusMessage: String? = null
    var practicingStatusMessage: String? = null
    var attemptingStatusMessage: String? = null
    var studyingStatusMessage: String? = null
    var otherStatusMessage: String? = null
    var fcStatusMessage: String? = null

    init {
        loadConfig()
    }

    fun loadConfig(configPath: String = CONFIG_PATH) {
        val configFile = File(configPath)
        if (configFile.createNewFile())
            println("Created configuration file at $CONFIG_PATH.")
        else
            println("Loading configuration from $CONFIG_PATH.")

        configFile.forEachLine {
            val configLine = it.split(Regex(" ?= ?"), 2)
            if (configLine.size == 2)
                configMap[configLine[0]] = configLine[1]
        }

        lastPathUsed = configMap["lastPathUsed"]
        logFilePath = configMap["logFilePath"]

        idleStatusMessage       = configMap["idleStatusMessage"]       ?: "Idle"
        practicingStatusMessage = configMap["practicingStatusMessage"] ?: "Practicing"
        attemptingStatusMessage = configMap["attemptingStatusMessage"] ?: "Doing runs"
        studyingStatusMessage   = configMap["studyingStatusMessage"]   ?: "Studying"
        otherStatusMessage      = configMap["otherStatusMessage"]      ?: "Playing something else"
        fcStatusMessage         = configMap["fcStatusMessage"]         ?: "check reddit"
    }

    fun saveConfig() {
        val configFile = File(CONFIG_PATH)
        val out = PrintWriter(configFile)

        for (config in configMap) {
            out.write("${config.key}=${config.value}\n")
        }
        println("Configuration saved to $CONFIG_PATH.")
        out.close()
    }
}
package no.hjeisa

import java.io.File
import java.io.PrintWriter

const val CONFIG_PATH = "./osuPracticeConf.txt"

class Configuration() {
    var lastPathUsed: String? = null

    init {
        loadConfig()
    }

    fun loadConfig(configPath: String = CONFIG_PATH) {
        val configFile = File(configPath)
        if (configFile.createNewFile())
            println("Created configuration file at $CONFIG_PATH.")
        else
            println("Loading configuration from $CONFIG_PATH.")

        val configMap = HashMap<String, String>()
        configFile.forEachLine {
            val configLine = it.split(Regex("="), 2)
            if (configLine.size == 2)
                configMap[configLine[0]] = configLine[1];
        }

        lastPathUsed = configMap["lastPathUsed"]
    }

    fun saveConfig() {
        val configFile = File(CONFIG_PATH)
        val out = PrintWriter(configFile)

        out.write("lastPathUsed=$lastPathUsed")
        println("Configuration saved to $CONFIG_PATH.")
        out.close()
    }
}
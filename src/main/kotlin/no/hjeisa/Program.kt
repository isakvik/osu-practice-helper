package no.hjeisa

import java.util.*
import java.util.concurrent.Executors
import javax.swing.JFileChooser

val config = Configuration()
var knownOsuPid: Int = -1

val executor = Executors.newFixedThreadPool(1)
var monitorTask: OsuMonitorTask? = null


fun main() {
    config.loadConfig()
    run()
    config.saveConfig()
}

fun run() {
    // use property as default
    val defaultDirectory = config.lastPathUsed ?: System.getProperty("user.home")
    val filePicker = JFileChooser(defaultDirectory)

    filePicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    filePicker.setDialogTitle("Choose directory to write status files into...")
    filePicker.setAcceptAllFileFilterUsed(false)
    val fpVal = filePicker.showOpenDialog(null)
    if (fpVal != JFileChooser.APPROVE_OPTION) {
        println("No directory chosen, exiting.")
        return
    }

    config.lastPathUsed = filePicker.selectedFile.absolutePath
    config.saveConfig()

    // start polling task
    monitorTask = OsuMonitorTask(filePicker.selectedFile.absolutePath)
    executor.submit(monitorTask!!)
    executor.shutdown()

    // console input loop
    var exit = false
    val systemIn = Scanner(System.`in`)
    println("osu! practice helper ready. Type '?' for help.")
    while (!exit) {
        exit = runInputLoop(systemIn)
    }
}

val commandHelp = "?"
val commandSet = "set"
val commandNowAttempting = "nat"
val commandNowPlaying = "np"
val commandOwo = "owo"
val commandExit = mapOf("exit" to "exit", "quit" to "quit", ":q" to ":q")

fun runInputLoop(scanner: Scanner): Boolean {
    print("> ")
    if (scanner.hasNext()) {
        when (val next = scanner.nextLine()) {
            "set" -> {
                monitorTask?.updateAttemptedMap()
                println("set map: " + monitorTask?.attemptedMap?.toString())
            }
            "nat" -> {
                println(monitorTask?.attemptedMap?.toString() ?: "no map set.")
            }
            "np" -> {
                println(monitorTask?.getOsuTitle() ?: "couldn't fetch osu! title...")
            }
            "owo" -> {
                println("what's this?")
            }
            "?" -> {
                println("""Commands:
                    | - $commandHelp: displays this text.
                    | - $commandSet: picks the map you're grinding. Shows text in attempts.txt when you're playing the chosen map.
                    | - $commandNowAttempting: displays currently attempted map.
                    | - $commandNowPlaying: displays currently playing map title.
                    | - $commandOwo: secret command.
                    | - ${commandExit.values.first()}: quits program, and shuts down monitor thread.""".trimMargin())
            }
            commandExit[next] -> {
                executor.shutdownNow()
                return true
            }
            else -> {
                println("Invalid command. Type '$commandHelp' to see list of avaliable commands.")
            }
        }
    }
    if (executor.isTerminated) {
        println("\npolling thread closed - exiting.")
        return true
    }
    return false
}
package no.hjeisa

import java.util.*
import java.util.concurrent.Executors
import javax.swing.JFileChooser
import kotlin.system.exitProcess

val config = Configuration()
var knownOsuPid: Int = -1

val executor = Executors.newFixedThreadPool(1)
var task: OsuMonitorTask? = null

fun main() {
    // use property as default
    val defaultDirectory = config.lastPathUsed ?: System.getProperty("user.home")

    val filePicker = JFileChooser(defaultDirectory)
    filePicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    filePicker.setDialogTitle("Choose directory to write status files into...");
    filePicker.setAcceptAllFileFilterUsed(false);
    val fpVal = filePicker.showOpenDialog(null)
    if (fpVal != JFileChooser.APPROVE_OPTION) {
        println("No directory chosen, exiting.")
        exitProcess(0)
    }

    config.lastPathUsed = filePicker.selectedFile.absolutePath
    config.saveConfig()

    // start polling task
    task = OsuMonitorTask(filePicker.selectedFile.absolutePath)
    executor.submit(task!!)
    executor.shutdown()

    // console input loop
    println("osu! practice helper ready. Type '?' for help.")
    var exit = false
    val input = Scanner(System.`in`)
    while (!exit) {
        exit = runInputLoop(input);
    }

    config.saveConfig()
}

fun runInputLoop(input: Scanner): Boolean {
    if (input.hasNextLine()) {
        if (executor.isTerminated) {
            println("polling thread closed.")
            return true;
        }

        when (input.nextLine()) {
            "run" -> {
                task?.updateAttemptedMap()
            }
            "nat" -> {
                println(task?.attemptedMap?.toString() ?: "no map set.")
            }
            "np" -> {
                println(task?.getOsuTitle() ?: "couldn't fetch osu! title...")
            }
            "owo" -> {
                println("what's this?")
            }
            "?" -> {
                println("""Commands:
                    | - ?: displays this text.
                    | - run: picks the map you're grinding. Shows text in attempts.txt when you're playing the chosen map.
                    | - nat: displays currently attempted map.
                    | - np: displays currently playing map title.
                    | - owo: secret command.
                    | - exit: quits program, and shuts down monitor thread.""".trimMargin())
            }
            "exit" -> {
                return true;
            }
        }
    }
    return false;
}
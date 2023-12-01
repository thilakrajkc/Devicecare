package com.lge.devicecare.utils

import android.os.Process
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object SLog {
    enum class Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        ASSERT,
        WTF
    }

    enum class Category(val label: String) {
        DEFAULT("default"),
        NETWORK("network"),
        APP_CRASH("app_crash")
    }

    data class LogMsg(
        val msg: String,
        val category: Category
    )

    private const val TAG = "SLog"
    private val channel = Channel<LogMsg>()

    private var logFileRollingTime = 6          // 6 days
    private var logFileSizeLimit = 200L * 1024L // 200 KB

    private var isFileLogging = false
    private var baseLogDirPath = ""
    private var minLevel = Level.DEBUG
    private var writeFileJob = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
        while (isActive) {
            val data = channel.receive()
            writeFile(data.msg, data.category)
        }
    }

    private var logListener: ((log: String) -> Unit)? = null

    fun enableFileLogging() {
        isFileLogging = true
    }

    fun disableFileLogging() {
        isFileLogging = false
    }

    fun init(newBaseLogDirPath: String, rollingTime: Int = 6, sizeLimit: Long = 200L * 1024L, newMinLevel: Level = Level.DEBUG) {
        Log.d(TAG, "init()")

        minLevel = newMinLevel
        baseLogDirPath = newBaseLogDirPath
        logFileRollingTime = rollingTime
        logFileSizeLimit = sizeLimit

        writeFileJob.start()
    }

    fun setLogListener(logListener: (log: String) -> Unit) {
        this.logListener = logListener
    }

    fun LogV(msg: String, isWriteFile: Boolean = true, category: Category = Category.DEFAULT) {
        val e = Exception()
        val element = e.stackTrace
        val tag = element.getOrNull(2)?.fileName ?: "SourceFile"
        val lineNumber = element.getOrNull(2)?.lineNumber ?: "?"
        val methodName = element.getOrNull(2)?.methodName ?: "?"
        val logMsg = "[Line:$lineNumber] $methodName -> $msg"
        Log.v(tag, logMsg)
        logListener?.invoke("$tag [V] $logMsg")

        if (isFileLogging && isWriteFile && baseLogDirPath.isNotBlank() && Level.VERBOSE >= minLevel) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.send(LogMsg("[${now()}] [V] ${Process.myTid()}/$tag: $logMsg", category))
            }
        }
    }

    fun LogD(msg: String, isWriteFile: Boolean = true, category: Category = Category.DEFAULT) {
        val e = Exception()
        val element = e.stackTrace
        val tag = element.getOrNull(2)?.fileName ?: "SourceFile"
        val lineNumber = element.getOrNull(2)?.lineNumber ?: "?"
        val methodName = element.getOrNull(2)?.methodName ?: "?"
        val logMsg = "[Line:$lineNumber] $methodName -> $msg"
        Log.d(tag, logMsg)
        logListener?.invoke("$tag [D] $logMsg")

        if (isFileLogging && isWriteFile && baseLogDirPath.isNotBlank() && Level.DEBUG >= minLevel) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.send(LogMsg("[${now()}] [D] ${Process.myTid()}/$tag: $logMsg", category))
            }
        }
    }

    fun LogI(msg: String, isWriteFile: Boolean = true, category: Category = Category.DEFAULT) {
        val e = Exception()
        val element = e.stackTrace
        val tag = element.getOrNull(2)?.fileName ?: "SourceFile"
        val lineNumber = element.getOrNull(2)?.lineNumber ?: "?"
        val methodName = element.getOrNull(2)?.methodName ?: "?"
        val logMsg = "[Line:$lineNumber] $methodName -> $msg"
        Log.i(tag, logMsg)
        logListener?.invoke("$tag [I] $logMsg")

        if (isFileLogging && isWriteFile && baseLogDirPath.isNotBlank() && Level.INFO >= minLevel) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.send(LogMsg("[${now()}] [I] ${Process.myTid()}/$tag: $logMsg", category))
            }
        }
    }

    fun LogW(msg: String, isWriteFile: Boolean = true, category: Category = Category.DEFAULT) {
        val e = Exception()
        val element = e.stackTrace
        val tag = element.getOrNull(2)?.fileName ?: "SourceFile"
        val lineNumber = element.getOrNull(2)?.lineNumber ?: "?"
        val methodName = element.getOrNull(2)?.methodName ?: "?"
        val logMsg = "[Line:$lineNumber] $methodName -> $msg"
        Log.w(tag, logMsg)
        logListener?.invoke("$tag [W] $logMsg")

        if (isFileLogging && isWriteFile && baseLogDirPath.isNotBlank() && Level.WARN >= minLevel) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.send(LogMsg("[${now()}] [W] ${Process.myTid()}/$tag: $logMsg", category))
            }
        }
    }

    fun LogE(msg: String, isWriteFile: Boolean = true, category: Category = Category.DEFAULT) {
        val e = Exception()
        val element = e.stackTrace
        val tag = element.getOrNull(2)?.fileName ?: "SourceFile"
        val lineNumber = element.getOrNull(2)?.lineNumber ?: "?"
        val methodName = element.getOrNull(2)?.methodName ?: "?"
        val logMsg = "[Line:$lineNumber] $methodName -> $msg"
        Log.e(tag, logMsg)
        logListener?.invoke("$tag [E] $logMsg")

        if (isFileLogging && isWriteFile && baseLogDirPath.isNotBlank() && Level.ERROR >= minLevel) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.send(LogMsg("[${now()}] [E] ${Process.myTid()}/$tag: $logMsg", category))
            }
        }
    }

    fun writeFile(level: Level, tag: String, msg: String, category: Category = Category.DEFAULT) {
        if (!isFileLogging || baseLogDirPath.isBlank() || level < minLevel) {
            return
        }

        val logMsg = LogMsg("[${now()}] [${level.name[0]}] ${Process.myTid()}/$tag: $msg", category)
        CoroutineScope(Dispatchers.IO).launch {
            channel.send(logMsg)
        }
    }

    private fun writeFile(msg: String, category: Category) {
        var counter = 0

        val now = Date()
        val dateSdf = SimpleDateFormat("yyyyMMdd", Locale.ROOT)
        val date = dateSdf.format(now)
        val todayLogDirPath = "$baseLogDirPath/${category.label}/$date"
        val todayLogDir = File(todayLogDirPath)

        if (!todayLogDir.exists()) {
            // 폴더 생성이 필요할 때
            // 날짜 폴더 개수 6개로 조절
            val categoryLogDirPath = "$baseLogDirPath/${category.label}"
            val logDirList = File(categoryLogDirPath).list()
            if (logDirList != null) {
                logDirList.sort()

                var i = 0
                while (logDirList.size - i > logFileRollingTime) {
                    val oldest = logDirList[i]
                    deleteDir(File("$categoryLogDirPath/$oldest"))
                    i++
                }
            }

            try {
                todayLogDir.mkdirs()
                counter = 0

            } catch (e: SecurityException) {
                e.printStackTrace()
                Log.e(TAG, "Create Log Dir Error : " + e.stackTraceToString())

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Create Log Dir Error : " + e.stackTraceToString())
            }

        } else {
            val logFileList = File(todayLogDirPath).listFiles()
            counter = logFileList?.size?.minus(1) ?: 0

            if (counter < 0) {
                counter = 0
            }
        }

        // 로그 파일 사이즈 체크
        val newest = File("$todayLogDirPath/log%03d.txt".format(counter))
        if (newest.exists() && newest.length() > logFileSizeLimit) {
            Log.d(TAG, "Size : " + newest.length())
            counter++
        }

        // 로그 파일 생성
        val path = "$todayLogDirPath/log%03d.txt".format(counter)
        val logfile = File(path)
        if (!logfile.exists()) {
            try {
                logfile.createNewFile()

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Create Log File Error : " + e.stackTraceToString())

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Create Log File Error : " + e.stackTraceToString())
            }
        }

        // 로그 추가
        val bufferedWriter = BufferedWriter(FileWriter(logfile, true))
        bufferedWriter.append(msg)
        bufferedWriter.newLine()
        bufferedWriter.close()
    }

    private fun deleteDir(parent: File) {
        val listFiles = parent.listFiles()
        listFiles?.let {
            for (child in listFiles) {
                if (child == null) {
                    continue

                } else if (child.isDirectory) {
                    deleteDir(child)

                } else {
                    child.delete()
                }
            }
        }

        parent.delete()
    }

    private fun getTimeZone(): String {
        val timeZone = TimeZone.getDefault()
        val offset = "UTC" + timeZone.getDisplayName(false, TimeZone.SHORT).substring(3)
        return "($offset) ${timeZone.id}"
    }

    private fun now(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.ROOT)
        return sdf.format(Date())
    }
}
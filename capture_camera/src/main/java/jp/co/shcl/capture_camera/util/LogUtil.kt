package jp.co.shcl.capture_camera.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ログ出力クラス
 */
internal class LogUtil {

    companion object {

        private const val TAG = "撮影ライブラリー"
        private const val ENABLE = true
        private const val WRITE_ENABLE = true
        private const val LOG_FILE_NAME = "log.txt"

        /**
         * ログ出力
         * @param message   出力するメッセージ
         */
        fun put(vararg message: Any?) {
            if (ENABLE) {
                val logMessage = createMessage(message)
                Log.d(TAG, logMessage)
            }
        }

        /**
         * ログ出力
         * @param context   コンテキスト
         * @param message   出力するメッセージ
         */
        fun toast(context: Context, vararg message: Any?) {
            if (ENABLE) {
                val logMessage = createMessage(message)
                Log.d(TAG, logMessage)
                Toast.makeText(context, logMessage, Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * ログファイルに出力
         * @param context
         * @param message   出力するメッセージ
         */
        fun write(context: Context, vararg message: Any?) {
            if (WRITE_ENABLE) {
                val timeString =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                val logString = createMessage(message)
                Log.d(TAG, logString)
                writeFile(context, "$timeString Written $logString")
            }
        }

        fun readLogFile(context: Context): String {
            return File(context.filesDir, LOG_FILE_NAME).let {
                if (it.exists()) it.readText()
                else "Log file not exists."
            }
        }

        fun clearLogFile(context: Context) {
            File(context.filesDir, LOG_FILE_NAME).writeText("")
        }

        private fun createMessage(message: Array<out Any?>): String {

            val stElement = Thread.currentThread().stackTrace[4]

            val baseMessage = baseMessage(stElement)
            val messageBuilder = StringBuilder().also { builder ->
                builder.append(baseMessage)
                message.forEach {
                    builder.append(" ")
                    builder.append(it.toString())
                }
            }
            return messageBuilder.toString()
        }

        private fun baseMessage(stackTraceElement: StackTraceElement): String {
            val simpleClassName = stackTraceElement.className.split(".").last()
            return "[${simpleClassName}.${stackTraceElement.methodName}(${stackTraceElement.fileName}:${stackTraceElement.lineNumber})]"
        }

        private fun writeFile(context: Context, message: String) {
            File(context.filesDir, LOG_FILE_NAME).appendText("${message}\n")
        }

    }
}
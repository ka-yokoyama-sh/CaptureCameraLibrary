package jp.co.shcl.capture_camera.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import jp.co.shcl.capture_camera.R

/** 撮影サウンドの管理クラス */
internal class CaptureSound {

    /** サウンドプールオブジェクト */
    private lateinit var soundPool: SoundPool

    /**
     * 出力できるサウンドの列挙
     * @param id    サウンドプールに紐づくID
     *
     * @property SHUTTER            シャッター音
     * @property START_RECORDING    録画開始音
     * @property STOP_RECORDING     録画停止音
     */
    enum class Sound(var id: Int) {
        SHUTTER(0),
        START_RECORDING(0),
        STOP_RECORDING(0)
    }

    companion object {
        /** ボリューム */
        private const val SOUND_VOLUME = 1.0f
    }

    /**
     * サウンドデータをロード.
     * @param context Context
     */
    fun load(context: Context) {
        // SE準備（サウンドプール設定）
        soundPool = SoundPool.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setMaxStreams(2)
            .build()
        Sound.SHUTTER.id = soundPool.load(context, R.raw.se_camera_shutter_2, 1)
        Sound.START_RECORDING.id = soundPool.load(context, R.raw.se_button_click_34, 1)
        Sound.STOP_RECORDING.id = soundPool.load(context, R.raw.se_button_click_35, 1)
    }

    /**
     * サウンドを再生.
     * @param sound 再生するサウンドを指定する[Sound]クラス
     */
    fun play(sound: Sound) {
        soundPool.play(
            sound.id,
            SOUND_VOLUME,
            SOUND_VOLUME,
            0,
            0,
            1.0f
        )
    }

    /** サウンドデータを開放 */
    fun release() {
        soundPool.release()
    }
}
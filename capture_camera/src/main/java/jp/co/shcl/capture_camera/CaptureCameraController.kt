package jp.co.shcl.capture_camera

import androidx.camera.core.CameraInfo
import jp.co.shcl.capture_camera.model.CameraFacing
import jp.co.shcl.capture_camera.model.CaptureMode
import jp.co.shcl.capture_camera.model.CaptureState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * キャプチャーユースケースクラス.
 */
interface CaptureCameraController {

    /**
     * 撮影画像の出力先ファイル生成インターフェース
     */
    interface ImageOutputFileGenerator {
        fun generate(): File
    }

    /**
     * 撮影ビデオの出力先ファイル生成インターフェース
     */
    interface VideoOutputFileGenerator {
        fun generate(): File
    }

    /** 撮影状態[CaptureState]の[SharedFlow] */
    val captureState: SharedFlow<CaptureState>

    /** 撮影モード[CaptureMode]の[StateFlow] */
    val captureMode: StateFlow<CaptureMode>

    /** カメラ処理用フラグメントが存在しているか否かの[StateFlow] */
    val hasCameraFragment: StateFlow<Boolean>

    /** カメラ情報[CameraInfo]の[StateFlow] */
    val cameraInfo: StateFlow<CameraInfo?>

    /** ズーム設定要求値の[SharedFlow] */
    val zoomRatioSettingRequestValue: SharedFlow<Float>

    /** カメラ切り替え有効性の[StateFlow] */
    val canSwitchCamera: StateFlow<Boolean>

    /** 現在のカメラの向き[CameraFacing]の[StateFlow] */
    val currentCameraFacing: StateFlow<CameraFacing>

    /**
     * 画像出力用ファイルの生成関数[ImageOutputFileGenerator]を設定
     * @param method  ファイルオブジェクト生成メソッド
     */
    fun setImageOutputFileGenerator(method: () -> File)

    /**
     * 画像出力用ファイルの生成関数[ImageOutputFileGenerator]を設定
     * @param generator  [ImageOutputFileGenerator]オブジェクト
     */
    fun setImageOutputFileGenerator(generator: ImageOutputFileGenerator)

    /**
     * 動画出力用ファイルの生成関数[VideoOutputFileGenerator]を設定
     * @param method  ファイルオブジェクト生成メソッド
     */
    fun setVideoOutputFileGenerator(method: () -> File)

    /**
     * 動画出力用ファイルの生成関数[VideoOutputFileGenerator]を設定
     * @param generator  [VideoOutputFileGenerator]オブジェクト
     */
    fun setVideoOutputFileGenerator(generator: VideoOutputFileGenerator)

    /** 動画撮影を開始 */
    suspend fun startRecording()
    /** 写真を撮影 */
    suspend fun takePicture()

    /** 動画撮影を終了 */
    suspend fun stopRecording()

    /** 撮影完了状態をクリア. */
    suspend fun clearCapturedState()

    /**
     * 撮影モードを設定.
     * [CaptureFragment][jp.co.shcl.media_capture.ui.CaptureFragment]を開く前に撮影モードを設定することで
     * 開始時のモードを選択できる.
     * @param mode  設定する撮影モード
     */
    fun setCaptureMode(mode: CaptureMode)

    /**
     * ズーム比率を設定.
     * @param float 設定する値
     */
    suspend fun setZoomRatio(float: Float)

    /**
     * カメラ向きを設定.
     * [canSwitchCamera]がfalseの場合は、設定せずにfalseを返す.
     * @param facing カメラ向きを示す[CameraFacing]
     * @return 正常に値を発行できた場合true
     */
    fun setCameraFacing(facing: CameraFacing): Boolean

    /**
     * カメラ向きを逆向きに設定.
     * [canSwitchCamera]がfalseの場合は、設定せずにfalseを返す.
     * @return 正常に値を発行できた場合true
     */
    fun toggleCamera(): Boolean

}
package jp.co.shcl.capture_camera

import androidx.camera.core.CameraInfo
import jp.co.shcl.capture_camera.model.*
import jp.co.shcl.capture_camera.util.LogUtil
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * キャプチャーユースケースクラス.
 */
internal class CaptureCameraControllerImpl : CaptureCameraController {

    companion object {
        // 例外時メッセージ
        private const val MUST_NOT_BE_RECORDING = "Must not be recording."
        private const val MUST_BE_INITIALIZED_USE_CASE = "Must be initialized this(CaptureUseCase)."
        private const val MUST_BE_INITIALIZED_OUTPUT_FILE_GENERATOR =
            "Must be initialized 'ImageOutputFileGenerator' and 'VideoOutputFileGenerator'."

        val DEFAULT_CAMERA_FACING = CameraFacing.BACK
    }

    /** 撮影状態[CaptureState]の[MutableSharedFlow] */
    private val _captureState = MutableSharedFlow<CaptureState>(1)

    /** 撮影状態[CaptureState]の[SharedFlow] */
    override val captureState: SharedFlow<CaptureState> get() = _captureState

    /** 撮影モード[CaptureMode]の[MutableStateFlow] */
    private val _captureMode = MutableStateFlow(CaptureMode.IMAGE_CAPTURE)

    /** 撮影モード[CaptureMode]の[StateFlow] */
    override val captureMode: StateFlow<CaptureMode> = _captureMode

    /** カメラ処理用フラグメントが存在しているか否かの[MutableStateFlow] */
    private val _hasCameraFragment = MutableStateFlow(false)

    /** カメラ処理用フラグメントが存在しているか否かの[StateFlow] */
    override val hasCameraFragment: StateFlow<Boolean> = _hasCameraFragment

    /** カメラ情報[CameraInfo]の[MutableStateFlow] */
    private val _cameraInfo = MutableStateFlow<CameraInfo?>(null)

    /** カメラ情報[CameraInfo]の[StateFlow] */
    override val cameraInfo: StateFlow<CameraInfo?> = _cameraInfo

    /** ズーム設定要求値の[MutableSharedFlow] */
    private val _zoomRatioSettingRequestValue =
        MutableSharedFlow<Float>(0, 1, BufferOverflow.DROP_OLDEST)

    /** ズーム設定要求値の[SharedFlow] */
    override val zoomRatioSettingRequestValue: SharedFlow<Float> = _zoomRatioSettingRequestValue

    /** 直近の出力ファイル */
    private var dstFilePath: String? = null

    /** カメラ切り替え有効性の[MutableStateFlow] */
    private val _canSwitchCamera = MutableStateFlow(false)

    /** カメラ切り替え有効性の[StateFlow] */
    override val canSwitchCamera: StateFlow<Boolean> = _canSwitchCamera

    /** 現在のカメラの向き[CameraFacing]の[MutableStateFlow] */
    private val _currentCameraFacing = MutableStateFlow(DEFAULT_CAMERA_FACING)

    /** 現在のカメラの向き[CameraFacing]の[StateFlow] */
    override val currentCameraFacing: StateFlow<CameraFacing> = _currentCameraFacing

    /** 画像出力用ファイル生成関数 */
    private lateinit var imageOutputFileGenerator: CaptureCameraController.ImageOutputFileGenerator

    /** 動画出力用ファイル生成関数 */
    private lateinit var videoOutputFileGenerator: CaptureCameraController.VideoOutputFileGenerator

    /**
     * 画像出力用ファイルの生成関数[CaptureCameraController.ImageOutputFileGenerator]を設定
     * @param method  ファイルオブジェクト生成メソッド
     */
    override fun setImageOutputFileGenerator(method: () -> File) {
        imageOutputFileGenerator = object : CaptureCameraController.ImageOutputFileGenerator {
            override fun generate(): File {
                return method()
            }
        }
    }

    /**
     * 画像出力用ファイルの生成関数[CaptureCameraController.ImageOutputFileGenerator]を設定
     * @param generator  [CaptureCameraController.ImageOutputFileGenerator]オブジェクト
     */
    override fun setImageOutputFileGenerator(generator: CaptureCameraController.ImageOutputFileGenerator) {
        imageOutputFileGenerator = generator
    }

    /**
     * 動画出力用ファイルの生成関数[CaptureCameraController.VideoOutputFileGenerator]を設定
     * @param method  ファイルオブジェクト生成メソッド
     */
    override fun setVideoOutputFileGenerator(method: () -> File) {
        videoOutputFileGenerator = object : CaptureCameraController.VideoOutputFileGenerator {
            override fun generate(): File {
                return method()
            }
        }
    }

    /**
     * 動画出力用ファイルの生成関数[CaptureCameraController.VideoOutputFileGenerator]を設定
     * @param generator  [CaptureCameraController.VideoOutputFileGenerator]オブジェクト
     */
    override fun setVideoOutputFileGenerator(generator: CaptureCameraController.VideoOutputFileGenerator) {
        videoOutputFileGenerator = generator
    }

    /** 動画撮影を開始 */
    override suspend fun startRecording() {
        check(captureMode.value == CaptureMode.VIDEO_CAPTURE)
        check(captureState.latest() is Initialized) { MUST_BE_INITIALIZED_USE_CASE }
        check(::imageOutputFileGenerator.isInitialized) { MUST_BE_INITIALIZED_OUTPUT_FILE_GENERATOR }
        dstFilePath = videoOutputFileGenerator.generate().absolutePath
        setCaptureState(StartRecording(dstFilePath!!))
    }

    /** 写真を撮影 */
    override suspend fun takePicture() {
        check(captureMode.value == CaptureMode.IMAGE_CAPTURE)
        check(captureState.latest() is Initialized) { MUST_BE_INITIALIZED_USE_CASE }
        check(::videoOutputFileGenerator.isInitialized) { MUST_BE_INITIALIZED_OUTPUT_FILE_GENERATOR }
        dstFilePath = imageOutputFileGenerator.generate().absolutePath
        setCaptureState(TakePicture(dstFilePath!!))
    }

    /** 動画撮影を終了 */
    override suspend fun stopRecording() {
        if (captureState.latest() is Recording)
            setCaptureState(StopRecording)
    }

    /** 撮影完了状態をクリア. */
    override suspend fun clearCapturedState() {
        check(captureState.latest() is Captured)
        dstFilePath = null
        if (hasCameraFragment.value) setCaptureState(Initialized)
        else setCaptureState(CameraClosed)
    }

    /** キャプチャーフラグメント初期化完了時の処理 */
    internal suspend fun onCaptureFragmentInitialized() {
        _hasCameraFragment.value = true
        setCaptureState(Initialized)
    }

    /** キャプチャーフラグメント破棄時の処理 */
    internal suspend fun onCaptureFragmentDestroy() {
        check(captureState.latest() !is Capturing) { MUST_NOT_BE_RECORDING }
        _hasCameraFragment.value = false
        setCaptureState(CameraClosed)
    }

    /** 動画撮影中になった時の処理 */
    internal suspend fun onRecording() {
        setCaptureState(Recording)
    }

    /** 撮影データが保存された時の処理 */
    internal suspend fun onSaved() {
        setCaptureState(CapturedFileSaved(dstFilePath!!))
    }

    /** 撮影が失敗した時の処理 */
    internal suspend fun onCaptureFailed(message: String) {
        setCaptureState(CaptureFailed(message))
    }

    /**
     * 撮影モードを設定.
     * [CaptureFragment][jp.co.shcl.capture_camera.ui.CaptureFragment]を開く前に撮影モードを設定することで
     * 開始時のモードを選択できる.
     * @param mode  設定する撮影モード
     */
    override fun setCaptureMode(mode: CaptureMode) {
        _captureMode.value = mode
    }

    /**
     * カメラ情報を[CaptureCameraControllerImpl]のプロパティへ設定.
     * @param cameraInfo    カメラ情報
     */
    internal fun setCameraInfo(cameraInfo: CameraInfo?) {
        _cameraInfo.value = cameraInfo
    }

    /**
     * ズーム比率を設定.
     * @param float 設定する値
     */
    override suspend fun setZoomRatio(float: Float) {
        _zoomRatioSettingRequestValue.emit(float)
    }

    /**
     * カメラ向き変更の有効性を設定.
     * @param boolean 有効な場合true
     */
    internal fun setCanSwitchCameraFacing(boolean: Boolean) {
        _canSwitchCamera.value = boolean
    }

    /**
     * カメラ向きを設定.
     * [canSwitchCamera]がfalseの場合は、設定せずにfalseを返す.
     * @param facing カメラ向きを示す[CameraFacing]
     * @return 正常に値を発行できた場合true
     */
    override fun setCameraFacing(facing: CameraFacing): Boolean {
        return if (canSwitchCamera.value) {
            _currentCameraFacing.value = facing
            true
        } else false
    }

    /**
     * カメラ向きを逆向きに設定.
     * [canSwitchCamera]がfalseの場合は、設定せずにfalseを返す.
     * @return 正常に値を発行できた場合true
     */
    override fun toggleCamera(): Boolean {
        return setCameraFacing(currentCameraFacing.value.toggle())
    }

    /**
     * 出力ファイルを削除
     */
    private fun deleteOutputFile() {
        dstFilePath?.let {
            val file = File(it)
            if (file.exists()) file.delete()
        }
    }

    /**
     * 撮影状態を設定.
     * @param state 設定する[CaptureState]
     */
    private suspend fun setCaptureState(state: CaptureState) {
        LogUtil.put("VideoCaptureState update: ${captureState.latest()?.javaClass?.simpleName ?: "[]"} -> ${state.javaClass.simpleName}")
        _captureState.emit(state)
    }

    /**
     * 同期処理として[SharedFlow.replayCache]から最新値を取得.
     * replayCache数が1以上であることが前提.
     * replayCache数が0 もしくは、まだreplayCacheが空の場合 null を返す.
     * @return [T]型の最新の値
     */
    private fun <T> SharedFlow<T>.latest(): T? {
        return replayCache.lastOrNull()
    }

}
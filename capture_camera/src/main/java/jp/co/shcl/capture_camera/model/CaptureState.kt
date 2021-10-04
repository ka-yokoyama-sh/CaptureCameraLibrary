package jp.co.shcl.capture_camera.model

/** 撮影状態インターフェース */
sealed interface CaptureState

/** 撮影中状態インターフェース */
sealed interface Capturing

/** ビデオ撮影中状態インターフェース */
sealed interface VideoCapturing: Capturing

/** 撮影終了状態インターフェース */
sealed interface Captured

/** カメラが閉じている状態 */
object CameraClosed: CaptureState

/** 初期化完了状態 */
object Initialized: CaptureState

/** 画像撮影状態 */
data class TakePicture(val outputFilePath: String): CaptureState, Capturing

/** 録画開始状態 */
data class StartRecording(val outputFilePath: String): CaptureState, VideoCapturing

/** 録画中状態 */
object Recording: CaptureState, VideoCapturing

/** 録画停止状態 */
object StopRecording: CaptureState, VideoCapturing

/**  撮影ファイル保存完了状態 */
data class CapturedFileSaved(val outputFilePath: String): CaptureState, Captured

/** 撮影失敗状態 */
data class CaptureFailed(val message: String): CaptureState, Captured

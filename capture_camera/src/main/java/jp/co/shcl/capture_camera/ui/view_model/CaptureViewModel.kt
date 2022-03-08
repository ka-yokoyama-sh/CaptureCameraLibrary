package jp.co.shcl.capture_camera.ui.view_model

import android.content.pm.ActivityInfo
import androidx.camera.core.CameraInfo
import androidx.lifecycle.*
import jp.co.shcl.capture_camera.CaptureCameraControllerImpl
import jp.co.shcl.capture_camera.model.CameraFacing
import jp.co.shcl.capture_camera.model.CaptureMode
import jp.co.shcl.capture_camera.model.CaptureState
import kotlinx.coroutines.launch

internal class CaptureViewModel(
    private val controllerImpl: CaptureCameraControllerImpl
) : ViewModel() {

    /** 撮影状態を指定する[CaptureState]の[LiveData] */
    val captureState: LiveData<CaptureState> =
        controllerImpl.captureState.asLiveData(viewModelScope.coroutineContext)

    /** 撮影モードを指定する[CaptureMode]の[LiveData] */
    val captureMode: LiveData<CaptureMode> =
        controllerImpl.captureMode.asLiveData(viewModelScope.coroutineContext)

    /** ズーム比率の設定要求値の[LiveData] */
    val zoomRatioSettingRequestValue: LiveData<Float> =
        controllerImpl.zoomRatioSettingRequestValue.asLiveData(viewModelScope.coroutineContext)

    /** カメラセレクターを指定する[CameraFacing]の[LiveData] */
    val cameraSelector: LiveData<CameraFacing> =
        controllerImpl.currentCameraFacing.asLiveData(viewModelScope.coroutineContext)

    /** 既定の画面向きを示す[ActivityInfo]のSCREEN_ORIENTATION定数.  */
    val defaultScreenOrientation: Int get() = controllerImpl.defaultScreenOrientation

    /** 撮影フラグメント初期化時の処理 */
    fun onFragmentInitialized() {
        viewModelScope.launch {
            controllerImpl.onCaptureFragmentInitialized()
        }
    }

    /** 撮影フラグメント破棄時の処理 */
    fun onFragmentDestroy() {
        viewModelScope.launch {
            controllerImpl.onCaptureFragmentDestroy()
        }
    }

    /** 動画撮影開始時の処理 */
    fun onStartRecording() {
        viewModelScope.launch {
            controllerImpl.onRecording()
        }
    }

    /** 撮影データ保存時の処理 */
    fun onSaved() {
        viewModelScope.launch {
            controllerImpl.onSaved()
        }
    }

    /** 撮影失敗時の処理 */
    fun onCaptureFailed(message: String) {
        viewModelScope.launch {
            controllerImpl.onCaptureFailed(message)
        }
    }

    /** 録画停止. */
    fun stopRecording() {
        viewModelScope.launch {
            controllerImpl.stopRecording()
        }
    }

    /**
     * カメラ情報をユースケースクラスへ設定.
     * @param cameraInfo カメラ情報
     */
    fun setCameraInfo(cameraInfo: CameraInfo?) {
        controllerImpl.setCameraInfo(cameraInfo)
    }

    /**
     * カメラ数を設定.
     * 合わせてカメラ切り替えフラグを設定.
     * @param count カメラ数
     */
    fun setCameraCount(count: Int) {
        controllerImpl.setCameraCount(count)
    }

    class Factory(private val captureCameraControllerImpl: CaptureCameraControllerImpl) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CaptureViewModel(captureCameraControllerImpl) as T
        }
    }

}
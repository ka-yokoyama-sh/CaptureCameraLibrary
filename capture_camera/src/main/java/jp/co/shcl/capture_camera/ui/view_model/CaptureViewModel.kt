package jp.co.shcl.capture_camera.ui.view_model

import androidx.camera.core.CameraInfo
import androidx.lifecycle.*
import jp.co.shcl.capture_camera.model.*
import jp.co.shcl.capture_camera.CaptureCameraControllerImpl
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class CaptureViewModel(
    private val controllerImpl: CaptureCameraControllerImpl
) : ViewModel() {

    /** カメラ数 */
    var cameraCount = 0
        private set

    /** カメラが2機存在する場合true */
    private val haveTwoCameras get() = cameraCount == 2

    /** 撮影状態を指定する[CaptureState]の[LiveData] */
    val captureState: LiveData<CaptureState> =
        controllerImpl.captureState
            .onEach {
                if (it is Captured) controllerImpl.setCanSwitchCameraFacing(haveTwoCameras)
                if (it is StartRecording) controllerImpl.setCanSwitchCameraFacing(false)
            }
            .asLiveData(viewModelScope.coroutineContext)

    /** 撮影モードを指定する[CaptureMode]の[LiveData] */
    val captureMode: LiveData<CaptureMode> =
        controllerImpl.captureMode.asLiveData(viewModelScope.coroutineContext)

    /** ズーム比率の設定要求値の[LiveData] */
    val zoomRatioSettingRequestValue: LiveData<Float> =
        controllerImpl.zoomRatioSettingRequestValue.asLiveData(viewModelScope.coroutineContext)

    /** カメラセレクターを指定する[CameraFacing]の[LiveData] */
    val cameraSelector: LiveData<CameraFacing> =
        controllerImpl.currentCameraFacing.asLiveData(viewModelScope.coroutineContext)

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
    fun onCaptureFailed() {
        viewModelScope.launch {
            controllerImpl.onCaptureFailed()
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
        cameraCount = count
        controllerImpl.setCanSwitchCameraFacing(haveTwoCameras)
    }

    class Factory(private val captureCameraControllerImpl: CaptureCameraControllerImpl) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CaptureViewModel(captureCameraControllerImpl) as T
        }
    }

}
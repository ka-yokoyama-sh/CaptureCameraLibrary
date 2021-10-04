package jp.co.shcl.capture_camera.ui.view_model

import androidx.lifecycle.*
import jp.co.shcl.capture_camera.CaptureCameraControllerImpl
import jp.co.shcl.capture_camera.model.CaptureMode
import jp.co.shcl.capture_camera.model.CaptureState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal class CaptureControlViewModel(
    private val controllerImpl: CaptureCameraControllerImpl,
) : ViewModel() {

    val captureStatus: LiveData<Pair<CaptureState, CaptureMode>> = combine(
        controllerImpl.captureState,
        controllerImpl.captureMode,
        ::Pair
    ).asLiveData(viewModelScope.coroutineContext)

    fun takePicture() {
        viewModelScope.launch {
            controllerImpl.takePicture()
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            controllerImpl.startRecording()
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            controllerImpl.stopRecording()
        }
    }

    fun setImageCaptureMode() {
        controllerImpl.setCaptureMode(CaptureMode.IMAGE_CAPTURE)
    }

    fun setVideoCaptureMode() {
        controllerImpl.setCaptureMode(CaptureMode.VIDEO_CAPTURE)
    }

    class Factory(private val captureCameraControllerImpl: CaptureCameraControllerImpl) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CaptureControlViewModel(captureCameraControllerImpl) as T
        }
    }

}
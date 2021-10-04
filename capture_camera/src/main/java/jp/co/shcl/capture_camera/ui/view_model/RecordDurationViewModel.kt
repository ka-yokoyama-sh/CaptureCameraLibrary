package jp.co.shcl.capture_camera.ui.view_model

import androidx.lifecycle.*
import jp.co.shcl.capture_camera.CaptureCameraControllerImpl
import jp.co.shcl.capture_camera.model.CaptureMode
import jp.co.shcl.capture_camera.model.Initialized
import jp.co.shcl.capture_camera.model.StartRecording
import jp.co.shcl.capture_camera.model.StopRecording
import jp.co.shcl.capture_camera.ui.view_model.RecordDurationViewModel.Companion.TIMER_INTERVAL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 録画時間表示用ViewModelクラス
 * @property _isRecording           録画中か否かの[MutableLiveData]
 * @property isRecording            録画中か否かの[LiveData]
 * @property isVideoCaptureMode     ビデオ撮影モードか否かの[LiveData]
 * @property recordDuration         録画時間
 * @property recordDurationAsString 文字列としての録画時間
 * @property timer                  タイマーオブジェクト
 */
internal class RecordDurationViewModel(
    controllerImpl: CaptureCameraControllerImpl
) : ViewModel() {

    /**
     * @property TIMER_INTERVAL 録画時間更新間隔
     */
    companion object {
        private const val TIMER_INTERVAL = 200L
        private const val DURATION_STRING_FORMAT = "mm:ss"
    }

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    val isVideoCaptureMode: LiveData<Boolean> =
        controllerImpl.captureMode.map { it == CaptureMode.VIDEO_CAPTURE }
            .asLiveData(viewModelScope.coroutineContext)

    private val recordDuration = MutableStateFlow(0L)

    val recordDurationAsString: LiveData<String> =
        recordDuration.map { it.toTimeAsString() }
            .asLiveData(viewModelScope.coroutineContext)

    private var timer = Timer()

    init {
        viewModelScope.launch {
            // 撮影状態を購読
            controllerImpl.captureState.collect {
                when (it) {
                    is Initialized -> {
                        recordDuration.value = 0L
                    }
                    is StartRecording -> {
                        _isRecording.value = true
                        startTimer()
                    }
                    is StopRecording -> {
                        stopTimer()
                        _isRecording.value = false
                        recordDuration.value = 0L
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 録画時間更新用タイマー開始
     */
    private fun startTimer() {
        viewModelScope.launch {
            timer = Timer()
            timer.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        recordDuration.value = recordDuration.value.plus(TIMER_INTERVAL)
                    }
                },
                TIMER_INTERVAL,
                TIMER_INTERVAL
            )
        }
    }

    /**
     * 録画時間更新用タイマー停止
     */
    private fun stopTimer() {
        timer.cancel()
    }

    /**
     * 時間を表す文字列を取得.
     * 文字列の書式は[DURATION_STRING_FORMAT]に従う.
     * @return 時間を表す文字列
     */
    private fun Long.toTimeAsString(): String {
        val date = Date(this)
        val locale = Locale.getDefault()
        val dateFormat = SimpleDateFormat(DURATION_STRING_FORMAT, locale)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(date)
    }

    class Factory(private val captureCameraControllerImpl: CaptureCameraControllerImpl) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RecordDurationViewModel(captureCameraControllerImpl) as T
        }
    }

}
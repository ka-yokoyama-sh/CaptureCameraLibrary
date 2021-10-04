package jp.co.shcl.capture_camera.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileOptions
import androidx.camera.view.video.OutputFileResults
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import jp.co.shcl.capture_camera.CaptureCameraControllerProvider
import jp.co.shcl.capture_camera.databinding.FragmentCaptureBinding
import jp.co.shcl.capture_camera.model.*
import jp.co.shcl.capture_camera.ui.view_model.CaptureViewModel
import jp.co.shcl.capture_camera.util.*
import java.io.File

/**
 * 撮影用フラグメント.
 * [CaptureUseCase][jp.co.shcl.capture_camera.CaptureCameraControllerImpl]によりハンドリングされる.
 *
 * @property _binding               Viewバインディングオブジェクト
 * @property binding                Viewバインディングオブジェクト（参照用）
 * @property viewModel              ViewModelクラス
 * @property cameraController       カメラコントローラーオブジェクト
 * @property captureSound           撮影効果音管理クラス
 */
class CaptureFragment : Fragment() {

    private var _binding: FragmentCaptureBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaptureViewModel by viewModels {
        CaptureViewModel.Factory(
            CaptureCameraControllerProvider.getInstanceAsImpl()
        )
    }
    private lateinit var cameraController: LifecycleCameraController
    private val captureSound = CaptureSound()

    /** 写真撮影データ保存時のコールバックオブジェクト */
    private val onImageSavedCallback =
        object : ImageCapture.OnImageSavedCallback {
            // 撮影データ保存完了時
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                viewModel.onSaved()
            }

            // 撮影失敗時
            override fun onError(exception: ImageCaptureException) {
                LogUtil.write(requireContext(), exception.message)
                viewModel.onCaptureFailed()
                showErrorSnackbar(exception.message)
            }
        }

    /** ビデオ撮影データ保存時のコールバックオブジェクト */
    private val onVideoSavedCallback =
        @androidx.camera.view.video.ExperimentalVideo
        object : OnVideoSavedCallback {
            // 撮影データ保存完了時
            override fun onVideoSaved(outputFileResults: OutputFileResults) {
                viewModel.onSaved()
            }

            // 撮影失敗時
            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                LogUtil.write(requireContext(), message)
                viewModel.onCaptureFailed()
                showErrorSnackbar(message)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureBinding.inflate(inflater, container, false)

        return binding.root
    }

    @androidx.camera.view.video.ExperimentalVideo
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // プレビュー画面の状態を監視
        binding.previewView.previewStreamState.observe(viewLifecycleOwner) {

            // ストリーミングが開始された場合、初期化完了を通知
            if (it == PreviewView.StreamState.STREAMING) viewModel.onFragmentInitialized()

            viewModel.setCameraInfo(cameraController.cameraInfo)

        }

        // カメラコントローラー取得
        cameraController = createCameraController()

        viewModel.captureMode.observe(viewLifecycleOwner) {
            when (it!!) {
                CaptureMode.IMAGE_CAPTURE -> cameraController.setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                CaptureMode.VIDEO_CAPTURE -> cameraController.setEnabledUseCases(CameraController.VIDEO_CAPTURE)
            }
        }

        // ユースケースにフラグメント作成を通知
        viewModel.captureState.observe(viewLifecycleOwner) {
            when (it) {
                is Initialized -> {
                    // アクティビティーの回転ロックを解除
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
                is TakePicture -> {
                    if (cameraController.isImageCaptureEnabled)
                        takePicture(it.outputFilePath)
                }
                // 撮影開始の場合、開始処理実行
                is StartRecording -> {
                    if (cameraController.isVideoCaptureEnabled)
                        startRecording(it.outputFilePath)
                }
                // 撮影停止の場合、停止処理実行
                is StopRecording -> {
                    stopRecording()
                }
                else -> {
                }
            }
        }

        viewModel.zoomRatioSettingRequestValue.observe(viewLifecycleOwner) {
            cameraController.setZoomRatio(it)
        }

        viewModel.cameraSelector.observe(viewLifecycleOwner) {
            if (cameraController.currentCameraFacing() != it) {
                cameraController.setCameraFacing(it)
            }
        }

        // カメラの初期化を待って、カメラ前後切り替えの有効性を設定
        cameraController.initializationFuture.addListener(
            {
                viewModel.setCameraCount(cameraController.cameraCount())
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    override fun onResume() {
        super.onResume()
        // SE準備（サウンドプール設定）
        captureSound.load(requireContext())
    }

    override fun onPause() {
        super.onPause()
        // サウンドプール開放
        captureSound.release()
        viewModel.setCameraInfo(null)

        if (viewModel.captureState.value == Recording) {
            viewModel.stopRecording()
        }
    }

    /**
     * bindingオブジェクトを破棄
     */
    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.onFragmentDestroy()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        _binding = null
    }

    /**
     * 静止画撮影
     * @param dstFilePath   保存先ファイルパス
     */
    private fun takePicture(dstFilePath: String) {

        captureSound.play(CaptureSound.Sound.SHUTTER)

        // 出力ファイル取得
        val dstFile = File(dstFilePath)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(dstFile).build()
        binding.blind.isVisible = true
        binding.blind.fadeOut(viewLifecycleOwner.lifecycleScope, true)
        cameraController.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            onImageSavedCallback
        )
    }

    /**
     * 録画開始
     * @param dstFilePath   保存先ファイルパス
     */
    @androidx.camera.view.video.ExperimentalVideo
    private fun startRecording(dstFilePath: String) {
        // アクティビティーの回転をロック
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        captureSound.play(CaptureSound.Sound.START_RECORDING)

        // 出力ファイル取得
        val dstFile = File(dstFilePath)
        val outputFileOptions = OutputFileOptions.builder(dstFile).build()

        // カメラコントローラーで撮影開始処理
        cameraController.startRecording(
            outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            onVideoSavedCallback
        )

        // ViewModelに撮影中を通知
        viewModel.onStartRecording()
    }

    /**
     * 録画停止
     */
    @androidx.camera.view.video.ExperimentalVideo
    private fun stopRecording() {
        captureSound.play(CaptureSound.Sound.STOP_RECORDING)
        // カメラコントローラーで撮影停止処理
        cameraController.stopRecording()
    }

    /**
     * カメラプロバイダーにライフサイクルと各種機能をバインド
     */
    @androidx.camera.view.video.ExperimentalVideo
    private fun createCameraController(): LifecycleCameraController {
        val cameraController = LifecycleCameraController(requireContext())
        cameraController.bindToLifecycle(viewLifecycleOwner)
        binding.previewView.controller = cameraController
        return cameraController
    }

    private fun showErrorSnackbar(errorMessage: String?) {
        Snackbar
            .make(
                binding.previewView,
                errorMessage?.let {
                    "撮影に失敗しました. ($it)"
                } ?: "撮影に失敗しました.",
                Snackbar.LENGTH_LONG
            )
            .show()
    }
}
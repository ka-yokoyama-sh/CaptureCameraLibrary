package jp.co.shcl.capture_camera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import jp.co.shcl.capture_camera.CaptureCameraControllerProvider
import jp.co.shcl.capture_camera.R
import jp.co.shcl.capture_camera.databinding.FragmentCaptureControlBinding
import jp.co.shcl.capture_camera.model.*
import jp.co.shcl.capture_camera.ui.view_model.CaptureControlViewModel

internal class CaptureControlFragment : Fragment() {

    private val viewModel: CaptureControlViewModel by viewModels {
        CaptureControlViewModel.Factory(
            CaptureCameraControllerProvider.getInstanceAsImpl()
        )
    }
    private var _binding: FragmentCaptureControlBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 写真撮影ボタンのクリックリスナー設定
        binding.includeCaptureController.buttonShutter.setOnClickListener {
            it.isClickable = false
            viewModel.takePicture()
        }

        // 録画開始ボタンのクリックリスナー設定
        binding.includeCaptureController.buttonRecord.setOnClickListener {
            it.isClickable = false
            viewModel.startRecording()
        }

        // 録画終了ボタンのクリックリスナー設定
        binding.includeCaptureController.buttonStop.setOnClickListener {
            it.isClickable = false
            viewModel.stopRecording()
        }

        // 撮影モード切替ボタンのチェック変更リスナー設定
        binding.includeCaptureModeSwitch.chipGroupCaptureModeSwitch.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipCaptureImage -> viewModel.setImageCaptureMode()
                R.id.chipCaptureVideo -> viewModel.setVideoCaptureMode()
            }
        }

        viewModel.captureStatus.observe(viewLifecycleOwner) { (state: CaptureState, mode: CaptureMode) ->

            // 写真撮影ボタンのプロパティー設定
            binding.includeCaptureController.buttonShutter.apply {
                isVisible = state is Initialized && mode == CaptureMode.IMAGE_CAPTURE
                isClickable = state is Initialized && mode == CaptureMode.IMAGE_CAPTURE
            }

            // 録画開始ボタンのプロパティー設定
            binding.includeCaptureController.buttonRecord.apply {
                isVisible = state is Initialized && mode == CaptureMode.VIDEO_CAPTURE
                isClickable = state is Initialized && mode == CaptureMode.VIDEO_CAPTURE
            }

            // 録画終了ボタンのプロパティー設定
            binding.includeCaptureController.buttonStop.apply {
                isVisible = state is Recording
                isClickable = state is Recording
            }

            // 撮影モード切替ボタンのプロパティー設定
            binding.includeCaptureModeSwitch.chipGroupCaptureModeSwitch.apply {
                if (state is Initialized) {
                    if (!isVisible) fadeIn(viewLifecycleOwner.lifecycleScope)
                } else if (state is Capturing) {
                    if (isVisible) fadeOut(viewLifecycleOwner.lifecycleScope, false)
                }
            }
            when (mode) {
                CaptureMode.IMAGE_CAPTURE -> {
                    if (!binding.includeCaptureModeSwitch.chipCaptureImage.isChecked)
                        binding.includeCaptureModeSwitch.chipCaptureImage.isChecked = true
                }
                CaptureMode.VIDEO_CAPTURE -> {
                    if (!binding.includeCaptureModeSwitch.chipCaptureVideo.isChecked)
                        binding.includeCaptureModeSwitch.chipCaptureVideo.isChecked = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
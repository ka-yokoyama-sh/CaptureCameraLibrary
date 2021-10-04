package jp.co.shcl.capture_camera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import jp.co.shcl.capture_camera.CaptureCameraControllerProvider
import jp.co.shcl.capture_camera.databinding.FragmentCameraZoomRatioControlBinding
import jp.co.shcl.capture_camera.util.LogUtil
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CameraZoomRatioControlFragment : Fragment() {

    private var _binding: FragmentCameraZoomRatioControlBinding? = null
    private val binding get() = _binding!!

    private val captureCameraController = CaptureCameraControllerProvider.getInstance()

    companion object {
        private const val ZOOM_RATIO_SET_ON_CLICK = 2.0F
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraZoomRatioControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var cameraInfo: CameraInfo? = null

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            captureCameraController.cameraInfo.collect { nullableCameraInfo ->
                cameraInfo = nullableCameraInfo
                cameraInfo?.let { info ->
                    info.zoomState.observe(viewLifecycleOwner) { zoomState ->

                        // ズーム比率を取得してテキストへ反映
                        "%.1fx".format(zoomState.zoomRatio)
                            .also { binding.textViewZoomRatio.text = it }

                        // カメラのズームへの対応をViewの可視性へ反映
                        (zoomState.maxZoomRatio != zoomState.minZoomRatio).also { canZoom: Boolean ->
                            binding.frameLayout.isVisible = canZoom
                            if (!canZoom) LogUtil.put("This camera does not support zoom.")
                        }
                    }
                }
            }
        }

        binding.frameLayout.setOnClickListener {
            // ZoomState取得
            cameraInfo?.zoomState?.value?.let { zoomState ->
                // ズーム比率設定がSuspend関数のためCoroutineScopeで実行
                viewLifecycleOwner.lifecycleScope.launch {
                    // ズーム比率設定
                    captureCameraController.setZoomRatio(
                        // 現在のズーム比率が最小の場合
                        if (zoomState.zoomRatio == zoomState.minZoomRatio) {
                            // 最大ズーム比率が既定より大きい場合、既定に設定
                            if (zoomState.maxZoomRatio > ZOOM_RATIO_SET_ON_CLICK) ZOOM_RATIO_SET_ON_CLICK
                            // そうでない場合、最大に設定
                            else zoomState.maxZoomRatio
                        } // 現在のズーム比率が最小ではない場合、最小に設定
                        else zoomState.minZoomRatio
                    )
                }
            } ?: run {
                LogUtil.put("Could not get zoomState.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
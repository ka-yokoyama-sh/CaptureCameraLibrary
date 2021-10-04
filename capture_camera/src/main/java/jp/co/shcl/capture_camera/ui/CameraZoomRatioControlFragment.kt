package jp.co.shcl.capture_camera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraInfo
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
                        "%.1fx".format(zoomState.zoomRatio)
                            .also { binding.textViewZoomRatio.text = it }
                    }
                }
            }
        }

        binding.frameLayout.setOnClickListener {
            cameraInfo?.zoomState?.value?.let {
                viewLifecycleOwner.lifecycleScope.launch {
                    captureCameraController.setZoomRatio(
                        if (it.zoomRatio == it.minZoomRatio) {
                            if (it.maxZoomRatio > ZOOM_RATIO_SET_ON_CLICK) ZOOM_RATIO_SET_ON_CLICK
                            else it.maxZoomRatio
                        } else it.minZoomRatio
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
package jp.co.shcl.capture_camera_library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import jp.co.shcl.capture_camera.CaptureCameraController
import jp.co.shcl.capture_camera.CaptureCameraControllerProvider
import jp.co.shcl.capture_camera.databinding.FragmentCaptureTestBinding
import jp.co.shcl.capture_camera.model.Captured
import kotlinx.coroutines.flow.collect
import java.io.File

class CaptureTestFragment : Fragment() {

    private var _binding: FragmentCaptureTestBinding? = null
    private val binding get() = _binding!!
    private val captureUseCase: CaptureCameraController =
        CaptureCameraControllerProvider.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            captureUseCase.setImageOutputFileGenerator {
                File(
                    requireContext().cacheDir,
                    "test.jpg"
                )
            }
            captureUseCase.setVideoOutputFileGenerator {
                File(
                    requireContext().cacheDir,
                    "test.mp4"
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            captureUseCase.captureState.collect {
                if (it is Captured) captureUseCase.clearCapturedState()
            }
        }

    }

    /**
     * bindingオブジェクトを破棄
     */
    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}
package jp.co.shcl.capture_camera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import jp.co.shcl.capture_camera.CaptureCameraControllerProvider
import jp.co.shcl.capture_camera.databinding.FragmentCameraSelectorControlBinding
import kotlinx.coroutines.flow.collect

class CameraSelectorControlFragment : Fragment() {

    private var _binding: FragmentCameraSelectorControlBinding? = null
    private val binding get() = _binding!!

    private val controllerImpl = CaptureCameraControllerProvider.getInstanceAsImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraSelectorControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // カメラ向き切り替えの有効性を購読
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            controllerImpl.canSwitchCamera.collect {
                // カメラ向き切り替えボタンの有効性に反映
                binding.buttonSwitchCamera.isEnabled = it
            }
        }

        // カメラ向き切り替えボタンのクリックリスナー設定
        binding.buttonSwitchCamera.setOnClickListener {
            controllerImpl.toggleCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package jp.co.shcl.capture_camera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import jp.co.shcl.capture_camera.CaptureCameraControllerProvider
import jp.co.shcl.capture_camera.databinding.FragmentRecordDurationBinding
import jp.co.shcl.capture_camera.ui.view_model.RecordDurationViewModel

class RecordDurationFragment : Fragment() {

    private val viewModel: RecordDurationViewModel by viewModels {
        RecordDurationViewModel.Factory(
            CaptureCameraControllerProvider.getInstanceAsImpl()
        )
    }
    private var _binding: FragmentRecordDurationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordDurationBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isVideoCaptureMode.observe(viewLifecycleOwner) {
            if (it) binding.textViewRecordDuration.fadeIn(viewLifecycleOwner.lifecycleScope)
            else binding.textViewRecordDuration.fadeOut(viewLifecycleOwner.lifecycleScope, false)
        }

        viewModel.isRecording.observe(viewLifecycleOwner) {
            if (it) binding.imageRecMark.fadeIn(viewLifecycleOwner.lifecycleScope)
            else binding.imageRecMark.fadeOut(viewLifecycleOwner.lifecycleScope, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
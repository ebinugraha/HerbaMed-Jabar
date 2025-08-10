package edu.unikom.herbamedjabar.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.databinding.FragmentScanBinding
import edu.unikom.herbamedjabar.viewModel.ScanViewModel
import edu.unikom.herbamedjabar.viewModel.UiState

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private val viewModel: ScanViewModel by viewModels()
    private var _binding: FragmentScanBinding? = null
    private val binding
        get() = _binding!!

    private var processingDialog: ProcessingDialogFragment? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean
            ->
            if (isAdded) {
                if (isGranted) {
                    takePictureLauncher.launch(null)
                } else {
                    Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                binding.plantImageView.setImageBitmap(bitmap)
                viewModel.analyzeImage(bitmap)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        parentFragmentManager.setFragmentResultListener("scan_again_request", this) { _, bundle ->
            if (bundle.getBoolean("open_camera")) {
                checkCameraPermissionAndOpenCamera()
            }
        }

        binding.scanButton.setOnClickListener { checkCameraPermissionAndOpenCamera() }
    }

    private fun observeViewModel() {
        // Observer untuk navigasi
        viewModel.navigateToResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                // Panggil fungsi di MainActivity untuk menampilkan halaman hasil
                (activity as? MainActivity)?.showResultFragment(it.imagePath, it.resultText)
                viewModel.onNavigationComplete() // Reset state
            }
        }

        // Observer untuk UI State (loading, error, etc)
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    processingDialog = ProcessingDialogFragment()
                    processingDialog?.show(childFragmentManager, ProcessingDialogFragment.TAG)
                }
                is UiState.Success,
                is UiState.Error -> {
                    processingDialog?.dismiss()
                    if (state is UiState.Error) {
                        context?.let { ctx ->
                            Toast.makeText(ctx, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                else -> {
                    /* Idle */
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        processingDialog?.dismiss()
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> {
                takePictureLauncher.launch(null)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

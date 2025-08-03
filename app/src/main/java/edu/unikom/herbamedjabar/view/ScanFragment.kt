package edu.unikom.herbamedjabar.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.R
import edu.unikom.herbamedjabar.viewModel.ScanViewModel
import edu.unikom.herbamedjabar.viewModel.UiState

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private val viewModel: ScanViewModel by viewModels()

    private lateinit var plantImageView: ImageView
    private lateinit var scanButton: Button

    private var processingDialog: ProcessingDialogFragment? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                takePictureLauncher.launch(null)
            } else {
                Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                plantImageView.setImageBitmap(bitmap)
                viewModel.analyzeImage(bitmap)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        observeViewModel()

        scanButton.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }
    }

    private fun initViews(view: View) {
        plantImageView = view.findViewById(R.id.plantImageView)
        scanButton = view.findViewById(R.id.scanButton)
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
                    processingDialog?.show(childFragmentManager, "processing_dialog")
                }
                is UiState.Success, is UiState.Error -> {
                    processingDialog?.dismiss()
                    if (state is UiState.Error) {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
                else -> { /* Idle */ }
            }
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        // ... (kode ini tidak berubah)
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePictureLauncher.launch(null)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

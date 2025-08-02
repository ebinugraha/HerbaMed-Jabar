package edu.unikom.herbamedjabar.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.R
import edu.unikom.herbamedjabar.databinding.FragmentScanBinding
import edu.unikom.herbamedjabar.viewModel.ScanViewModel
import edu.unikom.herbamedjabar.viewModel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private val viewModel: ScanViewModel by viewModels()

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    // Variabel untuk dialog
    private var processingDialogInstance: ProcessingDialogFragment? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isAdded) {
                if (isGranted) {
                    takePictureLauncher.launch(null)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Izin kamera ditolak",
                        Toast.LENGTH_SHORT
                    ).show()
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        binding.scanButton.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Idle -> {
                    binding.scanButton.isEnabled = true
                    binding.resultCardView.visibility = View.INVISIBLE
                    dismissProcessingDialog()
                }
                is UiState.Loading -> {
                    binding.scanButton.isEnabled = false
                    binding.resultCardView.visibility = View.INVISIBLE
                    // Tampilkan dialog
                    showProcessingDialog()
                }
                is UiState.Success -> {
                    binding.scanButton.isEnabled = true
                    // Tutup dialog
                    dismissProcessingDialog()
                    val markdown = state.data

                    lifecycleScope.launch {
                        val htmlResult = withContext(Dispatchers.Default) {
                            val flavour = CommonMarkFlavourDescriptor()
                            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
                            HtmlGenerator(markdown, parsedTree, flavour).generateHtml()
                        }
                        binding.resultTextView.text =
                            HtmlCompat.fromHtml(htmlResult, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        binding.resultCardView.visibility = View.VISIBLE
                        val fadeInAnimation =
                            AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                        binding.resultCardView.startAnimation(fadeInAnimation)
                    }
                }
                is UiState.Error -> {
                    binding.scanButton.isEnabled = true
                    // Tutup dialog
                    dismissProcessingDialog()
                    binding.resultCardView.visibility = View.VISIBLE
                    binding.resultTextView.text = state.message
                    context?.let { ctx ->
                        Toast.makeText(ctx, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showProcessingDialog() {
        if (processingDialogInstance == null || processingDialogInstance?.dialog?.isShowing != true) {
            processingDialogInstance = ProcessingDialogFragment()
            // Use the TAG from ProcessingDialogFragment
            processingDialogInstance?.show(childFragmentManager, ProcessingDialogFragment.TAG)
        }
    }

    private fun dismissProcessingDialog() {
        processingDialogInstance?.dismissAllowingStateLoss()
        processingDialogInstance = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dismissProcessingDialog()
    }

    private fun checkCameraPermissionAndOpenCamera() {
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

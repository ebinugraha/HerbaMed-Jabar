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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.R
import edu.unikom.herbamedjabar.viewModel.ScanViewModel
import edu.unikom.herbamedjabar.viewModel.UiState
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

@AndroidEntryPoint
class ScanFragment : Fragment() {

    private val viewModel: ScanViewModel by viewModels()

    private lateinit var plantImageView: ImageView
    private lateinit var scanButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var resultCardView: CardView

    // Variabel untuk dialog
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
        // Hapus skeleton loader dari layout fragment_scan.xml jika masih ada
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
        resultTextView = view.findViewById(R.id.resultTextView)
        resultCardView = view.findViewById(R.id.resultCardView)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Idle -> {
                    scanButton.isEnabled = true
                    resultCardView.visibility = View.INVISIBLE
                }
                is UiState.Loading -> {
                    scanButton.isEnabled = false
                    resultCardView.visibility = View.INVISIBLE
                    // Tampilkan dialog
                    processingDialog = ProcessingDialogFragment()
                    processingDialog?.show(childFragmentManager, "processing_dialog")
                }
                is UiState.Success -> {
                    scanButton.isEnabled = true
                    // Tutup dialog
                    processingDialog?.dismiss()
                    val markdown = state.data
                    val flavour = CommonMarkFlavourDescriptor()
                    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
                    val html = HtmlGenerator(markdown, parsedTree, flavour).generateHtml()
                    resultTextView.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    resultCardView.visibility = View.VISIBLE
                    val fadeInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
                    resultCardView.startAnimation(fadeInAnimation)
                }
                is UiState.Error -> {
                    scanButton.isEnabled = true
                    // Tutup dialog
                    processingDialog?.dismiss()
                    resultCardView.visibility = View.VISIBLE
                    resultTextView.text = state.message
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
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

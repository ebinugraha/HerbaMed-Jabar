package edu.unikom.herbamedjabar.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.R
import edu.unikom.herbamedjabar.viewModel.ScanViewModel
import edu.unikom.herbamedjabar.viewModel.UiState
import kotlinx.coroutines.launch
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

@AndroidEntryPoint
class ScanFragment : Fragment() {

    // Injeksi ViewModel menggunakan Hilt
    private val viewModel: ScanViewModel by viewModels()

    private lateinit var plantImageView: ImageView
    private lateinit var scanButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var skeletonLoader: LinearLayout
    private lateinit var resultCardView: CardView

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
                // Panggil fungsi di ViewModel
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
        resultTextView = view.findViewById(R.id.resultTextView)
        skeletonLoader = view.findViewById(R.id.skeletonLoader)
        resultCardView = view.findViewById(R.id.resultCardView)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Idle -> {
                    scanButton.isEnabled = true
                    skeletonLoader.visibility = View.GONE
                    resultCardView.visibility = View.INVISIBLE
                }
                is UiState.Loading -> {
                    scanButton.isEnabled = false
                    skeletonLoader.visibility = View.VISIBLE
                    resultCardView.visibility = View.INVISIBLE
                }
                is UiState.Success -> {
                    scanButton.isEnabled = true
                    skeletonLoader.visibility = View.GONE
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
                    skeletonLoader.visibility = View.GONE
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
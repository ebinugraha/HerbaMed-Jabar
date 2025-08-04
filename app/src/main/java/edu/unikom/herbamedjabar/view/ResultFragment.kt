package edu.unikom.herbamedjabar.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.databinding.FragmentResultBinding
import java.io.File
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

@AndroidEntryPoint
class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dari arguments
        val imagePath = arguments?.getString(ARG_IMAGE_PATH)
        val resultText = arguments?.getString(ARG_RESULT_TEXT)

        // Tampilkan gambar dari path
        if (imagePath != null) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                binding.resultImageView.setImageURI(Uri.fromFile(imageFile))
            }
        }

        // Tampilkan teks hasil
        if (resultText != null) {
            val flavour = CommonMarkFlavourDescriptor()
            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(resultText)
            val html = HtmlGenerator(resultText, parsedTree, flavour).generateHtml()
            binding.resultTextView.text =
                HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        binding.backButton.setOnClickListener { activity?.supportFragmentManager?.popBackStack() }

        binding.scanAgainButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                "scan_again_request",
                Bundle().apply { putBoolean("open_camera", true) },
            )
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    // Companion object untuk membuat instance fragment dengan cara yang bersih
    companion object {
        private const val ARG_IMAGE_PATH = "image_path"
        private const val ARG_RESULT_TEXT = "result_text"

        fun newInstance(imagePath: String, resultText: String): ResultFragment {
            val fragment = ResultFragment()
            val args =
                Bundle().apply {
                    putString(ARG_IMAGE_PATH, imagePath)
                    putString(ARG_RESULT_TEXT, resultText)
                }
            fragment.arguments = args
            return fragment
        }
    }
}

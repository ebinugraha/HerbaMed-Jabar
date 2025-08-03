package edu.unikom.herbamedjabar.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.R
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
@AndroidEntryPoint
class ResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resultImageView: ImageView = view.findViewById(R.id.resultImageView)
        val resultTextView: TextView = view.findViewById(R.id.resultTextView)

        // Ambil data dari arguments
        val imagePath = arguments?.getString(ARG_IMAGE_PATH)
        val resultText = arguments?.getString(ARG_RESULT_TEXT)

        // Inisialisasi tombol baru
        val backButton: Button = view.findViewById(R.id.backButton)
        val scanAgainButton: Button = view.findViewById(R.id.scanAgainButton)

        // Tampilkan gambar dari path
        if (imagePath != null) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                resultImageView.setImageURI(Uri.fromFile(imageFile))
            }
        }

        // Tampilkan teks hasil
        if (resultText != null) {
            val flavour = CommonMarkFlavourDescriptor()
            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(resultText)
            val html = HtmlGenerator(resultText, parsedTree, flavour).generateHtml()
            resultTextView.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

    }

    // Companion object untuk membuat instance fragment dengan cara yang bersih
    companion object {
        private const val ARG_IMAGE_PATH = "image_path"
        private const val ARG_RESULT_TEXT = "result_text"

        fun newInstance(imagePath: String, resultText: String): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle().apply {
                putString(ARG_IMAGE_PATH, imagePath)
                putString(ARG_RESULT_TEXT, resultText)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

package edu.unikom.herbamedjabar.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.databinding.FragmentResultBinding
import edu.unikom.herbamedjabar.viewModel.ResultViewModel
import java.io.File
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

@AndroidEntryPoint
class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    // Inject ResultViewModel
    private val viewModel: ResultViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        val imagePath = arguments?.getString(ARG_IMAGE_PATH)
        val resultText = arguments?.getString(ARG_RESULT_TEXT)

        if (imagePath != null) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                binding.resultImageView.setImageURI(Uri.fromFile(imageFile))
            }
        }

        if (resultText != null) {
            val flavour = CommonMarkFlavourDescriptor()
            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(resultText)
            val html = HtmlGenerator(resultText, parsedTree, flavour).generateHtml()
            binding.resultTextView.text =
                HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { activity?.supportFragmentManager?.popBackStack() }

        binding.scanAgainButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                "scan_again_request",
                Bundle().apply { putBoolean("open_camera", true) },
            )
            activity?.supportFragmentManager?.popBackStack()
        }

        binding.btnPostToForum.setOnClickListener {
            val imagePath = arguments?.getString(ARG_IMAGE_PATH)
            val resultText = arguments?.getString(ARG_RESULT_TEXT)

            if (imagePath == null || resultText == null) {
                Toast.makeText(requireContext(), "Data tidak lengkap untuk diposting", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val plantName = resultText.lines().firstOrNull()
                ?.replace("#", "")?.replace("*", "")?.trim() ?: "Tanaman Hasil Scan"

            val imageUri = Uri.fromFile(File(imagePath))

            viewModel.createPostFromScan(
                imageUri = imageUri,
                plantName = plantName,
                description = resultText
            )
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnPostToForum.isEnabled = !isLoading
            binding.scanAgainButton.isEnabled = !isLoading
        }

        viewModel.postResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Berhasil diposting ke forum!", Toast.LENGTH_SHORT).show()
                // Kembali ke halaman scan setelah berhasil
                activity?.supportFragmentManager?.popBackStack()
            }.onFailure {
                Toast.makeText(requireContext(), "Gagal memposting: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

package edu.unikom.herbamedjabar.view

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import edu.unikom.herbamedjabar.databinding.FragmentProcessingDialogBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProcessingDialogFragment : DialogFragment() {

    private var _binding: FragmentProcessingDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set dialog agar fullscreen
        setStyle(STYLE_NORMAL, R.style.Theme_Material_Light_NoActionBar)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProcessingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animasikan progress bar untuk UX
        lifecycleScope.launch {
            var progress = 0
            while (progress <= 100) {
                binding.progressBar.progress = progress
                binding.progressTextView.text = "$progress%"
                progress++
                // Jeda bervariasi agar terlihat lebih natural
                val randomDelay = (50..150).random().toLong()
                delay(randomDelay)
                if (progress > 95) { // Jangan sampai 100% agar user tahu proses masih berjalan
                    delay(1000) // Tahan di akhir
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG: String = "ProcessingDialog"
    }
}

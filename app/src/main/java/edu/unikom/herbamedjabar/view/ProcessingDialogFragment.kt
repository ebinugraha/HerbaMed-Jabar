package edu.unikom.herbamedjabar.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import edu.unikom.herbamedjabar.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProcessingDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set dialog agar fullscreen
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_processing_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val progressTextView: TextView = view.findViewById(R.id.progressTextView)

        // Animasikan progress bar untuk UX
        lifecycleScope.launch {
            var progress = 0
            while (progress <= 100) {
                progressBar.progress = progress
                progressTextView.text = "$progress%"
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
}

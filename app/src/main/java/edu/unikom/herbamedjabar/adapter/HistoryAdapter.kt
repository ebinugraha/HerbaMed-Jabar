package edu.unikom.herbamedjabar.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import edu.unikom.herbamedjabar.R
import edu.unikom.herbamedjabar.data.ScanHistory
import edu.unikom.herbamedjabar.databinding.ItemHistoryBinding
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onClick: (ScanHistory) -> Unit
) :
    ListAdapter<ScanHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding =
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = getItem(position)
        holder.bind(historyItem, position)
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(history: ScanHistory, position: Int) { // Terima posisi di sini
            binding.apply {
                // Menggunakan ID dari layout baru Anda dan data class yang sudah diperbarui
                val flavour = CommonMarkFlavourDescriptor()
                val parsedTree =
                    MarkdownParser(flavour).buildMarkdownTreeFromString(history.resultText)
                val html = HtmlGenerator(history.resultText, parsedTree, flavour).generateHtml()
                historyTextView.text =
                    HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)

                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = Date(history.timestamp)
                time.text = sdf.format(date)

                val imageFile = File(history.imagePath)
                if (imageFile.exists()) {
                    historyImageView.load(Uri.fromFile(imageFile)) {
                        crossfade(true)
                        placeholder(R.drawable.bg_place_holder)
                    }
                }

                itemView.setOnClickListener {
                    onClick(history)
                }
            }
        }
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<ScanHistory>() {
    override fun areItemsTheSame(oldItem: ScanHistory, newItem: ScanHistory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ScanHistory, newItem: ScanHistory): Boolean {
        return oldItem == newItem
    }
}
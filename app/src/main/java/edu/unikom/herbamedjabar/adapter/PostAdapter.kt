package edu.unikom.herbamedjabar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.FirebaseAuth
import edu.unikom.herbamedjabar.R
import edu.unikom.herbamedjabar.data.Post
import edu.unikom.herbamedjabar.databinding.ItemPostBinding
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val onLikeClicked: (String) -> Unit,
    private val onDeleteClicked: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            binding.apply {
                tvUsername.text = post.username
                ivUserProfile.load(post.userProfilePictureUrl) {
                    placeholder(R.drawable.ic_user_image)
                    error(R.drawable.ic_user_image)
                }
                ivPostImage.load(post.imageUrl) {
                    placeholder(R.drawable.bg_place_holder)
                }

                fun formatMarkdownLists(input: String): String {
                    return input.replace(
                        Regex("""(\d+\.\s*)""")
                    ) { match ->
                        if (match.range.first == 0) match.value else "\n${match.value}"
                    }
                }

                val formattedContent = post.content ?: ""
                val formattedBenefit = formatMarkdownLists(post.benefit ?: "")
                val formattedWarning = formatMarkdownLists(post.warning ?: "")

                val flavour = CommonMarkFlavourDescriptor()

                val parsedTreeContent = MarkdownParser(flavour).buildMarkdownTreeFromString(formattedContent)
                val htmlContent = HtmlGenerator(formattedContent, parsedTreeContent, flavour).generateHtml()

                val parsedTreeBenefit = MarkdownParser(flavour).buildMarkdownTreeFromString(formattedBenefit)
                val htmlBenefit = HtmlGenerator(formattedBenefit, parsedTreeBenefit, flavour).generateHtml()

                val parsedTreeWarning = MarkdownParser(flavour).buildMarkdownTreeFromString(formattedWarning)
                val htmlWarning = HtmlGenerator(formattedWarning, parsedTreeWarning, flavour).generateHtml()

                tvPlantName.text = post.plantName
                tvContent.text = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvManfaat.text = HtmlCompat.fromHtml(htmlBenefit, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvEfek.text = HtmlCompat.fromHtml(htmlWarning, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvLikeCount.text = "${post.likes.size}"

                ivLike.setImageResource(
                    if (post.likes.contains(currentUser?.uid)) R.drawable.ic_heart_filled
                    else R.drawable.ic_hearth_outline
                )

                ivLike.setOnClickListener { onLikeClicked(post.id) }

                ivMenuOptions.visibility = if (post.userId == currentUser?.uid) View.VISIBLE else View.GONE
                ivMenuOptions.setOnClickListener { onDeleteClicked(post) }

                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                tvPostTimestamp.text = sdf.format(Date(post.timestamp))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
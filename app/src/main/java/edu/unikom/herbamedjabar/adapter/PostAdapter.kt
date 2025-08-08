package edu.unikom.herbamedjabar.adapter

import android.view.LayoutInflater
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

class PostAdapter(private val onLikeClicked: (String) -> Unit) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffCallback()) {

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
                    placeholder(R.drawable.ic_user)
                    error(R.drawable.ic_user)
                }
                ivPostImage.load(post.imageUrl) {
                    placeholder(R.drawable.bg_place_holder)
                }

                val flavour = CommonMarkFlavourDescriptor()
                val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(post.description)
                val html = HtmlGenerator(post.description, parsedTree, flavour).generateHtml()

                tvPlantName.text = post.plantName
                tvDescription.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvLikeCount.text = "${post.likes.size}"

                // Update ikon like berdasarkan status
                if (post.likes.contains(currentUser?.uid)) {
                    ivLike.setImageResource(R.drawable.ic_heart_filled)
                } else {
                    ivLike.setImageResource(R.drawable.ic_hearth_outline)
                }

                // Set listener klik
                ivLike.setOnClickListener {
                    onLikeClicked(post.id)
                }

                // Format timestamp
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = Date(post.timestamp)
                tvPostTimestamp.text = sdf.format(date)
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

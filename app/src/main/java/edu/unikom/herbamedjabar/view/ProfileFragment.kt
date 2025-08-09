package edu.unikom.herbamedjabar.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import edu.unikom.herbamedjabar.R
import edu.unikom.herbamedjabar.adapter.PostAdapter
import edu.unikom.herbamedjabar.databinding.FragmentProfileBinding
import edu.unikom.herbamedjabar.viewModel.ProfileViewModel

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            activity?.finish()
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter { postId ->
            viewModel.toggleLikeOnPost(postId)
        }
        binding.rvMyPosts.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                // Menggunakan tvFullName dari layout baru Anda
                binding.tvUsername.text = it.displayName ?: "Nama Pengguna"
                binding.tvEmail.text = it.email ?: "Email Pengguna"
                binding.ivProfilePicture.load(it.photoUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_user_image2)
                    error(R.drawable.ic_user_image2)
                }
            }
        }

        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
            val postCount = posts.size

            // Panggil fungsi untuk update lencana
            updateBadgesVisibility(postCount)

            if (posts.isEmpty()) {
                binding.tvNoPosts.visibility = View.VISIBLE
                binding.rvMyPosts.visibility = View.GONE
            } else {
                binding.tvNoPosts.visibility = View.GONE
                binding.rvMyPosts.visibility = View.VISIBLE
            }
        }
    }

    private fun updateBadgesVisibility(postCount: Int) {
        binding.apply {
            badge1.visibility = View.GONE
            badge2.visibility = View.GONE
            badge3.visibility = View.GONE
            badge4.visibility = View.GONE

            if (postCount >= 1) {
                badge1.visibility = View.VISIBLE
            }
            if (postCount >= 5) {
                badge2.visibility = View.VISIBLE
            }
            if (postCount >= 10) {
                badge3.visibility = View.VISIBLE
            }
            if (postCount >= 20) {
                badge4.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

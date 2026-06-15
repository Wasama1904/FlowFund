package com.flowfund.app.ui.transactions

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.flowfund.app.R
import com.flowfund.app.databinding.FragmentPhotoViewBinding

class PhotoViewFragment : DialogFragment() {
    private var _binding: FragmentPhotoViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FlowFund_FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPhotoViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val path = arguments?.getString(ARG_PATH)
        if (path != null) Glide.with(this).load(Uri.parse(path)).into(binding.ivFullPhoto)
        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    companion object {
        private const val ARG_PATH = "photo_path"
        fun newInstance(path: String) = PhotoViewFragment().apply {
            arguments = Bundle().apply { putString(ARG_PATH, path) }
        }
    }
}

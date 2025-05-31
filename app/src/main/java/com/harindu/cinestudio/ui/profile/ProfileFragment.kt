package com.harindu.cinestudio.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View // Make sure this import is present
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.harindu.cinestudio.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    // FIX: Changed 'View.VIEW' to 'View' for the parameter type
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // You can add logic here to populate profile data if you expand the dummy view
        // For example, if you had a ViewModel, you'd observe its LiveData here.
        // For now, the dummy layout itself provides the content.
        // Example of setting dummy data (if you want to see text):
        binding.profileName.text = "Harindu"
        binding.profileEmail.text = "harindu.dev@gmail.com"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding reference to prevent memory leaks
        _binding = null
    }
}
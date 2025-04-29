package com.example.cashroyale

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.cashroyale.databinding.FragmentCalenderBinding

class CalenderFragment : Fragment() {

    private var _binding: FragmentCalenderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalenderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnExpenses.setOnClickListener {
            viewModel.onExpensesButtonClicked()
        }

        viewModel.navigateToExpenses.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate == true) {
                val intent = Intent(requireContext(), Expenses::class.java)
                startActivity(intent)
                viewModel.onExpensesNavigationComplete()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

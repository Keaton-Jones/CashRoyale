package com.example.cashroyale.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cashroyale.viewmodels.GoalViewModel
import com.example.cashroyale.R

class GoalFragment : Fragment() {

    companion object {
        /** Creates a new instance of the GoalFragment. */
        fun newInstance() = GoalFragment()
    }

    private val viewModel: GoalViewModel by viewModels() // Initializes the ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflates the layout for this fragment
        return inflater.inflate(R.layout.fragment_goal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
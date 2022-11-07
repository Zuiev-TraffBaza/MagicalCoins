package com.big.win.casino.games.main.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.big.win.casino.games.R
import com.big.win.casino.games.databinding.FragmentGameBinding
import kotlin.system.exitProcess

class GameFragment : Fragment() {
    private lateinit var binding : FragmentGameBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val clickableStartGame = listOf(binding.imageViewStartGame, binding.textViewStartGame)

        clickableStartGame.forEach { it.setOnClickListener{
         findNavController().navigate(R.id.navigation_slot)
        }}

        val clickableExitGame = listOf(binding.imageViewExitGame, binding.textViewExitGame)

        clickableExitGame.forEach { it.setOnClickListener {
            requireActivity().finishAffinity()
            exitProcess(0)
        }}

        val clickablePolicy = listOf(binding.imageViewHelpButton, binding.imageViewHelp)
        clickablePolicy.forEach {
            it.setOnClickListener {
                findNavController().navigate(R.id.navigation_policy)
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                }
            })
    }

}

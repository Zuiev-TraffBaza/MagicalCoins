package com.big.win.casino.games.main.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.big.win.casino.games.R
import com.big.win.casino.games.databinding.FragmentSlotBinding
import kotlinx.coroutines.*


class SlotFragment : Fragment() {
 private val gameScope = CoroutineScope(Job() + Dispatchers.IO)
 private lateinit var binding : FragmentSlotBinding
 private var balance = 5000
    set(value) {
        if (balance > 50) {
            binding.textViewBalance.text = value.toString()
            field = value
        } else {
            Toast.makeText(requireContext(),"\"Whew, Good we have another points for you!:)\"" , Toast.LENGTH_SHORT).show()
            field = 500
        }

    }
 private val normalSymbols = mapOf(
     "100" to R.drawable.symbol_00,
     "200" to R.drawable.symbol_01,
     "300" to R.drawable.symbol_02,
     "400" to R.drawable.symbol_03,
     "500" to R.drawable.symbol_04,
     "600" to R.drawable.symbol_05,
     "700" to R.drawable.symbol_06
 )
 private val allSymbols = normalSymbols + mapOf(
     "scatter" to R.drawable.scatter_symbol,
     "wild" to R.drawable.wild_symbol
 )
 private lateinit var lines : List<List<ImageView>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSlotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            lines = listOf(
                listOf(imageView11, imageView21, imageView31),
                listOf(imageView12, imageView22, imageView32),
                listOf(imageView13, imageView23, imageView33),
            )
        }

        binding.imageViewSpin.setOnClickListener {
            initSpin()
        }
    }

    private fun initSpin(){
        binding.imageViewSpin.isFocusable = false
        binding.imageViewSpin.isClickable = false
        balance -= 50
        gameScope.launch {
            for(i in lines.indices){
                delay(100L)
                lines[i].forEach {
                    val randomSymbol = allSymbols.entries.random()
                    launch(Dispatchers.Main) {
                        it.setImageResource(randomSymbol.value)
                        it.tag = randomSymbol.key
                    }
                    delay(100L)
                }
            }
            launch(Dispatchers.Main) {
                binding.imageViewSpin.isFocusable = true
                binding.imageViewSpin.isClickable = true
                calculateSpin()
            }
        }
    }
    private fun calculateSpin(){
        var scatter = 0
        lines.forEach { it.forEach { if(it.tag == "scatter") scatter++ } }
        if(scatter == 3){
            balance += 10000
            Toast.makeText(requireContext(),"BIG WIN!!!! FOR YOU" , Toast.LENGTH_SHORT).show()
            binding.textViewBalance.text = String.format("Great")
        }
        lines.forEach {
           val elements =  it.groupBy { it.tag.toString() }
            elements.values.find {
                it.size >= 2
            }
            val winningElement = elements.values.find {
                it.count() >= 2
            }?.get(0)
            val normalSymbol  = normalSymbols.entries.find { it.key == winningElement?.tag }?.key
            if(normalSymbol != null){
                balance += normalSymbol.toInt()*2
            }
        }
    }
}
package com.example.cashroyale.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.cashroyale.R
import com.example.cashroyale.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatisticsFragment : Fragment() {

    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = view.findViewById(R.id.chart)

        // You’ll replace this with real data from Firestore or Room
        val dummyData = mapOf(
            "Food" to 120.5f,
            "Transport" to 60f,
            "Entertainment" to 90.25f,
            "Bills" to 150f
        )

        showBarChart(dummyData)
    }

    private fun showBarChart(data: Map<String, Float>) {
        val entries = data.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value)
        }

        val dataSet = BarDataSet(entries, "Expenses by Category").apply {
            valueTextSize = 14f
        }

        barChart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(data.keys.toList())
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM

            description = Description().apply { text = "" }
            animateY(1000)
            setFitBars(true)
            this.data = BarData(dataSet)
            invalidate()
        }
    }
}

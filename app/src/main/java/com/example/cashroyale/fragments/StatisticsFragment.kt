package com.example.cashroyale.fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cashroyale.R
import com.example.cashroyale.Services.FireStore
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticsFragment : Fragment() {

    private lateinit var edtStart: EditText
    private lateinit var edtEnd: EditText
    private lateinit var btnGenerate: Button
    private lateinit var chart: PieChart
    private lateinit var legendLayout: LinearLayout

    private val firestore by lazy { FireStore(FirebaseFirestore.getInstance()) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_statistics, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        edtStart = view.findViewById(R.id.edtStartDate)
        edtEnd = view.findViewById(R.id.edtEndDate)
        btnGenerate = view.findViewById(R.id.btnGenerate)
        chart = view.findViewById(R.id.pieChart)
        legendLayout = view.findViewById(R.id.legendLayout)

        edtStart.setOnClickListener { pickDate(edtStart) }
        edtEnd.setOnClickListener { pickDate(edtEnd) }

        btnGenerate.setOnClickListener {
            val start = edtStart.text.toString()
            val end = edtEnd.text.toString()

            if (start.isBlank() || end.isBlank()) {
                Toast.makeText(context, "Pick both dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startDate = formatter.parse(start)!!
            val endDate = formatter.parse(end)!!

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            lifecycleScope.launch {
                val cats = firestore.getAllCategoriesFlow(uid).first()
                val exps = firestore.getAllExpensesFlow(uid).first()

                val filtered = exps.filter {
                    val d = formatter.parse(it.date)
                    d != null && !d.before(startDate) && !d.after(endDate)
                }

                val totals = cats.associate { it.name to 0.0 }.toMutableMap()
                filtered.forEach { totals[it.category] = totals.getOrDefault(it.category, 0.0) + it.amount }

                displayPieChart(totals.filter { it.value > 0.0 })
            }
        }
    }

    private fun pickDate(target: EditText) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
            val selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
            target.setText(selectedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun displayPieChart(data: Map<String, Double>) {
        val entries = data.map { PieEntry(it.value.toFloat(), it.key) }
        val colors = generateColorPalette(data.size)

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            setColors(colors)
            valueTextColor = Color.WHITE
            valueTextSize = 14f
            sliceSpace = 3f
        }

        chart.apply {
            this.data = PieData(dataSet)
            description.isEnabled = false
            centerText = "Expenses"
            animateY(800)
            setEntryLabelColor(Color.BLACK)
            setUsePercentValues(false)
            setDrawEntryLabels(true)
            legend.isEnabled = false
            invalidate()
        }

        renderLegend(data, colors)
    }

    private fun renderLegend(data: Map<String, Double>, colors: List<Int>) {
        legendLayout.removeAllViews()
        val inflater = LayoutInflater.from(context)
        data.keys.forEachIndexed { index, name ->
            val item = TextView(context).apply {
                text = "⬤ $name"
                setPadding(10)
                setTextColor(colors[index])
                textSize = 16f
            }
            legendLayout.addView(item)
        }
    }

    private fun generateColorPalette(count: Int): List<Int> {
        val baseColors = listOf(
            Color.parseColor("#FF6F61"),
            Color.parseColor("#6B5B95"),
            Color.parseColor("#88B04B"),
            Color.parseColor("#F7CAC9"),
            Color.parseColor("#92A8D1"),
            Color.parseColor("#955251"),
            Color.parseColor("#B565A7"),
            Color.parseColor("#009B77")
        )
        return List(count) { baseColors[it % baseColors.size] }
    }
}

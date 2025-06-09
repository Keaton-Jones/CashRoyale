package com.example.cashroyale.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cashroyale.R
import com.example.cashroyale.Services.FireStore
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
    private lateinit var chart: BarChart

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
        chart = view.findViewById(R.id.chart)



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

                displayChart(totals)
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


    private fun displayChart(data: Map<String, Double>) {
        val entries = data.entries
            .filter { it.value > 0 }
            .mapIndexed { i, e -> BarEntry(i.toFloat(), e.value.toFloat()) }
        val adapter = BarDataSet(entries, "Spent per Category")
        val barData = BarData(adapter)
        chart.apply {
            this.data = barData
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(data.keys.toList())
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
            }
            setFitBars(true)
            description = Description().apply { text = "" }
            animateY(500)
            invalidate()
        }
    }
}
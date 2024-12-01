package com.example.reimbifyapp.user.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.reimbifyapp.databinding.FragmentDashboardUserBinding
import com.example.reimbifyapp.user.viewmodel.DashboardViewModel

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dashboard_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set Greeting
        user_greeting.text = "Hello, John Doe"

        // Setup Reimbursement Chart
        setupReimbursementHistory(line_chart)
    }

    private fun setupReimbursementHistory(chart: LineChart) {
        val entries = listOf(
            Entry(0f, 200f),
            Entry(1f, 800f),
            Entry(2f, 600f),
            Entry(3f, 400f),
            Entry(4f, 600f)
        )

        val dataSet = LineDataSet(entries, "Reimbursement History").apply {
            lineWidth = 2f
            circleRadius = 4f
        }

        val lineData = LineData(dataSet)
        chart.apply {
            data = lineData
            description.isEnabled = false
            axisLeft.setDrawLabels(false)
            axisRight.setDrawLabels(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            invalidate()
        }
    }
}
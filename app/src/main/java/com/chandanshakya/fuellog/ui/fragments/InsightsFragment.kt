package com.chandanshakya.fuellog.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chandanshakya.fuellog.FuelLogApplication
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.AppViewModelFactory
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.databinding.FragmentInsightsBinding
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.InsightsViewModel
import com.chandanshakya.fuellog.viewmodel.MileageTrend
import kotlinx.coroutines.launch

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as FuelLogApplication }

    private val viewModel: InsightsViewModel by viewModels {
        AppViewModelFactory.insights(app.vehicleDao, app.fuelEntryDao, app.userSettingsDao, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.toolbar.navigationIcon?.setTint(requireContext().getColor(R.color.md_theme_light_primary))

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.insightsState.collect { state -> updateUi(state) }
            }
        }
    }

    private fun updateUi(state: com.chandanshakya.fuellog.viewmodel.InsightsState) {
        binding.toolbar.title = state.vehicle?.let { "${it.name} Insights" } ?: getString(R.string.insights_title)

        if (state.entries.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            return
        }

        binding.emptyState.visibility = View.GONE

        val distanceUnit = state.vehicle?.distanceUnit ?: DistanceUnit.KM
        val volumeUnit = state.vehicle?.volumeUnit ?: VolumeUnit.LITERS
        val efficiencyLabel = UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)

        if (state.mileageDataPoints.isNotEmpty()) {
            binding.cardMileageTrend.visibility = View.VISIBLE
            binding.mileageChart.setData(
                values = state.mileageDataPoints.map { it.mileage },
                dates = state.mileageDataPoints.map { it.date },
                valueLabel = { "%.2f".format(it) },
                lineColor = ContextCompat.getColor(requireContext(), R.color.chart_line_primary)
            )
            updateTrendIndicator(state.mileageTrend)
        } else {
            binding.cardMileageTrend.visibility = View.GONE
        }

        populateInfoCard(binding.cardAvgMileage, R.drawable.ic_speed, state.averageMileageKmPerLiter?.let { "%.2f $efficiencyLabel".format(it) } ?: getString(R.string.na_value), getString(R.string.average_mileage))
        populateInfoCard(binding.cardBestMileage, R.drawable.ic_arrow_upward, state.bestMileageKmPerLiter?.let { "%.2f $efficiencyLabel".format(it) } ?: getString(R.string.na_value), getString(R.string.best_mileage))
        populateInfoCard(binding.cardWorstMileage, R.drawable.ic_arrow_downward, state.worstMileageKmPerLiter?.let { "%.2f $efficiencyLabel".format(it) } ?: getString(R.string.na_value), getString(R.string.worst_mileage))
        populateInfoCard(binding.cardEntriesCount, R.drawable.ic_analytics, state.entriesCount.toString(), getString(R.string.total_entries))
        populateInfoCard(binding.cardTotalDistance, R.drawable.ic_road, "%.2f %s".format(state.totalDistanceKm, UnitConverter.getDistanceUnitLabel(distanceUnit)), getString(R.string.total_distance))
        populateInfoCard(binding.cardTotalFuel, R.drawable.ic_local_gas_station, "%.2f %s".format(state.totalFuelLiters, UnitConverter.getVolumeUnitLabel(volumeUnit)), getString(R.string.total_fuel))
        populateInfoCard(binding.cardTotalCost, R.drawable.ic_payments, CurrencyFormatter.formatCurrency(state.totalCost, state.currency), getString(R.string.total_cost))
        populateInfoCard(binding.cardCostPerKm, R.drawable.ic_speed, state.costPerKm?.let { CurrencyFormatter.formatCurrency(it, state.currency) } ?: getString(R.string.na_value), getString(R.string.cost_per_km))

        if (state.priceDataPoints.isNotEmpty()) {
            binding.cardPriceTrend.visibility = View.VISIBLE
            binding.textPriceTrendTitle.text = getString(R.string.fuel_price_trend, UnitConverter.getVolumeUnitLabel(volumeUnit))
            val symbol = CurrencyFormatter.getCurrencySymbol(state.currency)
            binding.priceChart.setData(
                values = state.priceDataPoints.map { it.pricePerUnit },
                dates = state.priceDataPoints.map { it.date },
                valueLabel = { "$symbol%.2f".format(it) },
                lineColor = ContextCompat.getColor(requireContext(), R.color.chart_line_secondary)
            )
        } else {
            binding.cardPriceTrend.visibility = View.GONE
        }
    }

    private fun populateInfoCard(card: View, iconRes: Int, value: String, label: String) {
        card.findViewById<ImageView>(R.id.icon)?.setImageResource(iconRes)
        card.findViewById<TextView>(R.id.text_value)?.text = value
        card.findViewById<TextView>(R.id.text_label)?.text = label
    }

    private fun updateTrendIndicator(trend: MileageTrend) {
        when (trend) {
            MileageTrend.IMPROVING -> {
                binding.iconTrend.setImageResource(R.drawable.ic_arrow_upward)
                binding.iconTrend.setColorFilter(ContextCompat.getColor(requireContext(), R.color.trend_improving))
                binding.textTrend.text = getString(R.string.improving)
                binding.textTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.trend_improving))
            }
            MileageTrend.DECLINING -> {
                binding.iconTrend.setImageResource(R.drawable.ic_arrow_downward)
                binding.iconTrend.setColorFilter(ContextCompat.getColor(requireContext(), R.color.trend_declining))
                binding.textTrend.text = getString(R.string.declining)
                binding.textTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.trend_declining))
            }
            MileageTrend.STABLE -> {
                binding.iconTrend.setImageResource(R.drawable.ic_trending_flat)
                binding.iconTrend.setColorFilter(ContextCompat.getColor(requireContext(), R.color.trend_stable))
                binding.textTrend.text = getString(R.string.stable)
                binding.textTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.trend_stable))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(vehicleId: Long) = InsightsFragment().apply {
            arguments = Bundle().apply { putLong("vehicleId", vehicleId) }
        }
    }
}

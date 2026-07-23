package com.chandanshakya.fuellog.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandanshakya.fuellog.FuelLogApplication
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.AppViewModelFactory
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.databinding.FragmentFuelLogBinding
import com.chandanshakya.fuellog.ui.dialogs.AddFuelEntryDialogFragment
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.FuelLogViewModel
import kotlinx.coroutines.launch

class FuelLogFragment : Fragment() {

    private var _binding: FragmentFuelLogBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as FuelLogApplication }

    private val viewModel: FuelLogViewModel by viewModels {
        AppViewModelFactory.fuelLog(app.vehicleDao, app.fuelEntryDao, app.userSettingsDao, this)
    }

    private lateinit var adapter: FuelEntryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFuelLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FuelEntryAdapter(
            onEdit = { entry -> showEditDialog(entry) },
            onDelete = { entry -> viewModel.deleteFuelEntry(entry.id) }
        )

        binding.recyclerFuelEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFuelEntries.adapter = adapter

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.toolbar.inflateMenu(R.menu.menu_fuel_log)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_insights -> { navigateToInsights(); true }
                else -> false
            }
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fuelLogState.collect { state -> updateUi(state) }
            }
        }
    }

    private fun showAddDialog() {
        val state = viewModel.fuelLogState.value
        val v = state.vehicle
        AddFuelEntryDialogFragment.newInstanceForNew(
            v?.distanceUnit ?: DistanceUnit.KM,
            v?.volumeUnit ?: VolumeUnit.LITERS,
            state.currency
        ).show(childFragmentManager, "add_fuel_entry")
    }

    private fun showEditDialog(entry: FuelEntry) {
        val state = viewModel.fuelLogState.value
        val v = state.vehicle
        AddFuelEntryDialogFragment.newInstanceForEdit(
            entry,
            v?.distanceUnit ?: DistanceUnit.KM,
            v?.volumeUnit ?: VolumeUnit.LITERS,
            state.currency
        ).show(childFragmentManager, "edit_fuel_entry")
    }

    private fun updateUi(state: com.chandanshakya.fuellog.viewmodel.FuelLogState) {
        binding.toolbar.title = state.vehicle?.name ?: getString(R.string.fuel_log_title)

        if (state.vehicle != null && state.entries.isNotEmpty()) {
            binding.summaryStats.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE

            val v = state.vehicle
            binding.textAvgMileageValue.text = state.averageMileage?.let {
                "%.2f %s".format(it, UnitConverter.getEfficiencyLabel(v.distanceUnit, v.volumeUnit))
            } ?: getString(R.string.na_value)

            binding.textTotalDistanceValue.text = "%.2f %s".format(state.totalDistance, UnitConverter.getDistanceUnitLabel(v.distanceUnit))
            binding.textTotalCostValue.text = CurrencyFormatter.formatCurrency(state.totalCost, state.currency)
        } else {
            binding.summaryStats.visibility = View.GONE
            binding.divider.visibility = View.GONE
        }

        adapter.submitList(state.entries)
        binding.emptyState.visibility = if (state.entries.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerFuelEntries.visibility = if (state.entries.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun navigateToInsights() {
        val vehicleId = arguments?.getLong("vehicleId", -1L) ?: return
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, InsightsFragment.newInstance(vehicleId))
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(vehicleId: Long) = FuelLogFragment().apply {
            arguments = Bundle().apply { putLong("vehicleId", vehicleId) }
        }
    }
}

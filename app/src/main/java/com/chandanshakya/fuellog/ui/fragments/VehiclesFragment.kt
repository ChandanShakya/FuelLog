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
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.databinding.FragmentVehiclesBinding
import com.chandanshakya.fuellog.ui.dialogs.AddVehicleDialogFragment
import com.chandanshakya.fuellog.viewmodel.VehiclesState
import com.chandanshakya.fuellog.viewmodel.VehiclesViewModel
import kotlinx.coroutines.launch

class VehiclesFragment : Fragment() {

    private var _binding: FragmentVehiclesBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as FuelLogApplication }

    private val viewModel: VehiclesViewModel by viewModels {
        AppViewModelFactory.vehicles(app.vehicleDao, app.userSettingsDao, app.fuelEntryDao)
    }

    private lateinit var adapter: VehicleAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVehiclesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VehicleAdapter(
            onClick = { vehicle -> navigateToFuelLog(vehicle.id) },
            onEdit = { vehicle -> showEditDialog(vehicle) },
            onDelete = { vehicle -> viewModel.deleteVehicle(vehicle.id) }
        )

        binding.recyclerVehicles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerVehicles.adapter = adapter

        binding.toolbar.title = getString(R.string.vehicles_title)
        binding.toolbar.inflateMenu(R.menu.menu_vehicles)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> { navigateToSettings(); true }
                else -> false
            }
        }

        binding.fabAdd.setOnClickListener {
            AddVehicleDialogFragment().show(childFragmentManager, "add_vehicle")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.vehiclesState.collect { state -> updateUi(state) }
            }
        }
    }

    private fun updateUi(state: VehiclesState) {
        adapter.submitList(state.vehicles)
        binding.emptyState.visibility = if (state.vehicles.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerVehicles.visibility = if (state.vehicles.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun navigateToFuelLog(vehicleId: Long) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FuelLogFragment.newInstance(vehicleId))
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToSettings() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SettingsFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showEditDialog(vehicle: Vehicle) {
        AddVehicleDialogFragment.newInstance(vehicle).show(childFragmentManager, "edit_vehicle")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

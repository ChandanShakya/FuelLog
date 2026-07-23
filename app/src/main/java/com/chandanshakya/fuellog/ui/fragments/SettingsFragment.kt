package com.chandanshakya.fuellog.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chandanshakya.fuellog.FuelLogApplication
import com.chandanshakya.fuellog.AppViewModelFactory
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.databinding.FragmentSettingsBinding
import com.chandanshakya.fuellog.viewmodel.SettingsState
import com.chandanshakya.fuellog.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as FuelLogApplication }

    private val viewModel: SettingsViewModel by viewModels {
        AppViewModelFactory.settings(app.userSettingsDao)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.settings_title)
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.toolbar.navigationIcon?.setTint(requireContext().getColor(R.color.md_theme_light_primary))

        binding.btnSave.setOnClickListener { saveSettings() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settingsState.collect { state -> updateUi(state) }
            }
        }
    }

    private fun updateUi(state: SettingsState) {
        state.settings?.let { settings ->
            if (binding.editCurrency.text.isNullOrEmpty() || binding.editCurrency.text.toString() != settings.defaultCurrency) {
                binding.editCurrency.setText(settings.defaultCurrency)
            }
            when (settings.defaultDistanceUnit) {
                DistanceUnit.KM -> binding.radioKm.isChecked = true
                DistanceUnit.MILES -> binding.radioMiles.isChecked = true
            }
            when (settings.defaultVolumeUnit) {
                VolumeUnit.LITERS -> binding.radioLiters.isChecked = true
                VolumeUnit.GALLONS -> binding.radioGallons.isChecked = true
            }
        }
    }

    private fun saveSettings() {
        val currency = binding.editCurrency.text.toString().uppercase().trim()
        val distanceUnit = if (binding.radioKm.isChecked) DistanceUnit.KM else DistanceUnit.MILES
        val volumeUnit = if (binding.radioLiters.isChecked) VolumeUnit.LITERS else VolumeUnit.GALLONS

        viewModel.updateSettings(currency, distanceUnit, volumeUnit)
        Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

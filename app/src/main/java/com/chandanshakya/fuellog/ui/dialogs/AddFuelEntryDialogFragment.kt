package com.chandanshakya.fuellog.ui.dialogs

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.chandanshakya.fuellog.FuelLogApplication
import com.chandanshakya.fuellog.AppViewModelFactory
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.databinding.DialogAddFuelEntryBinding
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.util.Validation
import com.chandanshakya.fuellog.viewmodel.FuelLogViewModel
import java.time.LocalDate
import java.util.Calendar

class AddFuelEntryDialogFragment : DialogFragment() {

    private var _binding: DialogAddFuelEntryBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as FuelLogApplication }

    private val viewModel: FuelLogViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    ) {
        AppViewModelFactory.fuelLog(app.vehicleDao, app.fuelEntryDao, app.userSettingsDao, requireParentFragment())
    }

    private var selectedDate: LocalDate = LocalDate.now()
    private var editEntry: FuelEntry? = null
    private var distanceUnit = DistanceUnit.KM
    private var volumeUnit = VolumeUnit.LITERS
    private var currency = "USD"
    private var isNewEntry = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isNewEntry = arguments?.getBoolean("isNewEntry", true) ?: true
        if (!isNewEntry) {
            editEntry = arguments?.let { BundleCompat.getParcelable(it, "entry", FuelEntry::class.java) }
        }
        distanceUnit = DistanceUnit.valueOf(arguments?.getString("distanceUnit") ?: "KM")
        volumeUnit = VolumeUnit.valueOf(arguments?.getString("volumeUnit") ?: "LITERS")
        currency = arguments?.getString("currency") ?: "USD"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddFuelEntryBinding.inflate(LayoutInflater.from(requireContext()))

        if (!isNewEntry) {
            editEntry?.let { entry ->
                selectedDate = entry.date
                binding.editOdometer.setText("%.2f".format(entry.odometer))
                binding.editVolume.setText("%.2f".format(entry.fuelVolume))
                binding.editCost.setText("%.2f".format(entry.fuelCost))
                if (entry.fuelVolume > 0) {
                    binding.editRate.setText("%.2f".format(entry.fuelCost / entry.fuelVolume))
                }
            }
        }

        updateFieldLabels()
        setupInputModeToggle()
        setupTextWatchers()
        setupDatePicker()

        return AlertDialog.Builder(requireContext())
            .setTitle(if (!isNewEntry) R.string.edit_fuel_entry else R.string.add_fuel_entry)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    override fun onStart() {
        super.onStart()
        (dialog as? AlertDialog)?.getButton(Dialog.BUTTON_POSITIVE)?.setOnClickListener { save() }
    }

    private fun setupInputModeToggle() {
        binding.btnModeVolRate.isChecked = true
        binding.toggleInputMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.btn_mode_vol_rate -> showVolRateMode()
                R.id.btn_mode_vol_cost -> showVolCostMode()
                R.id.btn_mode_rate_cost -> showRateCostMode()
            }
        }
        showVolRateMode()
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateComputedText() }
        }
        binding.editVolume.addTextChangedListener(watcher)
        binding.editRate.addTextChangedListener(watcher)
        binding.editCost.addTextChangedListener(watcher)
    }

    private fun showVolRateMode() {
        binding.editVolume.visibility = View.VISIBLE
        binding.editRate.visibility = View.VISIBLE
        binding.editCost.visibility = View.GONE
        binding.editVolume.hint = getString(R.string.fuel_volume_label, UnitConverter.getVolumeUnitLabel(volumeUnit))
        binding.editRate.hint = getString(R.string.rate_label, currency, UnitConverter.getVolumeUnitLabel(volumeUnit))
        updateComputedText()
    }

    private fun showVolCostMode() {
        binding.editVolume.visibility = View.VISIBLE
        binding.editRate.visibility = View.GONE
        binding.editCost.visibility = View.VISIBLE
        binding.editVolume.hint = getString(R.string.fuel_volume_label, UnitConverter.getVolumeUnitLabel(volumeUnit))
        binding.editCost.hint = getString(R.string.total_cost_label, currency)
        updateComputedText()
    }

    private fun showRateCostMode() {
        binding.editVolume.visibility = View.GONE
        binding.editRate.visibility = View.VISIBLE
        binding.editCost.visibility = View.VISIBLE
        binding.editRate.hint = getString(R.string.rate_label, currency, UnitConverter.getVolumeUnitLabel(volumeUnit))
        binding.editCost.hint = getString(R.string.total_cost_label, currency)
        updateComputedText()
    }

    private fun updateFieldLabels() {
        binding.textDate.text = getString(R.string.date_label, selectedDate.toString())
        binding.editOdometer.hint = getString(R.string.odometer_label, UnitConverter.getDistanceUnitLabel(distanceUnit))
    }

    private fun setupDatePicker() {
        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate = LocalDate.of(year, month + 1, day)
                    updateFieldLabels()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateComputedText() {
        val vol = binding.editVolume.text.toString().toDoubleOrNull() ?: 0.0
        val rate = binding.editRate.text.toString().toDoubleOrNull() ?: 0.0
        val cost = binding.editCost.text.toString().toDoubleOrNull() ?: 0.0

        val volVisible = binding.editVolume.visibility == View.VISIBLE
        val rateVisible = binding.editRate.visibility == View.VISIBLE

        val volLabel = UnitConverter.getVolumeUnitLabel(volumeUnit)
        binding.textComputed.text = when {
            volVisible && rateVisible -> {
                val computed = if (vol > 0 && rate > 0) vol * rate else cost
                getString(R.string.total_cost_value, currency, if (computed > 0) "%.2f".format(computed) else "--")
            }
            volVisible -> {
                val computed = if (vol > 0 && cost > 0) cost / vol else rate
                getString(R.string.rate_value, currency, volLabel, if (computed > 0) "%.2f".format(computed) else "--")
            }
            else -> {
                val computed = if (rate > 0 && cost > 0) cost / rate else vol
                getString(R.string.fuel_volume_value, volLabel, if (computed > 0) "%.2f".format(computed) else "--")
            }
        }
    }

    private fun save() {
        val odo = binding.editOdometer.text.toString().toDoubleOrNull() ?: 0.0
        val vol = binding.editVolume.text.toString().toDoubleOrNull() ?: 0.0
        val rate = binding.editRate.text.toString().toDoubleOrNull() ?: 0.0
        val cost = binding.editCost.text.toString().toDoubleOrNull() ?: 0.0

        val odoError = Validation.getOdometerError(odo)
        val volError = if (binding.editVolume.visibility == View.VISIBLE) Validation.getFuelVolumeError(vol) else null
        val costError = if (binding.editCost.visibility == View.VISIBLE) Validation.getFuelCostError(cost) else null

        binding.editOdometer.error = odoError
        if (volError != null) binding.editVolume.error = volError
        if (costError != null) binding.editCost.error = costError

        if (odoError != null || volError != null || costError != null) return

        val volVisible = binding.editVolume.visibility == View.VISIBLE
        val rateVisible = binding.editRate.visibility == View.VISIBLE

        val finalVol: Double
        val finalCost: Double
        when {
            volVisible && rateVisible -> { finalVol = vol; finalCost = if (vol > 0 && rate > 0) vol * rate else cost }
            volVisible -> { finalVol = vol; finalCost = cost }
            else -> { finalVol = if (rate > 0 && cost > 0) cost / rate else vol; finalCost = cost }
        }

        val existing = editEntry
        if (existing != null) {
            viewModel.updateFuelEntry(existing.id, selectedDate, odo, finalVol, finalCost)
        } else {
            viewModel.addFuelEntry(selectedDate, odo, finalVol, finalCost)
        }

        parentFragmentManager.setFragmentResult("fuel_entry_saved", Bundle.EMPTY)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstanceForNew(distanceUnit: DistanceUnit, volumeUnit: VolumeUnit, currency: String) =
            AddFuelEntryDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isNewEntry", true)
                    putString("distanceUnit", distanceUnit.name)
                    putString("volumeUnit", volumeUnit.name)
                    putString("currency", currency)
                }
            }

        fun newInstanceForEdit(entry: FuelEntry, distanceUnit: DistanceUnit, volumeUnit: VolumeUnit, currency: String) =
            AddFuelEntryDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isNewEntry", false)
                    putParcelable("entry", entry)
                    putString("distanceUnit", distanceUnit.name)
                    putString("volumeUnit", volumeUnit.name)
                    putString("currency", currency)
                }
            }
    }
}

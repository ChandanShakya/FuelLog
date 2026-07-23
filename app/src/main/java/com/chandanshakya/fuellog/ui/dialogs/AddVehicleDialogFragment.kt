package com.chandanshakya.fuellog.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.BundleCompat
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.chandanshakya.fuellog.FuelLogApplication
import com.chandanshakya.fuellog.AppViewModelFactory
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VehicleType
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.databinding.DialogAddVehicleBinding
import com.chandanshakya.fuellog.util.Validation
import com.chandanshakya.fuellog.viewmodel.VehiclesViewModel

class AddVehicleDialogFragment : DialogFragment() {

    private var _binding: DialogAddVehicleBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as FuelLogApplication }

    private val viewModel: VehiclesViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    ) {
        AppViewModelFactory.vehicles(app.vehicleDao, app.userSettingsDao, app.fuelEntryDao)
    }

    private var selectedVehicleType: VehicleType = VehicleType.CAR
    private var editVehicle: Vehicle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editVehicle = arguments?.let { BundleCompat.getParcelable(it, "vehicle", Vehicle::class.java) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddVehicleBinding.inflate(LayoutInflater.from(requireContext()))

        editVehicle?.let { vehicle ->
            binding.editName.setText(vehicle.name)
            selectedVehicleType = vehicle.vehicleType
            when (vehicle.distanceUnit) {
                DistanceUnit.KM -> binding.radioKm.isChecked = true
                DistanceUnit.MILES -> binding.radioMiles.isChecked = true
            }
            when (vehicle.volumeUnit) {
                VolumeUnit.LITERS -> binding.radioLiters.isChecked = true
                VolumeUnit.GALLONS -> binding.radioGallons.isChecked = true
            }
        }

        buildVehicleTypeGrid()

        return AlertDialog.Builder(requireContext())
            .setTitle(if (editVehicle != null) R.string.edit_vehicle else R.string.add_vehicle)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    override fun onStart() {
        super.onStart()
        (dialog as? AlertDialog)?.getButton(Dialog.BUTTON_POSITIVE)?.setOnClickListener { save() }
    }

    private fun buildVehicleTypeGrid() {
        val grid = binding.gridVehicleType
        grid.removeAllViews()

        for (type in VehicleType.entries) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_vehicle_type_button, grid, false)

            itemView.findViewById<ImageView>(R.id.icon_type).setImageResource(type.iconRes)
            itemView.findViewById<TextView>(R.id.text_type).text = type.label
            updateTypeSelection(itemView, type == selectedVehicleType)

            itemView.setOnClickListener {
                selectedVehicleType = type
                for (i in 0 until grid.childCount) {
                    updateTypeSelection(grid.getChildAt(i), grid.getChildAt(i) == itemView)
                }
            }

            grid.addView(itemView, GridLayout.LayoutParams().apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            })
        }
    }

    private fun updateTypeSelection(view: View, selected: Boolean) {
        val icon = view.findViewById<ImageView>(R.id.icon_type)
        val text = view.findViewById<TextView>(R.id.text_type)
        val color = if (selected) R.color.md_theme_light_primary else R.color.md_theme_light_onSurfaceVariant
        view.setBackgroundResource(if (selected) R.drawable.bg_badge else 0)
        icon.setColorFilter(requireContext().getColor(color))
        text.setTextColor(requireContext().getColor(color))
    }

    private fun save() {
        val name = binding.editName.text.toString().trim()
        val nameError = Validation.getVehicleNameError(name)
        binding.editName.error = nameError
        if (nameError != null) return

        val distanceUnit = if (binding.radioKm.isChecked) DistanceUnit.KM else DistanceUnit.MILES
        val volumeUnit = if (binding.radioLiters.isChecked) VolumeUnit.LITERS else VolumeUnit.GALLONS

        val existing = editVehicle
        if (existing != null) {
            viewModel.updateVehicle(existing.copy(name = name, vehicleType = selectedVehicleType, distanceUnit = distanceUnit, volumeUnit = volumeUnit))
        } else {
            viewModel.addVehicle(name, selectedVehicleType, distanceUnit, volumeUnit)
        }

        parentFragmentManager.setFragmentResult("vehicle_saved", Bundle.EMPTY)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(vehicle: Vehicle) = AddVehicleDialogFragment().apply {
            arguments = Bundle().also { it.putParcelable("vehicle", vehicle) }
        }
    }
}

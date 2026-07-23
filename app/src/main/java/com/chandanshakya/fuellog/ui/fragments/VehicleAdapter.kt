package com.chandanshakya.fuellog.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.util.UnitConverter

class VehicleAdapter(
    private val onClick: (Vehicle) -> Unit,
    private val onEdit: (Vehicle) -> Unit,
    private val onDelete: (Vehicle) -> Unit
) : ListAdapter<Vehicle, VehicleAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val iconType: ImageView = view.findViewById(R.id.icon_vehicle_type)
        private val textName: TextView = view.findViewById(R.id.text_vehicle_name)
        private val textInfo: TextView = view.findViewById(R.id.text_vehicle_info)
        private val btnMore: ImageButton = view.findViewById(R.id.btn_more)

        fun bind(vehicle: Vehicle) {
            iconType.setImageResource(vehicle.vehicleType.iconRes)
            textName.text = vehicle.name
            textInfo.text = "${vehicle.vehicleType.label} \u2022 ${UnitConverter.getDistanceUnitLabel(vehicle.distanceUnit)} / ${UnitConverter.getVolumeUnitLabel(vehicle.volumeUnit)}"

            itemView.setOnClickListener { onClick(vehicle) }

            btnMore.setOnClickListener { v ->
                PopupMenu(v.context, v).apply {
                    menuInflater.inflate(R.menu.popup_entry_actions, menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> { onEdit(vehicle); true }
                            R.id.action_delete -> { onDelete(vehicle); true }
                            else -> false
                        }
                    }
                    show()
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Vehicle>() {
        override fun areItemsTheSame(a: Vehicle, b: Vehicle) = a.id == b.id
        override fun areContentsTheSame(a: Vehicle, b: Vehicle) = a == b
    }
}

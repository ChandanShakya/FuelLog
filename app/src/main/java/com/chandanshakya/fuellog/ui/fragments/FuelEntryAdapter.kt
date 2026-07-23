package com.chandanshakya.fuellog.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.EntryWithMileage
import java.time.format.DateTimeFormatter

class FuelEntryAdapter(
    private val onEdit: (FuelEntry) -> Unit,
    private val onDelete: (FuelEntry) -> Unit
) : ListAdapter<EntryWithMileage, FuelEntryAdapter.ViewHolder>(DiffCallback) {

    var distanceUnit: DistanceUnit = DistanceUnit.KM
    var volumeUnit: VolumeUnit = VolumeUnit.LITERS
    var currency: String = "USD"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fuel_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textDate: TextView = view.findViewById(R.id.text_date)
        private val textOdometer: TextView = view.findViewById(R.id.text_odometer)
        private val btnMore: ImageButton = view.findViewById(R.id.btn_more)
        private val badgesContainer: LinearLayout = view.findViewById(R.id.badges_container)

        fun bind(item: EntryWithMileage) {
            val entry = item.entry
            textDate.text = DateTimeFormatter.ISO_LOCAL_DATE.format(entry.date)
            textOdometer.text = "Odometer: %.2f %s".format(entry.odometer, UnitConverter.getDistanceUnitLabel(distanceUnit))

            badgesContainer.removeAllViews()
            item.mileage?.let { m ->
                addBadge("%.2f %s".format(m, UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)))
            }
            addBadge("%.2f %s".format(entry.fuelVolume, UnitConverter.getVolumeUnitLabel(volumeUnit)))
            addBadge(CurrencyFormatter.formatCurrency(entry.fuelCost, currency))

            btnMore.setOnClickListener { v ->
                PopupMenu(v.context, v).apply {
                    menuInflater.inflate(R.menu.popup_entry_actions, menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> { onEdit(entry); true }
                            R.id.action_delete -> { onDelete(entry); true }
                            else -> false
                        }
                    }
                    show()
                }
            }
        }

        private fun addBadge(text: String) {
            val badge = TextView(itemView.context).apply {
                this.text = text
                setTextAppearance(R.style.Widget_FuelLog_Badge)
                val h = itemView.context.resources.getDimensionPixelSize(R.dimen.badge_padding_h)
                val v = itemView.context.resources.getDimensionPixelSize(R.dimen.badge_padding_v)
                setPadding(h, v, h, v)
                setBackgroundResource(R.drawable.bg_badge)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = itemView.context.resources.getDimensionPixelSize(R.dimen.spacing_sm)
                layoutParams = params
            }
            badgesContainer.addView(badge)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<EntryWithMileage>() {
        override fun areItemsTheSame(a: EntryWithMileage, b: EntryWithMileage) = a.entry.id == b.entry.id
        override fun areContentsTheSame(a: EntryWithMileage, b: EntryWithMileage) = a == b
    }
}

package me.doteq.dolinabaryczy.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.doteq.dolinabaryczy.data.models.Poi
import me.doteq.dolinabaryczy.databinding.ItemNearestPointBinding
import me.doteq.dolinabaryczy.utilities.Utilities
import me.doteq.dolinabaryczy.utilities.Utilities.toDistanceString

class NearestPointsAdapter(val POIs: List<Poi>): RecyclerView.Adapter<NearestPointsAdapter.NearestPointsViewHolder>() {

    class NearestPointsViewHolder(val binding: ItemNearestPointBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearestPointsViewHolder {
        return NearestPointsViewHolder(
            ItemNearestPointBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return POIs.size
    }

    override fun onBindViewHolder(holder: NearestPointsViewHolder, position: Int) {
        holder.binding.textView2.text = POIs[position].name
        POIs[position].distance?.let {
            holder.binding.chip.text = it.toDistanceString()
            holder.binding.chip.visibility = View.VISIBLE
        } ?: run {
            holder.binding.chip.visibility = View.GONE
        }

        holder.binding.carouselImageView.setImageResource(Utilities.getPointDrawable(POIs[position].id) ?: 0)

        holder.binding.root.setOnClickListener {
            onSuggestionClickListener?.invoke(POIs[position])
        }
    }

    private var onSuggestionClickListener: ((poi: Poi) -> Unit)? = null
    fun setOnSuggestionClickListener(listener: (poi: Poi) -> Unit) {
        onSuggestionClickListener = listener
    }

}
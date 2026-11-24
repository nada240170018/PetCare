package com.example.petcare

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import kotlin.math.roundToInt
class PetAdapter(
    private var petList: List<Pet>,
    private val onPetClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val petIcon: ImageView = itemView.findViewById(R.id.ivPetIcon)
        val petName: TextView = itemView.findViewById(R.id.tvPetName)
        val petType: TextView = itemView.findViewById(R.id.tvPetType)
        val petDetails: TextView = itemView.findViewById(R.id.tvPetDetails)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet, parent, false)

        return PetViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PetViewHolder,
        position: Int
    ) {
        val pet = petList[position]

        holder.petName.text = pet.name
        holder.petType.text = "${pet.type} • ${pet.breed}"
        holder.petDetails.text = holder.itemView.context.getString(
            R.string.pet_details,
            formatAge(holder.itemView.context, pet.age),
            holder.itemView.context.getString(
                R.string.pet_weight,
                pet.weight.toString()
            )
        )

        if (!pet.photoUri.isNullOrEmpty()) {
            try {
                holder.petIcon.setImageURI(Uri.parse(pet.photoUri))
            } catch (e: Exception) {
                holder.petIcon.setImageResource(
                    android.R.drawable.ic_menu_gallery
                )
            }
        } else {
            holder.petIcon.setImageResource(
                android.R.drawable.ic_menu_gallery
            )
        }

        holder.itemView.setOnClickListener {
            onPetClick(pet)
        }
    }
    private fun formatAge(context: Context, ageInYears: Double): String {
        val totalMonths = (ageInYears * 12).roundToInt()

        val years = totalMonths / 12
        val months = totalMonths % 12

        return when {
            totalMonths < 12 ->
                context.getString(R.string.age_months, totalMonths)

            months == 0 ->
                context.getString(R.string.age_years, years)

            else ->
                context.getString(
                    R.string.age_years_months,
                    years,
                    months
                )
        }
    }
    override fun getItemCount(): Int {
        return petList.size
    }

    fun updateData(newList: List<Pet>) {
        petList = newList
        notifyDataSetChanged()
    }
}

package com.example.tablayout

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class CarAdapter(private var carList: List<Car>, private var onBookClick: (carTitle: String) -> Unit) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    fun updateData(newCars: List<Car>) {
        carList = newCars
        notifyDataSetChanged()  // Notify adapter that data has changed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_cards, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        holder.carTitle.text = car.title
        holder.carMileage.text = "${car.mileage}km"
        holder.carTransmission.text = car.transmisson
        holder.carCondition.text = car.condition
        holder.carDealership.text = car.dealership
        holder.carLocation.text = car.location

        // Debug log
        Log.d("CarAdapter", "Image URL: ${car.maincarimage}")

        // Check if the image URL is not empty
        if (car.maincarimage.isNotEmpty()) {
            Picasso.get()
                .load(car.maincarimage)
                .error(R.drawable.error)
                .into(holder.carimage)
        } else {
            holder.carimage.setImageResource(R.drawable.error) // Placeholder or error image
        }

        // Setup for nested horizontal RecyclerView for images
        val carImagesAdapter = CarImagesAdapter(car.imageResourceList)
        holder.nestedRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.nestedRecyclerView.adapter = carImagesAdapter

        holder.bookTestDriveButton.setOnClickListener{
            onBookClick(car.title)
        }
    }

    override fun getItemCount(): Int = carList.size

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val carimage: ImageView = itemView.findViewById(R.id.car_image)
        val carTitle: TextView = itemView.findViewById(R.id.car_title)
        val carMileage: TextView = itemView.findViewById(R.id.car_Mileage) //
        val carTransmission: TextView = itemView.findViewById(R.id.car_Transmission)//
        val carCondition: TextView = itemView.findViewById(R.id.car_Condition)//
        val carDealership: TextView = itemView.findViewById(R.id.car_dealership)
        val carLocation: TextView = itemView.findViewById(R.id.car_location)
        val nestedRecyclerView: RecyclerView = itemView.findViewById(R.id.nested_recycler_view)
        val bookTestDriveButton: Button = itemView.findViewById(R.id.book_test_drive_button)
    }
}



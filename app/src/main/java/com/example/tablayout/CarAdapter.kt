package com.example.tablayout

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import android.util.Base64


class CarAdapter(private var carList: List<Car>) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {
    
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

        // Picasso to load the main image from Firebase Storage URL
        if (car.maincarimage.isNotEmpty()) {
            Picasso.get()
                .load(car.maincarimage)
                .error(R.drawable.error) // Fallback image in case of error
                .into(holder.carimage)
        } else {
            holder.carimage.setImageResource(R.drawable.error) // Fallback for empty image
        }

        holder.buyprice.text = "R${car.price}"
        holder.carTitle.text = car.year +" "+ car.make +" "+ car.model +" "+ car.special
        holder.carMileage.text = "${car.mileage}km"
        holder.carTransmission.text = car.transmission
        holder.carCondition.text = car.condition
        holder.carDealership.text = car.dealership
        holder.carLocation.text = car.location

        // Setup for nested horizontal RecyclerView for additional images
        val carImagesAdapter = CarImagesAdapter(car.imageResourceList)
        holder.nestedRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.nestedRecyclerView.adapter = carImagesAdapter
    }

    override fun getItemCount(): Int = carList.size

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val carimage: ImageView = itemView.findViewById(R.id.car_image)
        val carTitle: TextView = itemView.findViewById(R.id.car_title)
        val buyprice: TextView = itemView.findViewById(R.id.buy_price)
        val carMileage: TextView = itemView.findViewById(R.id.car_Mileage)
        val carTransmission: TextView = itemView.findViewById(R.id.car_Transmission)
        val carCondition: TextView = itemView.findViewById(R.id.car_Condition)
        val carDealership: TextView = itemView.findViewById(R.id.car_dealership)
        val carLocation: TextView = itemView.findViewById(R.id.car_location)
        val nestedRecyclerView: RecyclerView = itemView.findViewById(R.id.nested_recycler_view)
    }
}




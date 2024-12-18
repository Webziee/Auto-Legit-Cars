package com.legitautocars.app


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.legitautocars.app.R
import com.squareup.picasso.Picasso
import kotlin.math.log

/**
 * CarAdapter class to bind and display car data in a RecyclerView.
 *
 * The following RecyclerView Adapter implementation has been adapted from
 * various Android development resources and tutorials, including:
 *
 * - Official Android Developer Documentation: https://developer.android.com
 * - Picasso Documentation for Image Loading: https://square.github.io/picasso/
 * - General RecyclerView and ViewHolder design patterns.
 *
 * The Adapter handles the binding of car details, including images, to UI components
 * within a card layout for each car, while also handling user actions like booking
 * a test drive.
 */

class CarAdapter(private var carList: List<Car>) : RecyclerView.Adapter<CarAdapter.CarViewHolder>()
{

    private var onTestDriveClickListener: ((Car) -> Unit)? = null

    fun setOnTestDriveClickListener(listener: (Car) -> Unit) {
        onTestDriveClickListener = listener
    }

    fun updateData(newCars: List<Car>) {
        carList = newCars
        Log.d("CarAdapter", "Updated car list in adapter:")
        carList.forEach { car -> Log.d("CarAdapter", "Car price: ${car.price}") }
        notifyDataSetChanged()  // Notify adapter that data has changed
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_cards, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = carList[position]
        // Bind the title
        holder.carTitle.text = car.title

        // Bind the price
        holder.carPrice.text = "R ${car.price}" // Assuming price is in Rands (South African currency)

        // Bind other car details
        holder.carMileage.text = "${car.mileage}km"
        holder.carTransmission.text = car.transmission
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
        val carImagesAdapter = CarImagesAdapter(car.imageresourcelist)
        holder.nestedRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.nestedRecyclerView.adapter = carImagesAdapter

        // Bind the test drive button click
        holder.testDriveButton.setOnClickListener {
            Log.d("CarAdapter", "Car ID passed for test drive: ${car.id}")  // Log the car ID
            val bundle = Bundle().apply {
                putInt("car_id", car.id ?:0)
                putString("car_title", car.title)
            }
            onTestDriveClickListener?.invoke(car)
        }
    }

    override fun getItemCount(): Int = carList.size

    class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val carimage: ImageView = itemView.findViewById(R.id.car_image)
        val carTitle: TextView = itemView.findViewById(R.id.car_title)
        val carMileage: TextView = itemView.findViewById(R.id.car_Mileage)
        val carTransmission: TextView = itemView.findViewById(R.id.car_Transmission)
        val carCondition: TextView = itemView.findViewById(R.id.car_Condition)
        val carDealership: TextView = itemView.findViewById(R.id.car_dealership)
        val carLocation: TextView = itemView.findViewById(R.id.car_location)
        val carPrice: TextView = itemView.findViewById(R.id.buy_price) // Add this for price
        val nestedRecyclerView: RecyclerView = itemView.findViewById(R.id.nested_recycler_view)
        val testDriveButton: Button = itemView.findViewById(R.id.book_test_drive_button) // Add this line
    }
}
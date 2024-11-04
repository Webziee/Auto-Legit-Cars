package com.legitautocars.app


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.legitautocars.app.R
import com.squareup.picasso.Picasso
/**
 * CarImagesAdapter to display car images in a RecyclerView.
 *
 * The following RecyclerView adapter implementation has been adapted based on general
 * tutorials and examples from official documentation, as well as resources from:
 *
 * - Picasso Documentation: https://square.github.io/picasso/
 * - RecyclerView Documentation: https://developer.android.com/guide/topics/ui/layout/recyclerview
 *
 * The adapter loads images using Picasso and displays them in a horizontal
 * RecyclerView.
 */
class CarImagesAdapter(private val imageUrlList: List<String>) : RecyclerView.Adapter<CarImagesAdapter.ImageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imageUrl = imageUrlList[position]

            // Load the image using Picasso
            Picasso.get()
                .load(imageUrl)
                .error(R.drawable.error)
                .into(holder.carImage)
        }

        override fun getItemCount(): Int {
            return imageUrlList.size
        }

        class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val carImage: ImageView = itemView.findViewById(R.id.car_image_view)
        }
}

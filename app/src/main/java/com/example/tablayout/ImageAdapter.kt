package com.example.tablayout

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ImageAdapter(private var imageUris: List<Uri>, private val onImageClick: (Uri) -> Unit) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.car_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = imageUris[position]
        Log.d("ImageAdapter", "Binding image at position: $position, URI: $imageUri")
        Picasso.get().load(imageUri).into(holder.imageView)

        holder.itemView.setOnClickListener {
            onImageClick(imageUri)
        }
    }


    override fun getItemCount(): Int {
        return imageUris.size
    }

    fun updateImageList(newImageUris: List<Uri>) {
        Log.d("ImageAdapter", "Updating image list. New size: ${newImageUris.size}")
        imageUris = newImageUris
        notifyDataSetChanged()
    }
}

package com.example.tablayout
import android.util.Log
import com.example.tablayout.Car
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreUtils {

    private val db = FirebaseFirestore.getInstance()

    // Add a car to Firestore
    fun addCar(car: Car, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("cars")
            .add(car)
            .addOnSuccessListener { documentReference ->
                Log.d("FirestoreUtils", "DocumentSnapshot added with ID: ${documentReference.id}")
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreUtils", "Error adding document", e)
                onFailure(e)
            }
    }

    // Fetch all cars from Firestore
    fun fetchCars(onSuccess: (List<Car>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("cars")
            .get()
            .addOnSuccessListener { result ->
                val cars = mutableListOf<Car>()
                for (document in result.documents) {
                    val car = document.toObject(Car::class.java)
                    car?.let { cars.add(it) }
                }
                onSuccess(cars)
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreUtils", "Error fetching documents", e)
                onFailure(e)
            }
    }
}

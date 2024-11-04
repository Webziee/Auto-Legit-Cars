package com.example.tablayout

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class CarDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "legit_auto_cars.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_CARS = "cars"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_IMAGE = "maincarimage"
        const val COLUMN_CONDITION = "condition"
        const val COLUMN_MILEAGE = "mileage"
        const val COLUMN_PRICE = "price"
        const val COLUMN_BODY_TYPE = "bodytype"
        const val COLUMN_MAKE = "make"
        const val COLUMN_MODEL = "model"
        const val COLUMN_YEAR = "year"
        const val COLUMN_FUEL_TYPE = "fueltype"
        const val COLUMN_DEALERSHIP = "dealership"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_TRANSMISSION = "transmission"
        const val COLUMN_IMAGE_RESOURCE_LIST = "imageresourcelist"  // New column for additional images
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_CARS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_IMAGE TEXT,
                $COLUMN_CONDITION TEXT,
                $COLUMN_MILEAGE INTEGER,
                $COLUMN_PRICE INTEGER,
                $COLUMN_BODY_TYPE TEXT,
                $COLUMN_MAKE TEXT,
                $COLUMN_MODEL TEXT,
                $COLUMN_YEAR INTEGER,
                $COLUMN_FUEL_TYPE TEXT,
                $COLUMN_DEALERSHIP TEXT,
                $COLUMN_LOCATION TEXT,
                $COLUMN_TRANSMISSION TEXT,
                $COLUMN_IMAGE_RESOURCE_LIST TEXT  
            )
        """
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_CARS ADD COLUMN $COLUMN_IMAGE_RESOURCE_LIST TEXT")
        }
    }

    // Insert a list of cars into the database
    fun insertCars(cars: List<Car>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (car in cars) {
                val values = ContentValues().apply {
                    put(COLUMN_ID, car.id)
                    put(COLUMN_TITLE, car.title)
                    put(COLUMN_IMAGE, car.maincarimage)
                    put(COLUMN_CONDITION, car.condition)
                    put(COLUMN_MILEAGE, car.mileage)
                    put(COLUMN_PRICE, car.price)
                    put(COLUMN_BODY_TYPE, car.bodytype)
                    put(COLUMN_MAKE, car.make)
                    put(COLUMN_MODEL, car.model)
                    put(COLUMN_YEAR, car.year)
                    put(COLUMN_FUEL_TYPE, car.fueltype)
                    put(COLUMN_DEALERSHIP, car.dealership)
                    put(COLUMN_LOCATION, car.location)
                    put(COLUMN_TRANSMISSION, car.transmission)
                    put(COLUMN_IMAGE_RESOURCE_LIST, car.imageresourcelist.joinToString(","))  // Save list as a comma-separated string
                }
                val id = db.insertWithOnConflict(TABLE_CARS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                Log.d("CarDatabaseHelper", "Inserted Car With ID: $id")
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // Helper function to parse comma-separated image URLs
    private fun parseImageResourceList(imageResourceListString: String?): List<String> {
        return imageResourceListString?.split(",")?.map { it.trim() } ?: emptyList()
    }

    // Get all cars from the database
    fun getAllCars(): List<Car> {
        val cars = mutableListOf<Car>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CARS, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val imageResourceListString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RESOURCE_LIST))
            val imageResourceList = parseImageResourceList(imageResourceListString)

            val car = Car(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                maincarimage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)),
                condition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                mileage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MILEAGE)),
                price = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                bodytype = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY_TYPE)),
                make = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAKE)),
                model = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODEL)),
                year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)),
                fueltype = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FUEL_TYPE)),
                dealership = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEALERSHIP)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                transmission = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSMISSION)),
                imageresourcelist = imageResourceList
            )

            cars.add(car)
        }
        cursor.close()
        db.close()
        return cars
    }

    // Method to retrieve cars based on filters from the local database
    fun getFilteredCars(filters: Map<String, String?>): List<Car> {
        val cars = mutableListOf<Car>()
        val db = readableDatabase

        val query = StringBuilder("SELECT * FROM $TABLE_CARS WHERE 1=1")
        filters.forEach { (key, value) ->
            value?.let { query.append(" AND $key LIKE ?") }
        }

        val args = filters.values.filterNotNull().toTypedArray()
        val cursor = db.rawQuery(query.toString(), args)
        while (cursor.moveToNext()) {
            val imageResourceListString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RESOURCE_LIST))
            val imageResourceList = parseImageResourceList(imageResourceListString)

            val car = Car(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                maincarimage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)),
                condition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                mileage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MILEAGE)),
                price = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                bodytype = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY_TYPE)),
                make = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAKE)),
                model = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODEL)),
                year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)),
                fueltype = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FUEL_TYPE)),
                dealership = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEALERSHIP)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                transmission = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSMISSION)),
                imageresourcelist = imageResourceList
            )
            cars.add(car)
        }
        cursor.close()
        db.close()
        return cars
    }

    // Method to retrieve favorite cars based on car IDs from the local database
    fun getFavoriteCars(carIds: List<Int>): List<Car> {
        val cars = mutableListOf<Car>()
        val db = readableDatabase

        val query = "SELECT * FROM $TABLE_CARS WHERE $COLUMN_ID IN (${carIds.joinToString(",")})"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val imageResourceListString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RESOURCE_LIST))
            val imageResourceList = parseImageResourceList(imageResourceListString)

            val car = Car(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                maincarimage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)),
                condition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)),
                mileage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MILEAGE)),
                price = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                bodytype = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY_TYPE)),
                make = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAKE)),
                model = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODEL)),
                year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)),
                fueltype = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FUEL_TYPE)),
                dealership = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEALERSHIP)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                transmission = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSMISSION)),
                imageresourcelist = imageResourceList
            )
            cars.add(car)
        }
        cursor.close()
        db.close()
        return cars
    }

    // Delete all cars in the database
    fun clearDatabase() {
        val db = writableDatabase
        db.delete(TABLE_CARS, null, null)
        db.close()
    }
}

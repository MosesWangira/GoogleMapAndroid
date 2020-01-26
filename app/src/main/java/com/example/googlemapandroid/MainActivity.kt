package com.example.googlemapandroid

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*
import java.util.Collections.singletonList


class MainActivity : AppCompatActivity() {

    val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 1

    // Define a Place Photo ID.
    var placePhotoId = "INSERT_PLACE_PHOTO_ID_HERE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Initialize the SDK
        Places.initialize(applicationContext, getString(R.string.api_key))

        // Create a new Places client instance
        val placesClient = Places.createClient(this)


        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        // Specify the types of place data to return.
        autocompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME))

//        Setting autocomplete constrained to kenya country only
//        autocompleteFragment.setCountry("KE")
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) { // TODO: Get info about the selected place.
                Log.d("Successful selection", "Place: " + place.name + ", " + place.id)
            }

            override fun onError(status: Status) { // TODO: Handle the error.
                Log.d("Error occurrence", "An error occurred: $status")
            }
        })

        /**
         * getting current place
         */

        // Use fields to define the data types to return.
        val placeFields =
            singletonList(Place.Field.NAME)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task: Task<FindCurrentPlaceResponse?> ->
                if (task.isSuccessful) {
                    val response = task.result
                    for (placeLikelihood in response!!.placeLikelihoods) {

                        //restraining current location accuracy to greater than 0.1
                        if(placeLikelihood.likelihood > 0.1) {
                            Log.d(
                                "place name", String.format(
                                    "Place '%s' has likelihood: %f",
                                    placeLikelihood.place.name,
                                    placeLikelihood.likelihood,
                                    placeLikelihood.place.address,
                                    placeLikelihood.place.id,
                                    placeLikelihood.place.latLng
                                )
                            )
                            toast("place name ${placeLikelihood.place.name}")
                            toast("place likelihood ${placeLikelihood.likelihood}")
                        }
                    }
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.d(
                            "Place not found",
                            "Place not found: " + exception.statusCode
                        )
                    }
                }
            }
        } else { // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            getLocationPermission()
        }
    }

    private fun getLocationPermission() {
        // Permission is not granted
        // Should we show an explanation?

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok",
                        DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(ACCESS_FINE_LOCATION),
                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                            )
                        })
                    .setNegativeButton("cancel",
                        DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                    .create().show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    //else permission already granted

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    toast("permission granted")
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    toast("permission denied")
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}

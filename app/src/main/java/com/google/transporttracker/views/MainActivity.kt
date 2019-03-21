package com.google.transporttracker.views

import android.Manifest
import com.google.transporttracker.R
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.transporttracker.data.User
import com.google.transporttracker.service.GoogleService
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mDatabase: DatabaseReference? = null
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        FirebaseApp.initializeApp(this@MainActivity)
        mDatabase = FirebaseDatabase.getInstance().reference
        val providers = Arrays.asList<AuthUI.IdpConfig>(
                AuthUI.IdpConfig.PhoneBuilder().build())
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
                RC_SIGN_IN)
        val mapFragment = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            fragmentManager.findFragmentById(R.id.map) as MapFragment
        } else {
            TODO("VERSION.SDK_INT < HONEYCOMB")
        }
        mapFragment.getMapAsync(this)
        val intent = Intent(applicationContext, GoogleService::class.java)
        startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().currentUser

                // ...
            } else {

            }
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney, Australia, and move the camera.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled = true
        mMap!!.setOnMyLocationChangeListener { location ->
            mMap!!.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 13.0f))
            if (user != null) {
                writeUpdateUser(user!!.uid, location.latitude,
                        location.longitude, if (user!!.phoneNumber != null) user!!.phoneNumber else "")
                addValuesToSharedPrefernce(location)
            }
        }
    }

    private fun writeUpdateUser(userId: String, lat: Double?, lang: Double?, number: String?) {
        val user = User(lat, lang, number)
        mDatabase!!.child("users").child(userId).setValue(user).addOnSuccessListener { }.addOnFailureListener { }
    }


    fun addValuesToSharedPrefernce(location: Location) {
        val editor = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
        editor.putString(ID, user?.uid);
        editor.putString(MOBILE_NUMBER, user?.phoneNumber);
        editor.putFloat(LAT, location.latitude.toFloat());
        editor.putFloat(LANG, location.longitude.toFloat());
        editor.apply();
    }


    companion object {
        private val RC_SIGN_IN = 123
        const val PREFS = "PREFS"
        const val ID = "ID"
        const val LAT = "LAT"
        const val LANG = "LANG"
        const val MOBILE_NUMBER ="MOBILE_NUMBER"
    }


}

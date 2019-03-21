package com.google.transporttracker.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.annotation.Nullable
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.transporttracker.data.User
import com.google.transporttracker.views.MainActivity.Companion.ID
import com.google.transporttracker.views.MainActivity.Companion.LANG
import com.google.transporttracker.views.MainActivity.Companion.LAT
import com.google.transporttracker.views.MainActivity.Companion.MOBILE_NUMBER
import com.google.transporttracker.views.MainActivity.Companion.PREFS
import java.util.*


class GoogleService : Service(), LocationListener {

    internal var isGPSEnable = false
    internal var isNetworkEnable = false
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    internal var locationManager: LocationManager? = null
    internal var location: Location? = null
    private val mHandler = Handler()
    private var mTimer: Timer? = null
    internal var notify_interval: Long = 1000
    internal lateinit var intent: Intent

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mTimer = Timer()
        mTimer!!.schedule(TimerTaskToGetLocation(), 5, notify_interval)

    }

    override fun onLocationChanged(location: Location) {

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    @SuppressLint("MissingPermission")
    private fun fn_getlocation() {
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGPSEnable = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        isNetworkEnable = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGPSEnable && !isNetworkEnable) {

        } else {

            if (isNetworkEnable) {
                location = null
                locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, this)
                if (locationManager != null) {
                    location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        latitude = location!!.getLatitude()
                        longitude = location!!.getLongitude()
                        fn_update(location!!)
                    }
                }

            }


            if (isGPSEnable) {
                location = null
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this)
                if (locationManager != null) {
                    location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (location != null) {

                        latitude = location!!.getLatitude()
                        longitude = location!!.getLongitude()
                        fn_update(location!!)
                    }
                }
            }


        }

    }

    private inner class TimerTaskToGetLocation : TimerTask() {
        override fun run() {
            mHandler.post({ fn_getlocation() })
        }
    }

    private fun fn_update(location: Location) {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val mRef = FirebaseDatabase.getInstance().reference;
        val user = User();
        user.apply {
            lat = prefs.getFloat(LAT, 0.0f).toDouble()
            lang = prefs.getFloat(LANG, 0.0f).toDouble()
            number = prefs.getString(MOBILE_NUMBER, "")
        }
        mRef.child("users").child(prefs.getString(ID, "")).setValue(user)
        Log.v("Google Service", user.toString())
    }

}

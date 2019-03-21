package com.google.transporttracker.views

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.google.transporttracker.R
import kotlinx.android.synthetic.main.activity_find_user.*
import com.google.transporttracker.service.GoogleService
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter


class FindUserActivity : AppCompatActivity(), OnMapReadyCallback {

    private val RC_SIGN_IN = 123
    private var mMap: GoogleMap? = null
    private var mDatabase: DatabaseReference? = null
    private var etNumberEmail: EditText? = null
    lateinit var mDataSnapShot: DataSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_user)
        init()
    }

    private fun init() {
        FirebaseApp.initializeApp(this@FindUserActivity)
        etNumberEmail = findViewById(R.id.et_mobile_number)
        mDatabase = FirebaseDatabase.getInstance().reference
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this@FindUserActivity)
        val usersdRef = mDatabase!!.child("users")
        val eventListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mDataSnapShot = dataSnapshot
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        usersdRef.addListenerForSingleValueEvent(eventListener)

    }

    private fun searchUserAndSetData(queryString: String) {
        findUserLatLang(queryString)
    }

    private fun findUserLatLang(queryString: String) {
        for (ds in mDataSnapShot.children) {
            val lat = ds.child("lat").getValue(Double::class.java)
            val lang = ds.child("lang").getValue(Double::class.java)
            if (ds.child("number").exists() && ds.child("number").getValue(String::class.java)!!.equals(queryString, ignoreCase = true)) {
                mMap!!.addMarker(MarkerOptions().position(LatLng(lat!!, lang!!)))
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lang), 13.0f))
                break
            }
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled = true
    }

    fun submit(view: View) {
        searchUserAndSetData(etNumberEmail!!.text.toString().trim { it <= ' ' })
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(ll_find_user.getWindowToken(), 0)
    }

    fun clear(view: View) {
        etNumberEmail?.setText("")
    }
}

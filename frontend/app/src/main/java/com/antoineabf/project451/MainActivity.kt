package com.antoineabf.project451

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayout
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.InetAddress
import java.net.NetworkInterface



class MainActivity : AppCompatActivity() {
    private var tabLayout: TabLayout? = null
    private var tabsViewPager: ViewPager2? = null
    private var mSocket: Socket? = null
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        tabsViewPager = findViewById(R.id.tabsViewPager)
        tabLayout?.tabMode = TabLayout.MODE_FIXED
        tabLayout?.isInlineLabel = true
        // Enable Swipe
        tabsViewPager?.isUserInputEnabled = true
        // Set the ViewPager Adapter
        val adapter = TabsPagerAdapter(supportFragmentManager, lifecycle)
        tabsViewPager?.adapter = adapter
        TabLayoutMediator(tabLayout!!, tabsViewPager!!) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Cell Data"
                }

                1 -> {
                    tab.text = "Statistics"
                }
            }
        }.attach()
        try {
            val url = "192.168.1.110"

            mSocket = IO.socket("http://$url:5000")
            mSocket?.connect()
            mSocket?.on(Socket.EVENT_CONNECT) {
                Log.d("connectionTag1", "Connected to server")
                val user_ip = getIPAddress(this)
                val user_mac = getMacAddress()
                val data = JSONObject()
                data.put("user_ip", user_ip)
                data.put("user_mac", user_mac)
                mSocket?.emit("user_data", data)

            }?.on(Socket.EVENT_DISCONNECT) {
                Log.d("disconnectionTag1", "Disconnected from server")

            }
        } catch (e: Exception) {
            Log.d("errorTag1","error")
            e.printStackTrace()
        }



    }
    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
    }
    fun getMacAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val mac = networkInterface.hardwareAddress
                if (mac != null && mac.isNotEmpty()) {
                    val stringBuilder = StringBuilder()
                    for (byte in mac) {
                        stringBuilder.append(String.format("%02X:", byte))
                    }
                    if (stringBuilder.isNotEmpty()) {
                        stringBuilder.deleteCharAt(stringBuilder.length - 1)
                    }
                    return stringBuilder.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Not Available"
    }
    fun getIPAddress(context: Context): String? {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress

        // Check if wifiInfo is not null and IP address is not 0
        if (wifiInfo != null && ipAddress != 0) {
            // Convert IP address from integer to human-readable format
            return InetAddress.getByAddress(
                byteArrayOf(
                    (ipAddress and 0xff).toByte(),
                    (ipAddress shr 8 and 0xff).toByte(),
                    (ipAddress shr 16 and 0xff).toByte(),
                    (ipAddress shr 24 and 0xff).toByte()
                )
            ).hostAddress
        } else {
            return "Not Available"
        }
    }






}

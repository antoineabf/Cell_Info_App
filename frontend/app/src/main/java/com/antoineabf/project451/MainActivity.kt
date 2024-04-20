package com.antoineabf.project451

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayout
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.antoineabf.project451.api.CellDataService
import com.antoineabf.project451.api.model.CellData
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress
import java.net.NetworkInterface



class MainActivity : AppCompatActivity() {
    private var tabLayout: TabLayout? = null
    private var tabsViewPager: ViewPager2? = null
    private var mSocket: Socket? = null
    private val handler = Handler(Looper.getMainLooper())
    private val postInfoRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.P)
        override fun run() {
            postInfo()
            handler.postDelayed(this, 10 * 1000) // Schedule next execution after 10 seconds
        }
    }
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
            val url = "172.20.10.3"

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
                val user_ip = getIPAddress(this)
                val user_mac = getMacAddress()
                val data = JSONObject()
                data.put("user_ip", user_ip)
                data.put("user_mac", user_mac)
                mSocket?.emit(Socket.EVENT_DISCONNECT, data)


                Log.d("disconnectionTag1", "Disconnected from server")

            }
        } catch (e: Exception) {
            Log.d("errorTag1","error")
            e.printStackTrace()
        }
        postInfo()
        handler.postDelayed(postInfoRunnable, 10 * 1000)




    }
    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
    }
    fun getMacAddress(): String {
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
       return "02:00:00:00:00:00"

    }
    fun getIPAddress(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress

            if (ipAddress != 0) {
                // Convert IP address from integer to human-readable format
                return InetAddress.getByAddress(
                    byteArrayOf(
                        (ipAddress and 0xff).toByte(),
                        (ipAddress shr 8 and 0xff).toByte(),
                        (ipAddress shr 16 and 0xff).toByte(),
                        (ipAddress shr 24 and 0xff).toByte()
                    )
                ).hostAddress
            }
        } else {
            try {
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                while (networkInterfaces.hasMoreElements()) {
                    val networkInterface = networkInterfaces.nextElement()
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') < 0) {
                            // Ensure it's not a loopback address and IPv6
                            return address.hostAddress
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return "Not Available"
    }
    @RequiresApi(Build.VERSION_CODES.P)
    fun postInfo(){
        val infoArray = CaptureInfo().generateInfo(this)
        val cellData = CellData()
        cellData.operator = if (infoArray[0] == "Not Available") null else infoArray[0]
        cellData.signal_power = if (infoArray[1] == "Not Available") null else infoArray[1]?.substring(0, infoArray[1]?.length?.minus(4) ?: 0)?.toFloatOrNull()
        cellData.sinr_snr = if (infoArray[2] == "Not Available") null else infoArray[2]?.substring(0,infoArray[2]?.length?.minus(3)?:0)?.toFloatOrNull()
        cellData.network_type = if (infoArray[3] == "Not Available") null else infoArray[3]
        cellData.frequency_band = if (infoArray[4] == "Not Available") null else infoArray[4]
        cellData.cell_id = if (infoArray[5] == "Not Available") null else infoArray[5]
        cellData.timestamp = if (infoArray[6] == "Not Available") null else infoArray[6]
        cellData.user_mac = if (infoArray[7] == "Not Available") null else infoArray[7]
        cellData.user_ip = if (infoArray[8] == "Not Available") null else infoArray[8]



        CellDataService.CellDataApi().add_cell_data(cellData).enqueue(object :
            Callback<Any> {
            override fun onResponse(call: Call<Any>, response:
            Response<Any>
            ) {

            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                return;

            }
        })
    }






}

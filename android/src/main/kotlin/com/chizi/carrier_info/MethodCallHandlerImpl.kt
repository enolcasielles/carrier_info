package com.chizi.carrier_info


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler


internal class MethodCallHandlerImpl(context: Context, activity: Activity?) : MethodCallHandler {


    private val TAG: String =  "carrier_info"
    private var context: Context?
    private var activity: Activity?
    private val E_NO_CARRIER_NAME = "no_carrier_name"
    private val E_NO_NETWORK_TYPE = "no_network_type"
    private val E_NO_ISO_COUNTRY_CODE = "no_iso_country_code"
    private val E_NO_MOBILE_COUNTRY_CODE = "no_mobile_country_code"
    private val E_NO_MOBILE_NETWORK = "no_mobile_network"
    private val E_NO_NETWORK_OPERATOR = "no_network_operator"
    private var mTelephonyManager: TelephonyManager? = null
    private lateinit var func: () -> Unit?



    fun setActivity(act: Activity?) {
        this.activity = act
    }

    init {
        this.activity = activity
        this.context = context

        mTelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Handler(Looper.getMainLooper()).post {
            when (call.method) {
                "carrierName" -> {
                    carrierName(result)
                }
                "isoCountryCode" -> {
                    isoCountryCode(result)
                }
                "mobileCountryCode" -> {
                    mobileCountryCode(result)
                }
                "mobileNetworkCode" -> {
                    mobileNetworkCode(result)
                }
                "mobileNetworkOperator" -> {
                    mobileNetworkOperator(result)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun requestForSpecificPermission(i: Int) {
        ActivityCompat.requestPermissions(this.activity!!, arrayOf(Manifest.permission.ACCESS_WIFI_STATE), i)
    }

    private fun checkIfAlreadyHavePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this.activity!!, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
    }

    private fun carrierName(result: MethodChannel.Result) {
        val carrierName = mTelephonyManager!!.simOperatorName
        if (carrierName != null && "" != carrierName) {
            result.success(carrierName)
        } else {
            result.error(E_NO_CARRIER_NAME, "No carrier name","")
        }
    }

    private fun isoCountryCode(result: MethodChannel.Result) {
        val iso = mTelephonyManager!!.simCountryIso
        if (iso != null && "" != iso) {
            result.success(iso)
        } else {
            result.error(E_NO_ISO_COUNTRY_CODE, "No iso country code","")
        }
    }

    // returns MCC (3 digits)

    private fun mobileCountryCode(result: MethodChannel.Result) {
        val plmn = mTelephonyManager!!.simOperator
        if (plmn != null && "" != plmn) {
            result.success(plmn.substring(0, 3))
        } else {
            result.error(E_NO_MOBILE_COUNTRY_CODE, "No mobile country code","")
        }
    }

    // returns MNC (2 or 3 digits)

    private fun mobileNetworkCode(result: MethodChannel.Result) {
        val plmn = mTelephonyManager!!.simOperator
        if (plmn != null && "" != plmn) {
            result.success(plmn.substring(3))
        } else {
            result.error(E_NO_MOBILE_NETWORK, "No mobile network code","")
        }
    }

    // return MCC + MNC (5 or 6 digits), e.g. 20601
    private fun mobileNetworkOperator(result: MethodChannel.Result){
        val plmn = mTelephonyManager!!.simOperator
        if (plmn != null && "" != plmn) {
            result.success(plmn)
        } else {
            result.error(E_NO_NETWORK_OPERATOR, "No mobile network operator", "")
        }
    }


    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        when (requestCode) {
            0 -> return if (grantResults!![0] == PackageManager.PERMISSION_GRANTED) {
                this.func()!!

            } else {
                requestForSpecificPermission(0)

            }
            1 -> return if (grantResults!![0] == PackageManager.PERMISSION_GRANTED) {
                this.func()!!

            } else {
                requestForSpecificPermission(1)

            }
        }
    }
}

package com.telemetrypro.app.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lightweight read-only provider for cellular / network positioning context.
 *
 * Does NOT register any listeners — every refresh() is a one-shot query.
 * This avoids background cost; the caller (ViewModel) decides how often to refresh.
 *
 * All info here is what the Android network positioning system uses internally
 * to estimate location from cell towers.
 */
class NetworkCellInfoProvider(private val context: Context) {

    data class CellTowerInfo(
        val operatorName: String = "—",
        val networkTypeName: String = "—",   // "LTE", "5G NR", "5G NSA", "3G", "2G"
        val isRoaming: Boolean = false,
        val mcc: String = "—",               // Mobile Country Code
        val mnc: String = "—",               // Mobile Network Code
        val cellId: String = "—",            // CI (LTE) or NCI (NR)
        val tac: String = "—",               // Tracking Area Code
        val pci: String = "—",               // Physical Cell ID
        val band: String = "—",              // EARFCN/ARFCN
        val rsrpDbm: Int = Int.MIN_VALUE,    // Reference Signal Received Power (dBm)
        val rsrqDb: Int = Int.MIN_VALUE,     // Reference Signal Received Quality (dB)
        val neighborCount: Int = 0,          // Number of neighbor cells visible
        val level: Int = 0,                  // 0-4 abstract signal level
        val available: Boolean = false
    )

    private val _cellInfo = MutableStateFlow(CellTowerInfo())
    val cellInfo: StateFlow<CellTowerInfo> = _cellInfo.asStateFlow()

    /** One-shot read of current cell tower info. Safe to call repeatedly. Must be called off main thread. */
    @SuppressLint("MissingPermission")
    fun refresh() {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return

        // Permission check — allCellInfo requires ACCESS_FINE_LOCATION on API 29+
        val hasFineLoc = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLoc = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFineLoc && !hasCoarseLoc) return

        try {
            val operator = tm.networkOperatorName ?: ""
            val networkType = mapNetworkType(tm)
            val isRoaming = try { tm.isNetworkRoaming } catch (e: Exception) { false }
            val level = 0

            // allCellInfo — list of serving + neighbor cells
            val cellInfoList = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tm.allCellInfo
                } else null
            } catch (e: SecurityException) { null }

            var servingCell: CellInfoLte? = null
            var servingCellNr: CellInfoNr? = null
            var neighborCount = 0

            cellInfoList?.forEach { info ->
                val isRegistered = try { info.isRegistered } catch (e: Exception) { false }
                when (info) {
                    is CellInfoLte -> {
                        if (isRegistered && servingCell == null) {
                            servingCell = info
                        } else if (!isRegistered) {
                            neighborCount++
                        }
                    }
                    is CellInfoNr -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (isRegistered && servingCellNr == null) {
                                servingCellNr = info
                            } else if (!isRegistered) {
                                neighborCount++
                            }
                        }
                    }
                    else -> {
                        if (!isRegistered) neighborCount++
                    }
                }
            }

            // Extract serving cell details
            var mcc = "—"; var mnc = "—"; var cellId = "—"; var tac = "—"; var pci = "—"
            var band = "—"; var rsrp = Int.MIN_VALUE; var rsrq = Int.MIN_VALUE

            servingCell?.let { info ->
                try {
                    val identity = info.cellIdentity as CellIdentityLte
                    val signal = info.cellSignalStrength as CellSignalStrengthLte
                    mcc = identity.mccString ?: "—"
                    mnc = identity.mncString ?: "—"
                    cellId = identity.ci.toString()
                    tac = identity.tac.toString()
                    pci = identity.pci.toString()
                    band = "EARFCN ${identity.earfcn}"
                    rsrp = signal.rsrp
                    rsrq = signal.rsrq
                } catch (e: Exception) { }
            }
            if (servingCell == null && servingCellNr != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val identity = servingCellNr!!.cellIdentity as CellIdentityNr
                    val signal = servingCellNr!!.cellSignalStrength as CellSignalStrengthNr
                    mcc = identity.mccString ?: "—"
                    mnc = identity.mncString ?: "—"
                    cellId = identity.nci.toString()
                    tac = identity.tac.toString()
                    pci = identity.pci.toString()
                    band = "NR ARFCN ${identity.nrarfcn}"
                    rsrp = signal.ssRsrp
                    rsrq = signal.ssRsrq
                } catch (e: Exception) { }
            }

            _cellInfo.value = CellTowerInfo(
                operatorName = operator.ifEmpty { "—" },
                networkTypeName = networkType,
                isRoaming = isRoaming,
                mcc = mcc, mnc = mnc, cellId = cellId, tac = tac,
                pci = pci, band = band,
                rsrpDbm = rsrp, rsrqDb = rsrq,
                neighborCount = neighborCount,
                level = level,
                available = true
            )
        } catch (e: Throwable) {
            // Catch Throwable (not just Exception) to handle NoClassDefFoundError
            // on API 26-28 where CellInfoNr/CellIdentityNr don't exist.
            Log.w("NetworkCellInfo", "refresh failed", e)
        }
    }

    /** Map TelephonyManager network type constant to human-readable name. */
    private fun mapNetworkType(tm: TelephonyManager): String {
        return try {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                tm.dataNetworkType
            } else {
                @Suppress("DEPRECATION")
                tm.networkType
            }
            when (type) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                else -> "未知 ($type)"
            }
        } catch (e: Exception) {
            "—"
        }
    }
}

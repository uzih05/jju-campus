package com.example.galaxy.data.location

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import java.net.Inet4Address

data class CampusInfo(
    val isOnCampus: Boolean,
    val ipAddress: String?,
    val subnetId: Int?,
    val building: String?,
)

class CampusDetector(private val context: Context) {

    fun detect(): CampusInfo {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return CampusInfo(false, null, null, null)
        val props = cm.getLinkProperties(network) ?: return CampusInfo(false, null, null, null)

        val isJjDomain = props.domains?.contains("jj.ac.kr") == true
        val ipv4 = props.linkAddresses
            .map { it.address }
            .filterIsInstance<Inet4Address>()
            .firstOrNull()
            ?.hostAddress

        val isPrivateJj = ipv4?.startsWith("10.50.") == true
        val isOnCampus = isJjDomain || isPrivateJj

        val subnetId = if (isPrivateJj) {
            ipv4?.split(".")?.getOrNull(2)?.toIntOrNull()
        } else null

        val building = subnetId?.let { BUILDING_MAP[it] }

        return CampusInfo(isOnCampus, ipv4, subnetId, building)
    }

    companion object {
        // TODO: 건물별 IP 매핑 — 여러 건물에서 확인 후 채우기
        private val BUILDING_MAP = mapOf(
            24 to "현재 위치 (subnet 24)",
            // 다른 건물에서 IP 확인 후 추가:
            // XX to "○○관",
        )
    }
}

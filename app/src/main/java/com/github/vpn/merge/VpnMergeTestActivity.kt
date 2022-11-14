package com.github.vpn.merge

import android.annotation.SuppressLint
import android.net.VpnService
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.TextView
import com.github.vpn.merge.state.*

class VpnMergeTestActivity : TestVpnBaseActivity(R.layout.activity_test_vpn_merge) {

    private var currentVpnType: VpnTypeMerge? = null
    private val autoStart by lazy { findViewById<View>(R.id.auto_vpn_start) }
    private val openvpnStart by lazy { findViewById<View>(R.id.openvpn_start) }
    private val strongSwanStart by lazy { findViewById<View>(R.id.strong_swan_start) }
    private val vpnStop by lazy { findViewById<View>(R.id.vpn_stop) }
    private val vpnStatus by lazy { findViewById<TextView>(R.id.vpn_status) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VpnMergeManagerNotification.notificationOpenVpnProxy()
        VpnMergeManagerNotification.notificationStrongSwanProxy()
        VpnMergeManagerNotification.showNotification(VpnStateMerge.DISCONNECTED.toString())
        VpnMergeManagerWrapper.register(object : VpnMergeManager.OnVpnMergeStateListener {
            @SuppressLint("SetTextI18n")
            override fun onVpnStateChanged(type: VpnTypeMerge, state: VpnStateMerge) {
                runOnUiThread { vpnStatus.text = "${type}->Status:$state" }
            }
        })
        vpnStop.setOnClickListener {
            VpnMergeManagerWrapper.stopStrongSwan()
            VpnMergeManagerWrapper.stopOpenVpn()
        }
        openvpnStart.setOnClickListener {
            currentVpnType = VpnTypeMerge.OPEN_VPN
            vpnStop.performClick()
            startVpnConnect()
        }
        strongSwanStart.setOnClickListener {
            currentVpnType = VpnTypeMerge.STRONG_SWAN
            vpnStop.performClick()
            startVpnConnect()
        }
        autoStart.setOnClickListener {
            currentVpnType = VpnTypeMerge.AUTO
            vpnStop.performClick()
            startVpnConnect()
        }
    }

    override fun startVpnConnect() {
        val prepare = VpnService.prepare(this)
        if (prepare != null) {
            vpnResult.launch(prepare)
            return
        }
        when (requireNotNull(currentVpnType)) {
            VpnTypeMerge.OPEN_VPN -> VpnMergeManagerWrapper.startOpenVpn(
                true,
                String(Base64.decode(BuildConfig.OPEN_VPN_UDP, Base64.DEFAULT))
            )
            VpnTypeMerge.STRONG_SWAN -> VpnMergeManagerWrapper.startStrongSwan(
                BuildConfig.IKEV2_IP,
                BuildConfig.IKEV2_CERT.getAlias(),
                BuildConfig.IKEV2_USER_NAME,
                BuildConfig.IKEV2_PASS_WORD
            )
            VpnTypeMerge.AUTO -> VpnMergeManagerWrapper.startAuto(
                true,
                String(Base64.decode(BuildConfig.OPEN_VPN_UDP, Base64.DEFAULT)),
                BuildConfig.IKEV2_USER_NAME, //模擬StrongSwan失敗
                BuildConfig.IKEV2_CERT.getAlias(),
                BuildConfig.IKEV2_USER_NAME,
                BuildConfig.IKEV2_PASS_WORD
            )
        }
    }

}
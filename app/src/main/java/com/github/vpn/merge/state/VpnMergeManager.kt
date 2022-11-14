package com.github.vpn.merge.state

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import de.blinkt.openvpn.core.ConnectionStatus
import de.blinkt.openvpn.core.VpnStatus
import de.blinkt.openvpn.custom.OpenVpnDisconnectVPN
import org.strongswan.android.logic.CharonVpnService
import org.strongswan.android.logic.VpnStateService
import org.strongswan.android.logic.VpnStateService.LocalBinder

class VpnMergeManager(private val context: Context) {

    interface OnVpnMergeStateListener {
        fun onVpnStateChanged(type: VpnTypeMerge, state: VpnStateMerge)
    }

    private fun String?.logI() {
        Log.i("VpnMergeManager", this.orEmpty().ifEmpty { "message == null" })
    }

    //class start
    private var ignoreOpenVpnStatus = true
    //class end

    private val stateListener: MutableList<OnVpnMergeStateListener> = mutableListOf()
    private val openVpnService by lazy { OpenVpnDisconnectVPN().apply { register(context) } }
    private var _strongSwanVpnService: VpnStateService? = null
    private val strongSwanVpnService get() = _strongSwanVpnService

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            _strongSwanVpnService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            with((service as LocalBinder).service) {
                _strongSwanVpnService = this
                registerListener {
                    val errorState = errorState
                    val state = state
                    "state:$state-error:$errorState".logI()
                    stateListener.forEach {
                        it.onVpnStateChanged(
                            VpnTypeMerge.STRONG_SWAN,
                            VpnStateMerge.getStrongSwanState(state, errorState)
                        )
                    }
                }
            }
        }
    }

    init {
        initOpenVpn()
        initStrongSwan()
        context.bindService(
            Intent(context, VpnStateService::class.java).apply {
                putExtra(VpnStateService.KEY_RETRY, false)
                putExtra(CharonVpnService.NOTIFICATION_ALWAYS_VISIBLE, true)
            }, mServiceConnection, Service.BIND_AUTO_CREATE
        )
    }

    fun registerListener(listener: OnVpnMergeStateListener) {
        if (!stateListener.contains(listener)) {
            stateListener.add(listener)
        }
    }

    fun unregisterListener() {
        stateListener.clear()
    }

    private fun initStrongSwan() {
    }

    private fun initOpenVpn() {
        openVpnService //
        VpnStatus.addStateListener(object : VpnStatus.StateListener {
            override fun updateState(
                state: String?,
                logmessage: String?,
                localizedResId: Int,
                level: ConnectionStatus?,
                Intent: Intent?
            ) {
                level ?: return
                if (ignoreOpenVpnStatus) return
                level.toString().logI()
                stateListener.forEach {
                    it.onVpnStateChanged(
                        VpnTypeMerge.OPEN_VPN, VpnStateMerge.getOpenVpnState(level)
                    )
                }
            }

            override fun setConnectedVPN(uuid: String?) {
            }
        })
    }

    fun startOpenVpn(alwaysShowNotification: Boolean, reader: String) {
        ignoreOpenVpnStatus = false
        context.startOpenVpn(alwaysShowNotification, reader)
    }

    fun stopOpenVpn() {
        ignoreOpenVpnStatus = false
        openVpnService.stopVpn()
    }

    fun startStrongSwan(ip: String, alias: String, userName: String, passWord: String) {
        val bundle = context.getStrongSwanBundle(ip, alias, userName, passWord)
        strongSwanVpnService?.connect(bundle, true)
    }

    fun stopStrongSwan() {
        strongSwanVpnService?.disconnect(true)
    }

    fun onDestroy() {
        runCatching {
            context.unbindService(mServiceConnection)
            openVpnService.unbindService()
        }
    }

}
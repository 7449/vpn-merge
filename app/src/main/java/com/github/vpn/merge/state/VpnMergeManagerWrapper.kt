package com.github.vpn.merge.state

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import com.github.vpn.merge.VpnApplication
import java.util.*

object VpnMergeManagerWrapper : VpnMergeManager.OnVpnMergeStateListener {

    @SuppressLint("StaticFieldLeak")
    private val manager = VpnMergeManager(VpnApplication.application)
    private val handler = Handler(Looper.getMainLooper())
    private val vpnRunnableQueue = LinkedList<Runnable>()
    private var listener: VpnMergeManager.OnVpnMergeStateListener? = null
    private var vpnState: VpnStateMerge? = null
    private var vpnType: VpnTypeMerge? = null

    init {
        manager.registerListener(this)
    }

    fun register(listener: VpnMergeManager.OnVpnMergeStateListener) {
        this.listener = listener
        onVpnStateChanged(vpnType ?: VpnTypeMerge.AUTO, vpnState ?: VpnStateMerge.DISCONNECTED)
        VpnMergeManagerNotification.showNotification(
            (vpnState ?: VpnStateMerge.DISCONNECTED).toString()
        )
    }

    fun unregister() {
        manager.unregisterListener()
    }

    fun startAuto(
        alwaysShowNotification: Boolean,
        reader: String,
        ip: String,
        alias: String,
        userName: String,
        passWord: String
    ) {
        // one strongSwan
        // two openVpn
        vpnRunnableQueue.clear()
        vpnRunnableQueue.add(SimpleStrongSwanRunnable(ip, alias, userName, passWord))
        vpnRunnableQueue.add(SimpleOpenVpnRunnable(alwaysShowNotification, reader))
        vpnRunnableQueue.pollFirst()?.let { handler.post(it) }
    }

    fun stopAuto() {
        //ignore
    }

    fun startOpenVpn(alwaysShowNotification: Boolean, reader: String) {
        vpnRunnableQueue.clear()
        vpnRunnableQueue.add(SimpleOpenVpnRunnable(alwaysShowNotification, reader))
        vpnRunnableQueue.pollFirst()?.let { handler.post(it) }
    }

    fun stopOpenVpn() {
        manager.stopOpenVpn()
    }

    fun startStrongSwan(ip: String, alias: String, userName: String, passWord: String) {
        vpnRunnableQueue.clear()
        vpnRunnableQueue.add(SimpleStrongSwanRunnable(ip, alias, userName, passWord))
        vpnRunnableQueue.pollFirst()?.let { handler.post(it) }
    }

    fun stopStrongSwan() {
        manager.stopStrongSwan()
    }

    fun onDestroy() {
        manager.onDestroy()
    }

    private class SimpleOpenVpnRunnable(
        val alwaysShowNotification: Boolean,
        val reader: String
    ) : Runnable {
        override fun run() {
            manager.startOpenVpn(alwaysShowNotification, reader)
        }
    }

    private class SimpleStrongSwanRunnable(
        val ip: String,
        val alias: String,
        val userName: String,
        val passWord: String
    ) : Runnable {
        override fun run() {
            manager.startStrongSwan(ip, alias, userName, passWord)
        }
    }

    override fun onVpnStateChanged(type: VpnTypeMerge, state: VpnStateMerge) {
        this.vpnState = state
        this.vpnType = type
        if (state == VpnStateMerge.ERROR && vpnRunnableQueue.isNotEmpty()) {
            vpnRunnableQueue.pollFirst()?.let { handler.post(it) }
            return
        }
        if (state == VpnStateMerge.CONNECTED) vpnRunnableQueue.clear()
        listener?.onVpnStateChanged(type, state)
    }

}
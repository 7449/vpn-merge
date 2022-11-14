package com.github.vpn.merge

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import de.blinkt.openvpn.core.ICSOpenVPNApplication
import de.blinkt.openvpn.custom.OpenVpnApplicationProxy
import org.strongswan.android.custom.StrongSwanApplicationProxy
import org.strongswan.android.logic.StrongSwanApplication

class VpnApplication : Application() {

    companion object {

        @Suppress("ObjectPropertyName")
        @SuppressLint("StaticFieldLeak")
        private var _context: Context? = null

        val application: Context get() = requireNotNull(_context)

    }

    private val openVpnApplicationProxy: OpenVpnApplicationProxy = ICSOpenVPNApplication()
    private val strongSwanApplicationProxy: StrongSwanApplicationProxy = StrongSwanApplication()

    override fun onCreate() {
        super.onCreate()
        _context = this.applicationContext
        openVpnApplicationProxy.onCreateProxy(applicationContext)
        strongSwanApplicationProxy.onCreateProxy(applicationContext)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(openVpnApplicationProxy.attachBaseContextProxy(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        openVpnApplicationProxy.onConfigurationChangedProxy(newConfig)
    }

}
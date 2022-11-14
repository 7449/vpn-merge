package com.github.vpn.merge.state

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import com.github.vpn.merge.R
import com.github.vpn.merge.VpnApplication
import de.blinkt.openvpn.core.ConnectionStatus
import de.blinkt.openvpn.custom.OpenVpnNotificationProxy
import org.strongswan.android.custom.StrongSwanNotificationProxy
import org.strongswan.android.logic.CharonVpnService
import org.strongswan.android.logic.VpnStateService

object VpnMergeManagerNotification {

    fun notificationStrongSwanProxy() {
        StrongSwanNotificationProxy.proxy(object :
            StrongSwanNotificationProxy.OnStrongSwanNotificationProxyListener {
            override fun showNotification(
                context: Context,
                status: VpnStateService.State?,
                errorState: VpnStateService.ErrorState?
            ): Notification {
                return notification(
                    VpnStateMerge.getStrongSwanStateString(status, errorState)
                ).first
            }
        })
    }

    fun notificationOpenVpnProxy() {
        OpenVpnNotificationProxy.proxy(object :
            OpenVpnNotificationProxy.OnOpenVpnNotificationProxyListener {
            override fun showNotification(
                context: Context,
                status: ConnectionStatus?
            ): Pair<Notification, Int> {
                return notification(VpnStateMerge.getOpenVpnStateString(status))
            }
        })
    }

    fun notification(status: String): Pair<Notification, Int> {
        return VpnApplication.application
            .createOpenVpnNotification()
            .setContentTitle(status)
            .setContentText("Speedy VPN")
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    VpnApplication.application.resources,
                    R.mipmap.ic_launcher
                )
            )
            .build() to CharonVpnService.VPN_STATE_NOTIFICATION_ID
    }

    fun showNotification(status: String) {
        val notificationManager =
            VpnApplication.application.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notification = notification(status)
        notificationManager?.notify(notification.second, notification.first)
    }

    private fun Context.createOpenVpnNotification(): Notification.Builder {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                packageName, getString(R.string.app_names), NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setShowBadge(false)
            notificationManager?.createNotificationChannel(notificationChannel)
            Notification.Builder(this, packageName)
        } else {
            Notification.Builder(this)
        }
    }

}
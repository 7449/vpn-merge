package com.github.vpn.merge.state

import de.blinkt.openvpn.core.ConnectionStatus
import org.strongswan.android.logic.VpnStateService.ErrorState
import org.strongswan.android.logic.VpnStateService.State

enum class VpnStateMerge {

    CONNECTING,

    CONNECTED,

    DISCONNECTING,

    DISCONNECTED,

    ERROR,

    ;

    companion object {

        fun getOpenVpnStateString(state: ConnectionStatus?): String {
            return "OpenVpn : " + getOpenVpnState(state)
        }

        fun getOpenVpnState(state: ConnectionStatus?): VpnStateMerge {
            return when (state ?: ConnectionStatus.LEVEL_NOTCONNECTED) {
                ConnectionStatus.LEVEL_CONNECTED -> CONNECTED
                ConnectionStatus.LEVEL_VPNPAUSED -> TODO()
                ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED -> CONNECTING
                ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET -> CONNECTING
                ConnectionStatus.LEVEL_NONETWORK -> CONNECTING
                ConnectionStatus.LEVEL_NOTCONNECTED -> DISCONNECTED
                ConnectionStatus.LEVEL_START -> CONNECTING
                ConnectionStatus.LEVEL_AUTH_FAILED -> ERROR
                ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT -> TODO()
                ConnectionStatus.UNKNOWN_LEVEL -> ERROR
            }
        }

        fun getStrongSwanStateString(state: State?, errorState: ErrorState?): String {
            return "StrongSwan:" + getStrongSwanState(state, errorState)
        }

        fun getStrongSwanState(state: State?, errorState: ErrorState?): VpnStateMerge {
            if (errorState != null && errorState != ErrorState.NO_ERROR) return ERROR
            return when (state ?: State.DISABLED) {
                State.DISABLED -> DISCONNECTED
                State.CONNECTING -> CONNECTING
                State.CONNECTED -> CONNECTED
                State.DISCONNECTING -> DISCONNECTED
            }
        }

    }

}
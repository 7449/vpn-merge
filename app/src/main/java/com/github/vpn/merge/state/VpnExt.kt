package com.github.vpn.merge.state

import android.content.Context
import android.os.Bundle
import android.os.RemoteException
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.github.vpn.merge.R
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import org.strongswan.android.data.VpnProfile
import org.strongswan.android.data.VpnProfileDataSource
import org.strongswan.android.data.VpnType
import org.strongswan.android.logic.TrustedCertificateManager
import java.io.ByteArrayInputStream
import java.io.StringReader
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.*

private const val CERTIFICATE_TYPE = "X509"
private const val KEYSTORE_TYPE = "LocalCertificateStore"

fun Context.startOpenVpn(
    alwaysShowNotification: Boolean,
    reader: String,
) {
    val cp = ConfigParser()
    cp.parseConfig(StringReader(reader))
    val vp = cp.convertProfile()
    if (vp.checkProfile(this) != de.blinkt.openvpn.R.string.no_error_found) {
        throw RemoteException(getString(vp.checkProfile(this)))
    }
    vp.mProfileCreator = packageName
    ProfileManager.setTemporaryProfile(this, vp)
    val startVPN = vp.getStartServiceIntent(this, null)
    startVPN.putExtra(OpenVPNService.ALWAYS_SHOW_NOTIFICATION, alwaysShowNotification)
    ContextCompat.startForegroundService(this, startVPN)
}

fun Context.getStrongSwanBundle(
    ip: String,
    alias: String,
    userName: String,
    passWord: String
): Bundle {
    val hardCodedVpnProfile = getStrongSwanProfile(ip, alias, userName, passWord)
    return bundleOf(
        VpnProfileDataSource.KEY_UUID to hardCodedVpnProfile.uuid.toString(),
        VpnProfileDataSource.KEY_PASSWORD to hardCodedVpnProfile.password,
    )
}

private fun Context.getStrongSwanProfile(
    ip: String,
    alias: String,
    userName: String,
    passWord: String
): VpnProfile {
    val profile = VpnProfile()
    profile.uuid = UUID.randomUUID()
    profile.name = resources.getString(R.string.app_names)
    profile.gateway = ip
    profile.username = userName
    profile.vpnType = VpnType.IKEV2_EAP
    profile.password = passWord
    profile.certificateAlias = alias
    profile.remoteId = ip
    with(VpnProfileDataSource(this)) {
        open().insertProfile(profile)
        close()
    }
    return profile
}

fun String.getAlias(): String {
    val certificate = convertCert()
    val store = KeyStore.getInstance(KEYSTORE_TYPE)
    store.load(null, null)
    store.setCertificateEntry(null, certificate)
    TrustedCertificateManager.getInstance().reset()
    return store.getCertificateAlias(certificate).orEmpty()
}

private fun String.convertCert(): Certificate? {
    return runCatching {
        CertificateFactory
            .getInstance(CERTIFICATE_TYPE)
            .generateCertificate(ByteArrayInputStream(Base64.decode(this, Base64.DEFAULT)))
    }.getOrNull()
}
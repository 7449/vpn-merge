package com.github.vpn.merge

import android.app.Activity
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

abstract class TestVpnBaseActivity(layout: Int) : AppCompatActivity(layout) {

    protected val vpnResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            vpnForActivityResult(result)
        }

    private fun vpnForActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnConnect()
        } else {
            Toast.makeText(this, "cancel vpn connect", Toast.LENGTH_SHORT).show()
        }
    }

    abstract fun startVpnConnect()

}
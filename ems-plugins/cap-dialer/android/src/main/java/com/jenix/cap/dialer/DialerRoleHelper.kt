package com.jenix.cap.dialer

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager

object DialerRoleHelper {
    fun isDefault(context: Context): Boolean {
        val telecom = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        return telecom.defaultDialerPackage == context.packageName
    }

    fun requestIntent(context: Context): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            return roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        }
        return Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
    }
}

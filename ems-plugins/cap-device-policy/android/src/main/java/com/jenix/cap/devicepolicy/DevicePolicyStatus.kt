package com.jenix.cap.devicepolicy

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.getcapacitor.JSObject

object DevicePolicyStatus {
    fun status(context: Context): JSObject {
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, FieldForceDeviceAdminReceiver::class.java)
        val deviceOwner = manager.isDeviceOwnerApp(context.packageName)
        val profileOwner = manager.isProfileOwnerApp(context.packageName)
        val active = manager.isAdminActive(admin)
        return JSObject().apply {
            put("managed", deviceOwner || profileOwner || active)
            put("deviceOwner", deviceOwner)
            put("profileOwner", profileOwner)
            put("deviceAdminActive", active)
        }
    }

    fun capabilities(context: Context): JSObject {
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, FieldForceDeviceAdminReceiver::class.java)
        val canManage = manager.isDeviceOwnerApp(context.packageName) || manager.isProfileOwnerApp(context.packageName)
        return JSObject().apply {
            put("canLockNow", manager.isAdminActive(admin))
            put("canWipeData", canManage)
            put("canSetKeyguardDisabled", canManage)
        }
    }
}

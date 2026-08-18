package com.jenix.cap.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

class CoreStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        CoreKeys.PREFS,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun baseUrl() = prefs.getString(CoreKeys.BASE_URL, null)
    fun companyId() = prefs.getString(CoreKeys.COMPANY_ID, null)
    fun userAccessToken() = prefs.getString(CoreKeys.USER_ACCESS_TOKEN, null)
    fun userRefreshToken() = prefs.getString(CoreKeys.USER_REFRESH_TOKEN, null)
    fun userId() = prefs.getString(CoreKeys.USER_ID, null)
    fun employeeId() = prefs.getString(CoreKeys.EMPLOYEE_ID, null)
    fun deviceToken() = prefs.getString(CoreKeys.DEVICE_TOKEN, null)

    fun defaultHeaders(): JSONObject {
        return JSONObject(prefs.getString(CoreKeys.DEFAULT_HEADERS, "{}") ?: "{}")
    }

    fun saveConfig(baseUrl: String, companyId: String?, headers: JSONObject) {
        prefs.edit()
            .putString(CoreKeys.BASE_URL, baseUrl)
            .putString(CoreKeys.COMPANY_ID, companyId)
            .putString(CoreKeys.DEFAULT_HEADERS, headers.toString())
            .apply()
    }

    fun saveUserSession(accessToken: String, refreshToken: String?, userId: String?, employeeId: String?, companyId: String?) {
        prefs.edit()
            .putString(CoreKeys.USER_ACCESS_TOKEN, accessToken)
            .putString(CoreKeys.USER_REFRESH_TOKEN, refreshToken)
            .putString(CoreKeys.USER_ID, userId)
            .putString(CoreKeys.EMPLOYEE_ID, employeeId)
            .putString(CoreKeys.COMPANY_ID, companyId ?: prefs.getString(CoreKeys.COMPANY_ID, null))
            .apply()
    }

    fun saveDeviceToken(token: String) {
        prefs.edit().putString(CoreKeys.DEVICE_TOKEN, token).apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(CoreKeys.USER_ACCESS_TOKEN)
            .remove(CoreKeys.USER_REFRESH_TOKEN)
            .remove(CoreKeys.USER_ID)
            .remove(CoreKeys.EMPLOYEE_ID)
            .remove(CoreKeys.DEVICE_TOKEN)
            .apply()
    }
}

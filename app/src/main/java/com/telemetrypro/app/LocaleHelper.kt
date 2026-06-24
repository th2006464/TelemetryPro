package com.telemetrypro.app

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Locale helper — persists language preference and wraps contexts
 * with the correct locale configuration.
 *
 * Language codes: "zh" (default, Chinese), "en" (English)
 */
object LocaleHelper {

    private const val PREFS_NAME = "telemetry_pro_prefs"
    private const val KEY_LANG = "app_language"
    private const val DEFAULT_LANG = "zh"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getLanguage(context: Context): String {
        init(context)
        return prefs?.getString(KEY_LANG, DEFAULT_LANG) ?: DEFAULT_LANG
    }

    fun setLanguage(context: Context, lang: String) {
        init(context)
        prefs?.edit()?.putString(KEY_LANG, lang)?.apply()
    }

    fun isZh(context: Context): Boolean = getLanguage(context) == "zh"

    /**
     * Wrap a context with the stored locale for proper resource resolution.
     * Uses safe Configuration copy to avoid modifying the base config.
     */
    fun wrapContext(context: Context): Context {
        return try {
            val lang = getLanguage(context)
            val locale = Locale(lang)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }

            context.createConfigurationContext(config)
        } catch (e: Exception) {
            // Fallback: return unwrapped context on any configuration error
            context
        }
    }
}

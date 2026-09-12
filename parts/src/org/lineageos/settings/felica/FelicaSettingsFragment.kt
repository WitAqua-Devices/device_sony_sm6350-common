/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.felica

import android.os.Bundle
import android.os.SystemProperties
import android.util.Log
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import java.io.File

private const val TAG = "FelicaParts"

const val FELICA_MODEL_KEY = "felica_model"

/* init.felica_model.rc binds <config dir>/felica_<value> over <config dir>/felica. */
const val FELICA_MODEL_PROP = "persist.felica.model"

private const val FELICA_PREFIX = "felica_"

class FelicaSettingsFragment :
    SettingsBasePreferenceFragment(), Preference.OnPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.felica_settings)

        val preference = findPreference<ListPreference>(FELICA_MODEL_KEY)!!

        /*
         * The sets that ship are whatever the device tree extracted, so read
         * them off the filesystem rather than carrying a model list here: this
         * app is the same on every handset that has an element, and only the
         * directory it looks in differs.
         */
        val models =
            File(getString(R.string.felica_config_dir))
                .list { dir, name ->
                    name.startsWith(FELICA_PREFIX) && File(dir, name).isDirectory
                }
                ?.map { it.removePrefix(FELICA_PREFIX) }
                ?.sorted()
                ?: emptyList()

        if (models.isEmpty()) {
            preference.isVisible = false
            return
        }

        /*
         * A set is named after whatever the device tree calls that model, which
         * is not always what the handset is sold as - joan spells it as an LG
         * SKU, for one. Devices that need a friendlier name give the pairs in
         * felica_model_labels; anything unlisted shows the name as it is.
         */
        val labels =
            resources.getStringArray(R.array.felica_model_labels)
                .mapNotNull { it.split('|', limit = 2).takeIf { p -> p.size == 2 } }
                .associate { (name, label) -> name to label }

        /* An empty value leaves the choice to the model detection in init. */
        preference.entries =
            (listOf(getString(R.string.felica_model_automatic)) +
                models.map { labels[it] ?: it })
                .toTypedArray()
        preference.entryValues = (listOf("") + models).toTypedArray()
        preference.value = SystemProperties.get(FELICA_MODEL_PROP, "")
        preference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        preference.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference.key != FELICA_MODEL_KEY) {
            return false
        }

        /*
         * The property is labelled in the device tree's sepolicy, and init binds
         * off it. Both of those are easy to ship without the other, and a
         * settings screen is no place to die over it, so say so and leave the
         * selection where it was.
         */
        try {
            SystemProperties.set(FELICA_MODEL_PROP, newValue as String)
        } catch (e: RuntimeException) {
            Log.e(TAG, "could not set $FELICA_MODEL_PROP", e)
            Toast.makeText(context, R.string.felica_model_failed, Toast.LENGTH_LONG).show()
            return false
        }

        /*
         * init picks this up straight away, but a bind mount is invisible to a
         * file descriptor somebody already holds - the FeliCa applications have
         * to be restarted, and a reboot is the honest way to say that.
         */
        return true
    }
}

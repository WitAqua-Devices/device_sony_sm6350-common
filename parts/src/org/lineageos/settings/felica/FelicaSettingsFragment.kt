/*
 * Copyright (C) 2026 The WitAqua Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.felica

import android.os.Bundle
import android.os.SystemProperties
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import java.io.File

const val FELICA_MODEL_KEY = "felicaModel"

/* init.felica_model.rc binds /vendor/etc/felica_<value> over /vendor/etc/felica. */
const val FELICA_MODEL_PROP = "persist.felica.model"

private const val FELICA_DIR = "/vendor/etc"
private const val FELICA_PREFIX = "felica_"

class FelicaSettingsFragment :
    SettingsBasePreferenceFragment(), Preference.OnPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.felica_settings)

        val preference = findPreference<ListPreference>(FELICA_MODEL_KEY)!!

        /*
         * The sets that ship are whatever the device tree extracted, so read
         * them off the filesystem rather than carrying a model list here - this
         * app is shared by every sm6350 handset.
         */
        val models =
            File(FELICA_DIR)
                .list { _, name -> name.startsWith(FELICA_PREFIX) }
                ?.map { it.removePrefix(FELICA_PREFIX) }
                ?.sorted()
                ?: emptyList()

        if (models.isEmpty()) {
            preference.isVisible = false
            return
        }

        /* An empty value means "leave it to the model detection in init.sony.rc". */
        preference.entries =
            (listOf(getString(R.string.felica_model_automatic)) + models).toTypedArray()
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
         * init picks this up straight away, but a bind mount is invisible to a
         * file descriptor somebody already holds - the FeliCa applications have
         * to be restarted, and a reboot is the honest way to say that.
         */
        SystemProperties.set(FELICA_MODEL_PROP, newValue as String)
        return true
    }
}

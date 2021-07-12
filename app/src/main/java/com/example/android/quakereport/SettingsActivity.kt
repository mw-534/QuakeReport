package com.example.android.quakereport

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    class EarthquakePreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
        /**
         * Called during [.onCreate] to supply the preferences for this fragment.
         * Subclasses are expected to call [.setPreferenceScreen] either
         * directly or via helper methods such as [.addPreferencesFromResource].
         *
         * @param savedInstanceState If the fragment is being re-created from a previous saved state,
         * this is the state.
         * @param rootKey            If non-null, this preference fragment should be rooted at the
         * [PreferenceScreen] with this key.
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val minMagnitudePref = findPreference<Preference>(getString(R.string.settings_min_magnitude_key))
            bindPreferenceSummaryToValue(minMagnitudePref)

            val orderByPref = findPreference<Preference>(getString(R.string.settings_order_by_key))
            bindPreferenceSummaryToValue(orderByPref)
        }

        /**
         * Called when a preference has been changed by the user. This is called before the state
         * of the preference is about to be updated and before the state is persisted.
         *
         * @param preference The changed preference
         * @param newValue   The new value of the preference
         * @return `true` to update the state of the preference with the new value
         */
        override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
            val strValue = newValue.toString()
            // Kotlin keyword 'is' functions as a check and a cast in one
            if (preference is ListPreference) {
                val prefIndex = preference.findIndexOfValue(strValue)
                if (prefIndex >= 0) {
                    val labels = preference.entries
                    preference.summary = labels[prefIndex]
                }
            } else {
                preference?.summary = strValue
            }
            return true
        }

        private fun bindPreferenceSummaryToValue(pref: Preference?) {
            pref?.onPreferenceChangeListener = this
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(pref?.context)
            val strPref = sharedPrefs.getString(pref?.key, "")
            onPreferenceChange(pref, strPref)
        }
    }
}
package com.example.android.quakereport

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager

/** URL for earthquake data from the USGS dataset */
private const val USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query"

/**
 * ViewModel for the EarthquakeActivity..
 */
class EarthquakesViewModel(application: Application) : AndroidViewModel(application) {
    /** List of [Earthquake]s. */
    val earthquakes: MutableLiveData<ArrayList<Earthquake>> by lazy {
        MutableLiveData<ArrayList<Earthquake>>().also {
            // Fetch earthquake data on a background thread.
            Thread {
                // Build URI from shared preferences.
                // Get shared preferences.
                val context = getApplication<Application>()
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                // Get shared pref 'min_magnitude'.
                val minMagnitude = sharedPrefs.getString(
                    context.getString(R.string.settings_min_magnitude_key),
                    context.getString(R.string.settings_min_magnitude_default)
                )
                // Get shared pref 'order_by'.
                val orderBy = sharedPrefs.getString(
                    context.getString(R.string.settings_order_by_key),
                    context.getString(R.string.settings_order_by_default)
                )
                // Build URI.
                val baseUri = Uri.parse(USGS_REQUEST_URL)
                val uriBuilder = baseUri.buildUpon()
                uriBuilder.apply {
                    appendQueryParameter("format","geojson")
                    appendQueryParameter("limit","10")
                    appendQueryParameter("minmag", minMagnitude)
                    appendQueryParameter("orderby", orderBy)
                }

                // Receive a list of earthquake locations from USGS.
                val result = QueryUtils.fetchEarthquakeData(uriBuilder.toString())
                // Use postValue() instead of setValue() because value is assigned
                // from a background thread which isn't allowed for setValue().
                it.postValue(result)
            }.start()
        }
    }
}
/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

class EarthquakeActivity : AppCompatActivity() {

    /** Adapter for the list of [Earthquake]s. */
    private var mAdapter: EarthquakeAdapter? = null

    /** TextView that is displayed when the list is empty */
    private var mEmptyStateTextView: TextView? = null

    /** ProgressBar that is displayed while list is loading. */
    private var mLoadingIndicator: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earthquake)

        // Find a reference to the {@link ListView} in the layout
        val earthquakeListView = findViewById<ListView>(R.id.list)

        // Find a reference to the empty state TextView.
        mEmptyStateTextView = findViewById<TextView>(R.id.empty_view)

        // Find a reference to the loading indicator
        mLoadingIndicator = findViewById<ProgressBar>(R.id.loading_indicator)

        // Set the alternative view which should be shown while the ListView contains no data.
        earthquakeListView.emptyView = mEmptyStateTextView

        // Create a new adapter that takes an empty list of earthquakes as input
        mAdapter = EarthquakeAdapter(this, ArrayList<Earthquake>())

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.adapter = mAdapter

        // Check if internet connection is available
        if (hasNetworkConnection()) {

            // Retrieve EarthquakesViewModel. If the ViewModel doesn't exit it will
            // be created otherwise e.g. on a configuration change it will assign
            // the already existing ViewModel instead.
            // Dependency needed for line below: 'androidx.activity:activity-ktx:1.2.3'
            val model: EarthquakesViewModel by viewModels()
            // alternative:
            //val model = ViewModelProvider(this).get(EarthquakesViewModel::class.java)

            // Register observer with ViewModel to update the EarthquakeAdapter on the UI thread
            // ones the earthquakes have been fetched asynchronously from the web server and
            // are posted to the LiveData wrapper of the list of earthquakes.
            model.earthquakes.observe(this, Observer { earthquakes ->
                // Hide loading indicator because the data has been loaded
                mLoadingIndicator?.visibility = View.GONE

                // Add text for empty state TextView after first load so user
                // won't be confused by showing a no data found message
                // if the data hasn't been loaded yet.
                mEmptyStateTextView?.text = getString(R.string.no_earthquakes)

                // Clear the adapter of previous earthquake data.
                mAdapter?.clear()

                // If there is a valid list of earthquakes then add them to the adapter's data set.
                // This will trigger the ListView to update.
                mAdapter?.addAll(earthquakes)
            })

            // --- Outdated approach: ---
            // Don't use this approach
            // as it introduces memory leaks on configuration changes.
            //  Query earthquakes on a background thread and update adapter on the UI thread
            //  ones the background query finishes.
            /*Thread {
                // Receive a list of earthquake locations from USGS.
                val earthquakes = QueryUtils.fetchEarthquakeData(USGS_REQUEST_URL)

                // Update adapter on the UI thread.
                this@EarthquakeActivity.runOnUiThread {
                    // Clear the adapter of previous earthquake data.
                    mAdapter?.clear()

                    // If there is a valid list of earthquakes then add them to the adapter's data set.
                    // This will trigger the ListView to update.
                    mAdapter?.addAll(earthquakes)
                }
            }.start()*/

        } else {
            mLoadingIndicator?.visibility = View.GONE
            mEmptyStateTextView?.text = getString(R.string.no_network_connection)
        }

        // Set click listener to open details on this earthquake in a web browser
        earthquakeListView.setOnItemClickListener { parent, view, position, id ->
            // Find the current earthquake that was clicked on
            val currentEarthquake = parent.getItemAtPosition(position) as Earthquake

            // Convert the String URL into a URI object (to pass into the Intent constructor)
            val earthquakeUri = Uri.parse(currentEarthquake.url)

            // Create a new intent to view the earthquake URI
            val websiteIntent = Intent(Intent.ACTION_VIEW, earthquakeUri)

            // Send the intent to launch a new activity
            //if (websiteIntent.resolveActivity(applicationContext.packageManager) != null) { // doesn't work with resolve activity even though an activity can be started without this if check. Problem occurs on emulator and real device.
                startActivity(websiteIntent)
            //}
        }
    }

    /**
     * Check if network connection is available.
     * Needs permission: android.permission.ACCESS_NETWORK_STATE
     */
    private fun hasNetworkConnection(): Boolean {
        var isConnected = false
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val nwCap = cm.getNetworkCapabilities(network) ?: return false
            when {
                nwCap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                nwCap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                nwCap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            cm.activeNetworkInfo?.isConnectedOrConnecting ?: false
        }
        return isConnected
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     *
     * This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * [.onPrepareOptionsMenu].
     *
     *
     * The default implementation populates the menu with standard system
     * menu items.  These are placed in the [Menu.CATEGORY_SYSTEM] group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     *
     *
     * You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     *
     *
     * When you add items to the menu, you can implement the Activity's
     * [.onOptionsItemSelected] method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     *
     * @see .onPrepareOptionsMenu
     *
     * @see .onOptionsItemSelected
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     *
     * Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     *
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     *
     * @see .onCreateOptionsMenu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        /** Tag for log messages */
        val LOG_TAG = EarthquakeActivity::class.java.name


    }
}
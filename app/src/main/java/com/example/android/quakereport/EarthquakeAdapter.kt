package com.example.android.quakereport

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * The part of the location string from the USGS service that we use to determine
 * whether or not there is a location offset present ("5km N of Cairo, Egypt").
 */
private const val LOCATION_SEPARATOR = " of "

/**
 * Fills list items based on [Earthquake] objects.
 *
 * @constructor Constructs a new [EarthquakeAdapter].
 *
 * @param context is the Context of the app.
 * @param earthquakes is a list of [Earthquake] objects, which is the data source of the adapter.
 */
class EarthquakeAdapter(context: Context, earthquakes: ArrayList<Earthquake>) :
    ArrayAdapter<Earthquake>(context, 0, earthquakes) {

    /**
     * Returns a list item view that displays information about the earthquake at the given position
     * in the list of earthquakes.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Check if there is an existing list item view (called convertView) that we can resue,
        // otherwise, if convertView is null, then inflate a new list item layout.
        val listItemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.earthquake_list_item, parent, false)

        // Find the earthquake at the given position in the list of earthquakes.
        val currentEarthquake = getItem(position) as Earthquake

        // Find magnitude TextView
        val magnitudeView = listItemView.findViewById<TextView>(R.id.magnitude)
        // Format the magnitude to show 1 decimal place
        val formattedMagnitude = formatMagnitude(currentEarthquake.magnitude)
        // Display the magnitude of the current earthquake in that TextView
        magnitudeView.text = formattedMagnitude

        // Set the proper background color on the magnitude circle.
        // Fetch the background from the TextView, which is a GradientDrawable.
        val magnitudeCircle = magnitudeView.background as GradientDrawable

        // Get the appropriate background color based on the current earthquake magnitude.
        val magnitudeColor = getMagnitudeColor(currentEarthquake.magnitude)

        // Set the color on the magnitude circle.
        magnitudeCircle.setColor(magnitudeColor)

        // Get the original location string from the Earthquake object,
        // which can be in the format of "5km N of Cairo, Egypt" or "Pacific-Antarctic Ridge".
        val originalLocation = currentEarthquake.location

        // If the original location string (i.e. "5km N of Cairo, Egypt") contains
        // a primary location (Cairo, Egypt) and a location offset (5km N of that city)
        // then store the primary location separately from the location offset in 2 Strings,
        // so they can be displayed in 2 TextViews.
        val primaryLocation: String
        val locationOffset: String

        // Check whether the originalLocation string contains the " of " text
        if (originalLocation.contains(LOCATION_SEPARATOR)) {
            // Split the string into different parts (as an array of Strings)
            // based on the " of " text. We expect an array of 2 Strings, where
            // the first String will be "5km N" and the second String will be "Cairo, Egypt".
            val parts = originalLocation.split(LOCATION_SEPARATOR)
            // Location offset should be "5km N " + " of " --> "5km N of"
            locationOffset = parts[0] + LOCATION_SEPARATOR
            // Primary location should be "Cairo, Egypt"
            primaryLocation = parts[1]
        } else {
            // Otherwise, there is no " of " text in the originalLocation string.
            // Hence, set the default location offset to say "Near the".
            locationOffset = context.getString(R.string.near_the)
            // The primary location will be the full location string "Pacific-Antarctic Ridge".
            primaryLocation = originalLocation
        }

        // Find offset location TextView and display location offset based on the current earthquake.
        val locationOffsetView = listItemView.findViewById<TextView>(R.id.location_offset)
        locationOffsetView.text = locationOffset

        // Find primary location TextView and display location of the current earthquake in it.
        val primaryLocationView = listItemView.findViewById<TextView>(R.id.primary_location)
        primaryLocationView.text = primaryLocation

        // Create a new Date object from the time in milliseconds of the earthquake
        val dateObject = Date(currentEarthquake.timeInMilliseconds)

        // Find the TextView with view ID date
        val dateView = listItemView.findViewById<TextView>(R.id.date)
        // Format the date string (i.e. "Mar 3, 1984")
        val formattedDate: String = formatDate(dateObject)
        // Display the date of the current earthquake in that TextView
        dateView.text = formattedDate

        // Find the TextView with view ID time
        val timeView = listItemView.findViewById<TextView>(R.id.time)
        // Format the time string (i.e. "4:30PM")
        val formattedTime: String = formatTime(dateObject)
        // Display the time of the current earthquake in that TextView
        timeView.text = formattedTime

        // Return the list item view that is now showing the appropriate data.
        return listItemView
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private fun formatDate(dateObject: Date): String {
        val dateFormat = SimpleDateFormat("LLL dd, yyyy")
        return dateFormat.format(dateObject)
    }

    /**
     * Return the formatted date string (i.e. "4:30 PM") from a Date object.
     */
    private fun formatTime(dateObject: Date): String {
        val timeFormat = SimpleDateFormat("h:mm a")
        return timeFormat.format(dateObject)
    }

    /**
     * Return the formatted magnitude string showing 1 decimal place (i.e. "3.2")
     * from a decimal magnitude value.
     */
    private fun formatMagnitude(magnitude: Double): String {
        val decimalFormatter = DecimalFormat("0.0")
        return decimalFormatter.format(magnitude)
    }

    /**
     * Return the color of the magnitude circle based on the intensity of the earthquake.
     *
     * @param magnitude of the earthquake
     */
    private fun getMagnitudeColor(magnitude: Double) : Int {
        val magnitudeColorResId = when (magnitude.toInt()) {
            in 0..1 -> R.color.magnitude1
            2 -> R.color.magnitude2
            3 -> R.color.magnitude3
            4 -> R.color.magnitude4
            5 -> R.color.magnitude5
            6 -> R.color.magnitude6
            7 -> R.color.magnitude7
            8 -> R.color.magnitude8
            9 -> R.color.magnitude9
            else -> R.color.magnitude10plus
        }
        return ContextCompat.getColor(context, magnitudeColorResId)
    }

}
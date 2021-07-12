package com.example.android.quakereport

import java.text.SimpleDateFormat
import java.util.*


/**
 * Contains data that describes an earthquake.
 *
 * @constructor Creates an earthquake object.
 *
 * @property magnitude is the magnitude of the earthquake.
 * @property location is the city location of the earthquake.
 * @property timeInMilliseconds is the time in milliseconds (from the Epoch) when the
 *  earthquake happened
 * @property url is the website URL to find more information about the earthquake.
 */
class Earthquake(val magnitude: Double,
                 val location: String,
                 val timeInMilliseconds: Long,
                 val url: String)
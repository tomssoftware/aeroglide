package com.alpsfly.aeroglide.data.repository

import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.alpsfly.aeroglide.data.util.locationDataFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface LocationRepository {
    val locationDataSource: Flow<Location>
}
@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override val locationDataSource = locationManager.locationDataFlow(context, 1000L)
}

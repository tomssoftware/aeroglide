package com.alpsfly.aeroglide.core.units

import java.util.*

private fun Locale.isMetricInternal() = country.uppercase() != "US"
fun isMetric() = Locale.getDefault().isMetricInternal()
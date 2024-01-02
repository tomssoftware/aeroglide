package com.alpsfly.aeroglide.core.common.filter
/*
 * Copyright 2018, Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * A base implementation of an averaging fusedOrientation.
 *
 * Created by kaleb on 7/6/17.
 */
abstract class AveragingFilter(open var timeConstant: Float = DEFAULT_TIME_CONSTANT) {
    protected var startTime: Long = 0L
    protected var count: Long = 0L
    protected var output = floatArrayOf(0f, 0f, 0f)
    protected var size = 3

    fun reset(data: FloatArray): FloatArray {
        startTime = 0L
        count = 0L
        output = data.clone()
        size = data.size
        return output
    }

    fun start(data: FloatArray): FloatArray {
        startTime = System.nanoTime()
        count = 0L
        size = data.size
        output = data.clone()
        return output
    }

    abstract fun filter(data: FloatArray, nanoTime: Long): FloatArray

    companion object {
        var DEFAULT_TIME_CONSTANT = 0.18f
    }
}
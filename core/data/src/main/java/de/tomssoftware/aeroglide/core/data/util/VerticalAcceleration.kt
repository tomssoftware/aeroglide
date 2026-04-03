package de.tomssoftware.aeroglide.core.data.util

import de.tomssoftware.aeroglide.core.model.hardware.SensorData
import kotlin.math.sqrt

fun getVerticalAcceleration(linearAcceleration: SensorData, rotationVector: SensorData): Float {
    // Based on http://stackoverflow.com/questions/31268852/measuring-vertical-movement-of-non-fixed-android-device
    val rotationMatrix = FloatArray(9) // Both 9 and 16 works, depending on what you're doing with it
    getRotationMatrixFromVector(rotationMatrix, rotationVector.values)
    val accelerationInWorldFrame = matrixMulti3x9(linearAcceleration.values, rotationMatrixTranspose(rotationMatrix))
    return accelerationInWorldFrame[2]
}

private fun matrixMulti3x9(a: FloatArray, b: FloatArray): FloatArray {
    val result = FloatArray(3)
    result[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6]
    result[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7]
    result[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8]
    return result
}

private fun rotationMatrixTranspose(rotationMatrix: FloatArray): FloatArray {
    val result = FloatArray(9)
    result[0] = rotationMatrix[0]
    result[1] = rotationMatrix[3]
    result[2] = rotationMatrix[6]
    result[3] = rotationMatrix[1]
    result[4] = rotationMatrix[4]
    result[5] = rotationMatrix[7]
    result[6] = rotationMatrix[2]
    result[7] = rotationMatrix[5]
    result[8] = rotationMatrix[8]
    return result
}

// copied from Sensormanager in order to run unit tests
private fun getRotationMatrixFromVector(R: FloatArray, rotationVector: FloatArray) {
    var q0: Float
    val q1 = rotationVector[0]
    val q2 = rotationVector[1]
    val q3 = rotationVector[2]
    if (rotationVector.size >= 4) {
        q0 = rotationVector[3]
    } else {
        q0 = 1 - q1 * q1 - q2 * q2 - q3 * q3
        q0 = if (q0 > 0) sqrt(q0.toDouble()).toFloat() else 0f
    }
    val sq_q1 = 2 * q1 * q1
    val sq_q2 = 2 * q2 * q2
    val sq_q3 = 2 * q3 * q3
    val q1_q2 = 2 * q1 * q2
    val q3_q0 = 2 * q3 * q0
    val q1_q3 = 2 * q1 * q3
    val q2_q0 = 2 * q2 * q0
    val q2_q3 = 2 * q2 * q3
    val q1_q0 = 2 * q1 * q0
    if (R.size == 9) {
        R[0] = 1 - sq_q2 - sq_q3
        R[1] = q1_q2 - q3_q0
        R[2] = q1_q3 + q2_q0
        R[3] = q1_q2 + q3_q0
        R[4] = 1 - sq_q1 - sq_q3
        R[5] = q2_q3 - q1_q0
        R[6] = q1_q3 - q2_q0
        R[7] = q2_q3 + q1_q0
        R[8] = 1 - sq_q1 - sq_q2
    } else if (R.size == 16) {
        R[0] = 1 - sq_q2 - sq_q3
        R[1] = q1_q2 - q3_q0
        R[2] = q1_q3 + q2_q0
        R[3] = 0.0f
        R[4] = q1_q2 + q3_q0
        R[5] = 1 - sq_q1 - sq_q3
        R[6] = q2_q3 - q1_q0
        R[7] = 0.0f
        R[8] = q1_q3 - q2_q0
        R[9] = q2_q3 + q1_q0
        R[10] = 1 - sq_q1 - sq_q2
        R[11] = 0.0f
        R[14] = 0.0f
        R[13] = R[14]
        R[12] = R[13]
        R[15] = 1.0f
    }
}

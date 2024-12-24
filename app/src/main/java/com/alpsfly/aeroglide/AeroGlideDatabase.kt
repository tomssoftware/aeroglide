package com.alpsfly.aeroglide

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alpsfly.aeroglide.core.data.model.local.Altitude
import com.alpsfly.aeroglide.core.data.model.local.AltitudeDao
import com.alpsfly.aeroglide.core.data.model.local.Calibration
import com.alpsfly.aeroglide.core.data.model.local.CalibrationDao
import com.alpsfly.aeroglide.core.data.model.local.Climbrate
import com.alpsfly.aeroglide.core.data.model.local.ClimbrateDao
import com.alpsfly.aeroglide.core.data.model.local.Elevation
import com.alpsfly.aeroglide.core.data.model.local.ElevationDao
import com.alpsfly.aeroglide.core.data.model.local.Position
import com.alpsfly.aeroglide.core.data.model.local.PositionDao
import com.alpsfly.aeroglide.core.data.model.local.Track
import com.alpsfly.aeroglide.core.data.model.local.TrackDao
import com.alpsfly.aeroglide.core.data.model.local.TrackLog
import com.alpsfly.aeroglide.core.data.model.local.TrackLogDao
import com.alpsfly.aeroglide.core.data.model.local.User
import com.alpsfly.aeroglide.core.data.model.local.UserDao
import com.alpsfly.aeroglide.core.data.model.local.VelocityDao
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Created by Thomas on 19.02.2018.
 */
@Database(
    version = 2,
    exportSchema = true,
    entities = [
    Altitude::class,
    Climbrate::class,
    Calibration::class,
    Elevation::class,
    Position::class,
    Track::class,
    TrackLog::class,
    User::class]
)
abstract class AeroGlideDatabase : RoomDatabase() {

    abstract fun altitudeDao(): AltitudeDao
    abstract fun climbrateDao(): ClimbrateDao
    abstract fun velocityDao(): VelocityDao
    abstract fun elevationDao(): ElevationDao
    abstract fun userDao(): UserDao
    abstract fun positionDao(): PositionDao
    abstract fun trackDao(): TrackDao
    abstract fun trackLogDao(): TrackLogDao
    abstract fun calibrationDao(): CalibrationDao

//    companion object {
//        @Volatile
//        private var INSTANCE: AeroGlideDatabase? = null
//
//        fun getDatabase(context: Context, name: String): AeroGlideDatabase {
//            val tempInstance = INSTANCE
//            if (tempInstance != null) {
//                return tempInstance
//            }
//            synchronized(this) {
//                val instance = Room.databaseBuilder(context.applicationContext, AeroGlideDatabase::class.java, name)
//                    .addMigrations(MIGRATION_01_02)
//                    .build()
//
//                INSTANCE = instance
//                return instance
//            }
//        }
//
//        const val trackLogCreateTableVer2 = "CREATE TABLE track_log_v2 " +
//                "(track_log_id INTEGER NOT NULL, " +
//                "track_id INTEGER NOT NULL, " +
//                "air_pressure_timestamp INTEGER NOT NULL, " +
//                "air_pressure REAL NOT NULL, " +
//                "altitude_timestamp INTEGER NOT NULL, " +
//                "altitude REAL NOT NULL, " +
//                "aviation_timestamp INTEGER NOT NULL, " +
//                "ascent REAL NOT NULL, " +
//                "descent REAL NOT NULL, " +
//                "climbrate_timestamp INTEGER NOT NULL, " +
//                "climbrate REAL NOT NULL, " +
//                "glideratio REAL NOT NULL, " +
//                "grade REAL NOT NULL, " +
//                "elevation_timestamp INTEGER NOT NULL, " +
//                "elevation INTEGER NOT NULL, " +
//                "compass_timestamp INTEGER NOT NULL, " +
//                "azimuth REAL NOT NULL, " +
//                "position_timestamp INTEGER NOT NULL, " +
//                "accuracy REAL NOT NULL, " +
//                "latitude REAL NOT NULL, " +
//                "longitude REAL NOT NULL, " +
//                "gps_altitude REAL NOT NULL, " +
//                "velocity_timestamp INTEGER NOT NULL, " +
//                "velocity REAL NOT NULL, " +
//                "bearing REAL NOT NULL, " +
//                "wind_indication_timestamp INTEGER NOT NULL, " +
//                "wind_speed REAL NOT NULL, " +
//                "wind_direction REAL NOT NULL, " +
//                "heartrate_timestamp INTEGER NOT NULL, " +
//                "heartrate INTEGER NOT NULL, " +
//                "PRIMARY KEY(track_log_id))"
//        const val trackLogCopyDataVer2 = "INSERT INTO track_log_v2(" +
//                "track_log_id, " +
//                "track_id, " +
//                "air_pressure_timestamp, " +
//                "air_pressure, " +
//                "altitude_timestamp, " +
//                "altitude, " +
//                "aviation_timestamp, " +
//                "ascent, " +
//                "descent, " +
//                "climbrate_timestamp, " +
//                "climbrate, " +
//                "glideratio, " +
//                "grade, " +
//                "elevation_timestamp, " +
//                "elevation, " +
//                "compass_timestamp, " +
//                "azimuth, " +
//                "position_timestamp, " +
//                "accuracy, " +
//                "latitude, " +
//                "longitude, " +
//                "gps_altitude, " +
//                "velocity_timestamp, " +
//                "velocity, " +
//                "bearing, " +
//                "wind_indication_timestamp, " +
//                "wind_speed, " +
//                "wind_direction, " +
//                "heartrate_timestamp, " +
//                "heartrate) " +
//                "SELECT " +
//                "track_log_id, " +
//                "track_id, " +
//                "air_pressure_timestamp, " +
//                "air_pressure, " +
//                "altitude_timestamp, " +
//                "altitude, " +
//                "aviation_timestamp, " +
//                "ascent, " +
//                "descent, " +
//                "climbrate_timestamp, " +
//                "climbrate, " +
//                "glideratio, " +
//                "0.0 as grade, " +
//                "elevation_timestamp, " +
//                "elevation, " +
//                "compass_timestamp, " +
//                "azimuth, " +
//                "position_timestamp, " +
//                "accuracy, " +
//                "latitude, " +
//                "longitude, " +
//                "gps_altitude, " +
//                "velocity_timestamp, " +
//                "velocity, " +
//                "bearing, " +
//                "wind_indication_timestamp, " +
//                "wind_speed, " +
//                "wind_direction, " +
//                "0 as heartrate_timestamp," +
//                "0 as heartrate " +
//                "FROM track_log"
//        const val trackLogDropTable = "DROP TABLE track_log"
//        const val trackLogRenameTableVer2 = "ALTER TABLE track_log_v2 RENAME TO track_log"
//
//        const val trackCreateTableVer2 = "CREATE TABLE `track_v2` " +
//                "(`track_id` INTEGER NOT NULL, " +
//                "`user_id` TEXT NOT NULL, " +
//                "`name` TEXT NOT NULL, " +
//                "`area` TEXT NOT NULL, " +
//                "`departure_date` INTEGER NOT NULL, " +
//                "`departure_time` INTEGER NOT NULL, " +
//                "`distance` REAL NOT NULL, " +
//                "`duration` INTEGER NOT NULL, " +
//                "`lat` REAL NOT NULL, " +
//                "`lon` REAL NOT NULL, " +
//                "`ascent` REAL NOT NULL, " +
//                "`descent` REAL NOT NULL, " +
//                "`max_altitude` REAL NOT NULL, " +
//                "`min_altitude` REAL NOT NULL, " +
//                "`max_speed` REAL NOT NULL, " +
//                "`min_speed` REAL NOT NULL, " +
//                "`max_climbrate` REAL NOT NULL, " +
//                "`min_climbrate` REAL NOT NULL, " +
//                "`max_climbrate_int` REAL NOT NULL, " +
//                "`min_climbrate_int` REAL NOT NULL, " +
//                "`max_grade` REAL NOT NULL, " +
//                "`min_grade` REAL NOT NULL, " +
//                "`max_heartrate` INTEGER NOT NULL, " +
//                "`min_heartrate` INTEGER NOT NULL, " +
//                "PRIMARY KEY(`track_id`))"
//        const val trackCopyDataVer2 = "INSERT INTO track_v2(" +
//                "track_id, " +
//                "user_id, " +
//                "name, " +
//                "area, " +
//                "departure_date, " +
//                "departure_time, " +
//                "distance, " +
//                "duration, " +
//                "lat, " +
//                "lon, " +
//                "ascent, " +
//                "descent, " +
//                "max_altitude, " +
//                "min_altitude, " +
//                "max_speed, " +
//                "min_speed, " +
//                "max_climbrate, " +
//                "min_climbrate, " +
//                "max_climbrate_int, " +
//                "min_climbrate_int, " +
//                "max_grade, " +
//                "min_grade, " +
//                "max_heartrate, " +
//                "min_heartrate) " +
//                "SELECT " +
//                "track_id, " +
//                "user_id, " +
//                "name, " +
//                "area, " +
//                "departure_date, " +
//                "departure_time, " +
//                "distance, " +
//                "duration, " +
//                "lat, " +
//                "lon, " +
//                "ascent, " +
//                "descent, " +
//                "max_altitude, " +
//                "0 as min_altitude, " +
//                "max_speed, " +
//                "0 as min_speed, " +
//                "max_climbrate, " +
//                "min_climbrate, " +
//                "max_climbrate_int, " +
//                "min_climbrate_int, " +
//                "0 as max_grade, " +
//                "0 as min_grade, " +
//                "0 as max_heartrate, " +
//                "0 as min_heartrate " +
//                "FROM track"
//        const val trackDropTable = "DROP TABLE track"
//        const val trackRenameTableVer2 = "ALTER TABLE track_v2 RENAME TO track"
//
//        const val climbrateCreateTableVer2 = "CREATE TABLE `climbrate_v2` " +
//                "(`timestamp` INTEGER NOT NULL, " +
//                "`climbrate` REAL NOT NULL, " +
//                "`grade` REAL NOT NULL, " +
//                "PRIMARY KEY(`timestamp`))"
//        const val climbrateCopyDataVer2 = "INSERT INTO climbrate_v2 SELECT *, 0.0 as grade FROM climbrate"
//        const val climbrateDropTable = "DROP TABLE climbrate"
//        const val climbrateRenameTableVer2 = "ALTER TABLE climbrate_v2 RENAME TO climbrate"
//
//        val MIGRATION_01_02 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                try {
//                    database.execSQL(trackLogCreateTableVer2)
//                    database.execSQL(trackLogCopyDataVer2)
//                    database.execSQL(trackLogDropTable)
//                    database.execSQL(trackLogRenameTableVer2)
//
//                    database.execSQL(trackCreateTableVer2)
//                    database.execSQL(trackCopyDataVer2)
//                    database.execSQL(trackDropTable)
//                    database.execSQL(trackRenameTableVer2)
//
//                    database.execSQL(climbrateCreateTableVer2)
//                    database.execSQL(climbrateCopyDataVer2)
//                    database.execSQL(climbrateDropTable)
//                    database.execSQL(climbrateRenameTableVer2)
//                } catch (e: java.lang.Exception) {
//                    Timber.wtf(e.localizedMessage)
//                }
//            }
//        }
//
//        fun backup(context: Context, name: String) {
//            try {
//                val sd = File(context.cacheDir.absolutePath + "/" + "backup")
//                // todo add resource + context.resources.getString(R.string.app_backup_folder))
//                if (sd.canWrite()) {
//                    val currentDBPath = context.applicationContext.getDatabasePath(name).absolutePath
//                    val backupDBPath = "$name.db"
//                    val currentDB = File(currentDBPath)
//                    val backupDB = File(sd, backupDBPath)
//
//                    if (currentDB.exists()) {
//                        val src = FileInputStream(currentDB).channel
//                        val dst = FileOutputStream(backupDB).channel
//                        dst.transferFrom(src, 0, src.size())
//                        src.close()
//                        dst.close()
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
}

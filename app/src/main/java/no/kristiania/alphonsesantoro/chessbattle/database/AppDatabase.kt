package no.kristiania.alphonsesantoro.chessbattle.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [UserModel::class, GameModel::class, GameLineModel::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameModelDao
    abstract fun gameLineDao(): GameLineDao
    abstract fun userDao(): UserDao

    companion object {
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "chessbattle"
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT INTO UserModel(id, username, email, firebase_key) VALUES (1, 'Player One', 'default@chessbattle.com', null)")
                        }
                    }).build()
                }
            }
            return INSTANCE
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `GameModel` ADD type TEXT NOT NULL DEFAULT 'unknown'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `GameModel` ADD perspective TEXT NOT NULL DEFAULT 'WHITE'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `GameLineModel` ADD whiteFen TEXT")
                database.execSQL("ALTER TABLE `GameLineModel` ADD blackFen TEXT")
            }
        }
    }
}
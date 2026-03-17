package com.romadmin.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.romadmin.app.data.local.dao.DownloadDao;
import com.romadmin.app.data.local.dao.DownloadDao_Impl;
import com.romadmin.app.data.local.dao.SaveSyncDao;
import com.romadmin.app.data.local.dao.SaveSyncDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile DownloadDao _downloadDao;

  private volatile SaveSyncDao _saveSyncDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `downloaded_games` (`gameId` INTEGER NOT NULL, `platformId` INTEGER NOT NULL, `platformFolderName` TEXT NOT NULL, `fileName` TEXT NOT NULL, `displayName` TEXT NOT NULL, `localPath` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `isDirectory` INTEGER NOT NULL, `downloadedAt` INTEGER NOT NULL, `status` TEXT NOT NULL, `bytesDownloaded` INTEGER NOT NULL, `thumbnailUrl` TEXT, PRIMARY KEY(`gameId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `save_sync_state` (`gameId` INTEGER NOT NULL, `localSaveFileName` TEXT, `localTimestamp` INTEGER, `serverSaveId` INTEGER, `serverTimestamp` INTEGER, `lastSyncAt` INTEGER, `syncStatus` TEXT NOT NULL, PRIMARY KEY(`gameId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '233c27859b2fac7e7e4d62f742c94339')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `downloaded_games`");
        db.execSQL("DROP TABLE IF EXISTS `save_sync_state`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsDownloadedGames = new HashMap<String, TableInfo.Column>(12);
        _columnsDownloadedGames.put("gameId", new TableInfo.Column("gameId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("platformId", new TableInfo.Column("platformId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("platformFolderName", new TableInfo.Column("platformFolderName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("localPath", new TableInfo.Column("localPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("fileSize", new TableInfo.Column("fileSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("isDirectory", new TableInfo.Column("isDirectory", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("downloadedAt", new TableInfo.Column("downloadedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("bytesDownloaded", new TableInfo.Column("bytesDownloaded", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDownloadedGames.put("thumbnailUrl", new TableInfo.Column("thumbnailUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDownloadedGames = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDownloadedGames = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDownloadedGames = new TableInfo("downloaded_games", _columnsDownloadedGames, _foreignKeysDownloadedGames, _indicesDownloadedGames);
        final TableInfo _existingDownloadedGames = TableInfo.read(db, "downloaded_games");
        if (!_infoDownloadedGames.equals(_existingDownloadedGames)) {
          return new RoomOpenHelper.ValidationResult(false, "downloaded_games(com.romadmin.app.data.local.entity.DownloadedGame).\n"
                  + " Expected:\n" + _infoDownloadedGames + "\n"
                  + " Found:\n" + _existingDownloadedGames);
        }
        final HashMap<String, TableInfo.Column> _columnsSaveSyncState = new HashMap<String, TableInfo.Column>(7);
        _columnsSaveSyncState.put("gameId", new TableInfo.Column("gameId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaveSyncState.put("localSaveFileName", new TableInfo.Column("localSaveFileName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaveSyncState.put("localTimestamp", new TableInfo.Column("localTimestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaveSyncState.put("serverSaveId", new TableInfo.Column("serverSaveId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaveSyncState.put("serverTimestamp", new TableInfo.Column("serverTimestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaveSyncState.put("lastSyncAt", new TableInfo.Column("lastSyncAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSaveSyncState.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSaveSyncState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSaveSyncState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSaveSyncState = new TableInfo("save_sync_state", _columnsSaveSyncState, _foreignKeysSaveSyncState, _indicesSaveSyncState);
        final TableInfo _existingSaveSyncState = TableInfo.read(db, "save_sync_state");
        if (!_infoSaveSyncState.equals(_existingSaveSyncState)) {
          return new RoomOpenHelper.ValidationResult(false, "save_sync_state(com.romadmin.app.data.local.entity.SaveSyncState).\n"
                  + " Expected:\n" + _infoSaveSyncState + "\n"
                  + " Found:\n" + _existingSaveSyncState);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "233c27859b2fac7e7e4d62f742c94339", "c71f97c854878ae8f21f3f1d57df76e0");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "downloaded_games","save_sync_state");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `downloaded_games`");
      _db.execSQL("DELETE FROM `save_sync_state`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(DownloadDao.class, DownloadDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SaveSyncDao.class, SaveSyncDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public DownloadDao downloadDao() {
    if (_downloadDao != null) {
      return _downloadDao;
    } else {
      synchronized(this) {
        if(_downloadDao == null) {
          _downloadDao = new DownloadDao_Impl(this);
        }
        return _downloadDao;
      }
    }
  }

  @Override
  public SaveSyncDao saveSyncDao() {
    if (_saveSyncDao != null) {
      return _saveSyncDao;
    } else {
      synchronized(this) {
        if(_saveSyncDao == null) {
          _saveSyncDao = new SaveSyncDao_Impl(this);
        }
        return _saveSyncDao;
      }
    }
  }
}

package com.romadmin.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.romadmin.app.data.local.entity.DownloadStatus;
import com.romadmin.app.data.local.entity.DownloadedGame;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DownloadDao_Impl implements DownloadDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DownloadedGame> __insertionAdapterOfDownloadedGame;

  private final EntityDeletionOrUpdateAdapter<DownloadedGame> __deletionAdapterOfDownloadedGame;

  private final EntityDeletionOrUpdateAdapter<DownloadedGame> __updateAdapterOfDownloadedGame;

  private final SharedSQLiteStatement __preparedStmtOfUpdateProgress;

  private final SharedSQLiteStatement __preparedStmtOfMarkCompleted;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByGameId;

  public DownloadDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDownloadedGame = new EntityInsertionAdapter<DownloadedGame>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `downloaded_games` (`gameId`,`platformId`,`platformFolderName`,`fileName`,`displayName`,`localPath`,`fileSize`,`isDirectory`,`downloadedAt`,`status`,`bytesDownloaded`,`thumbnailUrl`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DownloadedGame entity) {
        statement.bindLong(1, entity.getGameId());
        statement.bindLong(2, entity.getPlatformId());
        statement.bindString(3, entity.getPlatformFolderName());
        statement.bindString(4, entity.getFileName());
        statement.bindString(5, entity.getDisplayName());
        statement.bindString(6, entity.getLocalPath());
        statement.bindLong(7, entity.getFileSize());
        final int _tmp = entity.isDirectory() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindLong(9, entity.getDownloadedAt());
        statement.bindString(10, __DownloadStatus_enumToString(entity.getStatus()));
        statement.bindLong(11, entity.getBytesDownloaded());
        if (entity.getThumbnailUrl() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getThumbnailUrl());
        }
      }
    };
    this.__deletionAdapterOfDownloadedGame = new EntityDeletionOrUpdateAdapter<DownloadedGame>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `downloaded_games` WHERE `gameId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DownloadedGame entity) {
        statement.bindLong(1, entity.getGameId());
      }
    };
    this.__updateAdapterOfDownloadedGame = new EntityDeletionOrUpdateAdapter<DownloadedGame>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `downloaded_games` SET `gameId` = ?,`platformId` = ?,`platformFolderName` = ?,`fileName` = ?,`displayName` = ?,`localPath` = ?,`fileSize` = ?,`isDirectory` = ?,`downloadedAt` = ?,`status` = ?,`bytesDownloaded` = ?,`thumbnailUrl` = ? WHERE `gameId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DownloadedGame entity) {
        statement.bindLong(1, entity.getGameId());
        statement.bindLong(2, entity.getPlatformId());
        statement.bindString(3, entity.getPlatformFolderName());
        statement.bindString(4, entity.getFileName());
        statement.bindString(5, entity.getDisplayName());
        statement.bindString(6, entity.getLocalPath());
        statement.bindLong(7, entity.getFileSize());
        final int _tmp = entity.isDirectory() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindLong(9, entity.getDownloadedAt());
        statement.bindString(10, __DownloadStatus_enumToString(entity.getStatus()));
        statement.bindLong(11, entity.getBytesDownloaded());
        if (entity.getThumbnailUrl() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getThumbnailUrl());
        }
        statement.bindLong(13, entity.getGameId());
      }
    };
    this.__preparedStmtOfUpdateProgress = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE downloaded_games SET status = ?, bytesDownloaded = ? WHERE gameId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkCompleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE downloaded_games SET status = ?, downloadedAt = ? WHERE gameId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByGameId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM downloaded_games WHERE gameId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final DownloadedGame download,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDownloadedGame.insert(download);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final DownloadedGame download,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfDownloadedGame.handle(download);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final DownloadedGame download,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDownloadedGame.handle(download);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProgress(final int gameId, final DownloadStatus status, final long bytes,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateProgress.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __DownloadStatus_enumToString(status));
        _argIndex = 2;
        _stmt.bindLong(_argIndex, bytes);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, gameId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateProgress.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markCompleted(final int gameId, final DownloadStatus status, final long completedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkCompleted.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __DownloadStatus_enumToString(status));
        _argIndex = 2;
        _stmt.bindLong(_argIndex, completedAt);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, gameId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkCompleted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByGameId(final int gameId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByGameId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, gameId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteByGameId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DownloadedGame>> getAllDownloads() {
    final String _sql = "SELECT * FROM downloaded_games ORDER BY downloadedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"downloaded_games"}, new Callable<List<DownloadedGame>>() {
      @Override
      @NonNull
      public List<DownloadedGame> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfPlatformId = CursorUtil.getColumnIndexOrThrow(_cursor, "platformId");
          final int _cursorIndexOfPlatformFolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "platformFolderName");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfIsDirectory = CursorUtil.getColumnIndexOrThrow(_cursor, "isDirectory");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfBytesDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "bytesDownloaded");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final List<DownloadedGame> _result = new ArrayList<DownloadedGame>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DownloadedGame _item;
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final int _tmpPlatformId;
            _tmpPlatformId = _cursor.getInt(_cursorIndexOfPlatformId);
            final String _tmpPlatformFolderName;
            _tmpPlatformFolderName = _cursor.getString(_cursorIndexOfPlatformFolderName);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final boolean _tmpIsDirectory;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDirectory);
            _tmpIsDirectory = _tmp != 0;
            final long _tmpDownloadedAt;
            _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            final DownloadStatus _tmpStatus;
            _tmpStatus = __DownloadStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final long _tmpBytesDownloaded;
            _tmpBytesDownloaded = _cursor.getLong(_cursorIndexOfBytesDownloaded);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            _item = new DownloadedGame(_tmpGameId,_tmpPlatformId,_tmpPlatformFolderName,_tmpFileName,_tmpDisplayName,_tmpLocalPath,_tmpFileSize,_tmpIsDirectory,_tmpDownloadedAt,_tmpStatus,_tmpBytesDownloaded,_tmpThumbnailUrl);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<DownloadedGame>> getByStatus(final DownloadStatus status) {
    final String _sql = "SELECT * FROM downloaded_games WHERE status = ? ORDER BY downloadedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __DownloadStatus_enumToString(status));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"downloaded_games"}, new Callable<List<DownloadedGame>>() {
      @Override
      @NonNull
      public List<DownloadedGame> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfPlatformId = CursorUtil.getColumnIndexOrThrow(_cursor, "platformId");
          final int _cursorIndexOfPlatformFolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "platformFolderName");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfIsDirectory = CursorUtil.getColumnIndexOrThrow(_cursor, "isDirectory");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfBytesDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "bytesDownloaded");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final List<DownloadedGame> _result = new ArrayList<DownloadedGame>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DownloadedGame _item;
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final int _tmpPlatformId;
            _tmpPlatformId = _cursor.getInt(_cursorIndexOfPlatformId);
            final String _tmpPlatformFolderName;
            _tmpPlatformFolderName = _cursor.getString(_cursorIndexOfPlatformFolderName);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final boolean _tmpIsDirectory;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDirectory);
            _tmpIsDirectory = _tmp != 0;
            final long _tmpDownloadedAt;
            _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            final DownloadStatus _tmpStatus;
            _tmpStatus = __DownloadStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final long _tmpBytesDownloaded;
            _tmpBytesDownloaded = _cursor.getLong(_cursorIndexOfBytesDownloaded);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            _item = new DownloadedGame(_tmpGameId,_tmpPlatformId,_tmpPlatformFolderName,_tmpFileName,_tmpDisplayName,_tmpLocalPath,_tmpFileSize,_tmpIsDirectory,_tmpDownloadedAt,_tmpStatus,_tmpBytesDownloaded,_tmpThumbnailUrl);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getByGameId(final int gameId,
      final Continuation<? super DownloadedGame> $completion) {
    final String _sql = "SELECT * FROM downloaded_games WHERE gameId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, gameId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DownloadedGame>() {
      @Override
      @Nullable
      public DownloadedGame call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfPlatformId = CursorUtil.getColumnIndexOrThrow(_cursor, "platformId");
          final int _cursorIndexOfPlatformFolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "platformFolderName");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfIsDirectory = CursorUtil.getColumnIndexOrThrow(_cursor, "isDirectory");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfBytesDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "bytesDownloaded");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final DownloadedGame _result;
          if (_cursor.moveToFirst()) {
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final int _tmpPlatformId;
            _tmpPlatformId = _cursor.getInt(_cursorIndexOfPlatformId);
            final String _tmpPlatformFolderName;
            _tmpPlatformFolderName = _cursor.getString(_cursorIndexOfPlatformFolderName);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final boolean _tmpIsDirectory;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDirectory);
            _tmpIsDirectory = _tmp != 0;
            final long _tmpDownloadedAt;
            _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            final DownloadStatus _tmpStatus;
            _tmpStatus = __DownloadStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final long _tmpBytesDownloaded;
            _tmpBytesDownloaded = _cursor.getLong(_cursorIndexOfBytesDownloaded);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            _result = new DownloadedGame(_tmpGameId,_tmpPlatformId,_tmpPlatformFolderName,_tmpFileName,_tmpDisplayName,_tmpLocalPath,_tmpFileSize,_tmpIsDirectory,_tmpDownloadedAt,_tmpStatus,_tmpBytesDownloaded,_tmpThumbnailUrl);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<DownloadedGame> observeByGameId(final int gameId) {
    final String _sql = "SELECT * FROM downloaded_games WHERE gameId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, gameId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"downloaded_games"}, new Callable<DownloadedGame>() {
      @Override
      @Nullable
      public DownloadedGame call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfPlatformId = CursorUtil.getColumnIndexOrThrow(_cursor, "platformId");
          final int _cursorIndexOfPlatformFolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "platformFolderName");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfIsDirectory = CursorUtil.getColumnIndexOrThrow(_cursor, "isDirectory");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfBytesDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "bytesDownloaded");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final DownloadedGame _result;
          if (_cursor.moveToFirst()) {
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final int _tmpPlatformId;
            _tmpPlatformId = _cursor.getInt(_cursorIndexOfPlatformId);
            final String _tmpPlatformFolderName;
            _tmpPlatformFolderName = _cursor.getString(_cursorIndexOfPlatformFolderName);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final boolean _tmpIsDirectory;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDirectory);
            _tmpIsDirectory = _tmp != 0;
            final long _tmpDownloadedAt;
            _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            final DownloadStatus _tmpStatus;
            _tmpStatus = __DownloadStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final long _tmpBytesDownloaded;
            _tmpBytesDownloaded = _cursor.getLong(_cursorIndexOfBytesDownloaded);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            _result = new DownloadedGame(_tmpGameId,_tmpPlatformId,_tmpPlatformFolderName,_tmpFileName,_tmpDisplayName,_tmpLocalPath,_tmpFileSize,_tmpIsDirectory,_tmpDownloadedAt,_tmpStatus,_tmpBytesDownloaded,_tmpThumbnailUrl);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<DownloadedGame>> getByPlatformId(final int platformId) {
    final String _sql = "SELECT * FROM downloaded_games WHERE platformId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, platformId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"downloaded_games"}, new Callable<List<DownloadedGame>>() {
      @Override
      @NonNull
      public List<DownloadedGame> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfPlatformId = CursorUtil.getColumnIndexOrThrow(_cursor, "platformId");
          final int _cursorIndexOfPlatformFolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "platformFolderName");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfIsDirectory = CursorUtil.getColumnIndexOrThrow(_cursor, "isDirectory");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfBytesDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "bytesDownloaded");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final List<DownloadedGame> _result = new ArrayList<DownloadedGame>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DownloadedGame _item;
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final int _tmpPlatformId;
            _tmpPlatformId = _cursor.getInt(_cursorIndexOfPlatformId);
            final String _tmpPlatformFolderName;
            _tmpPlatformFolderName = _cursor.getString(_cursorIndexOfPlatformFolderName);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final boolean _tmpIsDirectory;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDirectory);
            _tmpIsDirectory = _tmp != 0;
            final long _tmpDownloadedAt;
            _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            final DownloadStatus _tmpStatus;
            _tmpStatus = __DownloadStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final long _tmpBytesDownloaded;
            _tmpBytesDownloaded = _cursor.getLong(_cursorIndexOfBytesDownloaded);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            _item = new DownloadedGame(_tmpGameId,_tmpPlatformId,_tmpPlatformFolderName,_tmpFileName,_tmpDisplayName,_tmpLocalPath,_tmpFileSize,_tmpIsDirectory,_tmpDownloadedAt,_tmpStatus,_tmpBytesDownloaded,_tmpThumbnailUrl);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<DownloadedGame>> getByStatuses(final List<? extends DownloadStatus> statuses) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM downloaded_games WHERE status IN (");
    final int _inputSize = statuses.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (DownloadStatus _item : statuses) {
      _statement.bindString(_argIndex, __DownloadStatus_enumToString(_item));
      _argIndex++;
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"downloaded_games"}, new Callable<List<DownloadedGame>>() {
      @Override
      @NonNull
      public List<DownloadedGame> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfPlatformId = CursorUtil.getColumnIndexOrThrow(_cursor, "platformId");
          final int _cursorIndexOfPlatformFolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "platformFolderName");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfIsDirectory = CursorUtil.getColumnIndexOrThrow(_cursor, "isDirectory");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfBytesDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "bytesDownloaded");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final List<DownloadedGame> _result = new ArrayList<DownloadedGame>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DownloadedGame _item_1;
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final int _tmpPlatformId;
            _tmpPlatformId = _cursor.getInt(_cursorIndexOfPlatformId);
            final String _tmpPlatformFolderName;
            _tmpPlatformFolderName = _cursor.getString(_cursorIndexOfPlatformFolderName);
            final String _tmpFileName;
            _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final String _tmpLocalPath;
            _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final boolean _tmpIsDirectory;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDirectory);
            _tmpIsDirectory = _tmp != 0;
            final long _tmpDownloadedAt;
            _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            final DownloadStatus _tmpStatus;
            _tmpStatus = __DownloadStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final long _tmpBytesDownloaded;
            _tmpBytesDownloaded = _cursor.getLong(_cursorIndexOfBytesDownloaded);
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            _item_1 = new DownloadedGame(_tmpGameId,_tmpPlatformId,_tmpPlatformFolderName,_tmpFileName,_tmpDisplayName,_tmpLocalPath,_tmpFileSize,_tmpIsDirectory,_tmpDownloadedAt,_tmpStatus,_tmpBytesDownloaded,_tmpThumbnailUrl);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> countByStatus(final DownloadStatus status) {
    final String _sql = "SELECT COUNT(*) FROM downloaded_games WHERE status = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __DownloadStatus_enumToString(status));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"downloaded_games"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __DownloadStatus_enumToString(@NonNull final DownloadStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case DOWNLOADING: return "DOWNLOADING";
      case COMPLETED: return "COMPLETED";
      case FAILED: return "FAILED";
      case PAUSED: return "PAUSED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private DownloadStatus __DownloadStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return DownloadStatus.PENDING;
      case "DOWNLOADING": return DownloadStatus.DOWNLOADING;
      case "COMPLETED": return DownloadStatus.COMPLETED;
      case "FAILED": return DownloadStatus.FAILED;
      case "PAUSED": return DownloadStatus.PAUSED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}

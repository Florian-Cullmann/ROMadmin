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
import com.romadmin.app.data.local.entity.SaveSyncState;
import com.romadmin.app.data.local.entity.SyncStatus;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
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
public final class SaveSyncDao_Impl implements SaveSyncDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SaveSyncState> __insertionAdapterOfSaveSyncState;

  private final EntityDeletionOrUpdateAdapter<SaveSyncState> __deletionAdapterOfSaveSyncState;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByGameId;

  public SaveSyncDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSaveSyncState = new EntityInsertionAdapter<SaveSyncState>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `save_sync_state` (`gameId`,`localSaveFileName`,`localTimestamp`,`serverSaveId`,`serverTimestamp`,`lastSyncAt`,`syncStatus`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaveSyncState entity) {
        statement.bindLong(1, entity.getGameId());
        if (entity.getLocalSaveFileName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getLocalSaveFileName());
        }
        if (entity.getLocalTimestamp() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getLocalTimestamp());
        }
        if (entity.getServerSaveId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getServerSaveId());
        }
        if (entity.getServerTimestamp() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getServerTimestamp());
        }
        if (entity.getLastSyncAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getLastSyncAt());
        }
        statement.bindString(7, __SyncStatus_enumToString(entity.getSyncStatus()));
      }
    };
    this.__deletionAdapterOfSaveSyncState = new EntityDeletionOrUpdateAdapter<SaveSyncState>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `save_sync_state` WHERE `gameId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SaveSyncState entity) {
        statement.bindLong(1, entity.getGameId());
      }
    };
    this.__preparedStmtOfDeleteByGameId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM save_sync_state WHERE gameId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final SaveSyncState state, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaveSyncState.insert(state);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<SaveSyncState> states,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSaveSyncState.insert(states);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final SaveSyncState state, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSaveSyncState.handle(state);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
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
  public Flow<List<SaveSyncState>> getAll() {
    final String _sql = "SELECT * FROM save_sync_state";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"save_sync_state"}, new Callable<List<SaveSyncState>>() {
      @Override
      @NonNull
      public List<SaveSyncState> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfLocalSaveFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "localSaveFileName");
          final int _cursorIndexOfLocalTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "localTimestamp");
          final int _cursorIndexOfServerSaveId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverSaveId");
          final int _cursorIndexOfServerTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "serverTimestamp");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final List<SaveSyncState> _result = new ArrayList<SaveSyncState>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaveSyncState _item;
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final String _tmpLocalSaveFileName;
            if (_cursor.isNull(_cursorIndexOfLocalSaveFileName)) {
              _tmpLocalSaveFileName = null;
            } else {
              _tmpLocalSaveFileName = _cursor.getString(_cursorIndexOfLocalSaveFileName);
            }
            final Long _tmpLocalTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocalTimestamp)) {
              _tmpLocalTimestamp = null;
            } else {
              _tmpLocalTimestamp = _cursor.getLong(_cursorIndexOfLocalTimestamp);
            }
            final Integer _tmpServerSaveId;
            if (_cursor.isNull(_cursorIndexOfServerSaveId)) {
              _tmpServerSaveId = null;
            } else {
              _tmpServerSaveId = _cursor.getInt(_cursorIndexOfServerSaveId);
            }
            final Long _tmpServerTimestamp;
            if (_cursor.isNull(_cursorIndexOfServerTimestamp)) {
              _tmpServerTimestamp = null;
            } else {
              _tmpServerTimestamp = _cursor.getLong(_cursorIndexOfServerTimestamp);
            }
            final Long _tmpLastSyncAt;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmpLastSyncAt = null;
            } else {
              _tmpLastSyncAt = _cursor.getLong(_cursorIndexOfLastSyncAt);
            }
            final SyncStatus _tmpSyncStatus;
            _tmpSyncStatus = __SyncStatus_stringToEnum(_cursor.getString(_cursorIndexOfSyncStatus));
            _item = new SaveSyncState(_tmpGameId,_tmpLocalSaveFileName,_tmpLocalTimestamp,_tmpServerSaveId,_tmpServerTimestamp,_tmpLastSyncAt,_tmpSyncStatus);
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
      final Continuation<? super SaveSyncState> $completion) {
    final String _sql = "SELECT * FROM save_sync_state WHERE gameId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, gameId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SaveSyncState>() {
      @Override
      @Nullable
      public SaveSyncState call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfLocalSaveFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "localSaveFileName");
          final int _cursorIndexOfLocalTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "localTimestamp");
          final int _cursorIndexOfServerSaveId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverSaveId");
          final int _cursorIndexOfServerTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "serverTimestamp");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final SaveSyncState _result;
          if (_cursor.moveToFirst()) {
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final String _tmpLocalSaveFileName;
            if (_cursor.isNull(_cursorIndexOfLocalSaveFileName)) {
              _tmpLocalSaveFileName = null;
            } else {
              _tmpLocalSaveFileName = _cursor.getString(_cursorIndexOfLocalSaveFileName);
            }
            final Long _tmpLocalTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocalTimestamp)) {
              _tmpLocalTimestamp = null;
            } else {
              _tmpLocalTimestamp = _cursor.getLong(_cursorIndexOfLocalTimestamp);
            }
            final Integer _tmpServerSaveId;
            if (_cursor.isNull(_cursorIndexOfServerSaveId)) {
              _tmpServerSaveId = null;
            } else {
              _tmpServerSaveId = _cursor.getInt(_cursorIndexOfServerSaveId);
            }
            final Long _tmpServerTimestamp;
            if (_cursor.isNull(_cursorIndexOfServerTimestamp)) {
              _tmpServerTimestamp = null;
            } else {
              _tmpServerTimestamp = _cursor.getLong(_cursorIndexOfServerTimestamp);
            }
            final Long _tmpLastSyncAt;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmpLastSyncAt = null;
            } else {
              _tmpLastSyncAt = _cursor.getLong(_cursorIndexOfLastSyncAt);
            }
            final SyncStatus _tmpSyncStatus;
            _tmpSyncStatus = __SyncStatus_stringToEnum(_cursor.getString(_cursorIndexOfSyncStatus));
            _result = new SaveSyncState(_tmpGameId,_tmpLocalSaveFileName,_tmpLocalTimestamp,_tmpServerSaveId,_tmpServerTimestamp,_tmpLastSyncAt,_tmpSyncStatus);
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
  public Flow<SaveSyncState> observeByGameId(final int gameId) {
    final String _sql = "SELECT * FROM save_sync_state WHERE gameId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, gameId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"save_sync_state"}, new Callable<SaveSyncState>() {
      @Override
      @Nullable
      public SaveSyncState call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfLocalSaveFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "localSaveFileName");
          final int _cursorIndexOfLocalTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "localTimestamp");
          final int _cursorIndexOfServerSaveId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverSaveId");
          final int _cursorIndexOfServerTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "serverTimestamp");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final SaveSyncState _result;
          if (_cursor.moveToFirst()) {
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final String _tmpLocalSaveFileName;
            if (_cursor.isNull(_cursorIndexOfLocalSaveFileName)) {
              _tmpLocalSaveFileName = null;
            } else {
              _tmpLocalSaveFileName = _cursor.getString(_cursorIndexOfLocalSaveFileName);
            }
            final Long _tmpLocalTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocalTimestamp)) {
              _tmpLocalTimestamp = null;
            } else {
              _tmpLocalTimestamp = _cursor.getLong(_cursorIndexOfLocalTimestamp);
            }
            final Integer _tmpServerSaveId;
            if (_cursor.isNull(_cursorIndexOfServerSaveId)) {
              _tmpServerSaveId = null;
            } else {
              _tmpServerSaveId = _cursor.getInt(_cursorIndexOfServerSaveId);
            }
            final Long _tmpServerTimestamp;
            if (_cursor.isNull(_cursorIndexOfServerTimestamp)) {
              _tmpServerTimestamp = null;
            } else {
              _tmpServerTimestamp = _cursor.getLong(_cursorIndexOfServerTimestamp);
            }
            final Long _tmpLastSyncAt;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmpLastSyncAt = null;
            } else {
              _tmpLastSyncAt = _cursor.getLong(_cursorIndexOfLastSyncAt);
            }
            final SyncStatus _tmpSyncStatus;
            _tmpSyncStatus = __SyncStatus_stringToEnum(_cursor.getString(_cursorIndexOfSyncStatus));
            _result = new SaveSyncState(_tmpGameId,_tmpLocalSaveFileName,_tmpLocalTimestamp,_tmpServerSaveId,_tmpServerTimestamp,_tmpLastSyncAt,_tmpSyncStatus);
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
  public Object getByGameIds(final List<Integer> gameIds,
      final Continuation<? super List<SaveSyncState>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM save_sync_state WHERE gameId IN (");
    final int _inputSize = gameIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int _item : gameIds) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SaveSyncState>>() {
      @Override
      @NonNull
      public List<SaveSyncState> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfGameId = CursorUtil.getColumnIndexOrThrow(_cursor, "gameId");
          final int _cursorIndexOfLocalSaveFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "localSaveFileName");
          final int _cursorIndexOfLocalTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "localTimestamp");
          final int _cursorIndexOfServerSaveId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverSaveId");
          final int _cursorIndexOfServerTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "serverTimestamp");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncAt");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final List<SaveSyncState> _result = new ArrayList<SaveSyncState>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SaveSyncState _item_1;
            final int _tmpGameId;
            _tmpGameId = _cursor.getInt(_cursorIndexOfGameId);
            final String _tmpLocalSaveFileName;
            if (_cursor.isNull(_cursorIndexOfLocalSaveFileName)) {
              _tmpLocalSaveFileName = null;
            } else {
              _tmpLocalSaveFileName = _cursor.getString(_cursorIndexOfLocalSaveFileName);
            }
            final Long _tmpLocalTimestamp;
            if (_cursor.isNull(_cursorIndexOfLocalTimestamp)) {
              _tmpLocalTimestamp = null;
            } else {
              _tmpLocalTimestamp = _cursor.getLong(_cursorIndexOfLocalTimestamp);
            }
            final Integer _tmpServerSaveId;
            if (_cursor.isNull(_cursorIndexOfServerSaveId)) {
              _tmpServerSaveId = null;
            } else {
              _tmpServerSaveId = _cursor.getInt(_cursorIndexOfServerSaveId);
            }
            final Long _tmpServerTimestamp;
            if (_cursor.isNull(_cursorIndexOfServerTimestamp)) {
              _tmpServerTimestamp = null;
            } else {
              _tmpServerTimestamp = _cursor.getLong(_cursorIndexOfServerTimestamp);
            }
            final Long _tmpLastSyncAt;
            if (_cursor.isNull(_cursorIndexOfLastSyncAt)) {
              _tmpLastSyncAt = null;
            } else {
              _tmpLastSyncAt = _cursor.getLong(_cursorIndexOfLastSyncAt);
            }
            final SyncStatus _tmpSyncStatus;
            _tmpSyncStatus = __SyncStatus_stringToEnum(_cursor.getString(_cursorIndexOfSyncStatus));
            _item_1 = new SaveSyncState(_tmpGameId,_tmpLocalSaveFileName,_tmpLocalTimestamp,_tmpServerSaveId,_tmpServerTimestamp,_tmpLastSyncAt,_tmpSyncStatus);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __SyncStatus_enumToString(@NonNull final SyncStatus _value) {
    switch (_value) {
      case IN_SYNC: return "IN_SYNC";
      case LOCAL_NEWER: return "LOCAL_NEWER";
      case SERVER_NEWER: return "SERVER_NEWER";
      case NEVER_SYNCED: return "NEVER_SYNCED";
      case SYNCING: return "SYNCING";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private SyncStatus __SyncStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "IN_SYNC": return SyncStatus.IN_SYNC;
      case "LOCAL_NEWER": return SyncStatus.LOCAL_NEWER;
      case "SERVER_NEWER": return SyncStatus.SERVER_NEWER;
      case "NEVER_SYNCED": return SyncStatus.NEVER_SYNCED;
      case "SYNCING": return SyncStatus.SYNCING;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}

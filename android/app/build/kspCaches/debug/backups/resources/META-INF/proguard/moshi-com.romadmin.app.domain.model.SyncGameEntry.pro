-if class com.romadmin.app.domain.model.SyncGameEntry
-keepnames class com.romadmin.app.domain.model.SyncGameEntry
-if class com.romadmin.app.domain.model.SyncGameEntry
-keep class com.romadmin.app.domain.model.SyncGameEntryJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

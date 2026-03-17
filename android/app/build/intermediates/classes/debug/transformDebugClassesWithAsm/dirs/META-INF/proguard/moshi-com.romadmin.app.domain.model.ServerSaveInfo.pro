-if class com.romadmin.app.domain.model.ServerSaveInfo
-keepnames class com.romadmin.app.domain.model.ServerSaveInfo
-if class com.romadmin.app.domain.model.ServerSaveInfo
-keep class com.romadmin.app.domain.model.ServerSaveInfoJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

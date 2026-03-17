-if class com.romadmin.app.domain.model.SyncStatusGame
-keepnames class com.romadmin.app.domain.model.SyncStatusGame
-if class com.romadmin.app.domain.model.SyncStatusGame
-keep class com.romadmin.app.domain.model.SyncStatusGameJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.romadmin.app.domain.model.SyncStatusGame
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.romadmin.app.domain.model.SyncStatusGame
-keepclassmembers class com.romadmin.app.domain.model.SyncStatusGame {
    public synthetic <init>(int,java.lang.String,com.romadmin.app.domain.model.ServerSaveInfo,int,kotlin.jvm.internal.DefaultConstructorMarker);
}

-if class com.romadmin.app.domain.model.ServerSaveInfo
-keepnames class com.romadmin.app.domain.model.ServerSaveInfo
-if class com.romadmin.app.domain.model.ServerSaveInfo
-keep class com.romadmin.app.domain.model.ServerSaveInfoJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.romadmin.app.domain.model.ServerSaveInfo
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.romadmin.app.domain.model.ServerSaveInfo
-keepclassmembers class com.romadmin.app.domain.model.ServerSaveInfo {
    public synthetic <init>(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}

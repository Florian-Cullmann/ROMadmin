-if class com.romadmin.app.domain.model.LoginRequest
-keepnames class com.romadmin.app.domain.model.LoginRequest
-if class com.romadmin.app.domain.model.LoginRequest
-keep class com.romadmin.app.domain.model.LoginRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.romadmin.app.domain.model.LoginRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.romadmin.app.domain.model.LoginRequest
-keepclassmembers class com.romadmin.app.domain.model.LoginRequest {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}

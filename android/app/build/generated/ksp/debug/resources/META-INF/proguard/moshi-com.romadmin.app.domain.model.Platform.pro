-if class com.romadmin.app.domain.model.Platform
-keepnames class com.romadmin.app.domain.model.Platform
-if class com.romadmin.app.domain.model.Platform
-keep class com.romadmin.app.domain.model.PlatformJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.romadmin.app.domain.model.Platform
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.romadmin.app.domain.model.Platform
-keepclassmembers class com.romadmin.app.domain.model.Platform {
    public synthetic <init>(int,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,int,java.lang.String,java.lang.String,com.romadmin.app.domain.model.GameCount,int,kotlin.jvm.internal.DefaultConstructorMarker);
}

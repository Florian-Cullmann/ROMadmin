-if class com.romadmin.app.domain.model.Game
-keepnames class com.romadmin.app.domain.model.Game
-if class com.romadmin.app.domain.model.Game
-keep class com.romadmin.app.domain.model.GameJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.romadmin.app.domain.model.Game
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.romadmin.app.domain.model.Game
-keepclassmembers class com.romadmin.app.domain.model.Game {
    public synthetic <init>(int,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,boolean,java.lang.String,java.lang.String,com.romadmin.app.domain.model.Platform,int,kotlin.jvm.internal.DefaultConstructorMarker);
}

-if class com.romadmin.app.domain.model.LoginRequest
-keepnames class com.romadmin.app.domain.model.LoginRequest
-if class com.romadmin.app.domain.model.LoginRequest
-keep class com.romadmin.app.domain.model.LoginRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

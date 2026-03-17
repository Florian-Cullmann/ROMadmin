-if class com.romadmin.app.domain.model.LoginResponse
-keepnames class com.romadmin.app.domain.model.LoginResponse
-if class com.romadmin.app.domain.model.LoginResponse
-keep class com.romadmin.app.domain.model.LoginResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

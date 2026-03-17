-if class com.romadmin.app.domain.model.ApiKeyResponse
-keepnames class com.romadmin.app.domain.model.ApiKeyResponse
-if class com.romadmin.app.domain.model.ApiKeyResponse
-keep class com.romadmin.app.domain.model.ApiKeyResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

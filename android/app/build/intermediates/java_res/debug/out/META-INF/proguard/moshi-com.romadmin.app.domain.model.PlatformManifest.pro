-if class com.romadmin.app.domain.model.PlatformManifest
-keepnames class com.romadmin.app.domain.model.PlatformManifest
-if class com.romadmin.app.domain.model.PlatformManifest
-keep class com.romadmin.app.domain.model.PlatformManifestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

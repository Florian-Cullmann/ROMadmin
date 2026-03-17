-if class com.romadmin.app.domain.model.GameCount
-keepnames class com.romadmin.app.domain.model.GameCount
-if class com.romadmin.app.domain.model.GameCount
-keep class com.romadmin.app.domain.model.GameCountJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

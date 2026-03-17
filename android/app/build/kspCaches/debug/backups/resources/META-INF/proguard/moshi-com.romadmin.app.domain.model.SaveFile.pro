-if class com.romadmin.app.domain.model.SaveFile
-keepnames class com.romadmin.app.domain.model.SaveFile
-if class com.romadmin.app.domain.model.SaveFile
-keep class com.romadmin.app.domain.model.SaveFileJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

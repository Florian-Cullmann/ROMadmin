-if class com.romadmin.app.domain.model.User
-keepnames class com.romadmin.app.domain.model.User
-if class com.romadmin.app.domain.model.User
-keep class com.romadmin.app.domain.model.UserJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

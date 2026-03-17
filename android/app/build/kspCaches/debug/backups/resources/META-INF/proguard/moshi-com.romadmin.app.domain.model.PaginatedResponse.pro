-if class com.romadmin.app.domain.model.PaginatedResponse
-keepnames class com.romadmin.app.domain.model.PaginatedResponse
-if class com.romadmin.app.domain.model.PaginatedResponse
-keep class com.romadmin.app.domain.model.PaginatedResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi,java.lang.reflect.Type[]);
}

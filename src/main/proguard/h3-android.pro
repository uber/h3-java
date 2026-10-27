# These classes and constructors are looked up by name from the JNI layer.
-keep class com.uber.h3core.util.LatLng {
    public <init>(double, double);
}
-keep class com.uber.h3core.exceptions.H3Exception {
    public <init>(int);
}

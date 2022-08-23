# Migrating from h3-java version 3

The H3 library introduced breaking [changes](https://h3geo.org/docs/library/migrating-3.x) in 4.0.0 which are reflected in H3-Java 4.0.0.

Cell indexes generated in 3.x and 4.x are the same and can be used in either library version interchangably.

## Renaming

Functions and classes were renamed in H3 version 4.0.0 to have a more consistent and predictable naming scheme. The corresponding H3-Java names were changed to match.

### Legacy API

If you do not wish to use the new names, the H3-Java library provides `H3CoreV3` as a temporary compatability layer for applications. Your application can use `H3CoreV3` where it previously used `H3Core` and continue using the same function names. Because `H3CoreV3` uses the core library version 4.0.0 internally, exceptions and error codes are not backwards compatible.

## Exceptions

Specialized exceptions in H3-Java have been replaced with `H3Exception`, which is a `RuntimeException`. This exception wraps the error codes now returned by the H3 core library. The library may also throw standard Java exceptions like `IllegalArgumentException`.

## Removed

* `kRings`: This function is no longer exposed in the H3 library as it existed only for FFI performance reasons.
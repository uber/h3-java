# Versioning

The H3-Java library adheres to [Semantic Versioning](http://semver.org/).
Major and minor versions of the H3-Java library may not have a one-to-one
correspondence with major and minor versions of H3 (the core library).
Incorporating major or minor version changes of H3 requires a major or minor
version change to H3-Java.

The reason for not enforcing a one-to-one correspondence is so that the H3-Java
library can make bugfixes, add features, and correct backwards-incompatible
issues without having a corresponding change to the H3 core library itself.
This makes it clear when upgrading the library whether breaking changes are
being introduced, regardless of where the breaking change comes from.

# Versioning

The H3 core library adheres to [Semantic Versioning](http://semver.org/).
H3-Java has a `major.minor.patch` version scheme. The major and minor version
numbers of H3-Java is the major and minor version of the bound core library,
respectively. The patch version is incremented independently of the core
library.

Because H3-Java is versioned in lockstep with the H3 core library, please
avoid adding features or APIs which do not map onto the
[H3 core API](https://uber.github.io/h3/#/documentation/api-reference/).

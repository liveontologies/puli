[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://ci.appveyor.com/api/projects/status/9456b7873va4nli7?svg=true)](https://ci.appveyor.com/project/ykazakov/puli)

# Proof Utility Library

A library for manipulating with proofs based on inference rules.

For further information, see <https://github.com/liveontologies/puli>. 

## Usage

To use this library add the following maven dependency:
```
<dependency>
  <groupId>org.liveontologies</groupId>
  <artifactId>puli</artifactId>
  <version>...</version>
</dependency>
```
See `src/test/test` for examples on how to use this library.

To use snapshots versions of this library (if not compiled from sources), please add
the sonatype snapshot repository either to your `pom.xml` or `settings.xml`:
```
<repositories>
  <repository>
    <id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

## License

Proof Utility Library is Copyright (c) 2014 - 2022 Live Ontologies Project

All sources of this project are available under the terms of the 
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
(see the file `LICENSE.txt`).
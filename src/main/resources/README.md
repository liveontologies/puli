[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://ci.appveyor.com/api/projects/status/9456b7873va4nli7?svg=true)](https://ci.appveyor.com/project/ykazakov/puli)

# ${project.name}

${project.description}

For further information, see <${project.scm.url}>. 

## Usage

To use this library add the following Maven dependency:
```
<dependency>
  <groupId>${project.groupId}</groupId>
  <artifactId>${project.artifactId}</artifactId>
  <version>${releasedVersion.version}</version>
</dependency>
```
See `src/test/test` for examples on how to use this library.

To use snapshots versions of this library (if not compiled from sources), please add
the Sonatype OSSRH snapshot repository either to your `pom.xml` or `settings.xml`:
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

${project.name} is Copyright (c) ${project.inceptionYear} - ${currentYear} ${project.organization.name}

All sources of this project are available under the terms of the 
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
(see the file `LICENSE.txt`).
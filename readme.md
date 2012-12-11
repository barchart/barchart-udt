<!--

    Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->

## Java wrapper around native C++ UDT protocol implementation
{:toc}

### TCP 

/barchart-udt/site/barchart-udt-core/apidocs

TCP is [slow](http://barchart-udt.googlecode.com/svn/site/presentation/img6.html). 
UDT is [fast](http://barchart-udt.googlecode.com/svn/site/presentation/img9.html).

### UDT

[UDT is a reliable UDP](http://udt.sourceforge.net)
based application level data transport protocol for distributed data intensive applications.


UDT is developed by 
[Dr.Yunhong Gu](http://users.lac.uic.edu/~yunhong)
and others at University of Illinois.

UDT C++ implementation is available under 
[BSD license](http://udt.sourceforge.net/license.html)

### Barchart-UDT

Barchart-UDT is a Java wrapper around native C++ UDT protocol implementation.

Barchart-UDT is developed by Andrei Pozolotin and others at
[Barchart, Inc.](http://www.barchart.com)

Barchart-UDT is available under
[BSD license](http://udt.sourceforge.net/license.html)
as well.

Barchart-UDT exposes UDT protocol as both 
[java.net.Socket](http://java.sun.com/javase/6/docs/api/java/net/Socket.html)
and 
[java.nio.channels.SocketChannel](http://java.sun.com/javase/6/docs/api/java/nio/channels/SocketChannel.html)
and comes with a 
[java.nio.channels.spi.SelectorProvider](http://java.sun.com/javase/6/docs/api/java/nio/channels/spi/SelectorProvider.html)
.

### Developers Welcome

If you are an expert in Java, NIO, JNI, C++, if you need this project for yourself,

and if you can contribute to this project - please get in touch.

### Maven Dependency

Barchart-UDT **RELEASE** is available in
[maven central repository]
(http://search.maven.org/#search%7Cga%7C1%7Cbarchart-udt)
.

```
<dependencies>
	<dependency>
		<groupId>com.barchart.udt</groupId>
		<artifactId>barchart-udt-bundle</artifactId>
		<version>X.X.X</version>
		<type>jar</type>
		<scope>compile</scope>
	</dependency>
</dependencies>
```

To use Barchart-UDT **SNAPSHOT** as maven2 dependency in your java project, 

please provide the following **repository** and **dependency** definitions in your **pom.xml**:

```
<repositories>
	<repository>
		<id>sonatype-nexus-snapshots</id>
		<name>Sonatype Nexus Snapshots</name>
		<url>https://oss.sonatype.org/content/repositories/snapshots</url>
		<releases>
			<enabled>false</enabled>
		</releases>
		<snapshots>
			<enabled>true</enabled>
		</snapshots>
	</repository>
</repositories>
<dependencies>
	<dependency>
		<groupId>com.barchart.udt</groupId>
		<artifactId>barchart-udt-bundle</artifactId>
		<version>X.X.X-SNAPSHOT</version>
		<type>jar</type>
		<scope>compile</scope>
	</dependency>
</dependencies>
```
Here is an
[example pom.xml](http://code.google.com/p/barchart-udt/source/browse/trunk/test-deps/pom.xml)
and
[eclipse project](http://code.google.com/p/barchart-udt/source/browse/trunk/test-deps/)
.

You can find out current *RELEASE* and *SNAPSHOT* versions here: 

Current *RELEASE*:

http://repo1.maven.org/maven2/com/barchart/udt/

Current *SNAPSHOT*:
<br>
https://oss.sonatype.org/content/repositories/snapshots/com/barchart/udt/

Please make sure you update *barchart-udt-bundle* artifact version in your pom.xml


### Supported Platforms

Barchart-UDT is currently used on:

* Oracle JDK: 6;
  
|| Platform || x86/i386 || x86-64/amd64 ||
|| Linux    ||     YES  ||     YES      ||
|| Mac OS X ||     YES  ||     YES      ||
|| Windows  ||     YES  ||     YES      ||


### Documentation

Read the 
[presentation](http://barchart-udt.googlecode.com/svn/site/presentation/udt-2009.html)
, study 
[javadoc](http://barchart.github.com/barchart-udt/site/barchart-udt-core/apidocs/index.html)
,
[doxygen](http://barchart.github.com/barchart-udt/site/barchart-udt-core/doxygen/index.html)
or browse the
[source xref](http://barchart.github.com/barchart-udt/site/barchart-udt-core/xref/index.html)
. 

Unit Tests in the source will provide good starting points for your java code.

### Development Environment

[Build System]
(https://github.com/barchart/barchart-udt/wiki/BuildSystem)
 * jdk 1.6.0_37
 * maven 3.0.4
 * jenkins 1.450
 * eclipse 3.7
 * cdt 7.1
 * gcc 4.5.1
 * tdm-gcc 4.5.1
 * vmware 7.1
 * ubuntu 10.10
 * macosx 10.6.5
 * windows 7
 
### Contact Information

Please:
 # enter your [bug reports / feature requests](https://github.com/barchart/barchart-udt/issues) in the "Issues";

Thank you.

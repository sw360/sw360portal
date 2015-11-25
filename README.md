[![Stories in Ready](https://badge.waffle.io/siemens/sw360portal.png?label=ready&title=Ready)](https://waffle.io/siemens/sw360portal)
### sw360portal

A software component catalogue application - designed to work with FOSSology.

SW360 is a liferay portal application to maintain your projects / products and
the software components within. It can send files to the open source
license scanner FOSSology for checking the license conditions and 
maintain license information.

### Introduction

This is a maven-based software project. It is comprised of one backend (services) part
and one frontent (portal) part:

* Backend: Tomcat-based thrift services for being called by different applications.
* Frontend: Liferay-(Tomcat-)based portal application using the Alloy UI framework.
* Database: we store software components and metadata about them in couchdb.

The reference platform is the Ubuntu server 14.04 (which is a LTS version). However, it
runs well on other OSes (see below).

### Project structure

This is a multi module maven file. please consider that we have the following modules:

* frontend: for portlets, themes and layouts, the liferay part.
* backend: for the thrift based services.
* libraries for general stuff that is reused among the above, for example, couchdb access.
* importers for provisioning tasks.
* scripts for deploying either inside the vagrant or on your development machine.

### Required software

* Java 1.8.X
* CouchDB, at least 1.5
* Liferay Portal CE 6.2 GA4
* Apache Tomcat 8.0.X

In order to build you will need:

* A git client
* Apache Maven 3.0.X
* Apache Thrift 0.9.3

http://maven.apache.org/download.html#Installation

Then, you must install Apache Tomcat, CouchDB. And, Java of course.

The software is tested with

* Maven 3.0.4 / 3.0.5
* Apache Tomcat 8.0.26 / 7.0.54 / 7.0.61
* Liferay 6.2 GA4
* CouchDB 1.5 / 1.5.1
* OpenJDK Java 1.8.0_45 (64-bit) 
* Tested with windows 7 SP1, ubuntu 14.04, macosx 10.8, 10.9 10.10
* We run Liferay with PostgreSQL 9.3, but HSQL (as of the bundle) runs also OK.

Please note that there are PROBLEMS with

t.b.d. (no known problems at this time)

### Deployment

There is a vagrant project for one-step-deployment. See the project wiki for details.

Apart from the vagrant way, the software can be deployed using the provided scripts.

### Commands

Actually, there is a hierarchy of maven files, in general

1. mvn clean
	- (boring) to clean everything up

2. mvn install
	- to run all targets including build the .war file at the end

3. to skip the tests
	-Dmaven.test.skip=true
	
You will find more details on the scripts and deployment in the shell
script in the scripts folder. There is the ```dirs.conf``` for the directories
or file paths used by the scripts.

Note that in general

* Backend: Tomcat must run on order to deploy using ```mvn tomcat7:deploy```
* Frontend: Liferay must not run in order to deploy using ```mvn install -Pdeploy```

### Liferay Configuration

You should provide below property configuration based on his/her liferay deployment
environment as found in the master pom.xml file.

Please note that you should run the Liferay installation procedures as found on the
Liferay documentation.

### War file packaging

As backend services are supposedly being deployed in an application Server.
So to avoid conflicts for servlets api (in case of tomcat, tomcat-servlet-api-x.x.x-jar)
are excluded from the WAR file while packaging. Using below configuration,

```
<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-war-plugin</artifactId>
				<version>2.1.1</version>
				<configuration>
					<webResources>
						<resource>
							<directory>${basedir}/src/main/java</directory>
							<targetPath>WEB-INF/classes</targetPath>
							<includes>
								<include>**/*.properties</include>
								<include>**/*.xml</include>
								<include>**/*.css</include>
								<include>**/*.html</include>
							</includes>
						</resource>
					</webResources>
					<packagingExcludes>
        					    WEB-INF/lib/tomcat-servlet-api-7.0.47.jar
         		 	</packagingExcludes>
				</configuration>
</plugin>
```

### Tomcat Deployment

Configuration for maven tomcat plugin:It makes sense to
protect your tomcat manager application with a password.
Normally in $CATALINA_HOME/conf/settings.xml you apply
something like:

```
  ...
    <role rolename="manager-gui"/>
	  <role rolename="manager-script"/>
    <user username="admin" password="whatever" roles="manager-gui,manager-script"/>
  ...
  </tomcat-users>
```

Note that the manager-gui is for logging in via Web browser
while the script is for the maven goal.
Then, the password must be provided with the maven settings.
This should be in the file $M2_HOME/conf/settings.xml:

```
  ...
  </servers>
    ...
    <server>
        <id>localhosttomcatserver</id>
        <username>admin</username>
        <password>whatever</password>
    </server>
  </servers>
  ...
```

 Because in this pom.xml it is written:

```
    ...
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>tomcat-maven-plugin</artifactId>
        <configuration>
            <server>localhosttomcatserver</server>
        </configuration>
    </plugin>
	...
```

### License

This software project is distributed under the terms of the
GNU General Public License v2.0 w/Classpath exception
(SPDX:GPL-2.0-with-classpath-exception) with the following
clarification and special exception:

Linking this library statically or dynamically with other modules
is making a combined work based on this library. Thus, the terms
and conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library
give you permission to link this library with independent modules
to produce an executable, regardless of the license terms of these
independent modules, and to copy and distribute the resulting
executable under terms of your choice, provided that you also
meet, for each linked independent module, the terms and conditions
of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify
this library, you may extend this exception to your version of the
library, but you are not obliged to do so. If you do not wish to
do so, delete this exception statement from your version.

See the link http://www.gnu.org/software/classpath/license.html
(accessed in Jan 2014) for more details.

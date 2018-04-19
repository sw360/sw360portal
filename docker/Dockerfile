FROM ubuntu:16.04
LABEL maintainer="nazar.medykh@gmail.com"

RUN apt-get update
RUN apt-get -y dist-upgrade
RUN apt-get -y install python-software-properties software-properties-common curl wget maven unzip git \
libboost-dev libboost-test-dev libboost-program-options-dev libevent-dev automake libtool flex \
bison pkg-config g++ libssl-dev

ENV DEBIAN_FRONTEND noninteractive
ENV JAVA_HOME       /usr/lib/jvm/java-8-oracle
ENV M2_HOME         /usr/share/maven
ENV MAVEN_HOME      /usr/share/maven

## Install Oracle's JDK
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN apt-get install --no-install-recommends oracle-java8-installer -y
RUN apt-get clean all


# Install Apache Thrift
RUN curl https://dist.apache.org/repos/dist/release/thrift/0.9.3/thrift-0.9.3.tar.gz | tar zx \
   && cd thrift-0.9.3 \
   && ./configure --prefix=/usr \
              --with-java  \
              --without-cpp --without-qt4 --without-c_glib --without-csharp --without-erlang --without-perl --without-php \
              --without-php_extension --without-python --without-ruby --without-haskell --without-go --without-d \
              --without-haskell --without-php --without-ruby --without-python --without-erlang --without-perl \
              --without-c_sharp --without-d --without-php --without-go --without-lua --without-nodejs \
   && make \
   && make install \
   && cd ..

# Install Liferay
RUN wget https://sourceforge.net/projects/lportal/files/Liferay%20Portal/6.2.4%20GA5/liferay-portal-tomcat-6.2-ce-ga5-20151119152357409.zip/download \
          -O liferay-portal-tomcat-6.2-ce-ga5.zip \
   && unzip liferay-portal-tomcat-6.2-ce-ga5.zip \
   && rm liferay-portal-tomcat-6.2-ce-ga5.zip

# Setup environment variables
ENV TOMCAT_HOME /liferay-portal-6.2-ce-ga5/tomcat-7.0.62
ENV CATALINA_HOME /liferay-portal-6.2-ce-ga5/tomcat-7.0.62
ENV LIFERAY_PATH /liferay-portal-6.2-ce-ga5
ENV CATALINA_OPTS -Xms512m -Xmx12g

# Build SW306
RUN git clone https://github.com/sw360/sw360portal.git \
  && cd sw360portal \
  && git checkout tags/sw360-3.1.0
COPY portal-ext.properties $LIFERAY_PATH/tomcat-7.0.62/webapps/ROOT/WEB-INF/classes/
COPY couchdb.properties /sw360portal/build-configuration/resources/
RUN cd sw360portal \
  && mvn clean install -DskipTests -Pdeploy -Ddeploy.dir=$LIFERAY_PATH/deploy -Dwebapps.dir=$TOMCAT_HOME/webapps \
  && cd .

EXPOSE 8080
COPY docker-entrypoint.sh .
ENTRYPOINT ["/bin/bash", "/docker-entrypoint.sh"]

#curl -s -X PUT http://couchdb:5984/_node/nonode@nohost/_config/admins/admin -d '"admin"'
#curl -H "Content-Type: application/json" -X POST -d '{"action":"enable_single_node","password":"admin","port":"5984","singlenode":"true","username":"admin"}' http://couchdb:5984/_cluster_setup

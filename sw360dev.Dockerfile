# Copyright (c) Bosch Software Innovations GmbH 2016.
# Part of the SW360 Portal Project.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

FROM debian:jessie
MAINTAINER Maximilian Huber <maximilian.huber@tngtech.com>
ENV DEBIAN_FRONTEND noninteractive

ENV THRIFT_PKG='http://dl.bintray.com/apache/thrift/debian/pool/main/t/thrift/thrift-compiler_0.9.3_amd64.deb'
ENV GOSU_VERSION='1.7'

ENV _update="apt-get update"
ENV _install="apt-get install -y --no-install-recommends"
ENV _cleanup="eval apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*"
ENV _purge="apt-get purge -y --auto-remove"

RUN set -x \
 && $_update && $_install ca-certificates wget && $_cleanup \
 && wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture)" \
 && chmod +x /usr/local/bin/gosu \
 && wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture).asc" \
 && export GNUPGHOME="$(mktemp -d)" \
 && gpg --keyserver ha.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
 && gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu \
 && rm -r "$GNUPGHOME" /usr/local/bin/gosu.asc && $_purge ca-certificates

RUN set -x \
 && echo "deb http://ftp.debian.org/debian jessie-backports main" > /etc/apt/sources.list.d/backports.list \
 && $_update && $_install openjdk-8-jdk wget maven && $_cleanup \
 && update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java \
 && echo "JAVA_HOME=\"/usr/lib/jvm/java-8-openjdk-amd64/jre\"" >> /etc/environment

RUN set -x \
 && wget -q $THRIFT_PKG -O thrift-compiler.deb && dpkg -i thrift-compiler.deb && rm thrift-compiler.deb \
 && $_purge wget \
 && ln -s /usr/bin/thrift /usr/local/bin/thrift

RUN set -x \
 && $_update && $_install ruby-dev rubygems build-essential rpm && $_cleanup \
 && echo "gem: --no-ri --no-rdoc" > ~/.gemrc \
 && gem install rake fpm

CMD /bin/bash

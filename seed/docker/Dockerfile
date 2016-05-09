# Docker container to use to build Ontrack
# Consider creating an `ontrack-build` image and publish it with a version (1, 2, etc.)
# This would avoid to rebuild it each time we switch from one slave to the other

# Base
# Using a Java based image in order to get the JDK we want, without having Jenkins to provision it
# each time on the slave
FROM java:openjdk-8u66-jdk

# Meta-information
MAINTAINER Damien Coraboeuf <damien.coraboeuf@gmail.com>

# Tools installation
RUN apt-get update \
    && apt-get install -y \
        subversion \
        git \
        xvfb \
        iceweasel

# Installing Docker (for building the Ontrack image)
RUN wget https://get.docker.io/builds/Linux/x86_64/docker-1.11.1.tgz -O docker.tgz
RUN tar -xvzf docker.tgz
RUN chmod +x docker/*
RUN mv docker/* /usr/bin/

# Installaing Docker compose
RUN curl -L https://github.com/docker/compose/releases/download/1.7.0/run.sh > /usr/bin/docker-compose
RUN chmod +x /usr/bin/docker-compose

# Gradle cache
VOLUME /root/.gradle

# Node JS cache
VOLUME /root/.cache

# Git configuration
RUN git config --global user.email "jenkins@nemerosa.net"
RUN git config --global user.name "Jenkins"

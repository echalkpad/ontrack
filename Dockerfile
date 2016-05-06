# Docker container to use to build Ontrack
# Consider creating an `ontrack-build` image and publish it with a version (1, 2, etc.)
# This would avoid to rebuild it each time we switch from one slave to the other

# Base
# Using a Java based image in order to get the JDK we want, without having Jenkins to provision it
# each time on the slave
FROM java:openjdk-8u66-jdk

# Meta-information
MAINTAINER Damien Coraboeuf <damien.coraboeuf@gmail.com>

# SVN & Git installation
RUN apt-get update \
    && apt-get install -y subversion git

# Gradle cache
VOLUME /root/.gradle

# Node JS cache
VOLUME /root/.cache

# Git configuration
RUN git config --global user.email "jenkins@nemerosa.net"
RUN git config --global user.name "Jenkins"

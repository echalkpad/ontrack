# Docker container to use to build Ontrack
# Consider creating an `ontrack-build` image and publish it with a version (1, 2, etc.)
# This would avoid to rebuild it each time we switch from one slave to the other

# Base
FROM ubuntu:14.04

# Meta-information
MAINTAINER Damien Coraboeuf <damien.coraboeuf@gmail.com>

# SVN & Git installation
RUN apt-get update \
    && apt-get install -y subversion git

# Git configuration
RUN git config --global user.email "jenkins@nemerosa.net"
RUN git config --global user.name "Jenkins"

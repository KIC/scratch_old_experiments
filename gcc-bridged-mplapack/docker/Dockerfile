FROM ubuntu:trusty
MAINTAINER KIC

ENV HOME /root
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get install -y software-properties-common
RUN apt-add-repository ppa:webupd8team/java
RUN apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer 
RUN apt-get install -y git maven gcc-4.6 gcc-4.6-plugin-dev gfortran-4.6 gcc-4.6.multilib g++-4.6 make vim
RUN git config --global user.email "you@example.com"

WORKDIR /root/
#RUN git clone https://github.com/bedatadriven/renjin.git
RUN git clone https://github.com/KIC/renjin.git
WORKDIR /root/renjin
RUN mvn clean install -DskipTests -Dmaven.test.skip=true
# RUN mvn clean install -Ptools
# RUN mvn clean install 

WORKDIR /root/
RUN git clone https://github.com/KIC/gcc-bridged-mplapack.git
WORKDIR /root/gcc-bridged-mplapack
#RUN bash init.sh 3.1.0


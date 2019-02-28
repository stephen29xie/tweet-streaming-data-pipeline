#
# Scala and sbt Dockerfile
#
# https://github.com/hseeberger/scala-sbt
#

# Pull base image
FROM openjdk:11.0.1

# Env variables
ENV SCALA_VERSION 2.12.8
ENV SBT_VERSION 1.2.8

#ARG MONGODB_CONNECTION_STRING
#ARG MONGODB_DATABASE
#ARG MONGODB_COLLECTION
#ARG CONSUMER_KEY
#ARG CONSUMER_KEY_SECRET
#ARG ACCESS_TOKEN
#ARG ACCESS_TOKEN_SECRET
#ARG KEYWORDS
#ARG LANGUAGES
#ARG KAFKA_TOPIC
#ARG KAFKA_BOOTSTRAP_SERVERS
#ARG SPARK_MASTER_URL

#ENV MONGODB_CONNECTION_STRING: "$MONGODB_CONNECTION_STRING"
#ENV MONGODB_DATABASE: "$MONGODB_DATABASE"
#ENV MONGODB_COLLECTION: "$MONGODB_COLLECTION"
#ENV CONSUMER_KEY: "$CONSUMER_KEY"
#ENV CONSUMER_KEY_SECRET: "$CONSUMER_KEY_SECRET"
#ENV ACCESS_TOKEN: "$ACCESS_TOKEN"
#ENV ACCESS_TOKEN_SECRET: "$ACCESS_TOKEN_SECRET" 
#ENV KEYWORDS: "$KEYWORDS"
#ENV LANGUAGES: "$LANGUAGES"
#ENV KAFKA_TOPIC: "$KAFKA_TOPIC"
#ENV KAFKA_BOOTSTRAP_SERVERS: "$KAFKA_BOOTSTRAP_SERVERS"
#ENV SPARK_MASTER_URL: "$SPARK_MASTER_URL"

# Install Scala
## Piping curl directly in tar
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion && \
  mkdir project && \
  echo "scalaVersion := \"${SCALA_VERSION}\"" > build.sbt && \
  echo "sbt.version=${SBT_VERSION}" > project/build.properties && \
  echo "case object Temp" > Temp.scala && \
  sbt compile && \
  rm -r project && rm build.sbt && rm Temp.scala && rm -r target

# Add current directory files to container directory
ADD . /root

# Define working directory
WORKDIR /root

# RUN is an image build step, the state of the container after a RUN command will be committed to the 
# docker image. A Dockerfile can have many RUN steps that layer on top of one another to build the 
# image.

# CMD is the command the container executes by default when you launch the built image.
CMD sbt run
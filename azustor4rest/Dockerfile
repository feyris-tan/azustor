FROM dyne/devuan:beowulf

RUN apt-get update
RUN apt-get install -y openjdk-11-jre-headless curl
RUN mkdir /data

COPY target/quarkus-app/ /deployments/

EXPOSE 8080

VOLUME /data

HEALTHCHECK --interval=1m --timeout=3s CMD curl -f http://127.0.0.1:8080/ || exit 1

WORKDIR /deployments
ENTRYPOINT [ "java","-Dazustor.directory=/data","-jar","/deployments/quarkus-run.jar" ]

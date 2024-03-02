# Port to expose for web console
# EXPOSE 9081
# Port to expose for tcp connections
# EXPOSE 9082

FROM openjdk:17
WORKDIR /app
COPY . ${WORKDIR}
EXPOSE 9081
EXPOSE 9082
CMD  java -cp h2-2.1.210.jar org.h2.tools.Server -web -webDaemon -webAllowOthers -webPort 9081 -tcp -tcpAllowOthers -tcpPort 9082 -ifNotExists

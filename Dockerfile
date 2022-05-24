# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.195.0/containers/java/.devcontainer/base.Dockerfile
# [Choice] Java version (use -bullseye variants on local arm64/Apple Silicon): 8, 11, 16, 8-bullseye, 11-bullseye, 16-bullseye, 8-buster, 11-buster, 16-buster
#ARG VARIANT=11-buster
ARG VARIANT=16-bullseye
FROM mcr.microsoft.com/vscode/devcontainers/java:0-${VARIANT}

USER root
WORKDIR /root

# Install Maven
ARG MAVEN_VERSION="3.6.3"
RUN su -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""
RUN mkdir -p .m2 

# Copy Simudyne license to its expected location
RUN mkdir .simudyne
COPY .simudyne/.license .simudyne/

# Use Simudyne Access Token to create the global Maven settings file
COPY .simudyne/.token .simudyne/
RUN ( \
    echo '<?xml version="1.0" encoding="UTF-8"?>' ; \
    echo '<settings><servers><server>' ; \
    echo '<id>simudyne.jfrog.io</id>'; \
    cat .simudyne/.token ; \
    echo '</server></servers>' ; \
    echo '</settings>'\
    ) >> .m2/settings.xml

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

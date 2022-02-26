# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.195.0/containers/java/.devcontainer/base.Dockerfile
# [Choice] Java version (use -bullseye variants on local arm64/Apple Silicon): 8, 11, 16, 8-bullseye, 11-bullseye, 16-bullseye, 8-buster, 11-buster, 16-buster
ARG VARIANT=11-buster
FROM mcr.microsoft.com/vscode/devcontainers/java:0-${VARIANT}

ARG MAVEN_VERSION="3.6.3"
RUN su root -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""

RUN mkdir ~/.simudyne
COPY .simudyne/.license ~/.simudyne/.license
COPY .simudyne/.settings.xml ~/.m2/.settings.xml

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

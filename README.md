# Simudyne Challenge Devcontainer

This repository is structured to help you get going with Simudyne quickly in a containerised environment.

## Requirements
- Visual Studio Code
    - With Remote-Development extension
- Docker

## Quickstart
- Ensure Docker is running `docker ps`
- Clone repository
- Open in VSCode
- Insert your Simudyne credentials in [./simudyne/.settings.xml]
- Insert your Simudyne license file in [./simudyne/.license]
- From VSCode - `CTRL+SHIFT+P` "Reopen in container"
- `mvn -s settings.xml clean compile exec:java`

## Details
This repository is design to be opene via VSCode as a [Devcontainer](https://code.visualstudio.com/docs/remote/containers).  This allows you to test Simudyne in a dedicated container so no changes need to be made to your host system.
for info on Access

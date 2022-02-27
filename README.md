# Simudyne Hunger Games Quickstart

This repository is structured to help you get going with Simudyne quickly in a containerised environment.

## Requirements
- Visual Studio Code
    - With Remote-Development extension
- Docker

## Quickstart
- Ensure Docker is running `docker ps`
- Clone repo locally
- Open repo in VSCode
- Place your Simudyne access token in [./simudyne/.token]
- Place your Simudyne license file in [./simudyne/.license]
- From VSCode - `CTRL+SHIFT+P` "Reopen in container"
- F5 to run the main java class
- open http://localhost:8080/


## Details
This repository is design to be opene via VSCode as a [Devcontainer](https://code.visualstudio.com/docs/remote/containers).  This allows you to test Simudyne in a dedicated container so no changes need to be made to your host system.

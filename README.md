# Simudyne Hunger Games Quickstart

This repository is structured to help you get going with the Simudyne Hunger Games challenge quickly in a fully contained development environment.


## Requirements
- [Visual Studio Code](https://code.visualstudio.com/)
- [Remote Development Extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.vscode-remote-extensionpack)
- [Docker](https://www.docker.com/)


## Quickstart
- Ensure Docker is running `docker info`
- Clone repo locally
- Place your Simudyne access token at [.simudyne/.token](.simudyne/.token)
- Place your Simudyne license file at [.simudyne/.license](.simudyne/.license)
- Open repo in VSCode
- `CTRL+SHIFT+P` "Remote-Container: Reopen folder in container"
    - This will take a few minutes on first open
- Hit `F5` to run the main java class
- Simudyne should now be running at http://localhost:8080/


## Details
TODO
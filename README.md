# Simudyne Hunger Games Quickstart

This repository is structured to help you get going with the Simudyne Hunger Games challenge quickly in a fully contained development environment.

The included model is unchanged from Ben Schumanns apart from public field definition that was preventing Simudyne from starting.


## Resources
- [Ben Schumann's blog post](https://www.benjamin-schumann.com/blog/2022/2/7/the-simudyne-hunger-games-improve-my-manufacturing-model-and-become-a-legend)
- [Ben Schumann's LinkedIn Post](https://www.linkedin.com/pulse/simudyne-hunger-games-improve-my-manufacturing-model-schumann-phd/?trackingId=t4TSC%2BwQTvu3C6XXZw0BEg%3D%3D)
- [Simudyne Competition Page](https://docs.simudyne.com/challenges/ben_schumann_challenge/)
- [Simudyne Website](https://simudyne.com/)
- [Simudyne SDK Documentation](https://docs.simudyne.com/)


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
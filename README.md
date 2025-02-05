# Onebot-MC-Connector

A bridge between Minecraft and OneBot v11 QQ Bot that enables communication between Minecraft servers and QQ groups.

## Features

- Bidirectional message relay between Minecraft and QQ
- Player join/leave notifications
- Server status updates
- Death messages forwarding
- Easy configuration via YAML
- Automatic reconnection handling
- Support for multiple QQ groups

## Server Support

- Currently supports Fabric servers
- Support for other server types (Spigot/Paper) is planned

## Setup

1. Install the mod in your Fabric server mods folder
2. Configure the `config.yml` file with your QQ bot details
3. Start/restart your server

## Configuration

Edit the `config.yml` file to set:
- Bot connection details (websocket address and port)
- Target QQ group numbers
- Message formatting options
- Event notification settings

## Requirements

- Minecraft Fabric server (1.12.2 or higher)
- A running OneBot v11 compatible QQ bot

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).

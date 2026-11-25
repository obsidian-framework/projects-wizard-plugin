<p align="center"><a href="https://obsidian-java.com/" target="_blank"><img src="https://obsidian-java.com/assets/img/logo.png" width="300" alt="Obsidian Logo"></a></p>

<p align="center">
    <a href="https://github.com">
        <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT">
    </a>
    <a href="https://www.oracle.com/java/">
        <img src="https://img.shields.io/badge/Java-17+-orange.svg" alt="Java 17+">
    </a>
</p>

<p align="center">
<strong>Obsidian Project Wizard</strong> is the official IntelliJ IDEA plugin for the Obsidian Java web framework. Bootstrap new Obsidian projects directly from the <em>New Project</em> dialog with always up-to-date starter kits fetched straight from GitHub — no manual cloning or boilerplate setup required.
</p>

## Features

- **Native New Project Integration** — Adds an **Obsidian** generator directly inside IntelliJ IDEA's **File | New | Project...** wizard.
- **Always Up-to-Date Templates** — Dynamically fetches and extracts the latest starter repositories directly from the `obsidian-framework` GitHub organization.
- **Zero-Ceremony Setup** — Automatically strips repository root folders and triggers Maven/IDE project indexing upon creation.

## Available Boilerplates

| Template | Repository | Description |
| :--- | :--- | :--- |
| **Default App (Base Obsidian)** | [`obsidian`](https://github.com/obsidian-framework/obsidian) | Clean Obsidian skeleton with annotation routing, migrations, and Pebble templates. |
| **Flint (Starter Kit avec Auth)** | [`flint`](https://github.com) | Official starter kit with authentication and user management out of the box. |
| **LiveComponents Examples** | [`livecomponents-examples`](https://github.com) | Reactive server-side UI components powered by SSE with zero JavaScript. |
| **Realtime Examples** | [`realtime-examples`](https://github.com) | Live WebSocket and Server-Sent Events (SSE) feeds and notifications. |

## Installation

### From JetBrains Marketplace *(Coming Soon)*
1. Open **Settings / Preferences** (`Ctrl+Alt+S` or `Cmd+,`) → **Plugins** → **Marketplace**.
2. Search for **Obsidian Project Wizard** and click **Install**.

### From Disk (Manual Build)
1. Download the latest release `.zip` from the [Releases](https://github.com) page (or build it locally with `./gradlew buildPlugin`).
2. In IntelliJ IDEA, go to **Settings / Preferences** → **Plugins** → **⚙️ (Gear icon)** → **Install Plugin from Disk...**.
3. Select the `.zip` archive and restart the IDE.

## Usage

1. Click **File | New | Project...** in IntelliJ IDEA.
2. Select **Obsidian** in the **Generators** list on the left.
3. Choose your project name, location, and select a **Boilerplate** from the dropdown menu.
4. Click **Create** — IntelliJ downloads the template and loads your Maven project automatically.

## Building from Source

```bash
git clone https://github.com/obsidian-framework/projects-wizard-plugin
cd projects-wizard-plugin
./gradlew buildPlugin
```

The installable plugin archive will be generated in `build/distributions/`.

## License

MIT © [Obsidian Framework](https://github.com/obsidian-framework)

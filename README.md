<p align="center"><a href="https://obsidian-java.com/" target="_blank"><img src="https://obsidian-java.com/assets/img/logo.png" width="300" alt="Obsidian Logo"></a></p>

<p align="center">
    <a href="https://opensource.org/licenses/MIT">
        <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT">
    </a>
    <a href="https://www.oracle.com/java/">
        <img src="https://img.shields.io/badge/Java-17+-orange.svg" alt="Java 17+">
    </a>
</p>

<p align="center">
<strong>Obsidian Project Wizard</strong> is the official IntelliJ IDEA plugin for the Obsidian Java web framework. Bootstrap new Obsidian projects directly from the <em>New Project</em> dialog with starter kits fetched straight from GitHub — no manual cloning or boilerplate setup required.
</p>

## Features

- **Native New Project Integration** — Adds an **Obsidian** generator directly inside IntelliJ IDEA's **File | New | Project...** wizard.
- **Latest Templates** — Downloads and extracts the current default branch of the starter repositories from the `obsidian-framework` GitHub organization.
- **Package Customization** — Renames the template's root package, and updates the `groupId`, `artifactId` and `mainClass` in the `pom.xml` to match your project.
- **Zero-Ceremony Setup** — Strips the repository root folder and refreshes the project files once the template is extracted.

## Available Templates

| Template | Repository | Description |
| :--- | :--- | :--- |
| **Default App (Base Obsidian)** | [`obsidian`](https://github.com/obsidian-framework/obsidian) | Clean Obsidian skeleton with annotation routing, migrations, and Pebble templates. |
| **Flint (Starter Kit with Auth)** | [`flint`](https://github.com/obsidian-framework/flint) | Official starter kit with authentication and user management out of the box. |
| **LiveComponents Examples** | [`livecomponents-examples`](https://github.com/obsidian-framework/livecomponents-examples) | Reactive server-side UI components powered by SSE with zero JavaScript. |
| **Realtime Examples (SSE / Live feeds)** | [`realtime-examples`](https://github.com/obsidian-framework/realtime-examples) | Live WebSocket and Server-Sent Events (SSE) feeds and notifications. |

## Requirements

- IntelliJ IDEA 2024.1 or newer, with the bundled Java plugin.
- Internet access (templates are downloaded from GitHub when the project is created).

## Installation

### From JetBrains Marketplace *(Coming Soon)*
1. Open **Settings / Preferences** (`Ctrl+Alt+S` or `Cmd+,`) → **Plugins** → **Marketplace**.
2. Search for **Obsidian Project Wizard** and click **Install**.

### From Disk (Manual Build)
1. Download the latest release `.zip` from the [Releases](https://github.com/obsidian-framework/projects-wizard-plugin/releases) page (or build it locally with `./gradlew buildPlugin`).
2. In IntelliJ IDEA, go to **Settings / Preferences** → **Plugins** → **⚙️ (Gear icon)** → **Install Plugin from Disk...**.
3. Select the `.zip` archive and restart the IDE.

## Usage

1. Click **File | New | Project...** in IntelliJ IDEA.
2. Select **Obsidian** in the **Generators** list on the left.
3. Choose your project name and location, then pick a **Template** from the dropdown.
4. Enter the **Package** for your project (for example `com.example.app`). If the field is empty or not a valid Java package name, the template's original package is kept.
5. Click **Create** — IntelliJ downloads the template and sets up your project. If IntelliJ offers to load the Maven project, accept.

## Building from Source

Requires JDK 21 or newer.

```bash
git clone https://github.com/obsidian-framework/projects-wizard-plugin
cd projects-wizard-plugin
./gradlew buildPlugin
```

On Windows, use `gradlew.bat buildPlugin`. To try the plugin in a sandbox IDE, run `./gradlew runIde`.

The installable plugin archive will be generated in `build/distributions/`.

## License

MIT © [Obsidian Framework](https://github.com/obsidian-framework)
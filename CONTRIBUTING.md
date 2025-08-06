# CONTRIBUTING

## Git conventions

### 💬 Branch naming

All branches must follow one of the following naming patterns:

| Type    | Pattern                       | Example                     |
|---------|-------------------------------|-----------------------------|
| Main    | `main` or `dev`               | `main`                      |
| Feature | `feature/<short-description>` | `feature/kafka-integration` |
| Hotfix  | `hotfix/<short-description>`  | `hotfix/fix-offset-issue`   |

Allowed characters: lowercase letters, numbers, dashes (`-`), underscores (`_`), and dots (`.`).  
Branch names must be descriptive and concise.

### 💬 Commit message format

We follow the [Conventional Commits](https://www.conventionalcommits.org) specification:

```
<type>(<scope>): <short summary>
```

**Types**:

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `perf`: A code change that improves performance
- `test`: Adding or correcting tests
- `security`: A change that addresses a security vulnerability or improves security
- `deprecated`: Marking features, APIs, or components as obsolete (scheduled for removal)
- `chore`: Maintenance, build, dependencies, etc

**Examples**:

```
feat(core): add new entity
fix(api): correct null pointer in message handler
docs(contributing): add commit format rules
```

## 📚 Functional Documentation and Diagrams

### 📁 `docs` Directory

All functional documentation (business specifications, use cases, etc.) is stored in the following directory:

```bash
/docs
```

Please keep this folder organized and up to date.

### 🖊️ Diagrams with Mermaid

We use [Mermaid](https://mermaid.js.org/) to create diagrams (such as flows, sequences, and architecture visuals).

Diagram source files should be written in Markdown or `.mmd` format and stored inside the `docs` directory.

### ⚙️ Install Mermaid CLI

To generate diagram images from Mermaid files, install the CLI tool:

```bash
npm install -g @mermaid-js/mermaid-cli
```

### 🖼️ Generate PNG Diagrams

Example command to generate a PNG from a Mermaid file:

```bash
mmdc -i docs/X.mmd -o docs/X.png
```

💡 _Any modification to a Mermaid diagram **must include a manual regeneration** of its corresponding PNG image._

Make sure the updated image file is included in the same commit as the diagram source changes.

## 🧼 Code Style and Checkstyle

To maintain a clean and consistent codebase, we use **Checkstyle** with a strict ruleset based on
the [Sun Java Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html),
complemented with additional best practices.

### 🔍 Automatic Checks

Checkstyle runs automatically during the Maven `verify` phase:

```bash
mvn verify
```

If your code violates any style rule, the build will fail, and the violations will be printed in the console.

### 📜 Ruleset

The Checkstyle configuration is defined in checkstyle.xml at the root of the project. It enforces:

* Mandatory Javadoc on types, methods, and fields
* Naming conventions for classes, methods, variables, and constants
* Whitespace, indentation, and brace positioning rules
* Bans on wildcard imports and unused imports
* Limits on method length, line length, parameter count
* Detection of common Java pitfalls (e.g., magic numbers, empty blocks)
* Required package-info.java for each package

You can inspect or customize the rules in that file.

### 🚧 Failing Builds

To ensure code quality, any style violation will fail the build. Before pushing code, always run:

```bash
mvn checkstyle:check
```

### 💡 IDE Integration

Most IDEs like IntelliJ IDEA and Eclipse support Checkstyle plugins:

* **IntelliJ IDEA**: Install the Checkstyle plugin and point it to the project’s `checkstyle.xml`
* **Eclipse**: Install Checkstyle from the marketplace, then import the config

This will highlight violations in real-time as you code.

## 🧪 Run Tests

This project uses **JUnit 5** and **Cucumber** for testing. There are two types of tests:

* **Unit tests**
* **End-to-end (E2E) tests**

### Run unit tests only

To run only unit tests (fast and isolated):

```bash
mvn test
```

## 🛠️ How to release

Development releases are automatically managed
using [Semantic Release](https://github.com/Zorin95670/semantic-version).
When a merge is performed into the `main` branch:

* Semantic Release calculates the next version based on commit messages.
* The pom.xml is updated with this version.
* The changelog is generated.
* The version bump and changelog are committed.
* A new Git tag is created.

⚠️ No manual intervention is required during this process.

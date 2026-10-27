# LearnWithAI for IntelliJ

An IntelliJ IDEA plugin that shows **learnwithai.unc.edu** in a sidebar, right
next to your code. It is deliberately not a browser: no address bar, just the
site, plus Home / Reload / Open in Browser buttons in the sidebar's title bar.

## Students: install it

**Go to https://christhisway.github.io/learnwithai-intellij/** and follow the
four steps there. It takes about a minute, and updates install themselves
afterwards.

Short version: in IntelliJ, open **Settings → Plugins → ⚙ → Manage Plugin
Repositories… → +**, paste

```
https://christhisway.github.io/learnwithai-intellij/updatePlugins.xml
```

then search **LearnWithAI** on the Marketplace tab and click **Install**.

Requires IntelliJ IDEA 2024.2 or newer (Community or Ultimate).

## Maintainers

- **Ship an update:** bump `version` in `build.gradle.kts` and push to `main`.
  The [Publish plugin](.github/workflows/publish.yml) workflow builds the
  plugin, redeploys the GitHub Pages site (landing page, `updatePlugins.xml`,
  zip), and creates a `v<version>` release. Students' IDEs pick up the new
  version on their next update check. Pushing without a version bump just
  redeploys the same version.
- **Build locally:** `./gradlew buildPlugin` → `build/distributions/`.
- **Try it in a sandbox IDE:** `./gradlew runIde`.
- **Change the site it opens:** `HOME_URL` in
  `src/main/kotlin/edu/unc/learnwithai/LearnWithAIToolWindowFactory.kt`.
- **Change the student landing page:** `site/index.html` (placeholders like
  `__VERSION__` are filled in by `scripts/build-site.sh`).

### How it works

The site sends `X-Frame-Options: DENY`, so it can't live in an iframe. The
plugin hosts it as a top-level page in IntelliJ's built-in Chromium (JCEF),
which also keeps the sign-in cookie between IDE restarts. Popups (`target=_blank`,
`window.open`) to `*.unc.edu` load in the sidebar; anything else opens in the
system browser. IDEs without JCEF (some remote-development setups) get a
button that opens the site in the browser instead.

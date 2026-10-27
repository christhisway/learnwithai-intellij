#!/bin/sh
# Assembles the GitHub Pages site students install from:
#   index.html          student landing page
#   updatePlugins.xml   IntelliJ custom plugin repository descriptor
#   <plugin zip>        the plugin itself
#
# Usage: scripts/build-site.sh <pages-base-url> <source-url>
# Expects the plugin to be built already (./gradlew buildPlugin).
set -eu

base_url=${1%/}
source_url=$2
cd "$(dirname "$0")/.."

version=$(sed -n 's/^version = "\(.*\)"$/\1/p' build.gradle.kts)
zip="learnwithai-plugin-$version.zip"
out=build/site

test -n "$version" || { echo "could not read version from build.gradle.kts" >&2; exit 1; }
test -f "build/distributions/$zip" || { echo "missing build/distributions/$zip; run ./gradlew buildPlugin" >&2; exit 1; }

rm -rf "$out"
mkdir -p "$out"
cp "build/distributions/$zip" "$out/"

sed -e "s|__REPO_URL__|$base_url/updatePlugins.xml|g" \
    -e "s|__ZIP__|$zip|g" \
    -e "s|__VERSION__|$version|g" \
    -e "s|__SOURCE_URL__|$source_url|g" \
    site/index.html > "$out/index.html"

cat > "$out/updatePlugins.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<plugins>
    <plugin id="edu.unc.learnwithai" url="$base_url/$zip" version="$version">
        <idea-version since-build="242"/>
        <name>LearnWithAI</name>
        <vendor>ThreadMind Course Staff</vendor>
        <description>Opens learnwithai.unc.edu in an IntelliJ sidebar.</description>
    </plugin>
</plugins>
EOF

echo "site for v$version written to $out"

#!/usr/bin/env python3
"""Discover the Minecraft versions supported by mcontain.

Only targets modern, non-obfuscated Minecraft lines (the "YY.N" year-based
lines). Picks EVERY release of each line and reports the Java release
needed to build it. Because these versions are non-obfuscated but have
per-version bytecode differences, we ship one jar per EXACT release version.

Used to auto-build for new Minecraft versions as Mojang publishes them.
"""
import json
import re
import urllib.request

MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

# Auto-adopt any year-based line: two-digit year, single-digit minor (e.g. 26.1, 27.1, 28.1)
# Pattern: ^YY.N.N or ^YY.N where YY=two-digit year, N=minor line number
YEAR_LINE_PATTERN = re.compile(r'^(\d{2})\.(\d+)(?:\.(\d+))?$')


def fetch(url):
    req = urllib.request.Request(url, headers={"User-Agent": "mcontain-ci"})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.load(resp)


def java_for(version):
    return 25


def sort_key(version):
    return [int(x) for x in version.split(".")]


def main():
    manifest = fetch(MANIFEST)
    releases = [v["id"] for v in manifest["versions"] if v["type"] == "release"]

    # Match any year-based line (YY.N or YY.N.N)
    supported = []
    for v in releases:
        m = YEAR_LINE_PATTERN.match(v)
        if m:
            supported.append(v)

    # One jar per exact release version
    versions = []
    for mc in sorted(supported, key=sort_key):
        versions.append({
            "mc": mc,
            "line": mc,  # exact version
            "range": mc,  # exact version for fabric.mod.json depends
            "jar": "mcontain-v4-" + mc + ".jar",
            "java_release": java_for(mc),
        })

    print(json.dumps({"versions": versions}, indent=2))


if __name__ == "__main__":
    main()

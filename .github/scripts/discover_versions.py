#!/usr/bin/env python3
"""Discover the Minecraft versions supported by mcontain.

Only targets modern, non-obfuscated Minecraft lines (the "YY.N" year-based
lines). Picks the final release of each line and reports the Java release
needed to build it. Because these versions are non-obfuscated, a single jar
built against the final release of a line works across every sub-version of
that line without remapping, so we ship one jar per minor line.

Used to auto-build for new Minecraft versions as Mojang publishes them.
"""
import json
import re
import urllib.request

MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

# Auto-adopt any year-based line: two-digit year, single-digit minor (e.g. 26.1, 27.1, 28.1)
# Pattern: ^YY.N.N or ^YY.N where YY=two-digit year, N=minor line number
YEAR_LINE_PATTERN = re.compile(r'^(\d{2})\.(\d+)(?:\.\d+)?$')


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

    line_of = {}
    for v in supported:
        parts = v.split(".")
        line = parts[0] + "." + parts[1]
        if line not in line_of or sort_key(v) > sort_key(line_of[line]):
            line_of[line] = v

    versions = []
    for line in sorted(line_of, key=sort_key):
        mc = line_of[line]
        versions.append({
            "mc": mc,
            "line": line,
            "range": line + ".x",
            "jar": "mcontain-v4-" + line + ".x.jar",
            "java_release": java_for(mc),
        })

    print(json.dumps({"versions": versions}, indent=2))


if __name__ == "__main__":
    main()

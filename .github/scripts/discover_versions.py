#!/usr/bin/env python3
"""Discover the Minecraft versions supported by mcontain.

Only targets modern, non-obfuscated Minecraft lines (the "26." year-based
lines). Picks the final release of each line and reports the Java release
needed to build it. Because these versions are non-obfuscated, a single jar
built against the final release of a line works across every sub-version of
that line without remapping, so we ship one jar per minor line.

Used to auto-build for new Minecraft versions as Mojang publishes them.
"""
import json
import urllib.request

MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

SUPPORTED_PREFIX = ("26.",)


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
    supported = [v for v in releases if v.startswith(SUPPORTED_PREFIX)]

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

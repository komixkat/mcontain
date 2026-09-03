#!/usr/bin/env python3
"""Regenerate the 'Supported versions' table in README.md.

Reads the versions JSON produced by discover_versions.py (a list of
{mc, line, range, jar, java_release}) and rewrites the section delimited by
<!-- MC-TABLE:START --> / <!-- MC-TABLE:END --> in README.md, linking each
jar to its asset on the latest GitHub release.
"""
import json
import sys

REPO = "komixkat/mcontain"
START = "<!-- MC-TABLE:START -->"
END = "<!-- MC-TABLE:END -->"


def block(versions):
    lines = [START, "", "| Version | Java | Download |", "| --- | --- | --- |"]
    for v in versions:
        url = f"https://github.com/{REPO}/releases/latest/download/{v['jar']}"
        display = v['jar'].replace('.jar', '')
        lines.append(
            f"| `{v['mc']}` | {v['java_release']} | [{display}]({url}) |"
        )
    lines.append("")
    lines.append(END)
    return "\n".join(lines)


def main():
    with open(sys.argv[1]) as fh:
        data = json.load(fh)
    versions = data["versions"]

    with open("README.md") as fh:
        readme = fh.read()

    if START not in readme or END not in readme:
        raise SystemExit("README.md missing MC-TABLE markers")

    head, _ = readme.split(START)
    _, tail = readme.split(END)
    new = head + block(versions) + tail

    with open("README.md", "w") as fh:
        fh.write(new)

    print(f"updated README with {len(versions)} versions")


if __name__ == "__main__":
    main()

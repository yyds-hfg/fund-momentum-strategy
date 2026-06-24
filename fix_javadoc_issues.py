#!/usr/bin/env python3
"""Fix duplicate class JavaDocs and move method JavaDocs before annotations."""

import re
import sys
from pathlib import Path

BASE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("/Users/hanfugui/code/fund-momentum-strategy/src/main/java/com/hacker/code")


def remove_duplicate_class_javadocs(src):
    """When two consecutive /** ... */ blocks appear before a public type declaration,
    keep only the first one."""
    pattern = re.compile(
        r"(?P<first>/\*\*.*?\*/\n)"
        r"(?P<second>/\*\*.*?\*/\n)"
        r"(?P<ann>(?:^[ \t]*@[^\n]+\n)*)"
        r"(?P<decl>^[ \t]*public\s+(?:class|interface|enum|record)\b)",
        re.MULTILINE | re.DOTALL,
    )
    return pattern.sub(lambda m: m.group("first") + m.group("ann") + m.group("decl"), src)


def move_method_javadoc_before_annotations(src):
    """Move method JavaDoc that appears after annotations to before annotations."""
    pattern = re.compile(
        r"(?P<ann>(?:^[ \t]*@[^\n]+\n)+)"
        r"(?P<javadoc>^[ \t]*/\*\*.*?\*/\n)"
        r"(?P<decl>^[ \t]*public\s+(?!class|interface|enum|record)\w[^;{]*[;{])",
        re.MULTILINE | re.DOTALL,
    )
    return pattern.sub(lambda m: m.group("javadoc") + m.group("ann") + m.group("decl"), src)


def fix_file(path):
    src = path.read_text(encoding="utf-8")
    new_src = remove_duplicate_class_javadocs(src)
    new_src = move_method_javadoc_before_annotations(new_src)
    if new_src != src:
        path.write_text(new_src, encoding="utf-8")
        return True
    return False


def main():
    modified = []
    for f in sorted(BASE.rglob("*.java")):
        try:
            if fix_file(f):
                modified.append(str(f))
        except Exception as e:
            print(f"ERROR {f}: {e}", file=sys.stderr)
    print(f"Fixed issues in {len(modified)} files:")
    for p in modified:
        print(p)


if __name__ == "__main__":
    main()

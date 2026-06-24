#!/usr/bin/env python3
"""Fix auto-generated low-quality Javadocs in Java source files.

This script removes meaningless Javadoc lines such as:
  - @param false 参数
  - @param DateTimeFormat.ISO.DATE 参数
  - @param xxx 参数
  - @param xxx xxx (parameter name repeated as description)
  - method descriptions like "执行 xxx。"

If after cleanup a Javadoc block contains no meaningful content, the entire
block is removed.
"""

import re
import sys
from pathlib import Path
from typing import Optional

BASE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("/Users/hanfugui/code/fund-momentum-strategy/src/main/java/com/hacker/code")

# Regex to find Javadoc blocks immediately preceding a method declaration.
METHOD_JAVADOC_RE = re.compile(
    r"(?P<javadoc>/\*\*.*?\*/\n)"
    r"(?P<annotations>(?:^[ \t]*@[^\n]+\n)*)"
    r"(?P<modifiers>(?:public|private|protected|static|final|abstract|synchronized|native|default|strictfp|\s)+)"
    r"(?P<returnType>[\w<>,?\[\]\.\s]+?)\s+"
    r"(?P<methodName>\w+)\s*"
    r"\((?P<params>[^)]*)\)",
    re.MULTILINE | re.DOTALL,
)

PARAM_NAME_RE = re.compile(r"(?:@\w+(?:\([^)]*\))?\s+)*([\w<>,?\[\]\.]+)\s+([\w$]+)\s*(?:,|$)")


def extract_param_names(params_text: str) -> list[str]:
    """Extract parameter names from a Java method signature parameter list."""
    names = []
    for match in PARAM_NAME_RE.finditer(params_text):
        names.append(match.group(2))
    return names


def is_meaningless_param_line(line: str, valid_names: list[str]) -> bool:
    """Return True if the @param line is auto-generated garbage."""
    stripped = line.strip()
    if not stripped.startswith("* @param "):
        return False
    # Example: "* @param false 参数"
    # Example: "* @param DateTimeFormat.ISO.DATE 参数"
    # Example: "* @param xxx 参数"
    # Example: "* @param request request"
    after_param = stripped[len("* @param "):].strip()
    parts = after_param.split(None, 1)
    if not parts:
        return True
    name = parts[0]
    desc = parts[1] if len(parts) > 1 else ""
    if name in ("false", "DateTimeFormat.ISO.DATE", "ISO.DATE"):
        return True
    if desc in ("参数", "param", "args"):
        return True
    if desc == name:
        return True
    if name not in valid_names:
        # Unknown parameter name -> likely auto-generated from annotation value
        return True
    return False


def is_meaningless_description_line(line: str) -> bool:
    """Return True for lines like '执行 xxx。' or 'xxx。' with no real description."""
    stripped = line.strip().lstrip("*").strip()
    if not stripped:
        return False
    # Keep first line if it looks like a real sentence (more than 2 chars and not just "执行 x。")
    if stripped.startswith("执行 ") and stripped.endswith("。"):
        return True
    # Keep lines that are not just a single word/phrase + period
    return False


def cleanup_javadoc_block(javadoc: str, param_names: list[str]) -> Optional[str]:
    """Clean up a Javadoc block. Return None if the block should be removed."""
    lines = javadoc.splitlines(keepends=True)
    cleaned_lines = []
    has_meaningful_content = False

    for i, line in enumerate(lines):
        stripped = line.strip()
        # Keep opening /** and closing */
        if stripped == "/**" or stripped == "*/":
            cleaned_lines.append(line)
            continue
        # Remove meaningless @param lines
        if is_meaningless_param_line(line, param_names):
            continue
        # Remove meaningless description lines ("执行 xxx。")
        if is_meaningless_description_line(line):
            continue
        # Remove generic @return 计算结果 if it's the only descriptive line
        if re.match(r"^\s*\*\s*@return\s+计算结果\s*$", line):
            continue
        # Remove @throws lines that are just placeholders
        if re.match(r"^\s*\*\s*@throws\s+\S+\s+异常\s*$", line):
            continue

        cleaned_lines.append(line)

        content = stripped.lstrip("*").strip()
        if content and not content.startswith("@return") and not content.startswith("@param") and not content.startswith("@throws"):
            has_meaningful_content = True
        # A @param with a non-trivial description is meaningful
        if content.startswith("@param"):
            parts = content.split(None, 2)
            if len(parts) >= 3 and parts[2] not in ("参数", "", parts[1]):
                has_meaningful_content = True
        # A @return with a non-trivial description is meaningful
        if content.startswith("@return"):
            desc = content[len("@return"):].strip()
            if desc and desc not in ("计算结果", "结果", "return"):
                has_meaningful_content = True

    # Remove empty javadoc blocks
    if not has_meaningful_content:
        return None

    # Remove leading blank lines inside javadoc
    while len(cleaned_lines) > 2 and cleaned_lines[1].strip() == "*":
        cleaned_lines.pop(1)
    # Remove trailing blank lines before */
    while len(cleaned_lines) > 2 and cleaned_lines[-2].strip() == "*":
        cleaned_lines.pop(-2)

    if len(cleaned_lines) <= 2:
        return None

    return "".join(cleaned_lines)


def fix_file(path: Path) -> bool:
    src = path.read_text(encoding="utf-8")
    new_src = src

    # Process from end to start to keep indices valid
    for match in reversed(list(METHOD_JAVADOC_RE.finditer(src))):
        javadoc = match.group("javadoc")
        params_text = match.group("params")
        param_names = extract_param_names(params_text)
        cleaned = cleanup_javadoc_block(javadoc, param_names)
        if cleaned is None:
            # Remove the whole javadoc block
            start, end = match.start("javadoc"), match.end("javadoc")
            new_src = new_src[:start] + new_src[end:]
        elif cleaned != javadoc:
            start, end = match.start("javadoc"), match.end("javadoc")
            new_src = new_src[:start] + cleaned + new_src[end:]

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
    print(f"Fixed Javadoc issues in {len(modified)} files:")
    for p in modified:
        print(p)


if __name__ == "__main__":
    main()

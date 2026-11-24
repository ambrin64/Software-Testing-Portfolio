"""Convert a JUnit console-launcher XML report into a readable HTML report and a Markdown table.

Usage:
    python junit_xml_to_html.py <TEST-junit-jupiter.xml> <output.html> [--md <output.md>] [--title "..."]

The XML file is also sanitised in place: machine-specific <properties> (user name, paths, host
name) are removed so the report can be published safely on GitHub.
"""
import argparse
import html
import re
import xml.etree.ElementTree as ET
from datetime import datetime

TC_ID = re.compile(r"(TC_[A-Z]+_[A-Z]+_\d+)")


def parse(xml_path):
    tree = ET.parse(xml_path)
    suite = tree.getroot()

    # --- sanitise ---------------------------------------------------------------------------
    for props in suite.findall("properties"):
        suite.remove(props)
    suite.set("hostname", "localhost")
    tree.write(xml_path, encoding="UTF-8", xml_declaration=True)

    rows = []
    for case in suite.iter("testcase"):
        out = case.findtext("system-out") or ""
        m = re.search(r"display-name: (.*)", out)
        display = m.group(1).strip() if m else case.get("name")
        tc = TC_ID.search(display)
        failure = case.find("failure")
        error = case.find("error")
        bad = failure if failure is not None else error
        expected = display.split("->", 1)[1].strip() if "->" in display else ""
        if bad is not None:
            message = (bad.get("message") or bad.get("type") or "")
            message = re.sub(r"Multiple Failures \(\d+ failures?\)", "", message)
            message = message.replace("org.opentest4j.AssertionFailedError: ", "")
            actual = "; ".join(line.strip() for line in message.splitlines() if line.strip())
            status = "FAIL"
        else:
            actual = expected or "As expected"
            status = "PASS"
        rows.append({
            "id": tc.group(1) if tc else case.get("name"),
            "description": TC_ID.sub("", display).lstrip(": ").strip(),
            "expected": expected,
            "actual": actual,
            "status": status,
            "time": float(case.get("time", 0)),
            "class": case.get("classname", "").split(".")[-1],
        })
    rows.sort(key=lambda r: r["id"])
    return suite, rows


def write_html(suite, rows, out_path, title):
    total = len(rows)
    failed = sum(r["status"] == "FAIL" for r in rows)
    passed = total - failed
    rate = 100.0 * passed / total if total else 0
    stamp = suite.get("timestamp", datetime.now().isoformat(timespec="seconds"))

    body_rows = "\n".join(
        f"<tr class='{r['status'].lower()}'><td>{i}</td><td><code>{html.escape(r['id'])}</code></td>"
        f"<td>{html.escape(r['description'])}</td><td>{html.escape(r['expected'])}</td>"
        f"<td>{html.escape(r['actual'])}</td><td class='st'>{r['status']}</td></tr>"
        for i, r in enumerate(rows, 1))

    doc = f"""<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8"><title>{html.escape(title)}</title>
<style>
 body {{ font-family: Segoe UI, Arial, sans-serif; margin: 32px; color: #1f2328; }}
 h1 {{ margin-bottom: 4px; }} .sub {{ color: #59636e; margin-top: 0; }}
 .cards {{ display: flex; gap: 16px; margin: 20px 0; }}
 .card {{ border: 1px solid #d1d9e0; border-radius: 8px; padding: 12px 20px; min-width: 120px; }}
 .card b {{ display: block; font-size: 28px; }}
 .ok b {{ color: #1a7f37; }} .bad b {{ color: #cf222e; }}
 .bar {{ height: 14px; background: #ffebe9; border-radius: 7px; overflow: hidden; margin-bottom: 24px; }}
 .bar div {{ height: 100%; background: #2da44e; width: {rate:.1f}%; }}
 table {{ border-collapse: collapse; width: 100%; font-size: 13px; }}
 th, td {{ border: 1px solid #d1d9e0; padding: 6px 8px; text-align: left; vertical-align: top; }}
 th {{ background: #f6f8fa; }}
 tr.fail td {{ background: #ffebe9; }} td.st {{ font-weight: 600; }}
 tr.pass td.st {{ color: #1a7f37; }} tr.fail td.st {{ color: #cf222e; }}
</style></head><body>
<h1>{html.escape(title)}</h1>
<p class="sub">Online Examination System &middot; JUnit 5 &middot; executed {html.escape(stamp)}</p>
<div class="cards">
 <div class="card"><b>{total}</b>Total</div>
 <div class="card ok"><b>{passed}</b>Passed</div>
 <div class="card bad"><b>{failed}</b>Failed</div>
 <div class="card"><b>{rate:.1f}%</b>Pass rate</div>
</div>
<div class="bar"><div></div></div>
<table><tr><th>#</th><th>Test Case ID</th><th>Description</th><th>Expected</th><th>Actual</th><th>Status</th></tr>
{body_rows}
</table></body></html>"""
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(doc)
    return total, passed, failed


def write_md(rows, out_path):
    lines = ["| # | Test Case ID | Description | Expected Result | Actual Result | Status |",
             "|---|--------------|-------------|-----------------|---------------|--------|"]
    for i, r in enumerate(rows, 1):
        cells = [str(i), r["id"], r["description"], r["expected"], r["actual"],
                 "✅ PASS" if r["status"] == "PASS" else "❌ FAIL"]
        lines.append("| " + " | ".join(c.replace("|", "\\|") for c in cells) + " |")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("xml")
    ap.add_argument("html")
    ap.add_argument("--md")
    ap.add_argument("--title", default="Test Execution Report")
    args = ap.parse_args()
    suite, rows = parse(args.xml)
    total, passed, failed = write_html(suite, rows, args.html, args.title)
    if args.md:
        write_md(rows, args.md)
    print(f"{total} tests: {passed} passed, {failed} failed -> {args.html}")


if __name__ == "__main__":
    main()

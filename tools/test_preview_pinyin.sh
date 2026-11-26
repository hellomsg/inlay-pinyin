#!/usr/bin/env bash
set -euo pipefail
if bash ./gradlew --no-daemon :app:connectedPreviewAndroidTest; then
    exit 0
fi
python3 - <<'PY'
from pathlib import Path
from xml.etree import ElementTree

reports = Path('app/build/outputs/androidTest-results').rglob('TEST-*.xml')
for report in reports:
    for case in ElementTree.parse(report).iter('testcase'):
        for kind in ('failure', 'error'):
            problem = case.find(kind)
            if problem is not None:
                detail = problem.get('message') or (problem.text or '').strip()
                summary = f"{case.get('name')}: {detail[:2500]}"
                print('::error::' + summary.replace('%', '%25').replace('\n', '%0A'))
PY
adb logcat -d -s InlayPinyin:E AndroidRuntime:E | tail -n 60 || true
exit 1

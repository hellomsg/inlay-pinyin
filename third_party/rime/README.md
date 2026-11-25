# Offline Pinyin dependencies

`python3 tools/prepare_rime.py` (Python 3.12+) downloads the exact source and vocabulary
archives listed in the JSON manifests, verifies their SHA-256 digests, and prepares the
Android assets. The original vocabulary is Luna Pinyin, Essay and Prelude. OpenCC's
traditional-to-simplified mappings convert candidates locally. The app keeps the
compiled vocabulary in Android's private no-backup directory and disables Rime learning.

The Android CMake setup is adapted from [MinIME](https://github.com/poi890poi/minime/tree/efe9186e886034272bf4266310f0064cdcbeaa5a),
licensed Apache-2.0. The combined third-party license texts and attribution are in
`NOTICES.txt` and are bundled in the APK. The manifests point to the corresponding
source revisions and archive hashes. The native build uses NDK 27.2.12479018 and CMake 3.22.1.

# Offline Pinyin dependencies

`python3 tools/prepare_rime.py` (Python 3.12+) downloads the exact source and vocabulary
archives and precompiled vocabulary, verifies SHA-256 digests, and prepares the
Android assets. The vocabulary is MinIME's Luna Pinyin, Essay and Prelude build for
librime 1.16.1. `data-sources.json` records the corresponding data sources; its
reproduction and configurations are published with [MinIME's Rime foundation](https://github.com/poi890poi/minime/tree/efe9186e886034272bf4266310f0064cdcbeaa5a/third_party/rime).
OpenCC converts the resulting traditional candidates locally. The app loads the
compiled vocabulary from Android's private no-backup directory, with Rime learning disabled.

The Android CMake setup is adapted from [MinIME](https://github.com/poi890poi/minime/tree/efe9186e886034272bf4266310f0064cdcbeaa5a),
licensed Apache-2.0. The combined third-party license texts and attribution are in
`NOTICES.txt` and are bundled in the APK. The manifests point to the corresponding
source revisions and archive hashes. The native build uses NDK 27.2.12479018 and CMake 3.22.1.

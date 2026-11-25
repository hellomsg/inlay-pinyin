"""Prepare the pinned Rime native sources and offline dictionary for Gradle."""
import hashlib
import json
from pathlib import Path
import shutil
import tarfile
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]
META = ROOT / 'third_party/rime'
CACHE = ROOT / '.tools/native-sources'
ASSETS = ROOT / 'app/build/generated/rime-assets/rime'
REFERENCE = 'https://raw.githubusercontent.com/poi890poi/minime/efe9186e886034272bf4266310f0064cdcbeaa5a/app/src/main/rimeAssets/rime/'


def archive_for(source, url):
    archive = CACHE / source['archive']
    if not archive.is_file():
        temporary = archive.with_suffix('.partial')
        with urllib.request.urlopen(url, timeout=90) as response, temporary.open('wb') as target:
            shutil.copyfileobj(response, target)
        with temporary.open('rb') as stream:
            valid = hashlib.file_digest(stream, 'sha256').hexdigest() == source['sha256']
        if not valid:
            temporary.unlink()
            raise ValueError(f"Unexpected SHA-256 for {source['name']}")
        temporary.replace(archive)
    with archive.open('rb') as stream:
        if hashlib.file_digest(stream, 'sha256').hexdigest() != source['sha256']:
            raise ValueError(f"Unexpected SHA-256 for {archive}")
    return archive


def entries(name):
    return json.loads((META / name).read_text())


CACHE.mkdir(parents=True, exist_ok=True)
for source in entries('native-sources.json'):
    archive = archive_for(source, source['url'])
    stamp = CACHE / (source['name'] + '.verified')
    if stamp.is_file() and stamp.read_text() == source['sha256'] and (CACHE / source['directory']).is_dir():
        continue
    if archive.suffix == '.zip':
        with zipfile.ZipFile(archive) as bundle:
            bundle.extractall(CACHE)
    else:
        # Only the Boost headers and license are needed.
        with tarfile.open(archive) as bundle:
            selected = (item for item in bundle if item.name.startswith(source['directory'] + '/boost/')
                        or item.name == source['directory'] + '/LICENSE_1_0.txt')
            bundle.extractall(CACHE, members=selected, filter='data')
    stamp.write_text(source['sha256'])

if ASSETS.exists():
    shutil.rmtree(ASSETS)
ASSETS.mkdir(parents=True)
for name, digest in entries('model.json')['assets'].items():
    model = {'archive': name, 'name': name, 'sha256': digest}
    shutil.copyfile(archive_for(model, REFERENCE + name), ASSETS / name)
opencc = ASSETS / 'opencc'
opencc.mkdir(exist_ok=True)
source = next(source for source in entries('native-sources.json') if source['name'] == 'opencc')
for name in ('TSCharacters.txt', 'TSPhrases.txt'):
    shutil.copyfile(CACHE / source['directory'] / 'data/dictionary' / name, opencc / name)
shutil.copyfile(META / 't2s.json', opencc / 't2s.json')
shutil.copyfile(META / 'NOTICES.txt', ASSETS / 'NOTICES.txt')
identity = hashlib.sha256()
for asset in sorted(path for path in ASSETS.rglob('*') if path.is_file() and path.name != 'bundle-id.txt'):
    identity.update(asset.relative_to(ASSETS).as_posix().encode())
    identity.update(asset.read_bytes())
(ASSETS / 'bundle-id.txt').write_text(identity.hexdigest())

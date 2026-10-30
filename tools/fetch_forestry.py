"""Download the pinned official Forestry dependency, verifying its SHA-256."""
import hashlib, pathlib, urllib.request
root=pathlib.Path(__file__).resolve().parents[1]
url='https://mediafilez.forgecdn.net/files/2333/823/forestry_1.7.10-4.2.16.64.jar'
expected='bdebf1569584106c11e8ab137ddaf2ab10c2b3c1c8f1be0020b5c7dbb2ca2135'
target=root/'libs/forestry_1.7.10-4.2.16.64.jar'
if target.exists():
    if hashlib.sha256(target.read_bytes()).hexdigest()!=expected:raise SystemExit('Existing dependency has a different checksum; file was left untouched.')
else:
    data=urllib.request.urlopen(url,timeout=60).read()
    if hashlib.sha256(data).hexdigest()!=expected:raise SystemExit('Dependency checksum mismatch; download rejected.')
    target.parent.mkdir(exist_ok=True);target.write_bytes(data)
print('Forestry 4.2.16.64 verified')

"""Materialize the already-authorized repository signing key only on the CI runner."""
import base64
import os
from pathlib import Path

names = ('ANDROID_KEYSTORE_BASE64', 'ANDROID_KEYSTORE_PASSWORD', 'ANDROID_KEY_ALIAS', 'ANDROID_KEY_PASSWORD')
if any(not os.environ.get(name) for name in names):
    raise SystemExit('Configure all four Android signing Secrets first')
def escaped(name):
    value = os.environ[name].encode('utf-16-be')
    return ''.join('\\u%04x' % int.from_bytes(value[i:i+2], 'big') for i in range(0,len(value),2))
key = Path('android/app/offline-release.keystore')
key.write_bytes(base64.b64decode(os.environ['ANDROID_KEYSTORE_BASE64'], validate=True))
key.chmod(0o600)
properties = Path('android/key.properties')
properties.write_text('storeFile=offline-release.keystore\n'
    + f"storePassword={escaped('ANDROID_KEYSTORE_PASSWORD')}\n"
    + f"keyAlias={escaped('ANDROID_KEY_ALIAS')}\n"
    + f"keyPassword={escaped('ANDROID_KEY_PASSWORD')}\n")
properties.chmod(0o600)

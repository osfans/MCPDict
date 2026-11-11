"""Run platform-independent phonology checks with Python 3 and a JDK (no dependencies)."""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile

tests = Path(__file__).resolve().parent
root = tests.parents[1]
java_home = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else None
def executable(name):
    found = shutil.which(str(java_home / name)) if java_home else shutil.which(name)
    if not found:
        raise SystemExit('Set JAVA_HOME or add a JDK to PATH: missing ' + name)
    return found

sources = root / 'app/src/main/java/com/osfans/mcpdict'
classes = ['PhoneticParts', 'CharacterSoundComparison', 'PhonologyOrders', 'SankeyHit']
checks = sorted(tests.glob('*Checks.java'))
with tempfile.TemporaryDirectory(prefix='mcpdict-phonology-') as output:
    subprocess.run([executable('javac'), '-encoding', 'UTF-8', '-d', output,
                    *[str(sources / (name + '.java')) for name in classes],
                    *map(str, checks)], check=True)
    for check in checks:
        subprocess.run([executable('java'), '-cp', output, check.stem], check=True)

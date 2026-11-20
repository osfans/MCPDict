"""Run custom-classification checks using a JDK, org.json and kxml2.

Pass --json-jar and --xml-jar, or set JSON_JAR and KXML_JAR.
No Android SDK, device or network is needed by this runner.
"""
from pathlib import Path
import argparse
import os
import shutil
import subprocess
import tempfile

here = Path(__file__).resolve().parent
root = here.parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java-home', default=os.environ.get('JAVA_HOME'))
parser.add_argument('--json-jar', default=os.environ.get('JSON_JAR'))
parser.add_argument('--xml-jar', default=os.environ.get('KXML_JAR'))
args = parser.parse_args()
for label, value in [('json', args.json_jar), ('xml', args.xml_jar)]:
    if not value or not Path(value).is_file():
        parser.error('Pass an existing --' + label + '-jar (org.json / kxml2).')
java_home = Path(args.java_home) / 'bin' if args.java_home else None

def executable(name):
    if os.name == 'nt':
        name += '.exe'
    result = shutil.which(str(java_home / name)) if java_home else shutil.which(name)
    if not result:
        raise SystemExit('Set JAVA_HOME or add a JDK to PATH: missing ' + name)
    return result

source = root / 'app/src/main/java/com/osfans/mcpdict'
production = [source / 'custom' / (name + '.java') for name in ['CustomSchemes', 'CustomResults', 'SchemeWorkbook']]
production.append(source / 'UI/ListReadings.java')
with tempfile.TemporaryDirectory(prefix='mcpdict-custom-') as output:
    dependencies = os.pathsep.join([str(Path(args.json_jar).resolve()), str(Path(args.xml_jar).resolve())])
    subprocess.run([executable('javac'), '-encoding', 'UTF-8', '-cp', dependencies, '-d', output,
                    *map(str, production), *map(str, (here / 'src').rglob('*.java'))], check=True)
    for check in sorted((here / 'src').glob('*Checks.java')):
        subprocess.run([executable('java'), '-cp', output + os.pathsep + dependencies, check.stem], check=True)

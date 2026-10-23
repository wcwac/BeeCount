"""Require real JUnit discovery and report failures independently of Gradle annotations."""
import xml.etree.ElementTree as ET
from pathlib import Path
import json

reports = list(Path('build/app/test-results/testDebugUnitTest').glob('TEST-*.xml'))
required=set(json.loads((Path(__file__).resolve().parents[1]/'reference-test-cases.json').read_text())['unit'])
observed=set()
totals = {key:0 for key in ('tests','failures','errors','skipped')}
for path in reports:
    root = ET.parse(path).getroot()
    for key in totals:
        totals[key] += int(root.get(key,0))
    for case in root.iter('testcase'):
        name=case.get('classname','')+'.'+case.get('name','')
        assert name not in observed, 'Duplicated unit result: '+name
        observed.add(name)
        for failure in list(case.findall('failure')) + list(case.findall('error')):
            message = (case.get('classname','')+'.'+case.get('name','')+'\n'+(failure.text or ''))[:6000]
            print('::error title=Unit test failed::'+message.replace('%','%25').replace('\r','%0D').replace('\n','%0A'))
print('JUnit totals:',totals)
assert totals['tests'] >= 146, 'Tests were not discovered or did not execute'
assert totals['errors'] == totals['failures'] == totals['skipped'] == 0, 'Unit tests failed or skipped'
assert observed==required, 'Unit case inventory mismatch: missing='+str(sorted(required-observed))+' unexpected='+str(sorted(observed-required))
assert totals['tests']==len(observed), 'JUnit total does not match individual case results'

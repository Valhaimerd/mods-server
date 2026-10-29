from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil
H=Path(__file__).resolve().parent;R=H.parents[2];W=R/'tmp/vanish-breathing'
record=json.loads((H/'artifacts.json').read_text());candidate=R/record['candidate'];active=R/record['input']
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=W/'baseline'/active.name
if not baseline.exists():
    assert sha(active)==record['input_sha256'],'Original baseline missing: recover the recorded input JAR before verification'
    baseline.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(active,baseline)
assert sha(baseline)==record['input_sha256'],'Pinned baseline hash changed'
assert sha(active) in {record['input_sha256'],record['candidate_sha256']},'Active JAR matches neither original baseline nor tested candidate'
assert sha(candidate)==record['candidate_sha256']
with ZipFile(baseline) as a,ZipFile(candidate) as b:
    changed=[n for n in a.namelist() if a.read(n)!=b.read(n)]
    added=sorted(set(b.namelist())-set(a.namelist()))
    assert set(changed)=={'rogues.mixins.json','net/rogues/mixin/client/StealthHandsMixin.class'},changed
    assert all(n.startswith('net/rogues/client/') or n.startswith('net/rogues/mixin/client/') for n in added)
    config=json.loads(b.read('rogues.mixins.json'))
    assert 'client.StealthHeldStateMixin' not in config['client']
    assert json.loads(b.read('data/rogues/spell/vanish.json'))['impacts'][0]['action']['status_effect']['duration']==7
    assert a.read('META-INF/neoforge.mods.toml')==b.read('META-INF/neoforge.mods.toml')
result=(W/'client/vanish-result.txt').read_text().strip();assert result.startswith('PASS 74'),result
checks=(W/'client/vanish-results.txt').read_text();assert 'FAIL ' not in checks
(H/'candidate-results.txt').write_text(checks)
(H/'runtime-tested-sha256.txt').write_text(sha(candidate)+'\n')
record.update(changed=changed,added=added,runtime_result=result,runtime_versions={'Minecraft':'26.2','NeoForge':'26.2.0.90','Java':'26.0.2 (class release 25)','Punchy':'2.8f','Player Animation Library':'1.2.8','CrossArms':'1.4'},runtime_engine_sha256=sha(next((W/'client/mods').glob('spell_engine-*.jar'))))
(H/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n')
print(result+'; gameplay classes/resources byte-identical; candidate hash pinned.')

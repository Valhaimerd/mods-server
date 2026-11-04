from pathlib import Path
from zipfile import ZipFile
import copy, hashlib, json

R = Path(__file__).resolve().parents[3]
H = Path(__file__).resolve().parent
W = R / 'tmp/holy-wand-dual-purpose'
NAME = 'paladins-neoforge-3.1.2+26.2.jar'
with ZipFile(W / 'before' / NAME) as z:
    infos = z.infolist(); old = {i.filename: z.read(i) for i in infos}
files = dict(old)
path = 'data/paladins/spell/heal.json'
spell = json.loads(files[path])
staff = json.loads(files['data/paladins/spell/holy_shock.json'])
spell['impacts'].append(copy.deepcopy(next(i for i in staff['impacts'] if i['action']['type'] == 'DAMAGE')))
files[path] = (json.dumps(spell, ensure_ascii=False, indent=2) + '\n').encode()
path = 'assets/paladins/lang/en_us.json'
lang = json.loads(files[path])
lang['spell.paladins.heal.description'] = 'Heal yourself or an ally for 50%% P, or strike an enemy for 80%% P.'
files[path] = (json.dumps(lang, ensure_ascii=False, indent=2) + '\n').encode()
candidate = W / 'built' / NAME
with ZipFile(candidate, 'w') as z:
    for info in infos: z.writestr(copy.copy(info), files[info.filename])
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
(H / 'artifact.json').write_text(json.dumps({'jar': NAME, 'before': sha(W / 'before' / NAME), 'after': sha(candidate), 'changed': [n for n in old if old[n] != files[n]]}, indent=2) + '\n')
print('Built Holy Wand candidate; production unchanged.')

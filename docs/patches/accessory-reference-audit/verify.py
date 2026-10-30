from pathlib import Path
from zipfile import ZipFile
import json,re,hashlib
ROOT=Path(__file__).resolve().parents[3]
audit=json.loads((Path(__file__).with_name('audit.json')).read_text())
for row in audit['archives']:
 p=ROOT/'mods'/row['jar'];assert hashlib.sha256(p.read_bytes()).hexdigest()==row['sha256']
 with ZipFile(p) as z:
  assert z.testzip() is None
  if p.name.startswith('Nameless'):
   text=(ROOT/'docs/patches/nameless-balance/effects.md').read_text(encoding='utf-8')
   available={v.split(':')[1] for v in json.loads(z.read('data/nameless_trinkets/tags/item/nameless_trinkets_tag.json'))['values']}
   active=text.split('## Individual trinkets')[1].split('## Removed relics')[0]
   documented=set(re.findall(r'\| `([^`]+)` \|',active));assert documented==available and len(available)==42
   policy=json.loads((ROOT/'docs/patches/nameless-balance/balance.json').read_text());removed=set(policy['removed'])
   disabled=text.split('## Removed relics')[1].split('## Titan')[0];assert set(re.findall(r'\| `([^`]+)` \|',disabled))==removed and len(removed)==12
   support=text.split('## Supporting items')[1].split('## Current archive')[0];support_ids=re.findall(r'\| `([^`]+)` \|',support);assert len(support_ids)==6
   lang=json.loads(z.read('assets/nameless_trinkets/lang/en_us.json'))
   for item in available|removed|set(support_ids):assert 'item.nameless_trinkets.'+item in lang
  else:
   text=(ROOT/'docs/patches/artifacts-balance/effects.md').read_text(encoding='utf-8')
   lang=json.loads(z.read('assets/artifacts/lang/en_us.json'));items={k.removeprefix('item.artifacts.') for k in lang if k.startswith('item.artifacts.') and '.' not in k.removeprefix('item.artifacts.')}
   listed=re.findall(r'\*\*Item:\*\* `artifacts:([^`]+)`',text);assert len(listed)==len(set(listed))==49 and set(listed)==items
   assert json.loads(z.read('artifacts-pack-balance.json'))==json.loads((ROOT/'docs/patches/artifacts-balance/balance.json').read_text())
   assert '#artifacts:slot/feet' in json.loads(z.read('data/curios/tags/item/feet.json'))['values']
   assert 'artifacts:steadfast_spikes' in json.loads(z.read('data/artifacts/tags/item/slot/feet.json'))['values']
   assert 'Planned value' not in text and '**Planned:**' not in text
  assert '\ufffd' not in text
print('PASS: both installed hashes/CRCs; Nameless42 active +12 removed +6 supporting; Artifacts49 IDs; packaged overrides and Feet slot; references cover every item. Current capped-balance archives verified.')

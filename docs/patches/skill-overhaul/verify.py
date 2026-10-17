"""Focused structural regression checks; run against a candidate JAR directory."""
from pathlib import Path
import sys,json,zipfile
folder=Path(sys.argv[1] if len(sys.argv)>1 else 'mods')
with zipfile.ZipFile(folder/'adventurerskills-EN-1.3-26.2-neoforge-fixed.jar') as z:
    base='data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
    d=json.loads(z.read(base+'definitions.json'));s=json.loads(z.read(base+'skills.json'));c=json.loads(z.read(base+'connections.json'))
    assert d[s['zdfpatqypd5nnqx8']['definition']]['title']=='Magistrate','Magistrate replacement missing'
    assert len(s)==len({*s})
    for kind in c.values():
        for edges in kind.values():
            for a,b in edges:assert a in s and b in s,(a,b)
    for k in ['i0j6pbbluuqne53e','s02hkhd2ha87ji1n','qxcrnvfslhwdg5ip','a036s916b2cp7hb2','i2oo5vl75gyj6b3d','usqd6s5h8ypx5gud','ijsno4eog2w0unyr','bysc5yiqm9ni1mkf']:assert k not in s,k
    assert d['root_versatile']['title']=='Companion'
    for v in d.values():
        assert not any(r['type']=='puffish_skills:command' and 'give ' in r['data'].get('command','') for r in v.get('rewards',[]))
    assert 'new_colossus_health' in s and 'new_colossus_healing' in s
with zipfile.ZipFile(folder/'skill_tree-neoforge-1.6.1+26.2.jar') as z:
    for cat in ['class_skills','weapon_skills']:
        b=f'data/skill_tree_rpgs/puffish_skills/categories/{cat}/'
        d=json.loads(z.read(b+'definitions.json'))
        for key in ['arcane','frost','fire','priest','rogue']:
            for suffix in ['root','boost']:assert d[key+'_'+suffix]['rewards'][0]['data']['value']==.03
        assert d['paladin_root']['rewards'][0]['data']['value']==.2
        assert d['paladin_root']['rewards'][0]['data']['operation']=='addition'
        exp=json.loads(z.read(b+'experience.json'));assert '1.35' in exp['sources'][0]['data']['experience']
print('PASS structural skill overhaul checks')

"""Focused structural regression checks; run against a candidate JAR directory."""
from pathlib import Path
import sys,json,zipfile
folder=Path(sys.argv[1] if len(sys.argv)>1 else 'mods')
with zipfile.ZipFile(folder/'adventurerskills-EN-1.3-26.2-neoforge-fixed.jar') as z:
    base='data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
    d=json.loads(z.read(base+'definitions.json'));s=json.loads(z.read(base+'skills.json'));c=json.loads(z.read(base+'connections.json'))
    assert d[s['zdfpatqypd5nnqx8']['definition']]['title']=={'text':'Magistrate','color':'blue'},'Magistrate blue title missing'
    for key in ['martial_prowess','hunter','magistrate','aegis','colossus','bulwark','packleader','guardian_stead','root_versatile']:assert d[key]['title']['color']=='blue',key
    for key,icon in [('magistrate','ender_eye'),('aegis','enchanted_golden_apple'),('bulwark','netherite_chestplate'),('guardian_stead','diamond_horse_armor'),('ranged_damage+3%','bow'),('draw_speed','crossbow'),('pet_hp','apple'),('pet_speed','rabbit_foot'),('pet_res','leather_chestplate'),('pet_kr','iron_horse_armor'),('packleader','cooked_beef')]:assert d[key]['icon']['data']['item']=='minecraft:'+icon,key
    assert d['packleader']['title']=={'text':'Pack Leader','color':'blue'}
    def rewards(key):return {r['data']['attribute']:r['data']['value'] for r in d[key]['rewards'] if r['type']=='puffish_skills:attribute'}
    assert rewards('martial_prowess')=={'puffish_attributes:melee_damage':.08,'attack_speed':.05,'critical_strike:chance':.1,'critical_strike:damage':.1}
    operations={r['data']['attribute']:r['data']['operation'] for r in d['martial_prowess']['rewards']}
    assert operations['critical_strike:chance']=='multiply_base'
    assert operations['critical_strike:damage']=='multiply_total'
    assert rewards('hunter')=={'puffish_attributes:ranged_damage':.1,'luck':1,'movement_speed':.05,'puffish_attributes:jump':.05,'puffish_attributes:armor_shred':.1,'puffish_attributes:bow_projectile_speed':.25,'puffish_attributes:crossbow_projectile_speed':.25}
    assert rewards('magistrate')=={**{'spell_power:'+x:.12 for x in ['arcane','fire','frost']},'spell_power:haste':.05}
    assert rewards('caster_power')=={'spell_power:'+x:.04 for x in ['arcane','fire','frost']}
    assert rewards('caster_haste')=={'spell_power:haste':.03}
    assert rewards('aegis')=={'puffish_attributes:magic_resistance':.15,'puffish_attributes:ranged_resistance':.1,'minecraft:movement_speed':.06}
    assert rewards('melee_damage+3%')=={'puffish_attributes:melee_damage':.02}
    assert rewards('ranged_damage+3%')=={'puffish_attributes:ranged_damage':.03}
    assert s['f8rbbpxi0myhi3cg']['definition']=='attack_speed+5%'
    assert rewards(s['vxmb1u84wzfwpuq6']['definition'])=={'puffish_attributes:armor_shred':.05}
    assert rewards('colossus')=={'minecraft:max_health':12,'puffish_attributes:healing':.06}
    assert len(s)==len({*s})
    for kind in c.values():
        for edges in kind.values():
            for a,b in edges:assert a in s and b in s,(a,b)
    for k in ['i0j6pbbluuqne53e','s02hkhd2ha87ji1n','qxcrnvfslhwdg5ip','a036s916b2cp7hb2','i2oo5vl75gyj6b3d','usqd6s5h8ypx5gud','ijsno4eog2w0unyr','bysc5yiqm9ni1mkf']:assert k not in s,k
    assert d['root_versatile']['title']['text']=='Companion'
    for v in d.values():
        assert not any(r['type']=='puffish_skills:command' and 'give ' in r['data'].get('command','') for r in v.get('rewards',[]))
    assert 'new_colossus_health' in s and 'new_colossus_healing' in s
with zipfile.ZipFile(folder/'skill_tree-neoforge-1.6.1+26.2.jar') as z:
    for cat in ['class_skills','weapon_skills']:
        b=f'data/skill_tree_rpgs/puffish_skills/categories/{cat}/'
        d=json.loads(z.read(b+'definitions.json'))
        for key in ['arcane','frost','fire','priest','rogue']:
            for suffix in ['root','boost']:assert d[key+'_'+suffix]['rewards'][0]['data']['value']==(.04 if key in ['arcane','frost','fire'] else .03)
        for v in d.values():
            for r in v.get('rewards',[]):
                a=r.get('data',{}).get('attribute')
                if a=='minecraft:attack_damage':assert r['data']['value']==.02
                if a=='ranged_weapon:damage':assert r['data']['value']==.02
        for k in ['archer_root','archer_boost','warrior_root','warrior_boost']:
            assert d[k]['description']=={'text':'+2% '+('Ranged Damage' if k.startswith('archer') else 'Attack Damage')},(cat,k)
        assert d['weapon_bow_root']['description']=={'text':'+2% Ranged Damage','extra':[{'text':'\n'},{'translate':'modifier_condition.skill_tree_rpgs.bow'}]},cat
        assert d['paladin_root']['rewards'][0]['data']['value']==.2
        assert d['paladin_root']['rewards'][0]['data']['operation']=='addition'
        exp=json.loads(z.read(b+'experience.json'));assert '1.35' in exp['sources'][0]['data']['experience']
print('PASS structural skill overhaul checks')

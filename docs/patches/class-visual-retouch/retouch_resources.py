"""Edit presentation subtrees only; retain existing projectile/beam/model dimensions."""
import copy
PALETTES={'rogue':0xBDA0DAFF,'warrior':0xCBA583FF,'archer':0xDFB961FF,'priest':0xF1E9CFFF,'arcane':0xB8A5E7FF,'fire':0xE99652FF,'frost':0xAEDDEFFF}
WARRIOR={'throw','throw_net','charge','shout','mortal_strike','last_stand'}
def family(path,d):
 name=path.rsplit('/',1)[-1][:-5]
 if '/skill_tree_rpgs/' in path:
  for key in PALETTES:
   if name.startswith(key+'_'):return key
  return None
 if '/rogues/' in path:return 'warrior' if name in WARRIOR else 'rogue'
 if '/archers/' in path:return 'archer'
 if '/paladins/' in path:return 'priest' if name in {'holy_beam','penance','circle_of_healing','levitate','lightwell','lightwell_orb','barrier','heal','holy_shock'} else None
 if '/wizards/' in path:
  for key in ['arcane','fire','frost']:
   if key in name or key in d.get('school',''):return key
 return None

def tune(path,d):
 kind=family(path,d)
 if kind is None:return False
 before=copy.deepcopy(d)
 name=path.rsplit('/',1)[-1][:-5]
 nature=kind=='archer' and (name in {'entangling_roots','spirit_wolf'} or name.startswith(('archer_tier_2_spell_2','archer_tier_3_spell_2')))
 color=0xA7D295FF if nature else PALETTES[kind]
 def walk(node,trail=(),flavor=None):
  if isinstance(node,list):
   for x in node:walk(x,trail,flavor)
  elif isinstance(node,dict):
   action=node.get('action',{})
   if action.get('type')=='HEAL':flavor='heal'
   if action.get('type')=='DAMAGE':flavor='damage'
   if action.get('type')=='STATUS_EFFECT':flavor=action.get('status_effect',{}).get('effect_id','')
   if 'batch' in node and 'id' in node:
    batch=node['batch'];appearance=node.setdefault('appearance',{});count=batch.get('count',0)
    ceiling=2 if 'travel_particles' in trail else 6 if 'cast' in trail else 12
    if count>ceiling:batch['count']=ceiling
    if node['id'].startswith('spell_engine:magic'):
     appearance['color']=0xC7E5D7FF if flavor=='heal' else 0x9BC76CFF if flavor and 'poison' in flavor else 0xCD7979FF if flavor and 'bleed' in flavor else 0xE1C789FF if kind=='priest' and flavor=='damage' else color
     if appearance.get('scale',1)>1:appearance['scale']=.7
   for key,value in node.items():walk(value,trail+(key,),flavor)
 walk(d)
 # Keep transport smoke brief and sparse; never add an armed-trap marker or Vanish trail.
 return d!=before

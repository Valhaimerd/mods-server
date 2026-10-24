"""Original deterministic synthesised portal audio. Requires numpy and soundfile.

No samples or movie/game sound assets are used. Packaged Oggs are build inputs,
so ordinary Java rebuilding does not require the synthesis dependencies.
"""
from pathlib import Path
import json, sys
import numpy as np

ROOT=Path(__file__).resolve().parents[3]
sys.path.insert(0,str(ROOT/'tmp/asgardian-expansion/audio-tools'))
import soundfile as sf

OUT=Path(__file__).resolve().parent/'resources/assets/bifrost'
SOUNDS=OUT/'sounds';SOUNDS.mkdir(parents=True,exist_ok=True)
rng=np.random.default_rng(20261024);rate=22050
for name,duration in [('charge',3),('descent',1.25),('impact',1.0),('closure',2.0)]:
    t=np.arange(int(rate*duration))/rate;u=t/duration
    noise=rng.standard_normal(t.size)
    soft=np.convolve(noise,np.ones(17)/17,'same')
    if name=='charge':
        signal=(np.sin(2*np.pi*(72*t+27*t*t))+.35*np.sin(2*np.pi*147*t)+soft*.5)*np.sin(np.pi*u)**.7
    elif name=='descent':
        signal=(soft*2+.35*np.sin(2*np.pi*(800*t-250*t*t)))*np.sin(np.pi*u)**.4
    elif name=='impact':
        signal=(np.sin(2*np.pi*(48*t+16*np.sqrt(t+.001)))+.8*soft)*np.exp(-6*u)*(1-np.exp(-80*u))
    else:
        signal=(.5*np.sin(2*np.pi*(120*t+180*t*t))+soft)*np.sin(np.pi*u)**1.2*(1-u*.7)
    signal*=.68/max(abs(signal).max(),1e-9)
    # Tiny fades avoid clicks and guarantee no clipping.
    signal*=np.minimum(1,t/.015)*np.minimum(1,(duration-t)/.025)
    sf.write(SOUNDS/f'transit_{name}.ogg',signal,rate,format='OGG',subtype='VORBIS')
entries={f'transit_{n}':{'subtitle':f'subtitles.bifrost.transit_{n}','sounds':[{'name':f'bifrost:transit_{n}','attenuation_distance':96}]} for n in ['charge','descent','impact','closure']}
(OUT/'sounds.json').write_text(json.dumps(entries,indent=2)+'\n',encoding='utf-8')
print('Generated four original mono Ogg Vorbis transit sounds')

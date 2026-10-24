from pathlib import Path
import subprocess,shutil
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'tmp/bifrost-alignment'
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/java.exe')
candidate=next((WORK/'built').glob('bifrost-*.jar'))
old=next((ROOT/'tmp/asgardian-implementation').glob('before-iourusraces-*.jar'))
for label in ['missing-race','upstream-race']:
    folder=WORK/label;mods=folder/'mods';mods.mkdir(parents=True,exist_ok=True)
    if not (folder/'libraries').exists():subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{folder/'libraries'}' -Target '{ROOT/'tmp/magic-third-tier/server/libraries'}' | Out-Null"],check=True)
    shutil.copy2(candidate,mods/candidate.name)
    if label=='upstream-race':shutil.copy2(old,mods/old.name.removeprefix('before-'))
    (folder/'eula.txt').write_text('eula=true\n')
    log=folder/'startup.log'
    with log.open('w',encoding='utf-8') as out:result=subprocess.run([str(JAVA),'-Xmx2G','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=folder,stdout=out,stderr=subprocess.STDOUT,timeout=60)
    text=log.read_text(encoding='utf-8')
    assert result.returncode!=0,label+' unexpectedly started'
    if label=='upstream-race':assert "Asgardian_Mod requires the pack's Asgardian-enabled Iourus Races JAR" in text
    else:assert 'iourusraces' in text and 'missing' in text.lower()
    print('PASS: incompatible installation rejected: '+label)

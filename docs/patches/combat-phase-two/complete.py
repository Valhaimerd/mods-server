from pathlib import Path
import json,hashlib,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-phase-two'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
artifacts=json.loads((H/'artifacts.json').read_text())
for a in artifacts:
 for root in (R/'mods',R/'mod fixing/1 fixed packs'):assert sha(root/a['jar'])==a['after']
preserved={
 'Better Revive - 2.2.0 - NeoForge - 26.2-MultiGraveFix-Java25.jar':'7968a97c78278f983c3e17c1287ba5aef66656b2899fe05234489968f78b8f01',
 'Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar':'99dfaa0963119cbc55facd5bd417d66327a7f2c0dc0275e9fd6e9ec4a2f714e3'}
for name,digest in preserved.items():assert sha(R/'mods'/name)==digest
for name in ('spatial-client-checks.txt','spatial-client-result.txt'):shutil.copy2(W/'client'/name,H/name)
shutil.copy2(W/'client/screenshots/2026-11-02_02.35.18.png',H/'purgatory-boundary.png')
review=json.loads((W/'handbook-review.json').read_text());assert not review['toc_corrections']
completion={'date':'2026-11-02','phase':2,'status':'complete','installed_locations':['mods/','mod fixing/1 fixed packs/'],'backup':'mod backup/combat-phase-two-2026-11-02','artifacts':artifacts,'preserved_repairs':preserved,'validation':{'dedicated_spatial_checks':78,'phase_one_regression_checks':196,'integrated_client_checks':9,'archive_checks':59,'handbook':review},'pdf_sha256':sha(R/'modpack-player-handbook.pdf'),'limitations':['Original affected Warrior save/loadout unavailable; repaired reproduced native/client range mismatch.','Controlled isolated native instances, not a live full-pack multiplayer playtest.','Barrage shares ordinary assist; controlled stationary near-miss fixtures verify each launched arrow, without increasing strength.'],'personal_installations_changed':False,'github_pushed':False}
(H/'completion.json').write_text(json.dumps(completion,indent=2)+'\n',encoding='utf-8')
p=R/'docs/superpowers/plans/2026-11-01-combat-rework-two-phase-handoff.md';s=p.read_text(encoding='utf-8');s=s.replace('Phase 2 is not started.','Phase 2 completed and installed on 2 November 2026; see [its completion record](../../patches/combat-phase-two/completion.json).');s=s.replace('**Status:** Deferred to the next implementation chat; not started.','**Status:** Completed and installed on 2 November 2026. See [the spatial patch](../../patches/combat-phase-two/README.md).');start=s.index('### Phase 2 completion gate');end=s.index('## Existing work',start);s=s[:start]+s[start:end].replace('- [ ]','- [x]')+s[end:];p.write_text(s,encoding='utf-8')
p=H/'ledger.md';s=p.read_text(encoding='utf-8').replace('In progress, 2 November 2026.','Completed, 2 November 2026.').replace('All work is isolated in `tmp/combat-phase-two` until tests pass.','Five tested same-filename replacements are installed in repository mods and fixed packs.').replace('The boundary uses the Priest Barrier texture/render layer on a five-block cylinder, without its buffs or defenses.','The boundary reuses the Priest Barrier entity and translucent-panel style on a five-block orange cylinder, without its buffs or defenses. A direct texture and world geometry submission avoid the native atlas/culling path that produced no visible enclosure in the client test.')
s+='''

## Completion and review

- Baseline reproduced native reach 3.0 versus expected 1.5 at scale 0.5. Final size fixtures cover 0.5 through 4.0, close targets, growth and modifier removal.
- Remote client entities do not expose the actual Stealth effect map. A dedicated synced Boolean fixes highlight eligibility without confusing ordinary invisibility; server checks still use the live effect.
- Final review found and corrected AREA filtering, inherited Priest projectile interception, and overlap-band collision above/below the wall. Their regressions pass.
- Native client rendering exposed missing geometry; direct world submission, an orange panel texture, and removal of duplicate coplanar faces produce the reviewed enclosure.
- Fresh isolated worlds prevent old failed-fixture entities from contaminating nearest-target checks. Barrage's four launched arrows use the shared assist handler with identical stationary near-miss fixtures; no Barrage-specific steering was added.
- 78 spatial server checks, 196 Phase 1 regressions, 9 client checks and 59 archive/resource checks pass. The 80-page handbook's changed page 20 and contents page were visually reviewed; 51 printed contents destinations and 471 internal links pass.
- Exact installed hashes, preservation hashes and limitations are in completion.json. No personal installation, live world, commit or GitHub push.
''';p.write_text(s,encoding='utf-8')
p=H/'README.md';s=p.read_text(encoding='utf-8');s+='\n## Completed validation\n\nInstalled five matching replacements in repository `mods/` and ignored fixed packs. **78 spatial server checks**, **196 Phase 1 regressions**, **9 integrated-client checks** and **59 archive/resource checks** passed. The [orange enclosure capture](purgatory-boundary.png) and the handbook\'s changed page were visually reviewed. The 80-page PDF retains 51 correct printed contents destinations and 471 valid internal links. [Completion record](completion.json).\n';p.write_text(s,encoding='utf-8')
# Drop the obsolete one-off generator; the final runner itself is retained.
(H/'make_regression_runner.py').unlink(missing_ok=True)
print('Completion record and handoff synchronized; grave and accessory repair hashes preserved.')

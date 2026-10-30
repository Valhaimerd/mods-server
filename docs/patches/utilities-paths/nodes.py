"""Exact approved allocation; also the runtime contract embedded in the compatibility JAR."""
F = ['9qyrqud3g16qneu1','yts2dfnkpz3xqziu','tv2lczi8824fadw6','cls3nuryikjn5pxx','tt2rbu9d7n91o1a2','o327why5r32ntmbc','1f01nhbrg46ypfdc','i0j6pbbluuqne53e']
A = ['onn0ggru4ltstcsx','axl8alff75wi5qjq','egztdm9c9i404yjo','0jtmcffuara4whxe','2lw02if80lxvfvh9','3i4j4n1jy9kq63b3','8qvbdogd9x4xoycl','s02hkhd2ha87ji1n']
I = ['2pj0fbogctx589t3','gh96zb6t67hnmswd','46o8u8oydffa04o6','gk57oho18h66ghxe','b4r7snauht7yus9f','vaios6drja2eapyg','arhes7xrvfitahm3','qxcrnvfslhwdg5ip']
REMOVED = ['yu7a75bw5anm9z2u','lkc01rrygmocbish']
SHARED = 'nu1hd3hh115xcpax'
ROOT = 'h8krq290mbf4dwdx'
REWARDS = {
 SHARED: {'stamina':.04}, F[0]:{'stamina':.08},F[2]:{'stamina':.08},
 **{F[n]:{'food':.07} for n in [1,4]}, **{F[n]:{'breath':.08} for n in [3,5,6]},
 A[0]:{'jump':.05,'fall':1},A[1]:{'fall':1},A[3]:{'fall':1},A[5]:{'jump':.05,'fall':1},
 **{A[n]:{'climb':.12,'sneak':.12} for n in [2,4]},A[6]:{'freeze':.25},
 **{I[n]:{'xp':.08} for n in [0,4]},**{I[n]:{'durability':.08} for n in [2,5]},
 **{I[n]:{'cost':.11} for n in [1,3,6]},I[7]:{'luck':1}}
CONTRACT = {'paths':[F,A,I],'removed':REMOVED,'shared':SHARED,'root':ROOT,'rewards':REWARDS}

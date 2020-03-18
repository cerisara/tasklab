import rss

s2sav = []
def save(i,s):
    if hash(s)==curhash[i]: return False
    s2sav.append(s)
    return True

flux = rss.rssAll()
try:
    with open("lasthash","r") as f: curhash = [int(s) for s in f.readlines()]
except:
    curhash = [hash("toto")]*len(flux)
newhash = []
# le RSS nous donne le plus recent en 1er
for i in range(len(flux)):
    s2sav = []
    fl = flux[i]
    cur=""
    hashdone = False
    for l in fl.split("\n"):
        if len(l)==0 and len(cur)>0:
            if not hashdone:
                newhash.append(hash(cur))
                hashdone = True
            # on s'arrete des qu'on a retrouve le plus recent de la session precedente
            if not save(i,cur): break
            cur=""
        else:
            cur=cur+l+'\n'

    if len(s2sav)>0:
        # on sauve le plus recent a la fin pour garder un ordre avec le write "append"
        s2sav = s2sav[::-1]
        with open("rssst"+str(i),"a") as f:
            for s in s2sav: f.write(s+'\n')

print("newhash",newhash)
print("curhash",curhash)
if any([newhash[i]!=curhash[i] for i in range(len(newhash))]):
    with open("lasthash","w") as f:
        for i in range(len(newhash)): f.write(str(newhash[i])+'\n')


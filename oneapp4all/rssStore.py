import rss

# ce prog est appelé depuis un CRON

s2sav = []
def save(i,s):
    # TODO place la liste des N derniers hash telecharges dans un fichier
    if hash(s)==curhash[i]: return False
    s2sav.append(s)
    return True

flux = rss.rssAll()
try:
    # curhash est un dble tableau: (NFlux,Nhash)
    # qui contient les hash des derniers RSS telecharges
    curhash = []
    with open("lasthash","r") as f:
        for l in f:
            ch = [int(s) for s in l.split(" ")]
        curhash.append(ch)
except:
    curhash = [[]]*len(flux)
newhash = []
# le RSS nous donne une liste sans garantie d'ordre chrono
for i in range(len(flux)):
    s2sav = []
    fl = flux[i]
    # fl est un array (NFlux,)
    # chaque elt de l'array est une grande string avec, pour chaque news, \n entre titre,resume,link 
    # et deux \n\n pour separer les news d'un meme flux
    cur=""
    for l in fl.split("\n"):
        if len(l)==0 and len(cur)>0:
            # ici: on a une seule news, complete, dans cur
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


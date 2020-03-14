import json
import os
from datetime import datetime

def zimbracal(js):
    now=datetime.today().strftime('%Y%m%d')
    o = json.loads(js)
    os = o['appt']
    resa = []
    for oo in os:
        nom = oo['inv'][0]['comp'][0]['name']
        deb = oo['inv'][0]['comp'][0]['s'][0]['d']
        fin = oo['inv'][0]['comp'][0]['e'][0]['d']
        dat = deb[0:8]
        if dat>=now:
            resa.append(deb+" "+fin+" "+nom)
    resa.sort()
    res = '\n'.join(resa)
    return res

def getZimbraCal():
    with open(".x.pass","r") as f: pp = f.read().strip()
    cmd="curl --user 'cerisara:"+pp+"' 'https://zimbra.inria.fr/home/cerisara/calendar?start=-2m&fmt=json' > tt"
    os.system(cmd)
    with open("tt","r") as f: s=f.read()
    return zimbracal(s)


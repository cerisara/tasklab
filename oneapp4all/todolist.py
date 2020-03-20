import os
from datetime import datetime

def todo():
    # this repo must be prepared beforehand
    os.system("cd TODO; git pull --no-edit")
    with open("TODO/todo.txt","r") as f: todos = f.readlines()
    s = ''.join(todos)
    return s

def todocal():
    # this repo must be prepared beforehand
    os.system("cd TODO; git pull --no-edit")
    now=datetime.today().strftime('%Y%m%d')
    with open("TODO/calperso.txt","r") as f: todos = f.readlines()
    res=[]
    for l in todos:
        i=l.find(' ')
        if i>=0:
            date=l[0:i]
            if date>=now: res.append(l)
    res.sort()
    s = '\n'.join(res)
    return s
 
def pushtodo(p):
    ll = p.split("£")
    print("push list %d" % (len(ll),))
    with open("TODO/todo.txt","w") as f:
        for l in ll:
            if not l.startswith("<New item>") and not l.startswith("<New Task>") and len(l)>0:
                f.write(l+'\n')
    os.system("cd TODO; git commit -am 'ok'; git push")
    return "OK"

def pushtodocal(p):
    ll = p.split("£")
    newcal=[]
    for l in ll:
        l=l.strip()
        if not l.startswith("<New item>") and not l.startswith("<New Task>") and len(l)>0:
            newcal.append(l)
    newcal.sort()
    with open("TODO/calperso.txt","r") as f: lines = f.readlines()
    now=datetime.today().strftime('%Y%m%d')
    olds=[]
    for l in lines:
        l=l.strip()
        if len(l)>8:
            d = l[0:8]
            if d<now: olds.append(l)
    al = olds+newcal
    with open("TODO/calperso.txt","w") as f:
        for l in al:
            f.write(l+'\n')
    os.system("cd TODO; git commit -am 'ok'; git push")
    return "OK"


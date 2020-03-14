import os

def todo():
    # this repo must be prepared beforehand
    os.system("cd TODO; git pull")
    with open("TODO/todo.txt","r") as f: todos = f.readlines()
    s = ''.join(todos)
    return s

def pushtodo(p):
    ll = p.split("£")
    print("push list %d" % (len(ll),))
    with open("TODO/todo.txt","w") as f:
        for l in ll:
            if not l.startswith("<New item>"):
                f.write(l+'\n')
    os.system("cd TODO; git commit -am 'ok'; git push")
    return "OK"


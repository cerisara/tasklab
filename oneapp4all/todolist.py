import os

def todo():
    # this repo must be prepared beforehand
    os.system("cd TODO; git pull")
    with open("TODO/todo.txt","r") as f: todos = f.readlines()
    s = ''.join(todos)
    return s


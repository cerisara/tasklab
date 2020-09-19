from flask import Flask, json, request
import auth
import meteo
import zimbra
import rss
from os import listdir
import os.path

api = Flask(__name__)

state = "init"
fichs = []

def getauth():
    code = request.args.get('auth')
    isauth = auth.checkauth(code)
    if not isauth:
        return "<p>bad auth</p>"
    else:
        return ""

@api.route('/menu', methods=['POST'])
def menu():
    r=getauth()
    if r!="": return r
    return getmenu()

@api.route('/select', methods=['POST'])
def select():
    r=getauth()
    if r!="": return r
    txt = request.values.get('txt')
    return select(txt)

def select(txt):
    try:
        pos = txt.strip()
        if state=="menu":
            if pos=="0": return getmeteo()
            if pos=="1": return getmails()
            if pos=="2": return getrss("f3")
            if pos=="3": return getrss("zd")
            if pos=="4": return getrss("hn")
            if pos=="5": return getdir("/home/cerisara/progs")
            return "ERRORI"
        if state.startswith("dir "): return selfile(pos)
        if state.startswith("fic "): return editfile(pos)
    except:
        return "ERROR"

def editfile(pos):
    global state
    if pos=="1": 
        curdir = state[4:]
        parentdir,_ = os.path.split(curdir)
        return getdir(parentdir)
    return "ERROREF"

def selfile(pos):
    global state
    i = int(pos)
    curdir = state[4:]
    if i==len(fichs):
        parentdir,_ = os.path.split(curdir)
        if len(parentdir)<=len("/home/cerisara/"): return getdir(curdir)
        return getdir(parentdir)
    if i>len(fichs) or i<0: return "ERRORDIR"
    curfile = curdir+"/"+fichs[i]
    if os.path.isdir(curfile): return getdir(curfile)
    state = "fic "+curfile
    with open(curfile) as f: lines = f.read()
    return lines+"\n_\n(back)\n"

def getmenu():
    global state
    state = "menu"
    s="METEO\n_n\n" + "MAILS\n_n\n" + "FRANCE 3\n_n\n" + "ZDnet\n_n\n" + "HackNews\n_n\n" + "SentEval\n"
    return s

def getdir(s):
    global state, fichs
    state = "dir "+s
    fichs = listdir(s)
    r = '\n_n\n'.join(fichs) + "\n_n\n(back)\n"
    return r

def getrss(s):
    global state
    state = "rss"
    if s=="f3": return rss.rssF3()
    if s=="zd": return rss.rssZDnet()
    if s=="hn": return rss.rssHN()
    return "ERRORR"
 
def getmeteo():
    global state
    state = "meteo"
    s = meteo.meteo()
    return s
 
def getmails():
    global state
    state = "mails"
    return zimbra.getZimbraMailAsRSS()
    
# ====================

if __name__ == '__main__':
    # this is the port of xolki.duckdns.org on talc2
    api.run(host="0.0.0.0", port=7937)


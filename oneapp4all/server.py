from flask import Flask, json, request
import auth
import meteo
import zimbra
import rss

api = Flask(__name__)

state = "init"

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
            return "ERRORI"
    except:
        return "ERROR"

def getmenu():
    global state
    state = "menu"
    s="METEO\n_n\n" + "MAILS\n_n\n" + "FRANCE 3\n_n\n" + "ZDnet\n_n\n" + "HackNews\n"
    return s

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


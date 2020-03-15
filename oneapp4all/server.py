from flask import Flask, json, request
import auth
import rss
import meteo
import todolist
import zimbra

api = Flask(__name__)

def getauth():
    code = request.args.get('auth')
    isauth = auth.checkauth(code)
    if not isauth:
        return "<p>bad auth</p>"
    else:
        return ""

@api.route('/meteo', methods=['GET'])
def rss_meteo():
    r=getauth()
    if r!="": return r
    return meteo.meteo()

@api.route('/rssf3', methods=['GET'])
def rss_france3():
    r=getauth()
    if r!="": return r
    return rss.rssF3()

@api.route('/rssf3link', methods=['GET'])
def rss_france3link():
    r=getauth()
    if r!="": return r
    link = request.values.get('link')
    return rss.france3link(link)

@api.route('/rsszdnet', methods=['GET'])
def rss_zdnet():
    r=getauth()
    if r!="": return r
    return rss.rssZDnet()

@api.route('/rsszdnetlink', methods=['GET'])
def rss_zdnetlink():
    r=getauth()
    if r!="": return r
    link = request.values.get('link')
    return rss.zdnetlink(link)

@api.route('/rsshn', methods=['GET'])
def rss_hn():
    r=getauth()
    if r!="": return r
    return rss.rssHN()

@api.route('/rsshnlink', methods=['GET'])
def rss_hnlink():
    r=getauth()
    if r!="": return r
    link = request.values.get('link')
    return rss.hnlink(link)

@api.route('/todo', methods=['GET'])
def todo_list():
    r=getauth()
    if r!="": return r
    return todolist.todo()

@api.route('/todocal', methods=['GET'])
def todo_cal():
    r=getauth()
    if r!="": return r
    return todolist.todocal()

@api.route('/pushtodo', methods=['POST'])
def push_todo_list():
    r=getauth()
    if r!="": return r
    parms = request.values.get('txt')
    return todolist.pushtodo(parms)

@api.route('/pushtodocal', methods=['POST'])
def push_todo_cal():
    r=getauth()
    if r!="": return r
    parms = request.values.get('txt')
    return todolist.pushtodocal(parms)

@api.route('/zimbracal', methods=['GET'])
def zimbra_cal():
    r=getauth()
    if r!="": return r
    return zimbra.getZimbraCal()

if __name__ == '__main__':
    # this is the port of xolki.duckdns.org on talc2
    api.run(host="0.0.0.0", port=7937)


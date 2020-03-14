from flask import Flask, json, request
import auth
import rss
import todolist

api = Flask(__name__)

"""
companies = [{"id": 1, "name": "Company One"}, {"id": 2, "name": "Company Two"}]

@api.route('/c', methods=['GET'])
def get_companies():
  return json.dumps(companies)

@api.route('/c', methods=['POST'])
def post_companies():
	language = request.args.get('l')
	return json.dumps({"success": True, "lg":language}), 201
"""

def getauth():
    code = request.args.get('auth')
    isauth = auth.checkauth(code)
    if not isauth:
        return "<p>bad auth</p>"
    else:
        return ""

@api.route('/rssf3', methods=['GET'])
def rss_france3():
    r=getauth()
    if r!="": return r
    return rss.rssF3()

@api.route('/rsszdnet', methods=['GET'])
def rss_zdnet():
    r=getauth()
    if r!="": return r
    return rss.rssZDnet()

@api.route('/rsshn', methods=['GET'])
def rss_hn():
    r=getauth()
    if r!="": return r
    return rss.rssHN()

@api.route('/todo', methods=['GET'])
def todo_list():
    r=getauth()
    if r!="": return r
    return todolist.todo()

@api.route('/pushtodo', methods=['POST'])
def push_todo_list():
    r=getauth()
    if r!="": return r
    parms = request.values.get('txt')
    return todolist.pushtodo(parms)

if __name__ == '__main__':
    # this is the port of xolki.duckdns.org on talc2
    api.run(host="0.0.0.0", port=7937)


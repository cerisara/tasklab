import os
import urllib.request

def meteo():
    with open("meteoapi.txt","r") as f: u = f.read()
    u=u.strip()
    with urllib.request.urlopen(u) as f: html = f.read()
    html = html.decode('utf8')
    html = html.split("\n")
    dates,pl = [],[]
    for l in html:
        if l.startswith("echeance (date/heure)"):
            i=l.find(',')
            dates=l[i+1:].split(',')
        elif l.startswith("pluie,"):
            pl=l[6:].split(',')
            break
    s=""
    for i in range(len(dates)):
        s+=dates[i]+" "+pl[i]+"\n"
    return s


import os
import urllib.request
from datetime import datetime, timedelta
from dateutil import parser
from dateutil import tz

def meteo():
    loczone = tz.gettz('Europe/Paris')
    now=datetime.today()
    now = now - timedelta(hours=3)
    now = now.replace(tzinfo=loczone)

    with open("meteoapi.txt","r") as f: u = f.read()
    u=u.strip()
    with urllib.request.urlopen(u) as f: html = f.read()
    html = html.decode('utf8')
    html = html.split("\n")
    dates,pl,neb = [],[],[]
    for l in html:
        if l.startswith("echeance (date/heure)"):
            i=l.find(',')
            dates=l[i+1:].split(',')
        elif l.startswith("pluie,"):
            pl=l[6:].split(',')
        elif l.startswith("nebulosite:totale,"):
            neb=l[18:].split(',')
    s=""
    for i in range(len(dates)):
        d = parser.parse(dates[i])
        dd = d.astimezone(loczone)
        if dd>now:
            ddd =dd.strftime('%Y%m%d_%H')
            s+=ddd+" "+pl[i]+" "+neb[i]+"\n"
    return s


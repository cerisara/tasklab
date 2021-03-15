import json
import os
from datetime import datetime
from rss import showFeed
import feedparser

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

def putZimbraCal():
    with open(".x.pass","r") as f: pp = f.read().strip()
    cmd="curl --user 'cerisara:"+pp+"' --upload-file /tmp/t.ics 'https://zimbra.inria.fr/home/cerisara/calendar?fmt=ics'"
    os.system(cmd)
    return "done"

def getZimbraCal():
    with open(".x.pass","r") as f: pp = f.read().strip()
    cmd="curl --user 'cerisara:"+pp+"' 'https://zimbra.inria.fr/home/cerisara/calendar?start=-2m&fmt=json' > tt"
    os.system(cmd)
    with open("tt","r") as f: s=f.read()
    return zimbracal(s)

def getZimbraMail(pos):
    import email
    with open("ttt","r") as f: s=f.readlines()
    mailid=s[pos]
    with open(".x.pass","r") as f: pp = f.read().strip()
    cmd="curl --user 'cerisara:"+pp+"' 'https://zimbra.inria.fr/home/cerisara/?id="+mailid+"' > t4"
    os.system(cmd)

    with open("t4","r") as f: s=f.readlines()
    a = '\n'.join(s)
    b = email.message_from_string(a)
    body = ""
    if b.is_multipart():
        for part in b.walk():
            ctype = part.get_content_type()
            cdispo = str(part.get('Content-Disposition'))

            # skip any text/plain (txt) attachments
            if ctype == 'text/plain' and 'attachment' not in cdispo:
                body = part.get_payload(decode=True)  # decode
                break
    # not multipart - i.e. plain text, no attachments, keeping fingers crossed
    else:
        body = b.get_payload(decode=True)
    return body

def getZimbraMailAsRSS():
    with open(".x.pass","r") as f: pp = f.read().strip()
    cmd="curl --user 'cerisara:"+pp+"' 'https://zimbra.inria.fr/home/cerisara/inbox.rss' > tt"
    os.system(cmd)
    cmd="curl --user 'cerisara:"+pp+"' 'https://zimbra.inria.fr/home/cerisara/inbox?fmt=xml' | xmllint --format - | grep ' id=' | grep '<m ' | sed 's,.* id=\",,g' | cut -d'\"' -f1 > ttt"
    os.system(cmd)
    with open("tt","r") as f: s=f.read()
    feed = feedparser.parse(s)
    res = []
    for it in feed['items']:
        res.append((it["title"],it["author"],it["summary"]))
    return showFeed(res)


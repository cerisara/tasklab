import feedparser
import urllib.request
from bs4 import BeautifulSoup

def cleanhtml(html):
    soup = BeautifulSoup(html)

    # kill all script and style elements
    for script in soup(["script", "style"]):
        script.extract()    # rip it out

    # get text
    text = soup.get_text()

    # break into lines and remove leading and trailing space on each
    lines = (line.strip() for line in text.splitlines())
    # break multi-headlines into a line each
    chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
    # drop blank lines
    text = '\n'.join(chunk for chunk in chunks if chunk)
    return text

def france3link(link):
    with urllib.request.urlopen(link) as f: html = f.read()
    html = html.decode('utf8')
    i = html.find('text-chapo--text')
    html = html[i:]
    i = html.find('>')
    html = html[i+1:]
    return cleanhtml(html)

def hnlink(link):
    with urllib.request.urlopen(link) as f: html = f.read()
    html = html.decode('utf8')
    ls = html.split("\n")
    for i in range(len(ls)-1):
        if ls[i].find("score")>=0:
            hh = ls[i+1]
            return cleanhtml(hh)
    # ce n'est pas un lien hackernews, mais un lien general
    return cleanhtml(html)

def zdnetlink(link):
    with urllib.request.urlopen(link) as f: html = f.read()
    html = html.decode('utf8')
    i = html.find("relatedContent")
    html = html[i:]
    i = html.find("div")
    html = html[i+4:]
    return cleanhtml(html)

def getFeed(url):
    feed = feedparser.parse(url)
    res = []
    for it in feed['items']:
        res.append((it["title"],it["summary"],it["link"]))
    return res

def showFeed(feed):
    res=""
    for t,s,l in feed:
        t = t.replace('\n','')
        s = s.replace('\n','')
        l = l.replace('\n','')
        t = t.replace('<p>','')
        s = s.replace('<p>','')
        l = l.replace('<p>','')
        t = t.replace('</p>','')
        s = s.replace('</p>','')
        l = l.replace('</p>','')
        res=res+t+"\n"+s+"\n"+l+"\n\n"
    return res 

def rssF3():
    url = "https://france3-regions.francetvinfo.fr/grand-est/actu/rss"
    feed = getFeed(url)
    return showFeed(feed)

def rssZDnet():
    url = "https://www.zdnet.com/news/rss.xml"
    feed = getFeed(url)
    return showFeed(feed)

def rssHN():
    url = "https://hnrss.org/newest"
    feed = getFeed(url)
    return showFeed(feed)


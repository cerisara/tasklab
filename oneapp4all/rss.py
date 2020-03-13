import feedparser

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


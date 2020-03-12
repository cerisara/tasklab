import feedparser

def getFeed(url):
    feed = feedparser.parse(url)
    res = []
    for it in feed['items']:
        res.append((it["title"],it["summary"],it["link"]))

def rssF3():
    url = "https://france3-regions.francetvinfo.fr/grand-est/actu/rss"
    return getFeed(url)

def rssZDnet():
    url = "https://www.zdnet.com/news/rss.xml"
    return getFeed(url)


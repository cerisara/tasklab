import feedparser

url = "https://www.zdnet.com/news/rss.xml"
url = "https://france3-regions.francetvinfo.fr/grand-est/actu/rss"
feed = feedparser.parse(url)
for it in feed['items']:
    print(it["title"])
    print(it["summary"])
    print(it["link"])
    print()


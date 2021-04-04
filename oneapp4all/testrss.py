#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import feedparser
import jsonpickle
from datetime import datetime

rsslist = ("https://www.france24.com/en/rss",
        "http://feeds.bbci.co.uk/news/world/rss.xml",
        "https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/world/rss.xml",
        "http://yahoo.com/news/rss/world",
        "https://www.theguardian.com/world/rss",
        "http://abcnews.go.com/abcnews/internationalheadlines",
        "https://sputniknews.com/export/rss2/world/index.xml",
        "https://www.cbsnews.com/latest/rss/world",
        "http://www.independent.co.uk/news/world/rss",
        "https://www.abc.net.au/news/feed/52278/rss.xml",
        "https://www.thesun.co.uk/news/worldnews/feed/",
        "https://www.euronews.com/rss?level=theme&name=news",
        "http://feeds.feedburner.com/daily-express-world-news",
        "https://www.ctvnews.ca/rss/world/ctvnews-ca-world-public-rss-1.822289",
        "https://www.scmp.com/rss/91/feed",
        "http://www.channelnewsasia.com/rssfeeds/8395884",
        "http://www.thestar.com/content/thestar/feed.RSSManagerServlet.articles.news.world.rss",
        "https://www.todayonline.com/feed/world",
        "https://www.dailytelegraph.com.au/news/world/rss",
        "https://feeds.breakingnews.ie/bnworld",
        "https://247newsaroundtheworld.com/feed")

rssurl = rsslist[0]

# Création d'une instance
feed = feedparser.parse(rssurl)

with open("f24.txt","w") as writer:
    for entry in feed.entries:
        title = entry['title']
        sumry = entry['summary']
        today = str(datetime.now())
        # s = jsonpickle.encode(entry)
        writer.write("TODAY "+today+'\n')
        writer.write("TITLE "+title+'\n')
        writer.write("SUMRY "+sumry+'\n')

exit()

# Propriétés du flux
print(news_feed.feed.keys())

# Titre du flux
print("Feed Title:", news_feed.feed.title) 

# Sous-titre du flux
print("Feed Subtitle:", news_feed.feed.subtitle)

# Lien du flux
print("Feed Link:", news_feed.feed.link, "\n")

# Propriétés de chaque item du flux
print(news_feed.entries[0].keys())

for entry in news_feed.entries:
    print(f"{entry.title} --> {entry.link}")
    
# Récupération du deernier feed
for i in range(0, len(news_feed.entries)):
    if i == (len(news_feed.entries)-1):
        print("Alert: {} \nLink: {}".format(news_feed.entries[0]['title'], news_feed.entries[0]['id']))

exit()

"""
- BBC (23/day) 
RSS http://feeds.bbci.co.uk/news/world/rss.xml
- NYT (30/day) 
RSS https://www.nytimes.com/svc/collections/v1/publish/https://www.nytimes.com/section/world/rss.xml
- Yahoo (28/day) 
RSS http://yahoo.com/news/rss/world
- Guardian (30/day) 
RSS https://www.theguardian.com/world/rss
- ABC News (30/day) 
RSS http://abcnews.go.com/abcnews/internationalheadlines
- Sputnik (30/day) 
RSS https://sputniknews.com/export/rss2/world/index.xml
- CBS (30/day) 
RSS https://www.cbsnews.com/latest/rss/world
- The Independent (30/day) 
RSS http://www.independent.co.uk/news/world/rss
- ABC Australia (30/day) 
RSS https://www.abc.net.au/news/feed/52278/rss.xml
- Sun (20/day) 
RSS https://www.thesun.co.uk/news/worldnews/feed/
- EuroNews (22/day) 
RSS https://www.euronews.com/rss?level=theme&name=news
- DailyExpress (30/day) 
RSS http://feeds.feedburner.com/daily-express-world-news
- CTV Canada (21/day) 
RSS https://www.ctvnews.ca/rss/world/ctvnews-ca-world-public-rss-1.822289
- France 24 (26/day) 
RSS https://www.france24.com/en/rss
- China Morning Post (30/day) 
RSS https://www.scmp.com/rss/91/feed
- News Asia (30/day) 
RSS http://www.channelnewsasia.com/rssfeeds/8395884
- Toronto Star (30/day) 
RSS http://www.thestar.com/content/thestar/feed.RSSManagerServlet.articles.news.world.rss
- Today Singapore (30/day) 
RSS https://www.todayonline.com/feed/world
- Daily Telegraph Australia (30/day) 
RSS https://www.dailytelegraph.com.au/news/world/rss
- Breaking News Ireland (30/day) 
RSS https://feeds.breakingnews.ie/bnworld
- 247 News (30/day) 
RSS https://247newsaroundtheworld.com/feed
"""


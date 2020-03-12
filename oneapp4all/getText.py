import urllib.request
from bs4 import BeautifulSoup

"""
# url = "http://news.bbc.co.uk/2/hi/health/2284783.stm"
url = "https://france3-regions.francetvinfo.fr/grand-est/vosges/gerardmer/municipales-2020-gerardmer-se-cherche-conscience-ecolo-1797498.html"
with urllib.request.urlopen(url) as f:
    html = f.read()
"""

def france3():
    with open("arrivee-du-tableau-christ-enfants-vic-seille-1797712.html","r") as f: ss=f.readlines()
    html = '\n'.join(ss)
    i = html.find('text-chapo--text')
    html = html[i:]
    i = html.find('>')
    html = html[i+1:]
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

    print(text)

def zdnet():
    with open("index.html","r") as f: ss=f.readlines()
    html = '\n'.join(ss)
    i = html.find("relatedContent")
    html = html[i:]
    i = html.find("div")
    html = html[i+4:]
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

    print(text)

zdnet()


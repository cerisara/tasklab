import urllib

def search(term):
    url="http://export.arxiv.org/api/query?search_query=all:"+term
    data = urllib.urlopen(url).read()
    print(data)

search("GAN")


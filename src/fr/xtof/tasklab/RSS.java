package fr.xtof.tasklab;

import java.util.ArrayList;

public class RSS {
    ArrayList titles = new ArrayList();
    ArrayList summary = new ArrayList();
    ArrayList links = new ArrayList();
    public int source = 0;
    // 0: France 3
    // 1: ZDNet
    // 2: HackerNews

    public RSS(int src) {
        source=src;
    }

    public void parse(String s) {
        // TODO: do better than erase all old stuff
        titles.clear();
        summary.clear();
        links.clear();

        String[] ss = s.split("\n");
        for (int i=0;i+3<ss.length;i+=4) {
            titles.add(ss[i]);
            summary.add(ss[i+1]);
            links.add(ss[i+2]);
        }
        System.out.println("detson rss list "+titles.size());
    }

    public int getNitems() {return titles.size();}
    public String getTitle(int i) {
        return (String)titles.get(i);
    }
    public String getSummary(int i) {
        return (String)summary.get(i);
    }
    public String getLinks(int i) {
        return (String)links.get(i);
    }
}


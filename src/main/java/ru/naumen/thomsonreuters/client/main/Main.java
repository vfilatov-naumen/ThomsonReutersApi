package ru.naumen.thomsonreuters.client.main;

import com.typesafe.config.Config;

import static ru.naumen.thomsonreuters.client.main.ConfigHolder.cfg;

public class Main {

    // описание параметров в application.conf
    public static void main(String[] args) {
        WokSearcher searcher = new WokSearcher();
        if (cfg.hasPath("search.query"))
            searchQuery(searcher);
        if (cfg.hasPath("search.one"))
            searchOne(searcher);
        if (cfg.hasPath("search.full"))
            searchFull(searcher);
        searcher.closeSession();
    }

    private static void searchQuery(WokSearcher searcher) {
        System.out.println("Begin search.query");
        int i = 1;
        for (Config searchCfg : cfg.getConfigList("search.query")) {
            WokSearcher.SearchResult result = searcher.query(searchCfg);
            System.out.println(result);
            System.out.println("results: " + i++);
        }
    }

    private static void searchOne(WokSearcher searcher) {
        System.out.println("Begin search.one");
        int i = 1;
        for (Config searchCfg : cfg.getConfigList("search.one")) {
            WokSearcher.SearchResult result = searcher.one(
                    searchCfg.getString("databaseId"),
                    searchCfg.getString("language"),
                    searchCfg.getStringList("uids"));
            System.out.println(result);
            System.out.println("results: " + i++);
        }
    }

    private static void searchFull(WokSearcher searcher) {
        System.out.println("Begin search.full");
        searcher.full(cfg.getConfig("search.full"));
    }
}

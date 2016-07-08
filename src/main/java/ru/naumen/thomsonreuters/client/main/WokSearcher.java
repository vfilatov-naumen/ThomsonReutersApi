package ru.naumen.thomsonreuters.client.main;

import com.typesafe.config.Config;
import ru.naumen.thomsonreuters.client.search.*;

import javax.xml.ws.BindingProvider;
import java.util.List;

public class WokSearcher extends Auth {

    private final WokSearch service;

    public WokSearcher() {
        super();
        service = new WokSearchService().getWokSearchPort();
        withAuth((BindingProvider)service);
    }

    public void full(Config cfg) {
        int i = 1;
        int pageSize = cfg.getInt("retrieve.count");

        System.out.println(query(cfg));
        System.out.println("results: " + i++);

        SearchResult res;
        while ((res = nextPage("1", i, pageSize)) != null) {
            System.out.println(res);
            System.out.println("results: " + i++);
        }
    }

    public SearchResult query(Config cfg) {
        QueryParameters qp = new QueryParameters();
        qp.setUserQuery(cfg.getString("query"));
        qp.setQueryLanguage(cfg.getString("language"));
        qp.setDatabaseId(cfg.getString("databaseId"));

        if (cfg.hasPath("timeSpan")) {
            TimeSpan timeSpan = new TimeSpan();
            timeSpan.setBegin(cfg.getString("timeSpan.begin"));
            timeSpan.setEnd(cfg.getString("timeSpan.end"));
            qp.setTimeSpan(timeSpan);
        }

        if (cfg.hasPath("editions")) {
            for (Config editionCfg : cfg.getConfigList("editions")) {
                EditionDesc edition = new EditionDesc();
                edition.setCollection(editionCfg.getString("collection"));
                edition.setEdition(editionCfg.getString("edition"));
                qp.getEditions().add(edition);
            }
        }

        RetrieveParameters rp = new RetrieveParameters();
        rp.setFirstRecord(cfg.getInt("retrieve.first"));
        rp.setCount(cfg.getInt("retrieve.count"));

        if (cfg.hasPath("retrieve.options")) {
            for (Config optCfg : cfg.getConfigList("retrieve.options")) {
                KeyValuePair option = new KeyValuePair();
                option.setKey(optCfg.getString("key"));
                option.setValue(optCfg.getString("value"));
                rp.getOption().add(option);
            }
        }

        try {
            return new SearchResult(service.search(qp, rp));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SearchResult one(String databaseId, String language, List<String> uids) {
        try {
            RetrieveParameters rp = new RetrieveParameters();
            rp.setFirstRecord(1);
            rp.setCount(uids.size());
            return new SearchResult(service.retrieveById(databaseId, uids, language, rp));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private SearchResult nextPage(String queryId, int page, int count) {
        RetrieveParameters rp = new RetrieveParameters();
        rp.setFirstRecord(page * count);
        rp.setCount(count);
        try {
            return new SearchResult(service.retrieve(queryId, rp));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public final class SearchResult {

        private FullRecordSearchResults res;
        private FullRecordData data;

        public SearchResult(FullRecordSearchResults res) {
            this.res = res;
            this.data = null;
        }

        public SearchResult(FullRecordData data) {
            this.res = null;
            this.data = data;
        }

        @Override
        public String toString() {
            if (res != null)
                return "SearchResult [\n" +
                        "queryId: " + res.getQueryId() + "\n" +
                        "recordsFound: " + res.getRecordsFound() + "\n" +
                        "recordsSearched: " + res.getRecordsSearched() + "\n" +
                        "parent: " + res.getParent() + "\n" +
                        "optionValue: " + res.getOptionValue() + "\n" +
                        "records: " + res.getRecords() + "\n" + "]";
            else return "SearchData [\n" +
                    "optionValue: " + data.getOptionValue() + "\n" +
                    "records: " + data.getRecords() + "\n" + "]";
        }
    }
}

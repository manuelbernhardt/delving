package eu.europeana.core.querymodel.query;

import org.apache.solr.client.solrj.SolrQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Track back to where you came from
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public class Breadcrumb {
    private static final String FACET_PROMPT = "&qf=";
    private String href;
    private String display;
    private String field;
    private String value;
    private boolean last;

    public Breadcrumb(String href, String display) {
        this.href = href;
        this.display = display;
    }

    public Breadcrumb(String href, String display, String field, String value) {
        this.href = href;
        this.display = display;
        this.field = field;
        this.value = value;
    }

    private void flagAsLast() {
        this.last = true;
    }

    public String getDisplay() {
        return display;
    }

    public String getHref() {
        return href;
    }

    public boolean isLast() {
        return last;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return "<a href=\"" + href + "\">" + display + "</a>";
    }

    public static List<Breadcrumb> createList(SolrQuery solrQuery) throws EuropeanaQueryException {
        if (solrQuery.getQuery() == null) {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_QUERY.toString());
        }
        List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
        String prefix = "query=" + encode(solrQuery.getQuery());
        breadcrumbs.add(new Breadcrumb(prefix, solrQuery.getQuery(), "", solrQuery.getQuery()));
        if (solrQuery.getFilterQueries() != null) {
            int facetQueryCount = solrQuery.getFilterQueries().length;
            for (int walk = 0; walk < facetQueryCount; walk++) {
                StringBuilder out = new StringBuilder(prefix);
                int count = walk;
                for (String facetTerm : SolrQueryUtil.getFilterQueriesWithoutPhrases(solrQuery)) {
                    if (facetTerm.contains(":")) {
                        int colon = facetTerm.indexOf(":");
                        String facetName = facetTerm.substring(0, colon);
                        String facetValue = facetTerm.substring(colon + 1);
                        appendToURI(out, facetName, facetValue);
                        if (count-- == 0) {
                            breadcrumbs.add(new Breadcrumb(out.toString(), facetName + ":" + facetValue, facetName, facetValue));
                            break;
                        }
                    }
                }
            }
        }
        breadcrumbs.get(breadcrumbs.size() - 1).flagAsLast();
        return breadcrumbs;

    }

    private static void appendToURI(StringBuilder uri, String name, String value) {
        uri.append(FACET_PROMPT).append(name).append(":").append(encode(value));
    }

    private static String encode(String value) {
        if (value == null) {
            throw new RuntimeException("Cannot encode null value!");
        }
        try {
            return URLEncoder.encode(value, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}

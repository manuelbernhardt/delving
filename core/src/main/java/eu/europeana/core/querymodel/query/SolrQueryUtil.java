package eu.europeana.core.querymodel.query;

import org.apache.solr.client.solrj.SolrQuery;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 20, 2010 5:41:56 PM
 */
public class SolrQueryUtil {

    private static final int RANDOM_RANGE = 1000;

    public static String[] getFilterQueriesAsPhrases(SolrQuery solrQuery) {
        String[] filterQueries = solrQuery.getFilterQueries();
        if (filterQueries == null) {
            return null;
        }
        List<String> phraseFilterQueries = new ArrayList<String>(filterQueries.length);
        for (String facetTerm : filterQueries) {
            if (facetTerm.contains(":")) {
                int colon = facetTerm.indexOf(":");
                String facetName = facetTerm.substring(0, colon);
                String facetValue = facetTerm.substring(colon + 1);
                phraseFilterQueries.add(MessageFormat.format("{0}:\"{1}\"", facetName, facetValue));
            }
        }
        return phraseFilterQueries.toArray(new String[phraseFilterQueries.size()]);
    }

    public static String[] getFilterQueriesWithoutPhrases(SolrQuery solrQuery) {
        String[] filterQueries = solrQuery.getFilterQueries();
        if (filterQueries == null) {
            return null;
        }
        List<String> nonPhraseFilterQueries = new ArrayList<String>(filterQueries.length);
        for (String facetTerm : filterQueries) {
            if (facetTerm.contains(":")) {
                int colon = facetTerm.indexOf(":");
                String facetName = facetTerm.substring(0, colon);
                if (facetName.contains("!tag")) {
                    facetName = facetName.replaceFirst("\\{!tag=.*?\\}", "");
                }
                String facetValue = facetTerm.substring(colon + 1);
                if (facetValue.length() >= 2 && facetValue.startsWith("\"") && facetValue.endsWith("\"")) {
                    facetValue = facetValue.substring(1, facetValue.length() - 1);
                }
                nonPhraseFilterQueries.add(MessageFormat.format("{0}:{1}", facetName, facetValue));
            }
        }
        return nonPhraseFilterQueries.toArray(new String[nonPhraseFilterQueries.size()]);
    }

    /*
    * Transform "LANGUAGE:en, LANGUAGE:de, PROVIDER:"The European Library" "  to "{!tag=lang}LANGUAGE:(en OR de), PRODIVER:"The European Library" "
    */

    public static String[] getFilterQueriesAsOrQueries(SolrQuery solrQuery, Map<String, String> facetMap) {
        String[] filterQueries = solrQuery.getFilterQueries();
        if (filterQueries == null) {
            return null;
        }
        String[] sortedFilterQueries = filterQueries.clone();
        Arrays.sort(sortedFilterQueries);
        Map<String, List<String>> terms = new TreeMap<String, List<String>>();
        for (String facetTerm : sortedFilterQueries) {
            int colon = facetTerm.indexOf(":");
            String facetName = facetTerm.substring(0, colon);
            if (facetMap.containsKey(facetName)) {
                String facetPrefix = facetMap.get(facetName);
                facetName = String.format("{!tag=%s}%s", facetPrefix, facetName);
            }
            String facetValue = facetTerm.substring(colon + 1);
            List<String> values = terms.get(facetName);
            if (values == null) {
                terms.put(facetName, values = new ArrayList<String>());
            }
            values.add(facetValue);
        }
        List<String> queries = new ArrayList<String>(sortedFilterQueries.length);
        for (Map.Entry<String, List<String>> entry : terms.entrySet()) {
            String facetName = entry.getKey();
            String facetValue;
            if (entry.getValue().size() == 1) {
                facetValue = entry.getValue().get(0);
            }
            else {
                StringBuilder orStatement = new StringBuilder("(");
                Iterator<String> walk = entry.getValue().iterator();
                while (walk.hasNext()) {
                    String value = walk.next();
                    orStatement.append(value);
                    if (walk.hasNext()) {
                        orStatement.append(" OR ");
                    }
                }
                orStatement.append(")");
                facetValue = orStatement.toString();
            }
            queries.add(MessageFormat.format("{0}:{1}", facetName, facetValue));
        }
        return queries.toArray(new String[queries.size()]);
    }

    public static SolrQuery createFromQueryParams(Map<String, String[]> params, QueryAnalyzer queryAnalyzer) throws EuropeanaQueryException {
        SolrQuery solrQuery = new SolrQuery();
        if (params.containsKey("query") || params.containsKey("query1")) {
            if (!params.containsKey("query")) {  // support advanced search
                solrQuery.setQuery(queryAnalyzer.createAdvancedQuery(params));
            }
            else {
                if (params.containsKey("zoeken_in") && !params.get("zoeken_in")[0].equalsIgnoreCase("text")) {
                    String zoekenIn = params.get("zoeken_in")[0];
                    solrQuery.setQuery(zoekenIn + ":\"" + queryAnalyzer.sanitize(params.get("query")[0]) + "\""); // only get the first one
                }
                else {
                    solrQuery.setQuery(queryAnalyzer.sanitize(params.get("query")[0])); // only get the first one
                }
            }
        }
        else {
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_QUERY.toString());
        }
        if (solrQuery.getQuery().trim().length() == 0) { // throw exception when no query is specified
            throw new EuropeanaQueryException(QueryProblem.MALFORMED_QUERY.toString());
        }
        if (params.containsKey("start")) {
            try {
                Integer start = Integer.valueOf(params.get("start")[0]);
                solrQuery.setStart(start);
            } catch (NumberFormatException e) {
                // if number exception is thrown take default setting 0 (hardening parameter handling)
            }
        }
        if (params.containsKey("rows")) {
            try {
                Integer rows = Integer.valueOf(params.get("rows")[0]);
                solrQuery.setRows(rows);
            } catch (NumberFormatException e) {
                // number exception is thrown take default setting 12 (hardening parameter handling)
            }
        }
        solrQuery.setQueryType(queryAnalyzer.findSolrQueryType(solrQuery.getQuery()).toString());

        // set sort field
        if (params.containsKey("sortBy") && !params.get("sortBy")[0].isEmpty()) {
            String sortField = params.get("sortBy")[0];
            if (sortField.equalsIgnoreCase("title")) {
                sortField = "title_sort";
            }
            else if (sortField.equalsIgnoreCase("creator")) {
                sortField = "creator_sort";
            }
            if (params.containsKey("sortOrder") && !params.get("sortOrder")[0].isEmpty()) {
                String sortOrder = params.get("sortOrder")[0];
                if (sortOrder.equalsIgnoreCase("desc")) {
                    solrQuery.setSortField(sortField, SolrQuery.ORDER.desc);
                }
            }
            else {
                solrQuery.setSortField(sortField, SolrQuery.ORDER.asc);
            }
        }

        //set constraints
        final String[] filterQueries = params.get("qf");
        if (filterQueries != null) {
            for (String filterQuery : filterQueries) {
                solrQuery.addFilterQuery(filterQuery);
            }
        }
        // find rq and add to filter queries
        if (params.containsKey("rq") && params.get("rq").length != 0) {
            String refineSearchFilterQuery = queryAnalyzer.createRefineSearchFilterQuery(params);
            if (!refineSearchFilterQuery.isEmpty()) {
                solrQuery.addFilterQuery(refineSearchFilterQuery);
            }
        }
        solrQuery.setFilterQueries(SolrQueryUtil.getFilterQueriesAsPhrases(solrQuery));
        return solrQuery;
    }

    public static int createRandomNumber() {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(RANDOM_RANGE);
    }

    public static String createRandomSortKey() {
        return "random_" + createRandomNumber();
    }
}

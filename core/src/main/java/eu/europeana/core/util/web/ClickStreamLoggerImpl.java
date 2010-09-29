/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.core.util.web;

import eu.europeana.core.database.domain.StaticPageType;
import eu.europeana.core.database.domain.User;
import eu.europeana.core.querymodel.query.BriefBeanView;
import eu.europeana.core.querymodel.query.DocIdWindowPager;
import eu.europeana.core.querymodel.query.FullBeanView;
import eu.europeana.definitions.domain.Language;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.joda.time.DateTime;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Map;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class ClickStreamLoggerImpl implements ClickStreamLogger {
    private Logger log = Logger.getLogger(getClass());
    private static String VERSION = "1.0";

    @Override
    public void logUserAction(HttpServletRequest request, UserAction action, ModelAndView model) {
        log.info(
                MessageFormat.format(
                        "[action={0}, view={1}, {2}]",
                        action, model.getViewName(), printLogAffix(request)));
    }

    /**
     * This method is used the basic information from the <code>HttpServletRequest<code>
     * (@See <code>printLogAffix</code> )
     *
     * @param request the HttpServletRequest from the controller
     * @param action  the UserAction performed in the controller
     */

    @Override
    public void logUserAction(HttpServletRequest request, UserAction action) {
        log.info(
                MessageFormat.format(
                        "[action={0}, {1}]",
                        action, printLogAffix(request)));
    }

    @Override
    public void logCustomUserAction(HttpServletRequest request, UserAction action, String logString) {
        log.info(
                MessageFormat.format(
                        "[action={0}, {2}, {1}]",
                        action, printLogAffix(request), logString));
    }

    @Override
    public void logStaticPageView(HttpServletRequest request, StaticPageType pageType) {
        log.info(
                MessageFormat.format(
                        "[action={0}, view={1}, {2}]",
                        UserAction.STATICPAGE, pageType.getViewName(), printLogAffix(request)));
    }

    @Override
    public void logLanguageChange(HttpServletRequest request, Language oldLocale, UserAction languageChange) {
        log.info(
                MessageFormat.format(
                        "[action={0}, oldLang={1}, {2}]",
                        languageChange, oldLocale.toString(), printLogAffix(request)));
    }

    @Override
    public void logBriefResultView(HttpServletRequest request, BriefBeanView briefBeanView, SolrQuery solrQuery, ModelAndView page) {
        String query = briefBeanView.getPagination().getPresentationQuery().getUserSubmittedQuery(); //
        String queryConstraints = "";
        if (solrQuery.getFilterQueries() != null) {
            String[] filterQueries = solrQuery.getFilterQueries(); // a comma separated list of qf's from url. // todo change to CU variant later
            StringBuilder out = new StringBuilder();
            for (String filterQuery : filterQueries) {
                out.append(filterQuery).append(",");
            }
            queryConstraints = out.toString().substring(0, out.toString().length() - 1);
        }
//        String pageId;
        // private String state;
        UserAction userAction = UserAction.BRIEF_RESULT;
        Map params = request.getParameterMap();
        if (params.containsKey("bt")) {
            if (request.getParameter("bt").equalsIgnoreCase("savedSearch")) {
                userAction = UserAction.BRIEF_RESULT_FROM_SAVED_SEARCH;
            }
        }
        else if (params.containsKey("rtr") && request.getParameter("rtr").equalsIgnoreCase("true")) {
            userAction = UserAction.RETURN_TO_RESULTS;
        }
        int pageNr = briefBeanView.getPagination().getPageNumber();
        int nrResults = briefBeanView.getPagination().getNumFound();
        String languageFacets = briefBeanView.getFacetLogs().get("LANGUAGE");
        String countryFacet = briefBeanView.getFacetLogs().get("COUNTRY");
        log.info(
                MessageFormat.format(
                        "[action={0}, view={1}, query={2}, queryType={7}, queryConstraints=\"{3}\", page={4}, " +
                                "numFound={5}, langFacet={8}, countryFacet={9}, {6}]",
                        userAction, page.getViewName(), query,
                        queryConstraints, pageNr, nrResults, printLogAffix(request), solrQuery.getQueryType(),
                        languageFacets, countryFacet));
    }

    @Override
    public void logFullResultView(HttpServletRequest request, FullBeanView fullResultView, ModelAndView page, String europeanaUri) {
        String originalQuery = "";
        String startPage = "";
        String numFound = "";
        try {
            DocIdWindowPager idWindowPager = fullResultView.getDocIdWindowPager();
            originalQuery = idWindowPager.getQuery();
            startPage = String.valueOf(idWindowPager.getFullDocUriInt());
            numFound = idWindowPager.getDocIdWindow().getHitCount().toString();
        }
        catch (UnsupportedEncodingException e) {
            // todo decide what to do with this error
        }
        catch (Exception e) {
            // todo decide what to do with this error
        }

        UserAction userAction = UserAction.FULL_RESULT;
        Map params = request.getParameterMap();
        if (params.containsKey("bt")) {
            if (request.getParameter("bt").equalsIgnoreCase("savedItem")) {
                userAction = UserAction.FULL_RESULT_FROM_SAVED_ITEM;
            }
            else if (request.getParameter("bt").equalsIgnoreCase("savedTag")) {
                userAction = UserAction.FULL_RESULT_FROM_SAVED_TAG;
            }
            else if (request.getParameter("bt").equalsIgnoreCase("bob")) {
                userAction = UserAction.FULL_RESULT_FROM_YEAR_GRID;
            }
        }
        log.info(
                MessageFormat.format(
                        "[action={0}, europeana_uri={2}, query={4}, start={3}, numFound={5}, {1}]",
                        userAction, printLogAffix(request), europeanaUri,
                        startPage, originalQuery, numFound));
    }

    private static String printLogAffix(HttpServletRequest request) {
        DateTime date = new DateTime();
        String ip = request.getRemoteAddr();
        String reqUrl = getRequestUrl(request);
        final User user = ControllerUtil.getUser();
        String userId;
        if (user != null) {
            userId = user.getId().toString();
        }
        else {
            userId = "";
        }
        String language = ControllerUtil.getLocale(request).toString();
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("referer");
        return MessageFormat.format(
                "userId={0}, lang={1}, req={4}, date={2}, ip={3}, user-agent={5}, referer={6}, v={7}",
                userId, language, date, ip, reqUrl, userAgent, referer, VERSION);
    }

    private static String getRequestUrl(HttpServletRequest request) {
        String base = ControllerUtil.getFullServletUrl(request);
        String queryStringParameters = request.getQueryString();
        Map postParameters = request.getParameterMap();
        StringBuilder out = new StringBuilder();
        out.append(base);
        String queryString;
        if (queryStringParameters != null) {
            out.append("?").append(queryStringParameters);
            queryString = out.toString();
        }
        else if (postParameters.size() > 0) {
            out.append("?");
            for (Object entryKey : postParameters.entrySet()) {
                Map.Entry entry = (Map.Entry) entryKey;
                String key = entry.getKey().toString();
                String[] values = (String[]) entry.getValue();
                for (String value : values) {
                    out.append(key).append("=").append(value).append("&");
                }
            }
            queryString = out.toString().substring(0, out.toString().length() - 1);
        }
        else {
            queryString = out.toString();
        }
        return queryString;
    }
}

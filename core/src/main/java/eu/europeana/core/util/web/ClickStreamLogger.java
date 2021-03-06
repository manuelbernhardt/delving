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

import eu.delving.domain.Language;
import eu.europeana.core.querymodel.query.BriefBeanView;
import eu.europeana.core.querymodel.query.FullBeanView;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 *         <p/>
 *         This interface is used add custom logging information to the application log.
 *         These logs are used to trace user behavior with special focus for how to best
 *         provide multilingual support for the Europeana Users.
 *         <p/>
 *         <p/>
 *         The idea is to log
 *         // [idPhd=1, userId="123", query="sjoerd", queryType="", constrains="country=, " ip=""]
 *         /*
 *         logTypeId=""
 *         userId=""
 *         query=""
 *         queryType=
 *         language=
 *         date=
 *         ip=
 *         pageId=
 *         state=
 *         pageNr=
 *         NrResults=
 *         LanguageFacets="en(12), de(9), fi(1)"
 *         CountryFacet="de(14), ..."
 *         templateName="" -.ftl
 *         <p/>
 *         actions=[languageChange, Result, staticPage, saveItem, saveSearch, SaveTag, register, Error, ReturnResults]
 */

public interface ClickStreamLogger {

    void logUserAction(HttpServletRequest request, UserAction action);
    void logUserAction(HttpServletRequest request, UserAction action, ModelAndView model);
    void logCustomUserAction(HttpServletRequest request, UserAction action, String logString);
    void logLanguageChange(HttpServletRequest request, Language oldLocale, UserAction languageChange);
    void logBriefResultView(HttpServletRequest request, BriefBeanView briefBeanView, SolrQuery solrQuery, ModelAndView page);
    void logFullResultView(HttpServletRequest request, FullBeanView fullResultView, ModelAndView page, String europeanaUri);

    /**
     * Enum for different user actions that can be logged.
     */
    public enum UserAction {
        // todo: add descriptions
        // language specific actions
        LANGUAGE_CHANGE,

        // search related actions
        BRIEF_RESULT,
        BRIEF_RESULT_FROM_SAVED_SEARCH,
        BRIEF_RESULT_FROM_OPEN_SEARCH,
        FULL_RESULT,
        FULL_RESULT_FROM_SAVED_ITEM,
        FULL_RESULT_FROM_SAVED_TAG,
        FULL_RESULT_FROM_YEAR_GRID,
        FULL_RESULT_FROM_TIME_LINE_VIEW,
        MORE_LIKE_THIS,
        RETURN_TO_RESULTS,
        REDIRECT_OUTLINK,
        REDIRECT_TO_SECURE,
        TIMELINE,
        TAG_GRID,
        YEAR_GRID,
        BROWSE_BOB,
        SITE_MAP_XML,

        // ajax related actions
        SAVE_ITEM,
        SAVE_SEARCH,
        SAVE_SOCIAL_TAG,
        REMOVE_SAVED_ITEM,
        REMOVE_SAVED_SEARCH,
        REMOVE_SOCIAL_TAG,
        SEND_EMAIL_TO_FRIEND,
        TAG_AUTOCOMPLETE,

        // navigation related actions
        DIRECT_LINK,

        // user management related actions
        REGISTER,
        REGISTER_SUCCESS,
        REGISTER_FAILURE,
        MY_EUROPEANA,
        UNREGISTER,
        CHANGE_PASSWORD_SUCCES,
        CHANGE_PASSWORD_FAILURE,
        LOGIN,
        FORGOT_PASSWORD,
        REGISTER_REQUEST,
        LOGOUT,
        LOGOUT_COOKIE_THEFT,

        // errors
        ERROR,
        AJAX_ERROR,
        ERROR_TOKEN_EXPIRED,
        ERROR_NO_TOKEN,
        EXCEPTION_CAUGHT,

        // static pages
        STATICPAGE,
        CONTACT_PAGE,
        FEEDBACK_SEND,
        FEEDBACK_SEND_FAILURE,
        INDEXPAGE,
        ADVANCED_SEARCH;

        private String description;

        UserAction() {
        }

        UserAction(String description) {
            this.description = description;
        }
    }

    public enum LogTypeId {
        PHD_JS("Juliane Stiller's custom log format"),
        PHD_MG("Maria Gäde's custom log format");

        private String description;

        LogTypeId(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}

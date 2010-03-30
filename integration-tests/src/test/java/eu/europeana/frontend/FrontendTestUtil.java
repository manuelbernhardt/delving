/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.frontend;

import java.io.IOException;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.log.Log;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import eu.europeana.bootstrap.PortalFullStarter;
import eu.europeana.bootstrap.SolrStarter;
import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.Role;
import eu.europeana.core.database.domain.User;
import eu.europeana.core.database.incoming.cli.ContentLoader;

/**
 * @author Borys Omelayenko
 * @author Vitali Kiruta
 */
public class FrontendTestUtil {

	public static class Constants {

		static final String CAROUSEL_EL_TYPE = "input";
		static final String CAROUSEL_STYLE = "carousel-input";
		static final String PACTA_STYLE = "pacta-input";
		final static String USER_1 = "1" + FrontendTestUtil.EMAIL;
		final static String USER_2 = "2" + FrontendTestUtil.EMAIL;
		final static String USER_SIMPLE = "simple" + FrontendTestUtil.EMAIL;

	}

	private static final String TEST_URL_CONFIG_PARAMETER_NAME = "europeana.test.external.server";
	@Ignore
	private static String getTestServerURL() {
		return (System.getProperty(TEST_URL_CONFIG_PARAMETER_NAME) == null) ? System.getenv(TEST_URL_CONFIG_PARAMETER_NAME) : System.getProperty(TEST_URL_CONFIG_PARAMETER_NAME);
	}

	/**
	 * Real portal URL we tests against.
	 * @return
	 */
	@Ignore
	public static String portalUrl() {
		return realPortalUrl;
	}

	private static final int TEST_PORT = 8081;
	private static final String TEST_PORTAL_URL = "http://localhost:" + TEST_PORT + "/portal/";
	private static String realPortalUrl = TEST_PORTAL_URL;

	public static final String EMAIL = "test@example.com";
	public static final String USERNAME = "test_user";
	public static final String PASSWORD = "test";
	public static final String FIRST_NAME = "First";
	public static final String LAST_NAME = "Last";

	public static WebClient createWebClient() {
		WebClient webClient = new WebClient();

		//A temprorary workaround to avoid javascript error caused by jQuery 1.3.1
		//htmlunit does not fully support jQuery 1.3.1 yet.
		webClient.setThrowExceptionOnScriptError(false);

		// TODO: remove it
		//webClient.setJavaScriptEnabled(false);

		return webClient;
	}

	/*
	 * get a Successful login page.
	 */
	public static HtmlPage login(WebClient webClient, String username, String password) throws IOException {
		HtmlPage page = webClient.getPage(TEST_PORTAL_URL + "login.html"); //go to login page

		HtmlTextInput usernameInput = (HtmlTextInput) page.getElementById("j_username");
		usernameInput.setValueAttribute(username);

		HtmlPasswordInput passwordInput = (HtmlPasswordInput) page.getElementById("j_password");
		passwordInput.setValueAttribute(password);

		HtmlSubmitInput loginButton = (HtmlSubmitInput) page.getElementsByName("submit_login").get(0);
		return loginButton.click();
	}

	private static boolean loaded = false;
	private static Server server;
	private static SolrStarter solr;


	@Ignore
	public static void start() throws Exception {

		String url = getTestServerURL();
		if (url == null) {
			Log.warn("Missing env parameter " + TEST_URL_CONFIG_PARAMETER_NAME + ", testing againast " + TEST_PORTAL_URL);
			if (server == null) {
				PortalFullStarter starter = new PortalFullStarter();
				if (!loaded) {
					ContentLoader.main();
					loaded = true;
				}
				server = starter.startServer(FrontendTestUtil.TEST_PORT);
				if (!server.isRunning())
					throw new Exception("Server not started");
				solr = new SolrStarter();
			}
			server.start();
			solr.start();
		} else {
			realPortalUrl = url;
		}
	}

	@Ignore
	public static void stop() throws Exception {
		if (getTestServerURL() == null) {
			server.stop();
			solr.stop();
		}
	}


}

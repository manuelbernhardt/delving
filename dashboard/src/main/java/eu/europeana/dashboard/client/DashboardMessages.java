package eu.europeana.dashboard.client;

import com.google.gwt.i18n.client.Messages;

import java.util.Date;

/**
 * This interface is the GWT trick for handling i18n messages
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface DashboardMessages extends Messages {
    String collectionsTab();

    String usersTab();

    String statisticsTab();

    String translationsTab();

    String searchTermsTab();

    String identifyUserPrompt();

    String fileUpload();

    String fileUploadNoFile();

    String fileUploadSuccessful();

    String fileUploadTitle();

    String incomingTab();

    String validating();

    String validated();

    String uploading();

    String uploaded();

    String commenceImport();

    String delete();

    String importing();

    String abortImport();

    String imported();

    String haltedWithAnError();

    String enterMessageKey();

    String translationSubmit();

    String translationReset();

    String pleaseWait();

    String serverProblem();

    String yesIAmSure();

    String submit();

    String collectionEnablementCaption();

    String enabled();

    String disable();

    String disabled();

    String cacheTab();

    String cacheDeleteThisOne();

    String cacheDeleteAllOrphans();

    String cachedObjectsOn(int objectCount, Date date);

    String loadingCollections();

    String collectionChoose();

    String collectionExamine();

    String theFileIs(String fileName);

    String close();

    String commenceIndexing();

    String queuedForIndexing();

    String indexing();

    String abort();

    String notCached();

    String commenceCacheing();

    String cached();

    String recache();

    String queuedForCacheing();

    String cacheing();

    String theCollectionIs();

    String itsThumbnailsAre();

    String fileUploadWrongType();

    String notReadyToIndex();

    String notReadyToCache();

    String indexingProgress(int recordsProcessed, int totalRecords);

    String cacheingProgress(int recordsProcessed, int totalRecords);

    String title();

    String creator();

    String year();

    String provider();

    String language();

    String type();

    String save();

    String carouselItem();

    String carouselTab();

    String uriNotFound(String uri);

    String deleteThisItem();

    String collectionCounters(int totalRecords, int totalObjects, int totalOrphans);

    String recount();

    String addSearchTermFor(String text);

    String more();

    String areYouSureAbort(String process);

    String areYouSureCollection(String action);

    String indexAndEnable();

    String cache();

    String selectSavedItem();

    String deleteCaption();

    String deleteThisUserQuestion();

    String select();

    String europeanaIdCreated(Date created);

    String recordEditTab();

    String solrRecordXML();

    String selectSavedSearch();

    String noSavedSearches();

    String typeSearchTerm();

    String partnersTab();

    String addPartnerTitle(String text);

    String namePrompt();

    String urlPrompt();

    String addContributorTitle(String text);

    String contributorsTab();

    String providerIdPrompt();

    String originalNamePrompt();

    String englishNamePrompt();

    String acroynmPrompt();

    String emailAddress();

    String password();

    String login();

    String europeanaDashboardTitle();

    String emailAddressPasswordNotFound();

    String loggedInAs(String userName, String email);

    String authenticating();

    String loginSuccessful();

    String accessDenied();

    String staticPagesTag();

    String selectPage();

    String selectLanguage();

    String previewPage();

    String editHtml();

    String revert();

    String numberOfPartnersPrompt();

    String languages();

    String userEnabled();

    String updateThisUser();

    String deleteThisUser();

    String messageKey();

    String add();

    String anyCollectionState();

    String anyCacheState();

    String anyImportFileState();

    String selectLanguages();

    String loadingPleaseWait();

    String olderEntries();

    String newerEntries();

    String logEntry(Date time, String who, String what);

    String logTab();

    String GodTab();

    String disableAll();

    String enableAll();

    String newDataSet();

    String showDataSet();

    String browseDataSets();

    String importFileStateTitle();

    String collectionStateTitle();

    String cacheStateTitle();

    String importTitle();

    String indexCacheTitle();

    String dataSetIs();

    String thereIsNoXmlFile();

    String dataset();

    String collectionDescription();

    String importFileStatus(Date lastModified, String userName);

    String role(String roleString);

    String projectId();

    String providerId();

    String sandboxTab();

    String filterNotStarted();

    String filterInProgress();

    String filterCompleted();

    String filterError();

    String selectAll();

    String selectNone();

    String noImportFilePresent();

    String dataSetCounters(int records, int thumbnails);

    String deleteFile();

    String noIAmSure();

    String userName();

    String firstName();

    String lastName();

    String newsletter();

    String rolePrompt();

    String createUser();

    String setPassword();
}

package eu.delving.core.binding

import scala.collection.JavaConversions._
import org.apache.solr.client.solrj.response.QueryResponse
import scala.reflect.BeanProperty
import collection.mutable.ListBuffer
import org.apache.solr.common.SolrDocumentList
import java.util. {Date, ArrayList, List => JList}
import java.lang.{Boolean => JBoolean}
import scala.collection.mutable.Map
import eu.europeana.core.querymodel.query._

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 10 /18/10 9:01 PM
 */

object SolrBindingService {

  def getSolrDocumentList(solrDocumentList : SolrDocumentList) : List[SolrDocument] = {
    val docs = new ListBuffer[SolrDocument]
    val ArrayListObject = classOf[ArrayList[Any]]
    val StringObject = classOf[String]
    val DateObject = classOf[Date]
    // check for required fields else check exception
    solrDocumentList.foreach{
        doc =>
          val solrDoc = SolrDocument()
          doc.entrySet.foreach{
            field =>
              val FieldValueClass: Class[_] = field.getValue.getClass
               FieldValueClass match {
                case ArrayListObject => solrDoc.add(field.getKey, field.getValue.asInstanceOf[ArrayList[Any]].toList)
                case StringObject => solrDoc.add(field.getKey, List(field.getValue))
                case DateObject => solrDoc.add(field.getKey, List(field.getValue))
                case _ => println("unknown class " + field.getKey)
              }
          }
      docs add solrDoc
    }
    docs.toList
  }

  def getSolrDocumentList(queryResponse : QueryResponse) : List[SolrDocument] = getSolrDocumentList(queryResponse.getResults)

  def getDocIds(queryResponse: QueryResponse): JList[SolrDocId] = {
    val docIds = new ListBuffer[SolrDocId]
    getSolrDocumentList(queryResponse).foreach{
      doc =>
        docIds add (SolrDocId(doc))
    }
    asList(docIds)
  }

  def getBriefDocs(queryResponse: QueryResponse): JList[BriefDocItem] = getBriefDocs(queryResponse.getResults)

  def getBriefDocs(resultList: SolrDocumentList): JList[BriefDocItem] = {
    val briefDocs = new ListBuffer[BriefDocItem]
    getSolrDocumentList(resultList).foreach{
      doc =>
        briefDocs add (BriefDocItem(doc))
    }
    asList(briefDocs)
  }

  def getFullDoc(queryResponse: QueryResponse): FullDocItem = {
    val results = getFullDocs(queryResponse.getResults)
    if (results.isEmpty) throw new EuropeanaQueryException("Full Doc not found")
    results.head
  }

  def getFullDocs(queryResponse: QueryResponse): JList[FullDocItem] = getFullDocs(queryResponse.getResults)

  def getFullDocs(matchDoc: SolrDocumentList): JList[FullDocItem] = {
    val fullDocs = new ListBuffer[FullDocItem]
    getSolrDocumentList(matchDoc).foreach{
      doc =>
        fullDocs add (FullDocItem(doc))
    }
    asList(fullDocs)
  }
}

case class SolrDocument(fieldMap : Map[String, List[Any]] = Map[String, List[Any]]()) {

  def get(field: String) : List[Any] = fieldMap.getOrElse(field, List[Any]())

  def getFirst(field: String) : String = fieldMap.getOrElse(field, List[Any]()).headOption.getOrElse("").asInstanceOf[String] // todo made generic later

  private[binding] def add(field: String, value : List[Any]) = fieldMap.put(field, value)

  private[binding] def getFieldNames = fieldMap.keys
}

case class FieldValue (key: String, solrDocument: SolrDocument) {

  private val fieldValues = solrDocument.get(key)

  def getKey = key

  def getFirst : String = solrDocument.getFirst(key)

  def getValueAsArray : Array[String] = fieldValues.asInstanceOf[List[String]].toArray

  def isNotEmpty = fieldValues.length != 0

}

case class SolrDocId(solrDocument : SolrDocument) extends DocId {
  def getEuropeanaUri : String = solrDocument.get("europeana_uri").head.asInstanceOf[String]
  def getTimestamp : Date = solrDocument.get("timestamp").head.asInstanceOf[Date]
}

case class BriefDocItem(solrDocument : SolrDocument) extends BriefDoc {
    private def assign(key: String) = solrDocument.getFirst(key)

    def getId : String = assign("europeana_uri")
    def getTitle : String = assign("title")
    def getThumbnail : String = assign("europeana_object")
    def getCreator : String = assign("creator")
    def getYear : String = assign("YEAR")
    def getProvider : String = assign("PROVIDER")
    def getDataProvider : String = assign("DATAPROVIDER")
    def getLanguage : String = assign("LANGUAGE")
    def getType : DocType = DocType.get(assign("TYPE"))

    @BeanProperty var index : Int = _
    @BeanProperty var fullDocUrl: String = _

    // debug and scoring information
    @BeanProperty var score : Int = _
    @BeanProperty var debugQuery : String = _
}

case class FullDocItem(solrDocument : SolrDocument) extends FullDoc {

    private def assign(key: String) = solrDocument.get(key).asInstanceOf[List[String]].toArray
    private def assignFirst(key: String) = solrDocument.getFirst(key)

    override def getAsArray(key: String) : Array[String] = assign(key)

    override def getAsString(key: String) : String = assignFirst(key)

    def getFieldValue(key : String) : FieldValue = FieldValue(key, solrDocument)

    // Europeana elements
    override def getId : String = assignFirst("europeana_uri")

    override def getThumbnails : Array[String] = assign("europeana_object") // this is europeanaObject

    override def getEuropeanaIsShownAt : Array[String] = assign("europeana_isShownAt")

    override def getEuropeanaIsShownBy: Array[String] = assign("europeana_isShownBy")

    override def getEuropeanaUserTag: Array[String] = assign("europeana_userTag")

    override def getEuropeanaHasObject : JBoolean = if (assign("europeana_object").isEmpty) false else true

    override def getEuropeanaCountry: Array[String] = assign("europeana_county")

    override def getEuropeanaProvider: Array[String] = assign("europeana_provider")

    override def getEuropeanaDataProvider: Array[String] = assign("europeana_dataProvider")

    override def getEuropeanaSource: Array[String] = assign("europeana_source")

    override def getEuropeanaType: DocType = DocType.get(assignFirst("europeana_type"))

    override def getEuropeanaLanguage: Array[String] = assign("europeana_language") // used to be Language

    override def getEuropeanaYear: Array[String] = assign("europeana_year")

    override def getEuropeanaCollectionName: String = assignFirst("europeana_collectionName")

    override def getEuropeanaCollectionTitle: String = assignFirst("europeana_collectionTitle")

    // here the dcterms namespaces starts
    override def getDcTermsAlternative: Array[String] = assign("dcterms_alternative")

    override def getDcTermsConformsTo: Array[String] = assign("dcterms_conformsTo")

    override def getDcTermsCreated: Array[String] = assign("dcterms_created")

    override def getDcTermsExtent: Array[String] = assign("dcterms_extent")

    override def getDcTermsHasFormat: Array[String] = assign("dcterms_hasFormat")

    override def getDcTermsHasPart: Array[String] = assign("dcterms_hasPart")

    override def getDcTermsHasVersion: Array[String] = assign("dcterms_hasVersion")

    override def getDcTermsIsFormatOf: Array[String] = assign("dcterms_isFormatOf")

    override def getDcTermsIsPartOf: Array[String] = assign("dcterms_isPartOf")

    override def getDcTermsIsReferencedBy: Array[String] = assign("dcterms_isReferencedBy")

    override def getDcTermsIsReplacedBy: Array[String] = assign("dcterms_isReplacedBy")

    override def getDcTermsIsRequiredBy: Array[String] = assign("dcterms_isRequiredBy")

    override def getDcTermsIssued: Array[String] = assign("dcterms_issued")

    override def getDcTermsIsVersionOf: Array[String] = assign("dcterms_isVersionOf")

    override def getDcTermsMedium: Array[String] = assign("dcterms_medium")

    override def getDcTermsProvenance: Array[String] = assign("dcterms_provenance")

    override def getDcTermsReferences: Array[String] = assign("dcterms_references")

    override def getDcTermsReplaces: Array[String] = assign("dcterms_replaces")

    override def getDcTermsRequires: Array[String] = assign("dcterms_requires")

    override def getDcTermsSpatial: Array[String] = assign("dcterms_spatial")

    override def getDcTermsTableOfContents: Array[String] = assign("dcterms_tableOfContents")

    override def getDcTermsTemporal: Array[String] = assign("dcterms_temporal")

    // here the dc namespace starts
    override def getDcContributor: Array[String] = assign("dc_contributor")

    override def getDcCoverage: Array[String] = assign("dc_coverage")

    override def getDcCreator: Array[String] = assign("dc_creator")

    override def getDcDate: Array[String] = assign("dc_date")

    override def getDcDescription: Array[String] = assign("dc_description")

    override def getDcFormat: Array[String] = assign("dc_format")

    override def getDcIdentifier: Array[String] = assign("dc_identifier")

    override def getDcLanguage: Array[String] = assign("dc_language")

    override def getDcPublisher: Array[String] = assign("dc_publisher")

    override def getDcRelation: Array[String] = assign("dc_relation")

    override def getDcRights: Array[String] = assign("dc_rights")

    override def getDcSource: Array[String] = assign("dc_source")

    override def getDcSubject: Array[String] = assign("dc_subject")

    override def getDcTitle: Array[String] = assign("dc_title")

    override def getDcType: Array[String] = assign("dc_type")

    override def getBriefDoc : BriefDoc = BriefDocItem(solrDocument)
}
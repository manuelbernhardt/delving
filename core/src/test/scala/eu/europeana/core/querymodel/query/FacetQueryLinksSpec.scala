package eu.europeana.core.querymodel.query

import _root_.java.lang.String
import _root_.org.apache.solr.client.solrj.response.FacetField
import _root_.org.apache.solr.client.solrj.SolrQuery
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer
import collection.immutable.List

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class FacetQueryLinksSpec extends Spec with ShouldMatchers {
  describe("A List of FacetQueryLinks") {

    describe("(when given a SolrQuery with filterQueries and a List of FacetFields)") {
      val (solrQuery, facets, filterQueries) = createQueryAndFacets
      val facetLinks = FacetQueryLinks.createDecoratedFacets(solrQuery, facets)

      it("should have all FacetFields that have filter queries selected") {
        facetLinks.foreach {
          facetLink => facetLink.getType match {
            case "TYPE" =>
              facetLink.isSelected should be(false)
              facetLink.getLinks.size should be(0)
            case _ =>
              facetLink.isSelected should be(true)
              facetLink.getLinks.size should not be (0)
          }
        }
      }

      it("should have isRemove as true for every selected FacetCountLink") {
        facetLinks.foreach {
          facetLink =>
            facetLink.getLinks.foreach {
              facetCountLink =>
                facetCountLink.isRemove should equal(filterQueries contains currentFilterQuery(facetLink, facetCountLink))
            }
        }
      }

      it("should not contain the selected queryFilter in the url when isRemove is true") {
        facetLinks.foreach {
          facetLink =>
            facetLink.getLinks.foreach {
              facetCountLink =>
                facetCountLink.isRemove should not equal (facetCountLink.getUrl contains currentFilterQuery(facetLink, facetCountLink))
            }
        }
      }

      it("should contain all the filterQueries in the url when isRemove is false") {
        facetLinks.foreach {
          facetLink =>
            facetLink.getLinks.filter(_.isRemove == false).foreach {
              facetCountLink =>
                {
                  val appliedQueryFilters = getAppliedQueryFilters(facetCountLink)
                  appliedQueryFilters.dropRight(1) should equal(filterQueries)
                  appliedQueryFilters.reverse.head should equal(currentFilterQuery(facetLink, facetCountLink))
                }
            }
        }
      }

      it("should not contain YEAR facet entries that contain '0000") {
        facetLinks.filter(_.getType == "YEAR").head.getLinks.exists(_.getValue == "0000") should be(false)
      }

    }

    describe("(when given a SolrQuery with FilterQueries but with an empty facetList)") {
      val solrQuery = new SolrQuery("query").addFilterQuery("YEAR:1977")
      val emptyFacetList = new ListBuffer[FacetField]

      it("should return an empty list") {
        val facetLinks = FacetQueryLinks.createDecoratedFacets(solrQuery, emptyFacetList)
        facetLinks should be('empty)
      }
    }

    describe("(when given a SolrQuery without FilterQueries)") {
      val (solrQuery, facets, filterQueries) = createQueryAndFacets
      filterQueries.foreach(fq => solrQuery.removeFilterQuery(fq))
      val facetLinks = FacetQueryLinks.createDecoratedFacets(solrQuery, facets)

      it("should not contain any select FacetFields") {
        facetLinks.forall(_.isSelected) should be(false)
      }

      it("should have all isRemove as false") {
        facetLinks.foreach(_.getLinks.foreach(_.isRemove should be(false)))
      }

      it("should only have the current Facet in the url") {
        facetLinks.foreach(
          facetLink =>
            facetLink.getLinks.foreach(
              facetCountLink =>
                {
                  val appliedFilters: List[String] = getAppliedQueryFilters(facetCountLink)
                  appliedFilters.size should be(1)
                  appliedFilters.head should equal(currentFilterQuery(facetLink, facetCountLink))
                }
              )
          )
      }

    }
  }

  def getAppliedQueryFilters(facetCountLink: FacetQueryLinks#FacetCountLink): List[String] = facetCountLink.getUrl.split("&qf=").toList.tail

  def currentFilterQuery(facetLink: FacetQueryLinks, facetCountLink: FacetQueryLinks#FacetCountLink): String = facetLink.getType + ":" + facetCountLink.getValue

  def createQueryAndFacets: (SolrQuery, ListBuffer[FacetField], ListBuffer[String]) = {
    val filterQueries = new ListBuffer[String] += ("LANGUAGE:de", "LANGUAGE:nl", "YEAR:1980")
    val solrQuery = new SolrQuery("everything")
            .addFacetField("LANGUAGE", "YEAR", "TYPE")
            .addFilterQuery(filterQueries: _*)

    val languageFacetEntries = Map("en" -> 1, "de" -> 2, "nl" -> 3)
    val languageFacet: FacetField = new FacetField("LANGUAGE")
    languageFacetEntries.foreach(entry => languageFacet add (entry._1, entry._2))

    val yearFacetEntries = Map("0000" -> 666, "1980" -> 1, "1981" -> 2, "1982" -> 3)
    val yearFacet = new FacetField("YEAR")
    yearFacetEntries.foreach(entry => yearFacet add (entry._1, entry._2))

    val typeFacet = new FacetField("TYPE")
    val facets = new ListBuffer[FacetField] += (languageFacet, yearFacet, typeFacet)
    (solrQuery, facets, filterQueries)
  }

}
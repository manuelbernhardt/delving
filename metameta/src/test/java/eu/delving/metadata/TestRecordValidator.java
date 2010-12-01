package eu.delving.metadata;

import eu.delving.metadata.MetadataException;
import eu.delving.metadata.MetadataModelImpl;
import eu.delving.metadata.RecordValidator;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Test that the validator is working properly
 * <br>
 * <ul>
 * <li> no duplicate fields - filtered silently
 * <li> no empty fields - filtered silently
 * <li> no unknown fields
 * <li> required fields must be there (checked by groups, so "or" requirements are possible)
 * <li> non-multivalued fields must not have multiple values
 * <li> URLs checked using java.net.URL
 * <li> regular expression checking
 * <li> ids unique per collection
 * </ul>
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestRecordValidator {
    private static final String[] VALID_FIELDZ = {
            "<europeana:isShownAt>http://is-shown-at.com/</europeana:isShownAt>",
            "<europeana:uri>http://uri.com/</europeana:uri>",
            "<europeana:provider>provider</europeana:provider>",
            "<europeana:country>netherlands</europeana:country>",
            "<europeana:collectionName>collectionName</europeana:collectionName>",
            "<europeana:language>en</europeana:language>",
            "<europeana:object>http://object.com/</europeana:object>",
            "<europeana:rights>nerd's</europeana:rights>",
            "<europeana:dataProvider>everyone</europeana:dataProvider>",
            "<europeana:type>IMAGE</europeana:type>",
            "<europeana:collectionTitle>Tittle</europeana:collectionTitle>",
    };
    private Logger log = Logger.getLogger(getClass());
    private RecordValidator recordValidator;
    private List<String> problems = new ArrayList<String>();
    private List<String> validFields = new ArrayList<String>(Arrays.asList(VALID_FIELDZ));

    @Before
    public void prepare() throws IOException, MetadataException {
        MetadataModelImpl metadataModel = new MetadataModelImpl();
        metadataModel.setRecordDefinitionResources(Arrays.asList("/abm-record-definition.xml"));
        metadataModel.setDefaultPrefix("abm");
        recordValidator = new RecordValidator(metadataModel, true);
        problems.clear();
    }

    private String toString(List<String> lines) {
        StringBuilder out = new StringBuilder("<record>\n");
        for (String line : lines) {
            out.append(line.trim()).append('\n');
        }
        out.append("</record>");
        return out.toString();
    }

    private void validate(String message, String[] expectArray, String[] inputArray, boolean includeRequired) {
        List<String> expectList = new ArrayList<String>(Arrays.asList(expectArray));
        List<String> inputList = new ArrayList<String>(Arrays.asList(inputArray));
        if (includeRequired) {
            expectList.addAll(validFields);
            inputList.addAll(validFields);
        }
        String expect = toString(expectList);
        String input = toString(inputList);
        log.info("input:\n" + input);
        String validated = recordValidator.validateRecord(input, problems);
        for (String problem : problems) {
            log.info("Problem: " + problem);
        }
        Assert.assertTrue("Problems", problems.isEmpty());
        List<String> validatedList = new ArrayList<String>(Arrays.asList(validated.split("\n")));
        validatedList.remove(0); // <record>
        validatedList.remove(validatedList.size()-1); // </record>
        validated = toString(validatedList);
        log.info("validated:\n" + validated);
        assertEquals(
                message,
                expect,
                validated
        );
    }

    private void problem(String [] inputArray, String problemContains, boolean includeRequired) {
        List<String> inputList = new ArrayList<String>(Arrays.asList(inputArray));
        if (includeRequired) {
            inputList.addAll(validFields);
        }
        String input = toString(inputList);
        log.info("input:\n" + input);
        recordValidator.validateRecord(input, problems);
        Assert.assertFalse("Expected problems", problems.isEmpty());
        boolean found = false;
        for (String problem : problems) {
            log.info("Problem: "+problem);
            if (problem.contains(problemContains)) {
                found = true;
            }
        }
        if (!found) {
            Assert.fail(String.format("Expected to find a problem containing [%s]", problemContains));
        }
    }

    @Test
    public void duplicateRemoval() {
        validate(
                "Duplicate not removed",
                new String[]{
                        "<dc:identifier>one</dc:identifier>",
                },
                new String[]{
                        "<dc:identifier>one</dc:identifier>",
                        "<dc:identifier>one</dc:identifier>",
                },
                true
        );
        Assert.assertTrue("Problems", problems.isEmpty());
    }

    @Test
    public void emptyRemoval() {
        validate(
                "Empty not removed",
                new String[]{
                        "<dc:identifier>one</dc:identifier>",
                },
                new String[]{
                        "<dc:identifier>one</dc:identifier>",
                        "<dc:title></dc:title>",
                },
                true
        );
        Assert.assertTrue("Problems", problems.isEmpty());
    }

    @Test
    public void optionsFalse() {
        problem(
                new String[]{
                        "<europeana:type>SOwUND</europeana:type>",
                },
                "which does not belong to",
                true
        );
    }

    @Test
    public void spuriousTag() {
        problem(
                new String[]{
                        "<description>illegal</description>",
                },
                "No field definition found",
                true
        );
    }

    @Test
    public void spuriousURI() {
        problem(
                new String[]{
                        "<europeana:isShownBy>httpthis is not</europeana:isShownBy>",
                },
                "malformed",
                true
        );
    }
    
    @Test
    public void uniqueness() {
        validate(
                "Should have worked",
                new String[]{},
                new String[]{},
                true
        );
        problem(
                new String[]{},
                "must be unique but the value",
                true
        );
    }

    @Test
    public void multivalued() {
        problem(
                new String[]{
                        "<europeana:type>IMAGE</europeana:type>",
                        "<europeana:type>SOUND</europeana:type>",
                },
                "has more than one value",
                true
        );
    }

    @Test
    public void requiredMissing() {
        problem(
                new String[]{
                        "<europeana:isShownAt>http://is-shown-at.com/</europeana:isShownAt>",
                        "<europeana:uri>http://uri.com/</europeana:uri>",
                        "<europeana:provider>provider</europeana:provider>",
                        "<europeana:country>netherlands</europeana:country>",
                        "<europeana:collectionName>collectionName</europeana:collectionName>",
                        "<europeana:language>en</europeana:language>",
                        "<europeana:object>http://object.com/</europeana:object>",
                        "<europeana:dataProvider>everyone</europeana:dataProvider>",
                        "<europeana:type>IMAGE</europeana:type>",
                        "<europeana:collectionTitle>Tittle</europeana:collectionTitle>",
                },
                "Required field violation for [Rights]",
                false
        );
    }

    @Test
    public void requiredGroupMissing() {
        problem(
                new String[]{
                        "<europeana:uri>http://uri.com/</europeana:uri>",
                        "<europeana:provider>provider</europeana:provider>",
                        "<europeana:country>netherlands</europeana:country>",
                        "<europeana:collectionName>collectionName</europeana:collectionName>",
                        "<europeana:language>en</europeana:language>",
                        "<europeana:object>http://object.com/</europeana:object>",
                        "<europeana:rights>nerd's</europeana:rights>",
                        "<europeana:dataProvider>everyone</europeana:dataProvider>",
                        "<europeana:type>IMAGE</europeana:type>",
                        "<europeana:collectionTitle>Tittle</europeana:collectionTitle>",
                },
                "Required field violation for [Shown-at or Shown-by]",
                false
        );
    }

}
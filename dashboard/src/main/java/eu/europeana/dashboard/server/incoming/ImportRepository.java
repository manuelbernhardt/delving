package eu.europeana.dashboard.server.incoming;

import eu.europeana.dashboard.client.dto.ImportFile;
import eu.europeana.database.domain.ImportFileState;

import java.io.File;
import java.util.List;

/**
 * This interface describes programmatic access to the directories
 * where the import files are stored.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface ImportRepository {

    File createFile(ImportFile importFile);

    ImportFile createForUpload(String fileName);

    List<String> getFiles(ImportFileState state);

    List<ImportFile> getAllFiles();

    ImportFile transition(ImportFile importFile, ImportFileState to);

    ImportFile checkStatus(String fileName);
}
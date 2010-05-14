/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
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

package eu.europeana.sip.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * The groovy helper code that precedes the mapping snippet.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ToolCodeModel {
    private static final String FILE_NAME = "ToolCode.groovy";
    private static final File TOOL_CODE_FILE = new File(FILE_NAME);
    private static final URL TOOL_CODE_URL = ToolCodeModel.class.getResource("/" + FILE_NAME);
    private String resourceCode;
    private String fileCode;
    private long fileModified;

    public ToolCodeModel() {
        try {
            resourceCode = readResourceCode();
            if (!TOOL_CODE_FILE.exists()) {
                FileWriter out = new FileWriter(TOOL_CODE_FILE);
                out.write("// ToolCode.groovy - the place for helpful closures\n\n");
                out.close();
            }
        }
        catch (IOException e) {
            resourceCode = "println 'Could not read tool code: "+e.toString()+"'";
        }
    }

    public String getToolCode() {
        try {
            long mod = TOOL_CODE_FILE.lastModified();
            if (mod > fileModified) {
                fileCode = readFileCode();
                fileModified = mod;
            }
            return resourceCode + fileCode;
        }
        catch (IOException e) {
            return "println 'Could not read tool code'";
        }
    }

    private String readFileCode() throws IOException {
        if (!TOOL_CODE_FILE.exists()) {
            FileWriter out = new FileWriter(TOOL_CODE_FILE);
            out.write("// ToolCode.groovy - the place for helpful closures\n\n");
            out.close();
            return "println 'No file code'";
        }
        else {
            return readCode(new FileReader(TOOL_CODE_FILE));
        }
    }

    private String readResourceCode() throws IOException {
        if (TOOL_CODE_URL == null) {
            throw new IOException("Cannot find resource");
        }
        InputStream in = TOOL_CODE_URL.openStream();
        Reader reader = new InputStreamReader(in);
        return readCode(reader);
    }

    private String readCode(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            out.append(line).append('\n');
        }
        in.close();
        return out.toString();
    }
}

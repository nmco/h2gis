/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * Some utilities
 * 
 * @author Erwan Bocher
 */
public class FileUtil {
    
    /**
     * Check if the file is well formatted regarding an extension prefix.
     * Check also if the file doesn't exist.
     * 
     * @param file
     * @param prefix
     * @return
     * @throws SQLException 
     * @throws java.io.FileNotFoundException 
     */
    public static boolean isFileImportable(File file, String prefix) throws SQLException, FileNotFoundException{
        if (isExtensionWellFormated(file, prefix)) {
            if (file.exists()) {
                return true;
            } else {
                throw new FileNotFoundException("The following file does not exists:\n" + file.getPath());
            }
        } else {
            throw new SQLException("Please use " + prefix + " extension.");
        }
    }
    
    /**
     * Check if the file has the good extension
     * @param file
     * @param prefix
     * @return 
     */
    public static boolean isExtensionWellFormated(File file, String prefix) {
        String path = file.getAbsolutePath();
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i >= 0) {
            extension = path.substring(i + 1);
        }
        return extension.equalsIgnoreCase(prefix);
    }
}

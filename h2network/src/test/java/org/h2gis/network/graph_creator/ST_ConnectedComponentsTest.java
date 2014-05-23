/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly: info_at_orbisgis.org
 */

package org.h2gis.network.graph_creator;

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.h2gis.utilities.GraphConstants;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.h2gis.utilities.GraphConstants.*;
import static org.junit.Assert.*;

/**
 * @author Adam Gouge
 */
public class ST_ConnectedComponentsTest {

    private static Connection connection;
    private Statement st;
    private static final String DO = "'directed - edge_orientation'";
    private static final String RO = "'reversed - edge_orientation'";
    private static final String U = "'undirected'";
    private static final String EDGES = "EDGES";

    @BeforeClass
    public static void setUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase("ST_ConnectedComponentsTest", true);
        CreateSpatialExtension.registerFunction(connection.createStatement(), new ST_ConnectedComponents(), "");
        registerEdges(connection);
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    public static void registerEdges(Connection connection) throws SQLException {
        final Statement st = connection.createStatement();
        st.execute("CREATE TABLE " + EDGES + "(" +
                "EDGE_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "START_NODE INT, END_NODE INT, EDGE_ORIENTATION INT);" +
                "INSERT INTO " + EDGES + "(START_NODE, END_NODE, EDGE_ORIENTATION) VALUES "
                + "(1, 2, 1),"
                + "(2, 3, 1),"
                + "(2, 5, 1),"
                + "(2, 6, 1),"
                + "(3, 4, 1),"
                + "(3, 7, 1),"
                + "(4, 3, 1),"
                + "(4, 8, 1),"
                + "(5, 1, 1),"
                + "(5, 6, 1),"
                + "(6, 7, 1),"
                + "(7, 6, 1),"
                + "(8, 4, 1),"
                + "(8, 7, 1),"
                + "(9, 10, 1),"
                + "(10, 9, 1),"
                + "(10, 11, 1),"
                + "(12, 12, 1);");
    }

    @Test
    public void DO() throws Exception {
        st.execute("DROP TABLE IF EXISTS " + EDGES + NODE_COMP_SUFFIX);
        st.execute("DROP TABLE IF EXISTS " + EDGES + EDGE_COMP_SUFFIX);
        // SELECT ST_ConnectedComponents('" + EDGES + "', 'directed - edge_orientation')
        checkBoolean(compute(DO));
        assertEquals(getVDOROPartition(),
                getVertexPartition(st.executeQuery("SELECT * FROM " + EDGES + NODE_COMP_SUFFIX)));
        assertEquals(getEDOROPartition(),
                getEdgePartition(st.executeQuery("SELECT * FROM " + EDGES + EDGE_COMP_SUFFIX)));
    }

    @Test
    public void RO() throws Exception {
        st.execute("DROP TABLE IF EXISTS " + EDGES + NODE_COMP_SUFFIX);
        st.execute("DROP TABLE IF EXISTS " + EDGES + EDGE_COMP_SUFFIX);
        // SELECT ST_ConnectedComponents('" + EDGES + "', 'reversed - edge_orientation')
        checkBoolean(compute(RO));
        assertEquals(getVDOROPartition(),
                getVertexPartition(st.executeQuery("SELECT * FROM " + EDGES + NODE_COMP_SUFFIX)));
        assertEquals(getEDOROPartition(),
                getEdgePartition(st.executeQuery("SELECT * FROM " + EDGES + EDGE_COMP_SUFFIX)));
    }

    @Test
    public void U() throws Exception {
        st.execute("DROP TABLE IF EXISTS " + EDGES + NODE_COMP_SUFFIX);
        st.execute("DROP TABLE IF EXISTS " + EDGES + EDGE_COMP_SUFFIX);
        // SELECT ST_ConnectedComponents('" + EDGES + "', 'undirected')
        checkBoolean(compute(U));
        assertEquals(getVUPartition(),
                getVertexPartition(st.executeQuery("SELECT * FROM " + EDGES + NODE_COMP_SUFFIX)));
//        checkEdges(st.executeQuery("SELECT * FROM " + EDGES + EDGE_COMP_SUFFIX),
//                new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3});
    }

    private ResultSet compute(String orientation) throws SQLException {
        return st.executeQuery("SELECT ST_ConnectedComponents('" + EDGES + "', " + orientation + ")");
    }

    private void checkBoolean(ResultSet rs) throws SQLException {
        try{
            assertTrue(rs.next());
            assertTrue(rs.getBoolean(1));
            assertFalse(rs.next());
        } finally {
            rs.close();
        }
    }

    private Set<Set<Integer>> getVDOROPartition() {
        Set<Set<Integer>> vertexPartition = new HashSet<Set<Integer>>();
        final HashSet<Integer> vCC1 = new HashSet<Integer>();
        vCC1.add(1);
        vCC1.add(2);
        vCC1.add(5);
        final HashSet<Integer> vCC2 = new HashSet<Integer>();
        vCC2.add(3);
        vCC2.add(4);
        vCC2.add(8);
        final HashSet<Integer> vCC3 = new HashSet<Integer>();
        vCC3.add(6);
        vCC3.add(7);
        final HashSet<Integer> vCC4 = new HashSet<Integer>();
        vCC4.add(9);
        vCC4.add(10);
        final HashSet<Integer> vCC5 = new HashSet<Integer>();
        vCC5.add(11);
        final HashSet<Integer> vCC6 = new HashSet<Integer>();
        vCC6.add(12);
        vertexPartition.add(vCC1);
        vertexPartition.add(vCC2);
        vertexPartition.add(vCC3);
        vertexPartition.add(vCC4);
        vertexPartition.add(vCC5);
        vertexPartition.add(vCC6);
        return vertexPartition;
    }

    private Set<Set<Integer>> getVUPartition() {
        Set<Set<Integer>> vertexPartition = new HashSet<Set<Integer>>();
        final HashSet<Integer> vCC1 = new HashSet<Integer>();
        vCC1.add(1);
        vCC1.add(2);
        vCC1.add(3);
        vCC1.add(4);
        vCC1.add(5);
        vCC1.add(6);
        vCC1.add(7);
        vCC1.add(8);
        final HashSet<Integer> vCC2 = new HashSet<Integer>();
        vCC2.add(9);
        vCC2.add(10);
        vCC2.add(11);
        final HashSet<Integer> vCC3 = new HashSet<Integer>();
        vCC3.add(12);
        vertexPartition.add(vCC1);
        vertexPartition.add(vCC2);
        vertexPartition.add(vCC3);
        return vertexPartition;
    }

    private Set<Set<Integer>> getEDOROPartition() {
        Set<Set<Integer>> edgePartition = new HashSet<Set<Integer>>();
        final HashSet<Integer> cc1 = new HashSet<Integer>();
        cc1.add(1);
        cc1.add(3);
        cc1.add(9);
        final HashSet<Integer> cc2 = new HashSet<Integer>();
        cc2.add(5);
        cc2.add(7);
        cc2.add(8);
        cc2.add(13);
        final HashSet<Integer> cc3 = new HashSet<Integer>();
        cc3.add(11);
        cc3.add(12);
        final HashSet<Integer> cc4 = new HashSet<Integer>();
        cc4.add(15);
        cc4.add(16);
        final HashSet<Integer> cc5 = new HashSet<Integer>();
        cc5.add(18);
        final HashSet<Integer> cc6 = new HashSet<Integer>();
        cc6.add(2);
        cc6.add(4);
        cc6.add(6);
        cc6.add(10);
        cc6.add(14);
        cc6.add(17);
        edgePartition.add(cc1);
        edgePartition.add(cc2);
        edgePartition.add(cc3);
        edgePartition.add(cc4);
        edgePartition.add(cc5);
        edgePartition.add(cc6);
        return edgePartition;
    }

    private Set<Set<Integer>> getVertexPartition(ResultSet nodeComponents) throws SQLException {
        try {
            Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
            while (nodeComponents.next()) {
                final int ccID = nodeComponents.getInt(CONNECTED_COMPONENT);
                if (map.get(ccID) == null) {
                    map.put(ccID, new HashSet<Integer>());
                }
                map.get(ccID).add(nodeComponents.getInt(GraphConstants.NODE_ID));
            }
            Set<Set<Integer>> vertexPartition = new HashSet<Set<Integer>>();
            for (Set<Integer> cc : map.values()) {
                vertexPartition.add(cc);
            }
            return vertexPartition;
        } finally {
            nodeComponents.close();
        }
    }

    private Set<Set<Integer>> getEdgePartition(ResultSet edgeComponents) throws SQLException {
        try {
            Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
            while (edgeComponents.next()) {
                final int ccID = edgeComponents.getInt(CONNECTED_COMPONENT);
                if (map.get(ccID) == null) {
                    map.put(ccID, new HashSet<Integer>());
                }
                map.get(ccID).add(edgeComponents.getInt(GraphConstants.EDGE_ID));
            }
            Set<Set<Integer>> edgePartition = new HashSet<Set<Integer>>();
            for (Set<Integer> cc : map.values()) {
                edgePartition.add(cc);
            }
            return edgePartition;
        } finally {
            edgeComponents.close();
        }
    }
}

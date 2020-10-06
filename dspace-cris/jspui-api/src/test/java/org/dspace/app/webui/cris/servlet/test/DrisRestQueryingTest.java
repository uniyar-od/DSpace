/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.cris.servlet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.dspace.app.webui.cris.servlet.DrisQueryingServlet;
import org.dspace.app.webui.cris.util.JsonLdResultsList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.restlet.util.Couple;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.gson.Gson;

/**
 * Test class for REST services provided by
 * <code>org.dspace.app.webui.cris.servlet.DrisQueryingServlet</code>.
 * 
 * @author Mastratisi
 */
public class DrisRestQueryingTest extends Mockito {

	public final static String BASIC_DRIS_QUERYING_URL = "http://localhost:8080/jspui/dris/";
	private DrisQueryingServlet servlet;
	@SuppressWarnings("deprecation")
	private static Map<String, Couple<Boolean, Long>> fileLoadedQueries;

	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void setUpClass() {
		DrisRestQueryingTest.fileLoadedQueries = new HashMap<String, Couple<Boolean, Long>>();
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}
	
	@SuppressWarnings("deprecation")
	private void loadQueriesFile() throws Exception {
	    String content = null;
		try {
			Resource resource = new ClassPathResource("org/dspace/app/webui/cris/servlet/test/queries.json");
			content = new String(Files.readAllBytes(Paths.get(resource.getURI())));
		} catch (IOException e) {
			fileLoadedQueries = new HashMap<String, Couple<Boolean, Long>>();
			e.printStackTrace();
			throw e;
		}
	    JSONArray listItems = null;
		try {
			listItems = new JSONArray(content);
		} catch (Exception e) {
			fileLoadedQueries = new HashMap<String, Couple<Boolean, Long>>();
			e.printStackTrace();
			throw e;
		}
	    for (int i = 0; i < listItems.length(); i++) {
	      JSONObject jsonItem = ((JSONObject) listItems.get(i));
	      try {
	    	  String query = (jsonItem.has("query"))? StringUtils.trimToEmpty(jsonItem.getString("query")) : "";
	    	  Boolean success = (jsonItem.has("success"))? jsonItem.getBoolean("success") : true;
	    	  Long expectedResults = (jsonItem.has("expectedResults"))? jsonItem.getLong("expectedResults") : -1L;
	    	  DrisRestQueryingTest.fileLoadedQueries.put(query, new Couple<Boolean, Long>(success, expectedResults));
		  } catch (JSONException e) {
			e.printStackTrace();
			continue;
		  }
	    }
	}
	
	@SuppressWarnings("deprecation")
	private FileWriter openResultsFile() throws Exception {
		try {
			FileWriter fileWriter = new FileWriter((new ClassPathResource("org/dspace/app/webui/cris/servlet/test/queries-results.json")).getFile());
			return fileWriter;
		} catch (IOException e) {
			e.printStackTrace();
			// Ignore....
			throw e;
			//return null;
		}
	}

	
	@SuppressWarnings("deprecation")
	@Test
	public void testAllFileLoadedQueries() {
		try {
			this.loadQueriesFile();
		} catch (Exception e1) {
			fail(e1.getMessage());
		}
		FileWriter resultsWriter = null;
		/*try {
			resultsWriter = this.openResultsFile();
		} catch (Exception e1) {
			fail(e1.getMessage());
		}*/
		for (Map.Entry<String, Couple<Boolean, Long>> fileEntry: DrisRestQueryingTest.fileLoadedQueries.entrySet()) {
			JSONArray res = new JSONArray();
			try {
				res = this.testSuccessDrisQuery(fileEntry.getKey(), resultsWriter);
				if (fileEntry.getValue().getSecond() > -1) {
					assertEquals(new Long(res.length()), fileEntry.getValue().getSecond());
				}
			} catch (Exception e) {
				if (fileEntry.getValue().getFirst()) {
					if (resultsWriter != null) {
						try {
							resultsWriter.flush();
							resultsWriter.close();
						} catch (IOException ioe) { }
					}
					fail(e.getMessage());
				} else continue;
			}
		}
		if (resultsWriter != null) {
			try {
				resultsWriter.flush();
				resultsWriter.close();
			} catch (IOException e) { }
		}
	}
	
	
	public void testAllDrisEntry() {
		JSONArray res = new JSONArray();
		try {
			res = this.testSuccessDrisQuery("entries");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		// Specific checks of this kind of query  
		assertEquals(res.length(), 769L);
	}

	public void testAllDrisEntryPaginated10() throws Exception {
		JSONArray resPag1 = new JSONArray();
		JSONArray resPag2 = new JSONArray();
		JSONArray resPag3 = new JSONArray();
		try {
			resPag1 = this.testSuccessDrisQuery("entries?max-page-size=10");
			resPag2 = this.testSuccessDrisQuery("entries?max-page-size=10&page-start-doc=10");
			//resPag3 = this.testSuccessDrisQuery("entries?max-page-size=10&page-start-doc=766");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		// Specific checks of this kind of query
		assertEquals(resPag1.length(), 10L);
		assertEquals(resPag2.length(), 10L);
		//assertEquals(resPag3.length(), 3L);
	}

	public void testDrisEntryById() throws Exception {
		// JsonLdResultsList res = this.testSuccessDrisQuery("entries/dris00892");
		JSONArray res = new JSONArray();
		try {
			res = this.testSuccessDrisQuery("entries/dris00896");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		// Specific checks of this kind of query
		assertEquals(res.length(), 1L);
		assertEquals(((JSONObject)res.get(0)).getString("@id"), "dris00896");
	}
	
	public void testFilteredDrisEntry() throws Exception {
		JSONArray res1 = new JSONArray();
		JSONArray res2 = new JSONArray();
		JSONArray res3 = new JSONArray();
		try {
			res1 = this.testSuccessDrisQuery("entries?country.id=country00725");
			res2 = this.testSuccessDrisQuery("entries?country.id=country00725&country.id=country00732");
			res3 = this.testSuccessDrisQuery("entries?country.id=country00725,country00732");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		// Specific checks of this kind of query  
		assertEquals(res1.length(), 196L);
		assertEquals(res2.length(), 196L + 76L);
		assertEquals(res3.length(), 196L + 76L);
	}
	
	public void testFilteredPaginatedDrisEntry() throws Exception {
		JSONArray res4 = new JSONArray();
		JSONArray res5 = new JSONArray();
		JSONArray res6 = new JSONArray();
		try {
			res4 = this.testSuccessDrisQuery("entries?country.id=country00725&max-page-size=10");
			res5 = this.testSuccessDrisQuery("entries?country.id=country00725&max-page-size=10&page-start-doc=10");
			res6 = this.testSuccessDrisQuery("entries?country.id=country00725&max-page-size=10&page-start-doc=193");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		// Specific checks of this kind of query  
		assertEquals(res4.length(), 10L);
		assertEquals(res5.length(), 10L);
		assertEquals(res6.length(), 3L);
	}
	
	/**
	 * Test of any query to Dris Servlet.
	 */
	private JSONArray testSuccessDrisQuery(String query) throws Exception {
		return this.testSuccessDrisQuery(query, null);
	}
	
	/**
	 * Test of any query to Dris Servlet.
	 */
	private JSONArray testSuccessDrisQuery(String query, FileWriter resultsWriter) throws Exception {
		URL url = null;
		HttpURLConnection connection = null;
		try {
			String urlBase = DrisRestQueryingTest.BASIC_DRIS_QUERYING_URL + query;
			url = new URL(urlBase);
		} catch (MalformedURLException e) {
			fail(e.getMessage());
		}
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);// abilita la scrittura
			connection.setRequestMethod("GET");// settaggio del protocollo di comunicazione
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.connect();
			BufferedReader read = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuffer risBuf = new StringBuffer();
			String singleLine = null;
			while ((singleLine = read.readLine()) != null) {
				risBuf.append(singleLine);
			}
			String responseAsString = risBuf.toString();
			read.close();
			connection.disconnect();
			assertNotNull(responseAsString);
			if (resultsWriter != null) {
				resultsWriter.append("\n\n********" + query + "*******\n" + responseAsString);
			}
			System.out.println("\n\n********" + query + "*******\n" + responseAsString);
			JSONObject responseAsJsonObj = new JSONObject(responseAsString);
			assertNotNull(responseAsJsonObj);
			assertNotNull(responseAsJsonObj.get("numberOfItems"));
			assertNotNull(responseAsJsonObj.get("itemListElement"));
			JSONArray listItems = responseAsJsonObj.getJSONArray("itemListElement");
			assertEquals(responseAsJsonObj.getJSONArray("itemListElement").length(),
					responseAsJsonObj.getInt("numberOfItems"));
			return listItems;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static Optional<? extends Object> getObjectFromDocument(Map<String, Object> document) {
		if ((document == null) || (document.isEmpty())) {
			return Optional.ofNullable(null);
		}
		try {
			Gson gson = new Gson();
			Object obj = gson.fromJson(gson.toJson(document), Object.class);
			return Optional.ofNullable(obj);
		} catch (Exception e) {
			return Optional.ofNullable(null);
		}
	}

	public static Optional<JsonLdResultsList> getJsonLdResultsListFromMap(Map<String, Object> map) {
		if ((map == null) || (map.isEmpty())) {
			return Optional.ofNullable(null);
		}
		try {
			Gson gson = new Gson();
			JsonLdResultsList obj = gson.fromJson(gson.toJson(map), JsonLdResultsList.class);
			return Optional.ofNullable(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.ofNullable(null);
		}
	}

}

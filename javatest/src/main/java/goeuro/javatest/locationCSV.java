package goeuro.javatest;

/**********************************************************************************************
 * Class: javatest.java
 * Author: Eeran Maiti
 * Creation Date: 19 November 2014
 * Description: Takes input location and uses geoEuro API to get JSON data and write in csv file.
 * Change Log: EMDDMMYY
 * Reference: 	http://www.tutorialspoint.com/json/json_java_example.htm
 * 				http://www.json.org/javadoc/org/json/JSONObject.html#keys%28%29
 **********************************************************************************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONObject;

public class locationCSV {
	
	String sLocation = null;
	
	/*************************************************************************************
	 * Constructor: locationCSV
	 * Parameter: String
	 * Description: Initiates local variables.
	 ************************************************************************************/
	public locationCSV(String sL) {
		this.sLocation = sL;
	}
	
	/*************************************************************************************
	 * Method: getURL
	 * Parameter: String
	 * Returns: String
	 * Description: Creates URL from input location.
	 ************************************************************************************/	
	private String getURL(String sLoc) {
		return ("http://api.goeuro.com/api/v2/position/suggest/en/"+sLoc);
	}
	
	/*************************************************************************************
	 * Method: readJSON
	 * Parameter: Reader
	 * Returns: String
	 * Description: Reads JSON from URL.
	 ************************************************************************************/	
	private String readJSON(Reader rd) throws IOException {
		StringBuilder sbBuilder = new StringBuilder();
		int i;
		while ((i = rd.read()) != -1) {
			sbBuilder.append((char) i);
		}
		return sbBuilder.toString();
	}

	/*************************************************************************************
	 * Method: getJSON
	 * Parameter: String
	 * Returns: JSONArray
	 * Description: Receives JSON from URL.
	 ************************************************************************************/	
	private JSONArray getJSON (String sU) {
		InputStream iStream = null;
		BufferedReader brReader =null;
		JSONArray jArray = null;
		try {
			iStream = new URL(sU).openStream();
			brReader = new BufferedReader(new InputStreamReader(iStream, Charset.forName("UTF-8")));
			String sJText = readJSON(brReader);
			jArray = new JSONArray(sJText);
			iStream.close();
		} catch (Exception eX) {
			System.out.println(eX.getMessage());
			System.exit(1);
		}
		return jArray;
	}
	
	/*************************************************************************************
	 * Method: writeCSV
	 * Parameter: JSONArray
	 * Returns: void
	 * Description: Parses JSON and writes in csv format.
	 ************************************************************************************/	
	private void writeCSV (JSONArray JA) {
		String sPath = System.getProperty("user.dir")+File.separator+"geo.csv";
		File fFile = new File(sPath);
		JSONObject JO = null;
		String sid, sname, stype, slatitude, slongitude, sLine;
		// Create csv file if not present
		if (!fFile.exists()) {
			try {
				fFile.getParentFile().mkdir(); 
				fFile.createNewFile();
				FileWriter fW = new FileWriter(fFile,true);
				// Write header
				fW.append("_id,name,type,latitude,longitude");
				fW.append('\n');
				fW.flush();
				fW.close();
			} catch (Exception eX) {
				System.out.println ("Error in creating file");
				System.exit(1);
			}
		}
		try {
			FileWriter fWriter = new FileWriter(fFile,true);
			JSONObject jGeo = null;
			// Loop for all elements
			for (int i=0; i<JA.length(); i++) {
				JO = JA.getJSONObject(i);
				// Look for required keys
				sid = JO.get("_id").toString();
				sname = JO.getString("name");
				stype = JO.getString("type");
				// Look for inner object for lat & long
				jGeo = JO.getJSONObject("geo_position");
				slatitude = jGeo.get("latitude").toString();
				slongitude = jGeo.get("longitude").toString();
				// Completed line
				sLine = sid+","+sname+","+stype+","+slatitude+","+slongitude;
				// Test Display
				//System.out.println(sLine);
				// Write line
				fWriter.append(sLine);
				fWriter.append('\n');
				fWriter.flush();
			}
			
			fWriter.close();
		} catch (Exception eX) {
			System.out.println(eX.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		
		// Get input location
		locationCSV lc = new locationCSV(args[0]);
		// Get URL for input location
		String sURL = lc.getURL (lc.sLocation);
		// Retrieve JSON from GoEuro
		JSONArray jA = lc.getJSON(sURL);
		// Write csv if data received
		if (jA.length() > 0) {
			lc.writeCSV(jA);
		} else {
			System.out.println("Information not found for "+args.toString());
		}
	}
}

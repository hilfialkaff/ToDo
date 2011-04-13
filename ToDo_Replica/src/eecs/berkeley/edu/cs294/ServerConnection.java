package eecs.berkeley.edu.cs294;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.sax.Element;
import android.util.Log;

public class ServerConnection extends Activity {
	
	/*
	 * Check if phone is connected to the internet
	 */
	public boolean isConnected() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connManager.getActiveNetworkInfo();
		
		if(netInfo == null) {
			Log.d("ServerDEBUG", "--------------- No internet connection --------- ");
			return false;
		}
			
		if (netInfo.isConnected()) {
			Log.d("ServerDEBUG", "------------- Connected to internet -------------");
			return true;
		}
		
		return false;
	}

	/*
	 * Pull server changes to local and synchronize it with the local database 
	 */
	public static void pullRemote() {
		Log.d("ServerDEBUG", "pullRemote()");
		retreiveUrl();
	}
	
	/*
	 * Push local changes to the server
	 */
	public static void pushRemote(List<String> oldEntry, List<String> newEntry) {
	
		if(oldEntry == null) {
			Log.d("ServerDEBUG", "old: null");
		}
		else {
			Log.d("ServerDEBUG", "old: " + oldEntry.toString());
		}	
	
		if(newEntry == null) {
			Log.d("ServerDEBUG", "new: null");
		}
		else {
			Log.d("ServerDEBUG", "new: " + newEntry.toString());
		}
	
		// TODO: push to remote database
	}

	private static ArrayList<MyTodo> eventsArrayList = null;

	private static void retreiveUrl()
	{
		HttpClient httpClient = new DefaultHttpClient();

		String xmlResponse;

		try
		{
			// FOR LOCAL DEV: String url = "http://192.168.0.186:3000/events?format=xml";
			String url = "http://10.0.2.2:3000/posts?format=xml";
			Log.d( "ServerDEBUG", "performing get " + url );

			HttpGet method = new HttpGet( new URI(url) );
			HttpResponse response = httpClient.execute(method);
			if ( response != null )
			{
				xmlResponse = getResponse(response.getEntity());
				Log.d( "ServerDEBUG", "received " + xmlResponse);
				// eventsArrayList = parseXMLString(xmlResponse);
			}
			else
			{
				Log.d( "ServerDEBUG", "got a null response" );
			}
		} catch (IOException e) {
			Log.e( "Error", "IOException " + e.getMessage() );
		} catch (URISyntaxException e) {
			Log.e( "Error", "URISyntaxException " + e.getMessage() );
		}

	}

	private static String getResponse( HttpEntity entity )
	{
		String response = "";

		try
		{
			int length = ( int ) entity.getContentLength();
			StringBuffer sb = new StringBuffer( length );
			InputStreamReader isr = new InputStreamReader( entity.getContent(), "UTF-8" );
			char buff[] = new char[length];
			int cnt;
			while ( ( cnt = isr.read( buff, 0, length - 1 ) ) > 0 )
			{
				sb.append( buff, 0, cnt );
			}

			response = sb.toString();
			isr.close();
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
		}

		return response;
	}

	/*
	private static ArrayList<MyTodo> parseXMLString(String xmlString) {
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlString));

			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("myevent");

			eventsArrayList = new ArrayList<MyTodo>(); //Gertig

			//Log.e("Gertig","There are "+nodes.getLength()+" events in this list");

			//Iterate the events
			for (int i = 0; i < nodes.getLength(); i++) {

				Element element = (Element) nodes.item(i);
				eventsArrayList.add(new MyTodo());

				NodeList eventIDNum = element.getElementsByTagName("id");
				Element line = (Element) eventIDNum.item(0);
				eventsArrayList.get(i).eventID = Integer.parseInt(getCharacterDataFromElement(line));

				NodeList eventName = element.getElementsByTagName("name");
				line = (Element) eventName.item(0);
				eventsArrayList.get(i).name = getCharacterDataFromElement(line).trim();


				NodeList eventBudget = element.getElementsByTagName("budget");
				line = (Element) eventBudget.item(0);
				eventsArrayList.get(i).budget = Double.parseDouble(getCharacterDataFromElement(line));

			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return eventsArrayList;

	}

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "?"; //ListActivity will display a ? if a null value is passed to the Rails server
	}*/
}

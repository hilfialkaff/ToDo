package eecs.berkeley.edu.cs294;

/* Should not have android.sax.element */
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ServerConnection extends Activity {
	
	static final String homeurl = "http://10.0.2.2:3000/";	//localhost ip
	static final String todolink = "groups/2/tododetails/";

	static public final int POST_REQUEST = 1;
	static public final int PUT_REQUEST = 2;
	static public final int DELETE_REQUEST = 3;
	
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

		HttpClient httpClient = new DefaultHttpClient();
		String xmlResponse;
		ArrayList<MyTodo> todoList = new ArrayList<MyTodo>();
		String url = homeurl + todolink + "?format=xml"; // For localhost use ip 10.0.2.2

		try
		{	
			Log.d("ServerDEBUG", "performing get " + url );

			HttpGet method = new HttpGet( new URI(url) );
			HttpResponse response = httpClient.execute(method);
			if ( response != null )
			{
				xmlResponse = getResponse(response.getEntity());
				Log.d( "ServerDEBUG", "received " + xmlResponse);
				todoList = parseXMLString(xmlResponse);
				
				/* The server is not empty */
				if(todoList != null) {
					SynchDatabase.synchDb(todoList);
				}
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

	/*
	 * Push local changes to the server
	 */
	public static void pushRemote(List<String> entry, int request_type) {
		
		int retCode = 0;
		
		if(entry == null) {
			Log.d("ServerDEBUG", "entry: null");
		}
		else {
			Log.d("ServerDEBUG", "entry: " + entry.toString() + " with request type: " + request_type);
		}	
		
		switch(request_type) {
		case POST_REQUEST:
			retCode = pushPost(entry);
			break;
		case PUT_REQUEST:
			retCode = pushPut(entry);
			break;
		case DELETE_REQUEST:
			retCode = pushDelete(entry);
			break;
		default:
			Log.d("ServerDEBUG", "! Wrong request_type" + request_type);
		}
		
		if(retCode == 0) {
			Log.d("ServerDEBUG", "Your post was successfully uploaded");
		}
	}
	
	/*
	 * Send a POST request to the server when a new todo is created
	 */
	public static int pushPost(List<String> entry) {
		Log.d("POSTING DEBUG", "POSTING CALEDEDJLIJHFLIHSHFE");
		String url = homeurl + todolink; // For localhost use ip 10.0.2.2
		DefaultHttpClient client = new DefaultHttpClient();
		
		HttpPost postRequest = new HttpPost(url);
		
		JSONObject posts = new JSONObject();
		JSONObject details = new JSONObject();

		/* Setting up the packet to be sent to server */
		try {
			details.put("todo", entry.get(DatabaseHelper.TITLE_INDEX_T));
			details.put("place", entry.get(DatabaseHelper.PLACE_INDEX_T));
			details.put("tag", entry.get(DatabaseHelper.TAG_INDEX_T));
			details.put("note", entry.get(DatabaseHelper.NOTE_INDEX_T));
			details.put("status", entry.get(DatabaseHelper.STATUS_INDEX_T));
			details.put("group", entry.get(DatabaseHelper.GROUP_ID_INDEX_T));

			posts.put("tododetail", details);

			Log.d("ServerDEBUG", "Event JSON = "+ posts.toString());

			StringEntity se = new StringEntity(posts.toString());

			postRequest.setEntity(se);
			postRequest.setHeader("Content-Type","application/json");
			postRequest.setHeader("Accept", "application/json");
			
		} catch (UnsupportedEncodingException e) {
			Log.e("Error",""+e);
			e.printStackTrace();	
			return -1;
		
		} catch (JSONException js) {
			js.printStackTrace();	
			return -1;
		}

		/* Sending... */
		HttpResponse response = null;
		try {
			response = client.execute(postRequest);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e("ClientProtocol",""+e);
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("IO",""+e);
			return -1;
		}

		/* Parsing the response from the server */
		HttpEntity entity = response.getEntity();
		String stringResponse = getResponse(entity);
		
		if (entity != null) {
			try {
				entity.consumeContent();
			} catch (IOException e) {
				Log.e("IO E",""+e);
				e.printStackTrace();
				return -1;
			}
		}

		Log.d("ServerDEBUG", "response: " + stringResponse);
		
		try {
			JSONObject jObject = new JSONObject(stringResponse);
			JSONObject tododetailObject = jObject.getJSONObject("tododetail");
			String railsID = tododetailObject.getString("id");
			
			int pk = Integer.parseInt(entry.get(DatabaseHelper.TD_ID_INDEX_T));
			ToDo_Replica.dh.update_to_do(pk, null, null, null, null, 0, null, null, null, null, railsID);
		} catch (Exception e) {
			Log.e("JSON E", ""+e);
			e.printStackTrace();
			return -1;
		}

		return 0;
	}
	
	/*
	 * Send a PUT request to the server in the case of a todo being edited
	 */
	public static int pushPut(List<String> entry) {
		// For localhost use ip 10.0.2.2
		String url = homeurl + todolink + entry.get(DatabaseHelper.TO_DO_RAILS_ID_INDEX_T); 
		
		Log.d("ServerDEBUG", "PUT to " + url);
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPut putRequest = new HttpPut(url);
		
		JSONObject tododetail= new JSONObject();
		JSONObject details = new JSONObject();

		/* Setting up the packet to be sent to server */
		try {
			details.put("todo", entry.get(DatabaseHelper.TITLE_INDEX_T));
			details.put("place", entry.get(DatabaseHelper.PLACE_INDEX_T));
			details.put("tag", entry.get(DatabaseHelper.TAG_INDEX_T));
			details.put("note", entry.get(DatabaseHelper.NOTE_INDEX_T));
			details.put("status", entry.get(DatabaseHelper.STATUS_INDEX_T));
			details.put("group", entry.get(DatabaseHelper.GROUP_ID_INDEX_T));

			tododetail.put("tododetail", details);

			Log.d("ServerDEBUG", "Event JSON = "+ tododetail.toString());

			StringEntity se = new StringEntity(tododetail.toString());

			putRequest.setEntity(se);
			putRequest.setHeader("Content-Type","application/json");
			putRequest.setHeader("Accept", "application/json");
			
		} catch (UnsupportedEncodingException e) {
			Log.e("Error",""+e);
			e.printStackTrace();	
			return -1;
		
		} catch (JSONException js) {
			js.printStackTrace();	
			return -1;
		}

		/* Sending... */
		HttpResponse response = null;
		try {
			response = client.execute(putRequest);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e("ClientProtocol",""+e);
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("IO",""+e);
			return -1;
		}

		/* Parsing the response from the server */
		HttpEntity entity = response.getEntity();
		String stringResponse = getResponse(entity);
		if (entity != null) {
			try {
				entity.consumeContent();
			} catch (IOException e) {
				Log.e("IO E",""+e);
				e.printStackTrace();
				return -1;
			}
		}

		Log.d("ServerDEBUG", "response: " + stringResponse);

		return 0;
	}
	
	/*
	 * Send a DELETE request to the server in the case of a todo being deleted
	 */
	public static int pushDelete(List<String> entry) {
		String url = homeurl + todolink + entry.get(DatabaseHelper.TO_DO_RAILS_ID_INDEX_T);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpDelete deleteRequest = new HttpDelete(url);
		HttpResponse response = null;

		Log.d("ServerDEBUG", "DELETE to " + url);
		
		/* Sending... */
		try {
			response = client.execute(deleteRequest);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e("ClientProtocol",""+e);
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("IO",""+e);
			return -1;
		}

		/* Parsing the response from the server */
		HttpEntity entity = response.getEntity();
		String stringResponse = getResponse(entity);
		if (entity != null) {
			try {
				entity.consumeContent();
			} catch (IOException e) {
				Log.e("IO E",""+e);
				e.printStackTrace();
				return -1;
			}
		}

		Log.d("ServerDEBUG", "response: " + stringResponse);
		return 0;
	}	
	
	/*
	 * Get an xml response from the server
	 */
	private static String getResponse( HttpEntity entity )
	{
		String response = "";

		try
		{
			int length = (int) entity.getContentLength();
			StringBuffer sb = new StringBuffer( length );
			InputStreamReader isr = new InputStreamReader(entity.getContent(), "UTF-8");
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
	 * Parse the XML from the server into a list of TODOs
	 * 
	 * TODO: Shouldn't be any hardcoding; Need to discuss w/ the actual server implementation
	 */
	private static ArrayList<MyTodo> parseXMLString(String xmlString) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		ArrayList<MyTodo> todoList = new ArrayList<MyTodo>();

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource();

			is.setCharacterStream(new StringReader(xmlString));
			Document doc = builder.parse(is);
			NodeList nodes = doc.getElementsByTagName("tododetail");

			Log.d("ServerDEBUG", "Begin iterating nodes");

			/* Iterating each todo node in the xml */
			for (int i = 0; i < nodes.getLength(); i++) {
				Log.d("ServerDEBUG", "iteration " + i);

				Node item = nodes.item(i);
				NodeList properties = item.getChildNodes();
				todoList.add(new MyTodo());

				/* Iterating each field (ie: name, content, etc) in a todo */
				for (int j=0;j<properties.getLength();j++){

					Node property = properties.item(j);
					String name = property.getNodeName();

					if(property.getFirstChild() == null) {
						continue;
					}
					
					else if (name.equalsIgnoreCase("group_id")){
						todoList.get(i).setTodoGroupId((property.getFirstChild().getNodeValue()));
					} 
					
					else if (name.equalsIgnoreCase("updated-at")){
						String timestamp = property.getFirstChild().getNodeValue();
						timestamp = timestamp.replaceAll("[:-]", ""); // Hack to eliminate UTC's parser's dumbness
						todoList.get(i).setTodoTimestamp(timestamp);
					} 
					
					else if (name.equalsIgnoreCase("tag")){
						todoList.get(i).setTodoTag((property.getFirstChild().getNodeValue()));
					} 
					
					else if (name.equalsIgnoreCase("id")){
						todoList.get(i).setTodoRailsID((property.getFirstChild().getNodeValue()));
					} 
					
					else if (name.equalsIgnoreCase("note")){
						todoList.get(i).setTodoNote((property.getFirstChild().getNodeValue()));
					}
					
					else if (name.equalsIgnoreCase("title")){
						todoList.get(i).setTodoTitle((property.getFirstChild().getNodeValue()));
					} 
					
					else if (name.equalsIgnoreCase("place")){
						todoList.get(i).setTodoPlace((property.getFirstChild().getNodeValue()));
					}
					
					else if (name.equalsIgnoreCase("status")){
						todoList.get(i).setTodoStatus((property.getFirstChild().getNodeValue()));
					}
					
					else if (name.equalsIgnoreCase("priority")){
						todoList.get(i).setTodoPriority((property.getFirstChild().getNodeValue()));
					}
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		/* For debugging purpose */
		for(Iterator<MyTodo> it = todoList.iterator(); it.hasNext();) {
			MyTodo todo = it.next();
			todo.printMembers();
		}

		return todoList;
	}
}
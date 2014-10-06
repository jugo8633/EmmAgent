package org.wso2.emm.agent.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.wso2.emm.agent.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.util.Log;

public class HTTPConnectorUtils {
	
	public static String TAG = ServerUtils.class.getSimpleName();
	
	private static final int MAX_ATTEMPTS = 2;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();
	
	public static HttpClient getCertifiedHttpClient(Context context) {
		try {
			HttpClient client = null;
			if (CommonUtilities.SERVER_PROTOCOL.toLowerCase()
					.equals("https://")) {
				Log.e("","in");
				KeyStore localTrustStore = KeyStore.getInstance("BKS");
				InputStream in = context.getResources().openRawResource(
						R.raw.emm_truststore);
				localTrustStore.load(in,
						CommonUtilities.TRUSTSTORE_PASSWORD.toCharArray());
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				SSLSocketFactory sslSocketFactory = new SSLSocketFactory(
						localTrustStore);
				schemeRegistry.register(new Scheme("https", sslSocketFactory,
						443));
				HttpParams params = new BasicHttpParams();
				ClientConnectionManager cm = new ThreadSafeClientConnManager(
						params, schemeRegistry);
				client = new DefaultHttpClient(cm, params);
			} else {
				Log.e("","out");
				client = new DefaultHttpClient();
			}

			return client;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<String, String>  getClientKey(String username, String password, Context context) {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> response = new HashMap<String, String>();
		
		params.put("username", username);
		params.put("password", password);
		
		try {
			response = sendWithTimeWait("devices/clientkey", params,
					"POST", context);
		} catch (Exception ex) {
			ex.printStackTrace();
			return response;
		}
		return response;
	}

	public static Map<String, String> sendWithTimeWait(String epPostFix,
			Map<String, String> params, String option, Context context) {
		Map<String, String> response = null;
		Map<String, String> responseFinal = null;
		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			Log.d(TAG, "Attempt #" + i + " to register");
			try {

				response = postData(context, epPostFix, params);
				if (response != null && !response.equals(null)) {
					responseFinal = response;
				}
				String message = context.getString(R.string.server_registered);
				Log.v("Check Reg Success", message.toString());

				return responseFinal;
			} catch (Exception e) {
				Log.e(TAG, "Failed to register on attempt " + i, e);
				if (i == MAX_ATTEMPTS) {
					break;
				}

				return responseFinal;
			}
		}
		String message = context.getString(R.string.server_register_error,
				MAX_ATTEMPTS);

		return responseFinal;
	}

	public static String getResponseBody(HttpResponse response) {

		String response_text = null;
		HttpEntity entity = null;
		try {
			entity = response.getEntity();
			response_text = _getResponseBody(entity);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException e1) {
				}
			}
		}
		return response_text;
	}

	public static String _getResponseBody(final HttpEntity entity)
			throws IOException, ParseException {

		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}

		InputStream instream = entity.getContent();

		if (instream == null) {
			return "";
		}

		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(

			"HTTP entity too large to be buffered in memory");
		}

		String charset = getContentCharSet(entity);

		if (charset == null) {

			charset = HTTP.DEFAULT_CONTENT_CHARSET;

		}

		Reader reader = new InputStreamReader(instream, charset);

		StringBuilder buffer = new StringBuilder();

		try {

			char[] tmp = new char[1024];

			int l;

			while ((l = reader.read(tmp)) != -1) {

				buffer.append(tmp, 0, l);

			}

		} finally {

			reader.close();

		}

		return buffer.toString();

	}

	public static String getContentCharSet(final HttpEntity entity)
			throws ParseException {

		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}

		String charset = null;

		if (entity.getContentType() != null) {

			HeaderElement values[] = entity.getContentType().getElements();

			if (values.length > 0) {

				NameValuePair param = values[0].getParameterByName("charset");

				if (param != null) {

					charset = param.getValue();

				}

			}

		}

		return charset;

	}

	public static Map<String, String> postData(Context context, String url,
			Map<String, String> params) {
		// Create a new HttpClient and Post Header
		Map<String, String> response_params = new HashMap<String, String>();
		HttpClient httpclient = getCertifiedHttpClient(context);

		String endpoint = CommonUtilities.SERVER_URL + url;

		SharedPreferences mainPref = context.getSharedPreferences("com.mdm",
				Context.MODE_PRIVATE);
		String ipSaved = mainPref.getString("ip", "");

		if (ipSaved != null && ipSaved != "") {
			endpoint = CommonUtilities.SERVER_PROTOCOL + ipSaved + ":"
					+ CommonUtilities.SERVER_PORT
					+ CommonUtilities.SERVER_APP_ENDPOINT + url;
		}
		Log.v(TAG, ipSaved+"Posting '" + params.toString() + "' to " + endpoint);
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
					.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}

		String body = bodyBuilder.toString();
		Log.v(TAG, "Posting '" + body + "' to " + endpoint);
		byte[] postData = body.getBytes();

		HttpPost httppost = new HttpPost(endpoint);
		httppost.setHeader("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		httppost.setHeader("Accept", "*/*");
		httppost.setHeader("User-Agent", "Mozilla/5.0 ( compatible ), Android");

		try {
			// Add your data
			httppost.setEntity(new ByteArrayEntity(postData));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			response_params.put("response", getResponseBody(response));
			response_params.put("status",
					String.valueOf(response.getStatusLine().getStatusCode()));
			return response_params;
		} catch (ClientProtocolException e) {
			Map<String, String> reply = new HashMap<String, String>();
			reply.put("status", CommonUtilities.INTERNAL_SERVER_ERROR);
			return reply;
		} catch (IOException e) {
			Map<String, String> reply = new HashMap<String, String>();
			reply.put("status", CommonUtilities.INTERNAL_SERVER_ERROR);
			return reply;
		}
	}

}

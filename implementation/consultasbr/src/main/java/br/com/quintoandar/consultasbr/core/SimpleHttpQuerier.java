package br.com.quintoandar.consultasbr.core;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHttpQuerier {
	private static final Logger log = LoggerFactory.getLogger(SimpleHttpQuerier.class);

	private static SimpleDateFormat sdfEn = new SimpleDateFormat("EEE', 'dd-MMM-yyyy HH:mm:ss z");

	static {
		sdfEn.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	protected ClientConnectionManager connMan;

	protected DefaultHttpClient client;

	public SimpleHttpQuerier() {
		super();

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		// schemeRegistry.register(new Scheme("https", 443,
		// SSLSocketFactory.getSocketFactory()));

		try {
			SSLSocketFactory sslsf = new SSLSocketFactory(new TrustStrategy() {
				public boolean isTrusted(final X509Certificate[] chain, String authType) throws CertificateException {
					// Oh, I am easy...
					return true;
				}
			});
			schemeRegistry.register(new Scheme("https", 443, sslsf));
		} catch (Throwable e1) {
			e1.printStackTrace();
		}

		connMan = new ThreadSafeClientConnManager(schemeRegistry);
		((ThreadSafeClientConnManager) connMan).setMaxTotal(50);
		((ThreadSafeClientConnManager) connMan).setDefaultMaxPerRoute(10);
		client = new DefaultHttpClient(connMan);
		client.setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
				// System.out.println("\tCookies: " +
				// client.getCookieStore().toString());
				for (Header c : response.getAllHeaders()) {
					forcaAtualizacaoDeCookies(c);
					if (c.getName().equals("Location")) {
						log.debug("Location: " + c.getValue());
					}
					// log.info(c.getName()+": " + c.getValue());
				}
				boolean isRedirect = false;
				try {
					isRedirect = super.isRedirected(request, response, context);
				} catch (ProtocolException e) {
					e.printStackTrace();
				}
				// if (!isRedirect) {
				// int responseCode = response.getStatusLine().getStatusCode();
				// if (responseCode == 301 || responseCode == 302) {
				// return true;
				// }
				// }
				return isRedirect;
			}

		});
	}

	private void forcaAtualizacaoDeCookies(Header c) {
		if (c.getName().equalsIgnoreCase("set-cookie")) {
			String rawCookie = c.getValue();
			BasicClientCookie ck = parseCookie(rawCookie);

			if (shouldPersistCookie(ck)) {
				Iterator<Cookie> it = client.getCookieStore().getCookies().iterator();
				while (it.hasNext()) {
					Cookie ck2 = it.next();
					if (ck2.equals(ck.getName()) && ck2.getDomain().equals(ck.getDomain()) && ck2.getPath().equals(ck.getPath())) {
						// temCookie = true;
						it.remove();
					}
				}
				client.getCookieStore().addCookie(ck);
			}
		}
	}

	private static BasicClientCookie parseCookie(String rawCookie) {
		log.info("CookieRaw: " + rawCookie);
		String[] vals = rawCookie.split("; ?");
		Map<String, String> cookieMap = new HashMap<String, String>();
		boolean first = true;
		for (String val : vals) {
			String[] valSplitted = val.split("=");
			if (valSplitted.length > 0) {
				if (valSplitted.length > 1) {
					if (first) {
						cookieMap.put("name", valSplitted[0]);
						cookieMap.put("value", valSplitted[1]);
						log.info("\t" + cookieMap.get("name") + "=" + cookieMap.get("value"));
					} else {
						cookieMap.put(valSplitted[0].toLowerCase(), valSplitted[1]);
						log.info("\t" + valSplitted[0].toLowerCase() + "=" + cookieMap.get(valSplitted[0].toLowerCase()));
					}
				} else {
					if (first) {
						cookieMap.put("name", valSplitted[0]);
						cookieMap.put("value", "");
						log.info("\t" + cookieMap.get("name") + "=" + cookieMap.get("value"));
					} else {
						cookieMap.put(valSplitted[0].toLowerCase(), null);
						log.info("\t" + valSplitted[0].toLowerCase() + "=null");
					}
				}
			}
			first = false;
		}
		BasicClientCookie ck = new BasicClientCookie(cookieMap.get("name"), cookieMap.get("value"));
		ck.setDomain(cookieMap.get("domain"));
		ck.setPath(cookieMap.containsKey("path") ? cookieMap.get("path") : "/");
		if (cookieMap.get("expires") != null && !cookieMap.get("expires").trim().isEmpty())
			try {
				ck.setExpiryDate(sdfEn.parse(cookieMap.get("expires")));
			} catch (ParseException e) {
				try {
					ck.setExpiryDate(sdfEn.parse(cookieMap.get("expires")));
				} catch (ParseException e2) {
					log.error("Error parsing cookie expiration date", e);
					log.error("Error parsing cookie expiration date", e2);
				}
			}
		return ck;
	}

	protected boolean shouldPersistCookie(BasicClientCookie ck) {
		return false;
	}

	public static String asQueryParams(Map<String, String> map) {
		return asQueryParams(map, "UTF-8");
	}

	public static String asQueryParams(Map<String, String> map, String charset) {
		StringBuilder build = new StringBuilder("");
		for (Entry<String, String> entry : map.entrySet()) {
			if (build.length() > 0) {
				build.append("&");
			}
			try {
				build.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), charset));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Charset '" + charset + "' not supported by this java env", e);
			}
		}
		if (build.length() > 0) {
			return "?" + build.toString();
		}
		return "";
	}

	public static HttpEntity getMapEntity(String charset, Map<String, String> map, String boundary) {
		if (boundary != null) {
			try {
				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, boundary, Charset.forName(charset));
				for (Map.Entry<String, String> ent : map.entrySet()) {
					String converted = ent.getValue();
					// if (converted != null && charset != null) {
					// return new String(converted.getBytes("UTF-8"),
					// Charset.forName(charset));
					// }

					entity.addPart(ent.getKey(), new StringBody(converted, Charset.forName(charset)));
					// formparams
					// .add(new BasicNameValuePair(ent.getKey(),
					// ent.getValue()));
				}
				return entity;
			} catch (UnsupportedEncodingException e) {
				log.error("Error creating HttpEntity from map", e);
			}
		} else {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> ent : map.entrySet()) {
				formparams.add(new BasicNameValuePair(ent.getKey(), ent.getValue()));
			}
			try {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, charset);
				return entity;
			} catch (UnsupportedEncodingException e) {
				log.error("Error creating HttpEntity from map", e);
			}
		}
		return null;
	}

	public static HttpEntity asKeyValueEntity(String charset, String... keysValue) {
		Map<String, String> map = new HashMap<String, String>();
		for (int idx = 0; idx < (keysValue.length - 1); idx = idx + 2) {
			map.put(keysValue[idx], keysValue[idx + 1]);
		}
		return getMapEntity(charset, map, null);
	}

	public static String getCookieValue(Header[] allHeaders, String cookieName) {
		for (Header h : allHeaders) {
			if (h.getName().equalsIgnoreCase("set-cookie")) {
				Cookie ck = parseCookie(h.getValue());
				if (ck.getName().equals(cookieName)) {
					return ck.getValue();
				}
			}
		}
		return null;
	}

	protected void novoCookie(String name, String value) {
		Cookie ck = new BasicClientCookie(name, value);
		client.getCookieStore().addCookie(ck);
	}

	protected String getCookieValue(String string) {
		if (client.getCookieStore() != null) {
			for (Cookie ck : client.getCookieStore().getCookies()) {
				if (ck.getName().equals(string)) {
					return ck.getValue();
				}
			}
		}
		return null;
	}

	protected void attachCookiesFromStore(AbstractHttpMessage ahm) {
		StringBuffer cookieHeaderStr = new StringBuffer();
		for (Cookie c : client.getCookieStore().getCookies()) {
			cookieHeaderStr.append(c.getName()).append("=").append(c.getValue()).append("; ");
		}
		ahm.addHeader("Cookie", cookieHeaderStr.toString().trim().replaceAll(";$", ""));
	}

	public static int copy(InputStream is, OutputStream out, int maxTries) throws IOException, InterruptedException {
		int tries = 0;
		int totalRead = 0;
		int avail = 0;
		if (maxTries < 0) {
			throw new IllegalArgumentException("MaxTries must be greater than zero");
		}
		while (avail > 0 || (tries < maxTries || tries == 0)) {
			avail = is.available();
			byte[] buf = new byte[2048];
			if (buf.length > avail) {
				buf = new byte[avail];
				if (avail == 0) {
					tries++;
					if (maxTries > 0) {
						Thread.sleep(300);
					}
					continue;
				}
			}
			tries = 0;
			is.read(buf);
			out.write(buf);
			totalRead += buf.length;
		}
		return totalRead;
	}
}

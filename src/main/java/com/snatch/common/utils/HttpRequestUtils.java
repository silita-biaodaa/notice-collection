package com.snatch.common.utils;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.Map;

public class HttpRequestUtils {
	private static String addParamToUrl(String baseUrl, Map<String, String> params) {
        if (params == null) {
            return baseUrl;
        }

        StringBuilder builder = new StringBuilder(baseUrl);
        if (baseUrl.contains("?")) {
            builder.append("&");
        } else {
            builder.append("?");
        }

        int i = 0;
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null) {
                continue;
            }

            if (i != 0) {
                builder.append('&');
            }

            builder.append(key);
            builder.append('=');
            builder.append(encode(value));

            i++;
        }

        return builder.toString();
    }

    private static String encode(String input) {
        if (input == null) {
            return "";
        }

        try {
            return URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return input;
    }
    
    public static String get(String url){
    	System.out.println("baseUrl: " + url);
    	return startConnect(url,"GET");
    }
    
    public static String post(String url,Map<String,String> params){
    	String sendUrl = addParamToUrl(url, params);
    	System.out.println("baseUrl: " + sendUrl);
    	return startConnect(sendUrl,"POST");
    }

    private static String startConnect(String strUrl,String method) {
		StringBuffer sbResult = new StringBuffer();

		try {
			HttpURLConnection http = null;
			URL url = new URL(strUrl);

			if (url.getProtocol().toLowerCase(Locale.getDefault()).equals("https")) {
				trustAllHosts();
				HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
				https.setHostnameVerifier(DO_NOT_VERIFY);
				http = https;
			} else {
				http = (HttpURLConnection) url.openConnection();
			}

			http.setDoInput(true);
			http.setConnectTimeout(10000);
			http.setRequestMethod(method);
			http.setRequestProperty("accept", "*/*");
			int resCode = http.getResponseCode();
			System.out.println("responseCode: " + resCode);

			if (resCode == 200) {
				InputStream input = http.getInputStream();
				BufferedReader data = new BufferedReader(new InputStreamReader(input, "UTF-8"));
				String strLine = "";

				while ((strLine = data.readLine()) != null) {
					sbResult.append(strLine);
				}

				input.close();
				http.disconnect();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sbResult.toString();
	}
    
    private static void trustAllHosts() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[]
				{};
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException { }
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException { }
		} };

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	
	public static String fetchContent(String fetchUrl, String charSet) {
		System.out.println("fetchContent: " + fetchUrl);
		HttpURLConnection conn = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer("");
		
		try {
			URL url = new URL(fetchUrl);
			
			conn = (HttpURLConnection) url.openConnection();
			conn.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0");
			conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.addRequestProperty("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
			InputStreamReader ir = new InputStreamReader(conn.getInputStream(), charSet);
			br = new BufferedReader(ir);
			String line = "";
			
			while ((line = br.readLine()) != null) {
				sb.append(line+"\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {}
			
			if (conn != null) {
				conn.disconnect();
			}
		}
		
		return sb.toString();
	}
}
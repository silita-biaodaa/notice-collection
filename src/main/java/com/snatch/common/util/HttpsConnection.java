package com.snatch.common.util;
import javax.net.ssl.*;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by 91567 on 2018/3/1.
 */
public class HttpsConnection {
    private static class TrustAnyTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static String connect(String url, String encoding) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        byte[] buffer = new byte[4096];
        String str_return = "";
        try {
//            URL console = new URL(url);
            URL console = new URL(new String(url.getBytes(encoding)));

            HttpURLConnection conn = (HttpURLConnection) console.openConnection();
            //如果是https
            if (conn instanceof HttpsURLConnection) {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
                ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
                ((HttpsURLConnection) conn).setHostnameVerifier(new TrustAnyHostnameVerifier());
            }
//            conn.setRequestProperty("Content-type", "text/html");
//            conn.setRequestProperty("Accept-Charset", "GBK");
//            conn.setRequestProperty("contentType", "GBK");
//            conn.setRequestMethod("POST");
//            conn.setDoOutput(true);
//            conn.setRequestProperty("User-Agent", "directclient");
//            PrintWriter outdate = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"));
//            outdate.println(url);
//            outdate.close();
            conn.connect();
            InputStream is = conn.getInputStream();
            DataInputStream indata = new DataInputStream(is);
            String ret = "";

            while (ret != null) {
                ret = indata.readLine();
                if (ret != null && !ret.trim().equals("")) {
                    str_return = str_return + new String(ret.getBytes("ISO-8859-1"), "utf-8");
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
            try {
                out.close();
            } catch (Exception e) {
            }
        }
        return str_return;
    }
}

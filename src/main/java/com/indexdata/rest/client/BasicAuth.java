package com.indexdata.rest.client;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * Sets basic credentials on a connection if any provided in authority part of URL
 * 
 * @author Niels Erik
 *
 */
public class BasicAuth  {
  
    private static final Logger logger = Logger.getLogger(BasicAuth.class);

    public void setCredentials (HttpURLConnection conn) {
      String authority = conn.getURL().getAuthority();
      if (authority != null && authority.contains(":")) {
        String un = extractUn(authority);
        String pw = extractPw(authority);
        try {
          final byte[] encodedBytes = Base64.encodeBase64((un + ':' + new String(pw)).getBytes("UTF-8"));
          final String encoded = new String(encodedBytes, "UTF-8");
          conn.setRequestProperty("Authorization", "Basic " + encoded);
        } catch (UnsupportedEncodingException uee) {
          logger.error("Unsupported encoding exception while encoding credentials for basic auth");
        }
      } 
    }

    private String extractUn (String authurl) {
      Pattern p = Pattern.compile("([^:]*):.*");
      Matcher m = p.matcher(authurl);
      if (m.find()) {
        return m.group(1);
      } else {
        return "";
      }
    }
    
    private String extractPw (String authurl) {
      Pattern p = Pattern.compile("[^:]*:([^@]*)@.*");
      Matcher m = p.matcher(authurl);
      if (m.find()) {
        return m.group(1);
      } else {
        return "";
      }
    }
}

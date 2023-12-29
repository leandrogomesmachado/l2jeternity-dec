package l2e.gameserver.model.entity.mods.facebook;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public interface ActionsExtractor {
   Logger _log = Logger.getLogger(ActionsExtractor.class.getName());
   SimpleDateFormat FACEBOOK_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

   void extractData(String var1) throws IOException;

   default JSONObject call(URL apiCallURL) throws IOException {
      HttpURLConnection httpConn = (HttpURLConnection)apiCallURL.openConnection();
      InputStream inputStream = httpConn.getInputStream();
      String result = IOUtils.toString(inputStream, "UTF-8");
      inputStream.close();
      httpConn.disconnect();
      return new JSONObject(result);
   }

   default long parseFacebookDate(String date) throws ParseException {
      return FACEBOOK_DATE_FORMAT.parse(date).getTime();
   }
}

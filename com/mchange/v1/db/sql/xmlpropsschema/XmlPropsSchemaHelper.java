package com.mchange.v1.db.sql.xmlpropsschema;

import com.mchange.v1.xmlprops.DomXmlPropsParser;
import com.mchange.v1.xmlprops.XmlPropsException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.StringTokenizer;

public class XmlPropsSchemaHelper {
   Properties props;

   public XmlPropsSchemaHelper(InputStream var1) throws XmlPropsException {
      DomXmlPropsParser var2 = new DomXmlPropsParser();
      this.props = var2.parseXmlProps(var1);
   }

   public PreparedStatement prepareXmlStatement(Connection var1, String var2) throws SQLException {
      return var1.prepareStatement(this.getKey(var2));
   }

   public void executeViaStatement(Statement var1, String var2) throws SQLException {
      var1.executeUpdate(this.getKey(var2));
   }

   public StringTokenizer getItems(String var1) {
      String var2 = this.getKey(var1);
      return new StringTokenizer(var2, ", \t\r\n");
   }

   public String getKey(String var1) {
      return this.props.getProperty(var1).trim();
   }
}

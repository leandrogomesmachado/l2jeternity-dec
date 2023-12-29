package com.mchange.v1.db.sql;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlSchema implements Schema {
   private static final int CREATE = 0;
   private static final int DROP = 1;
   List createStmts;
   List dropStmts;
   Map appMap;

   public XmlSchema(URL var1) throws SAXException, IOException, ParserConfigurationException {
      this.parse(var1.openStream());
   }

   public XmlSchema(InputStream var1) throws SAXException, IOException, ParserConfigurationException {
      this.parse(var1);
   }

   public XmlSchema() {
   }

   public void parse(InputStream var1) throws SAXException, IOException, ParserConfigurationException {
      this.createStmts = new ArrayList();
      this.dropStmts = new ArrayList();
      this.appMap = new HashMap();
      InputSource var2 = new InputSource();
      var2.setByteStream(var1);
      var2.setSystemId(XmlSchema.class.getResource("schema.dtd").toExternalForm());
      SAXParser var3 = SAXParserFactory.newInstance().newSAXParser();
      XmlSchema.MySaxHandler var4 = new XmlSchema.MySaxHandler();
      var3.parse(var2, var4);
   }

   private void doStatementList(List var1, Connection var2) throws SQLException {
      if (var1 != null) {
         Statement var3 = null;

         try {
            var3 = var2.createStatement();
            Iterator var4 = var1.iterator();

            while(var4.hasNext()) {
               var3.executeUpdate((String)var4.next());
            }

            var2.commit();
         } catch (SQLException var8) {
            ConnectionUtils.attemptRollback(var2);
            var8.fillInStackTrace();
            throw var8;
         } finally {
            StatementUtils.attemptClose(var3);
         }
      }
   }

   @Override
   public String getStatementText(String var1, String var2) {
      XmlSchema.SqlApp var3 = (XmlSchema.SqlApp)this.appMap.get(var1);
      String var4 = null;
      if (var3 != null) {
         var4 = var3.getStatementText(var2);
      }

      return var4;
   }

   @Override
   public void createSchema(Connection var1) throws SQLException {
      this.doStatementList(this.createStmts, var1);
   }

   @Override
   public void dropSchema(Connection var1) throws SQLException {
      this.doStatementList(this.dropStmts, var1);
   }

   public static void main(String[] var0) {
      try {
         new XmlSchema(XmlSchema.class.getResource("/com/mchange/v1/hjug/hjugschema.xml"));
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   class MySaxHandler extends HandlerBase {
      int state = -1;
      boolean in_statement = false;
      boolean in_comment = false;
      StringBuffer charBuff = null;
      XmlSchema.SqlApp currentApp = null;
      String currentStmtName = null;

      @Override
      public void startElement(String var1, AttributeList var2) {
         if (var1.equals("create")) {
            this.state = 0;
         } else if (var1.equals("drop")) {
            this.state = 1;
         } else if (var1.equals("statement")) {
            this.in_statement = true;
            this.charBuff = new StringBuffer();
            if (this.currentApp != null) {
               int var3 = 0;

               for(int var4 = var2.getLength(); var3 < var4; ++var3) {
                  String var5 = var2.getName(var3);
                  if (var5.equals("name")) {
                     this.currentStmtName = var2.getValue(var3);
                     break;
                  }
               }
            }
         } else if (var1.equals("comment")) {
            this.in_comment = true;
         } else if (var1.equals("application")) {
            int var7 = 0;

            for(int var8 = var2.getLength(); var7 < var8; ++var7) {
               String var9 = var2.getName(var7);
               if (var9.equals("name")) {
                  String var6 = var2.getValue(var7);
                  this.currentApp = (XmlSchema.SqlApp)XmlSchema.this.appMap.get(var6);
                  if (this.currentApp == null) {
                     this.currentApp = XmlSchema.this.new SqlApp();
                     XmlSchema.this.appMap.put(var6.intern(), this.currentApp);
                  }
                  break;
               }
            }
         }
      }

      @Override
      public void characters(char[] var1, int var2, int var3) throws SAXException {
         if (!this.in_comment && this.in_statement) {
            this.charBuff.append(var1, var2, var3);
         }
      }

      @Override
      public void endElement(String var1) {
         if (var1.equals("statement")) {
            String var2 = this.charBuff.toString().trim();
            if (this.state == 0) {
               XmlSchema.this.createStmts.add(var2);
            } else if (this.state == 1) {
               XmlSchema.this.dropStmts.add(var2);
            } else if (this.currentApp != null && this.currentStmtName != null) {
               this.currentApp.setStatementText(this.currentStmtName, var2);
            }
         } else if (var1.equals("create") || var1.equals("drop")) {
            this.state = -1;
         } else if (var1.equals("comment")) {
            this.in_comment = false;
         } else if (var1.equals("application")) {
            this.currentApp = null;
         }
      }

      @Override
      public void warning(SAXParseException var1) {
         System.err.println("[Warning] " + var1.getMessage());
      }

      @Override
      public void error(SAXParseException var1) {
         System.err.println("[Error] " + var1.getMessage());
      }

      @Override
      public void fatalError(SAXParseException var1) throws SAXException {
         System.err.println("[Fatal Error] " + var1.getMessage());
         throw var1;
      }
   }

   class SqlApp {
      Map stmtMap = new HashMap();

      public void setStatementText(String var1, String var2) {
         this.stmtMap.put(var1, var2);
      }

      public String getStatementText(String var1) {
         return (String)this.stmtMap.get(var1);
      }
   }
}

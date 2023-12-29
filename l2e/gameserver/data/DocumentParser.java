package l2e.gameserver.data;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.util.file.filter.XMLFilter;
import l2e.gameserver.Config;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public abstract class DocumentParser {
   protected final Logger _log = Logger.getLogger(this.getClass().getName());
   private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
   private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
   private static final XMLFilter xmlFilter = new XMLFilter();
   private File _currentFile;
   private Document _currentDocument;
   private FileFilter _currentFilter = null;

   public abstract void load();

   protected void parseDatapackFile(String path) {
      this.parseFile(new File(Config.DATAPACK_ROOT, path), false);
   }

   protected void parseFile(File f, boolean isReload) {
      if (!this.getCurrentFileFilter().accept(f)) {
         this._log.warning(this.getClass().getSimpleName() + ": Could not parse " + f.getName() + " is not a file or it doesn't exist!");
      } else {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware(true);
         dbf.setValidating(true);
         dbf.setIgnoringComments(true);
         this._currentDocument = null;
         this._currentFile = f;

         try {
            dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(new DocumentParser.XMLErrorHandler());
            this._currentDocument = db.parse(f);
         } catch (Exception var5) {
            this._log.warning(this.getClass().getSimpleName() + ": Could not parse " + f.getName() + " file: " + var5.getMessage());
            return;
         }

         if (isReload) {
            this.reloadDocument();
         } else {
            this.parseDocument();
         }
      }
   }

   public File getCurrentFile() {
      return this._currentFile;
   }

   protected Document getCurrentDocument() {
      return this._currentDocument;
   }

   protected boolean parseDirectory(File file) {
      return this.parseDirectory(file, false, false);
   }

   protected boolean parseDirectory(String path, boolean isReload) {
      return this.parseDirectory(new File(path), false, isReload);
   }

   protected boolean parseDirectory(String path, boolean recursive, boolean isReload) {
      return this.parseDirectory(new File(path), recursive, isReload);
   }

   protected boolean parseDirectory(File dir, boolean recursive, boolean isReload) {
      if (!dir.exists()) {
         if (Config.DEBUG) {
            this._log.warning(this.getClass().getSimpleName() + ": Folder " + dir.getAbsolutePath() + " doesn't exist!");
         }

         return false;
      } else {
         File[] listOfFiles = dir.listFiles();

         for(File f : listOfFiles) {
            if (recursive && f.isDirectory()) {
               this.parseDirectory(f, recursive, isReload);
            } else if (this.getCurrentFileFilter().accept(f)) {
               this.parseFile(f, isReload);
            }
         }

         return true;
      }
   }

   protected void parseDocument(Document doc) {
   }

   protected abstract void parseDocument();

   protected abstract void reloadDocument();

   protected static int parseInt(NamedNodeMap n, String name) {
      return Integer.parseInt(n.getNamedItem(name).getNodeValue());
   }

   protected static Integer parseInteger(NamedNodeMap n, String name) {
      return Integer.valueOf(n.getNamedItem(name).getNodeValue());
   }

   protected static int parseInt(Node n) {
      return Integer.parseInt(n.getNodeValue());
   }

   protected static Integer parseInteger(Node n) {
      return Integer.valueOf(n.getNodeValue());
   }

   protected Integer parseInteger(NamedNodeMap attrs, String name, Integer defaultValue) {
      return this.parseInteger(attrs.getNamedItem(name), defaultValue);
   }

   protected Integer parseInteger(Node node, Integer defaultValue) {
      return node != null ? Integer.valueOf(node.getNodeValue()) : defaultValue;
   }

   protected static Long parseLong(NamedNodeMap n, String name) {
      return Long.valueOf(n.getNamedItem(name).getNodeValue());
   }

   protected static float parseFloat(NamedNodeMap n, String name) {
      return Float.parseFloat(n.getNamedItem(name).getNodeValue());
   }

   protected static Double parseDouble(NamedNodeMap n, String name) {
      return Double.valueOf(n.getNamedItem(name).getNodeValue());
   }

   protected static boolean parseBoolean(NamedNodeMap n, String name) {
      Node b = n.getNamedItem(name);
      return b != null && Boolean.parseBoolean(b.getNodeValue());
   }

   protected static boolean parseBoolean(Node node, Boolean defaultValue) {
      return node != null ? Boolean.valueOf(node.getNodeValue()) : defaultValue;
   }

   protected static boolean parseBoolean(Node node) {
      return parseBoolean(node, null);
   }

   protected static boolean parseBoolean(NamedNodeMap attrs, String name, Boolean defaultValue) {
      return parseBoolean(attrs.getNamedItem(name), defaultValue);
   }

   protected static String parseString(NamedNodeMap n, String name) {
      Node b = n.getNamedItem(name);
      return b == null ? "" : b.getNodeValue();
   }

   protected static <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz, T defaultValue) {
      if (node == null) {
         return defaultValue;
      } else {
         try {
            return Enum.valueOf(clazz, node.getNodeValue());
         } catch (IllegalArgumentException var4) {
            return defaultValue;
         }
      }
   }

   protected static <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz) {
      return parseEnum(node, clazz, (T)null);
   }

   protected static <T extends Enum<T>> T parseEnum(NamedNodeMap attrs, Class<T> clazz, String name) {
      return parseEnum(attrs.getNamedItem(name), clazz);
   }

   protected static <T extends Enum<T>> T parseEnum(NamedNodeMap attrs, Class<T> clazz, String name, T defaultValue) {
      return parseEnum(attrs.getNamedItem(name), clazz, defaultValue);
   }

   public void setCurrentFileFilter(FileFilter filter) {
      this._currentFilter = filter;
   }

   public FileFilter getCurrentFileFilter() {
      return (FileFilter)(this._currentFilter != null ? this._currentFilter : xmlFilter);
   }

   protected class XMLErrorHandler implements ErrorHandler {
      @Override
      public void warning(SAXParseException e) throws SAXParseException {
         throw e;
      }

      @Override
      public void error(SAXParseException e) throws SAXParseException {
         throw e;
      }

      @Override
      public void fatalError(SAXParseException e) throws SAXParseException {
         throw e;
      }
   }
}

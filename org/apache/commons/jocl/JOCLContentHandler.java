package org.apache.commons.jocl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class JOCLContentHandler extends DefaultHandler {
   public static final String JOCL_NAMESPACE_URI = "http://apache.org/xml/xmlns/jakarta/commons/jocl";
   public static final String JOCL_PREFIX = "jocl:";
   protected ArrayList _typeList = new ArrayList();
   protected ArrayList _valueList = new ArrayList();
   protected JOCLContentHandler.ConstructorDetails _cur = null;
   protected boolean _acceptEmptyNamespaceForElements = true;
   protected boolean _acceptJoclPrefixForElements = true;
   protected boolean _acceptEmptyNamespaceForAttributes = true;
   protected boolean _acceptJoclPrefixForAttributes = true;
   protected Locator _locator = null;
   protected static final String ELT_OBJECT = "object";
   protected static final String ELT_ARRAY = "array";
   protected static final String ELT_COLLECTION = "collection";
   protected static final String ELT_LIST = "list";
   protected static final String ATT_CLASS = "class";
   protected static final String ATT_ISNULL = "null";
   protected static final String ELT_BOOLEAN = "boolean";
   protected static final String ELT_BYTE = "byte";
   protected static final String ELT_CHAR = "char";
   protected static final String ELT_DOUBLE = "double";
   protected static final String ELT_FLOAT = "float";
   protected static final String ELT_INT = "int";
   protected static final String ELT_LONG = "long";
   protected static final String ELT_SHORT = "short";
   protected static final String ELT_STRING = "string";
   protected static final String ATT_VALUE = "value";

   public static void main(String[] args) throws Exception {
      JOCLContentHandler jocl = parse(System.in, null);

      for(int i = 0; i < jocl.size(); ++i) {
         System.out.println("<" + jocl.getType(i) + ">\t" + jocl.getValue(i));
      }
   }

   public static JOCLContentHandler parse(File f) throws SAXException, FileNotFoundException, IOException {
      return parse(new FileInputStream(f), null);
   }

   public static JOCLContentHandler parse(Reader in) throws SAXException, IOException {
      return parse(new InputSource(in), null);
   }

   public static JOCLContentHandler parse(InputStream in) throws SAXException, IOException {
      return parse(new InputSource(in), null);
   }

   public static JOCLContentHandler parse(InputSource in) throws SAXException, IOException {
      return parse(in, null);
   }

   public static JOCLContentHandler parse(File f, XMLReader reader) throws SAXException, FileNotFoundException, IOException {
      return parse(new FileInputStream(f), reader);
   }

   public static JOCLContentHandler parse(Reader in, XMLReader reader) throws SAXException, IOException {
      return parse(new InputSource(in), reader);
   }

   public static JOCLContentHandler parse(InputStream in, XMLReader reader) throws SAXException, IOException {
      return parse(new InputSource(in), reader);
   }

   public static JOCLContentHandler parse(InputSource in, XMLReader reader) throws SAXException, IOException {
      JOCLContentHandler jocl = new JOCLContentHandler();
      if (null == reader) {
         reader = XMLReaderFactory.createXMLReader();
      }

      reader.setContentHandler(jocl);
      reader.parse(in);
      return jocl;
   }

   public JOCLContentHandler() {
      this(true, true, true, true);
   }

   public JOCLContentHandler(boolean emptyEltNS, boolean joclEltPrefix, boolean emptyAttrNS, boolean joclAttrPrefix) {
      this._acceptEmptyNamespaceForElements = emptyEltNS;
      this._acceptJoclPrefixForElements = joclEltPrefix;
      this._acceptEmptyNamespaceForAttributes = emptyAttrNS;
      this._acceptJoclPrefixForAttributes = joclAttrPrefix;
   }

   public int size() {
      return this._typeList.size();
   }

   public void clear() {
      this._typeList = new ArrayList();
      this._valueList = new ArrayList();
   }

   public void clear(int i) {
      this._typeList.remove(i);
      this._valueList.remove(i);
   }

   public Class getType(int i) {
      return (Class)this._typeList.get(i);
   }

   public Object getValue(int i) {
      return this._valueList.get(i);
   }

   public Object[] getValueArray() {
      return this._valueList.toArray();
   }

   public Object[] getTypeArray() {
      return this._typeList.toArray();
   }

   @Override
   public void startElement(String uri, String localName, String qname, Attributes attr) throws SAXException {
      try {
         if (this.isJoclNamespace(uri, localName, qname)) {
            if ("object".equals(localName)) {
               String cname = this.getAttributeValue("class", attr);
               String isnullstr = this.getAttributeValue("null", attr, "false");
               boolean isnull = "true".equalsIgnoreCase(isnullstr) || "yes".equalsIgnoreCase(isnullstr);
               this._cur = new JOCLContentHandler.ConstructorDetails(cname, this._cur, isnull);
            } else if ("array".equals(localName)) {
               this._cur = new JOCLContentHandler.ConstructorDetails(Object[].class, this._cur, false, true);
            } else if ("collection".equals(localName)) {
               this._cur = new JOCLContentHandler.ConstructorDetails(Collection.class, this._cur, false, true);
            } else if ("list".equals(localName)) {
               this._cur = new JOCLContentHandler.ConstructorDetails(List.class, this._cur, false, true);
            } else if ("boolean".equals(localName)) {
               String valstr = this.getAttributeValue("value", attr, "false");
               boolean val = "true".equalsIgnoreCase(valstr) || "yes".equalsIgnoreCase(valstr);
               this.addObject(Boolean.TYPE, val);
            } else if ("byte".equals(localName)) {
               byte val = Byte.parseByte(this.getAttributeValue("value", attr, "0"));
               this.addObject(Byte.TYPE, new Byte(val));
            } else if ("char".equals(localName)) {
               char val = 0;
               String valstr = this.getAttributeValue("value", attr);
               if (null == valstr) {
                  val = 0;
               } else {
                  if (valstr.length() > 1) {
                     throw new SAXException("if present, char value must be exactly one character long");
                  }

                  if (valstr.length() == 1) {
                     val = valstr.charAt(0);
                  } else if (valstr.length() == 0) {
                     throw new SAXException("if present, char value must be exactly one character long");
                  }
               }

               this.addObject(Character.TYPE, new Character(val));
            } else if ("double".equals(localName)) {
               double val = Double.parseDouble(this.getAttributeValue("value", attr, "0"));
               this.addObject(Double.TYPE, new Double(val));
            } else if ("float".equals(localName)) {
               float val = Float.parseFloat(this.getAttributeValue("value", attr, "0"));
               this.addObject(Float.TYPE, new Float(val));
            } else if ("int".equals(localName)) {
               int val = Integer.parseInt(this.getAttributeValue("value", attr, "0"));
               this.addObject(Integer.TYPE, new Integer(val));
            } else if ("long".equals(localName)) {
               long val = Long.parseLong(this.getAttributeValue("value", attr, "0"));
               this.addObject(Long.TYPE, new Long(val));
            } else if ("short".equals(localName)) {
               short val = Short.parseShort(this.getAttributeValue("value", attr, "0"));
               this.addObject(Short.TYPE, new Short(val));
            } else if ("string".equals(localName)) {
               String val = this.getAttributeValue("value", attr);
               this.addObject("".getClass(), val);
            }
         }
      } catch (NumberFormatException var8) {
         throw new SAXException(var8);
      } catch (ClassNotFoundException var9) {
         throw new SAXException(var9);
      }
   }

   @Override
   public void endElement(String uri, String localName, String qname) throws SAXException {
      try {
         if (this.isJoclNamespace(uri, localName, qname)
            && ("object".equals(localName) || "array".equals(localName) || "collection".equals(localName) || "list".equals(localName))) {
            JOCLContentHandler.ConstructorDetails temp = this._cur;
            this._cur = this._cur.getParent();
            if (null == this._cur) {
               this._typeList.add(temp.getType());
               this._valueList.add(temp.createObject());
            } else {
               this._cur.addArgument(temp.getType(), temp.createObject());
            }
         }
      } catch (Exception var5) {
         throw new SAXException(var5);
      }
   }

   @Override
   public void setDocumentLocator(Locator locator) {
      this._locator = locator;
   }

   protected boolean isJoclNamespace(String uri, String localname, String qname) {
      if ("http://apache.org/xml/xmlns/jakarta/commons/jocl".equals(uri)) {
         return true;
      } else if (!this._acceptEmptyNamespaceForElements || null != uri && !"".equals(uri)) {
         return this._acceptJoclPrefixForElements && (null == uri || "".equals(uri)) && qname.startsWith("jocl:");
      } else {
         return true;
      }
   }

   protected String getAttributeValue(String localname, Attributes attr) {
      return this.getAttributeValue(localname, attr, null);
   }

   protected String getAttributeValue(String localname, Attributes attr, String implied) {
      String val = attr.getValue("http://apache.org/xml/xmlns/jakarta/commons/jocl", localname);
      if (null == val && this._acceptEmptyNamespaceForAttributes) {
         val = attr.getValue("", localname);
      }

      if (null == val && this._acceptJoclPrefixForAttributes) {
         val = attr.getValue("", "jocl:" + localname);
      }

      return null == val ? implied : val;
   }

   protected void addObject(Class type, Object val) {
      if (null == this._cur) {
         this._typeList.add(type);
         this._valueList.add(val);
      } else {
         this._cur.addArgument(type, val);
      }
   }

   static class ConstructorDetails {
      private JOCLContentHandler.ConstructorDetails _parent = null;
      private Class _type = null;
      private ArrayList _argTypes = null;
      private ArrayList _argValues = null;
      private boolean _isnull = false;
      private boolean _isgroup = false;

      public ConstructorDetails(String classname, JOCLContentHandler.ConstructorDetails parent) throws ClassNotFoundException {
         this(Class.forName(classname), parent, false, false);
      }

      public ConstructorDetails(String classname, JOCLContentHandler.ConstructorDetails parent, boolean isnull) throws ClassNotFoundException {
         this(Class.forName(classname), parent, isnull, false);
      }

      public ConstructorDetails(String classname, JOCLContentHandler.ConstructorDetails parent, boolean isnull, boolean isgroup) throws ClassNotFoundException {
         this(Class.forName(classname), parent, isnull, isgroup);
      }

      public ConstructorDetails(Class type, JOCLContentHandler.ConstructorDetails parent, boolean isnull, boolean isgroup) {
         this._parent = parent;
         this._type = type;
         this._argTypes = new ArrayList();
         this._argValues = new ArrayList();
         this._isnull = isnull;
         this._isgroup = isgroup;
      }

      public void addArgument(Object value) {
         this.addArgument(value.getClass(), value);
      }

      public void addArgument(Class type, Object val) {
         if (this._isnull) {
            throw new NullPointerException("can't add arguments to null instances");
         } else {
            this._argTypes.add(type);
            this._argValues.add(val);
         }
      }

      public Class getType() {
         return this._type;
      }

      public JOCLContentHandler.ConstructorDetails getParent() {
         return this._parent;
      }

      public Object createObject() throws InstantiationException, IllegalAccessException, InvocationTargetException {
         if (this._isnull) {
            return null;
         } else if (this._isgroup) {
            if (this._type.equals(Object[].class)) {
               return this._argValues.toArray();
            } else if (!this._type.equals(Collection.class) && !this._type.equals(List.class)) {
               throw new IllegalStateException("implementation error: unhandled _type:" + this._type);
            } else {
               return this._argValues;
            }
         } else {
            Class k = this.getType();
            Class[] argtypes = this._argTypes.toArray(new Class[0]);
            Object[] argvals = this._argValues.toArray();
            return ConstructorUtil.invokeConstructor(k, argtypes, argvals);
         }
      }
   }
}

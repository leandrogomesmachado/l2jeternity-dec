package com.mchange.v1.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StdErrErrorHandler implements ErrorHandler {
   @Override
   public void warning(SAXParseException var1) {
      System.err.println("[Warning]");
      this.showExceptionInformation(var1);
      var1.printStackTrace();
   }

   @Override
   public void error(SAXParseException var1) {
      System.err.println("[Error]");
      this.showExceptionInformation(var1);
      var1.printStackTrace();
   }

   @Override
   public void fatalError(SAXParseException var1) throws SAXException {
      System.err.println("[Fatal Error]");
      this.showExceptionInformation(var1);
      var1.printStackTrace();
      throw var1;
   }

   private void showExceptionInformation(SAXParseException var1) {
      System.err.println("[\tLine Number: " + var1.getLineNumber() + ']');
      System.err.println("[\tColumn Number: " + var1.getColumnNumber() + ']');
      System.err.println("[\tPublic ID: " + var1.getPublicId() + ']');
      System.err.println("[\tSystem ID: " + var1.getSystemId() + ']');
   }
}

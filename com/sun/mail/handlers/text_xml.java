package com.sun.mail.handlers;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class text_xml extends text_plain {
   private final DataFlavor[] flavors = new DataFlavor[]{
      new ActivationDataFlavor(String.class, "text/xml", "XML String"),
      new ActivationDataFlavor(String.class, "application/xml", "XML String"),
      new ActivationDataFlavor(StreamSource.class, "text/xml", "XML"),
      new ActivationDataFlavor(StreamSource.class, "application/xml", "XML")
   };

   @Override
   public DataFlavor[] getTransferDataFlavors() {
      return (DataFlavor[])this.flavors.clone();
   }

   @Override
   public Object getTransferData(DataFlavor df, DataSource ds) throws IOException {
      for(int i = 0; i < this.flavors.length; ++i) {
         DataFlavor aFlavor = this.flavors[i];
         if (aFlavor.equals(df)) {
            if (aFlavor.getRepresentationClass() == String.class) {
               return super.getContent(ds);
            }

            if (aFlavor.getRepresentationClass() == StreamSource.class) {
               return new StreamSource(ds.getInputStream());
            }

            return null;
         }
      }

      return null;
   }

   @Override
   public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
      if (!this.isXmlType(mimeType)) {
         throw new IOException("Invalid content type \"" + mimeType + "\" for text/xml DCH");
      } else if (obj instanceof String) {
         super.writeTo(obj, mimeType, os);
      } else if (!(obj instanceof DataSource) && !(obj instanceof Source)) {
         throw new IOException("Invalid Object type = " + obj.getClass() + ". XmlDCH can only convert DataSource or Source to XML.");
      } else {
         try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(os);
            if (obj instanceof DataSource) {
               transformer.transform(new StreamSource(((DataSource)obj).getInputStream()), result);
            } else {
               transformer.transform((Source)obj, result);
            }
         } catch (TransformerException var6) {
            IOException ioex = new IOException("Unable to run the JAXP transformer on a stream " + var6.getMessage());
            ioex.initCause(var6);
            throw ioex;
         } catch (RuntimeException var7) {
            IOException ioexx = new IOException("Unable to run the JAXP transformer on a stream " + var7.getMessage());
            ioexx.initCause(var7);
            throw ioexx;
         }
      }
   }

   private boolean isXmlType(String type) {
      try {
         ContentType ct = new ContentType(type);
         return ct.getSubType().equals("xml") && (ct.getPrimaryType().equals("text") || ct.getPrimaryType().equals("application"));
      } catch (ParseException var3) {
         return false;
      } catch (RuntimeException var4) {
         return false;
      }
   }
}

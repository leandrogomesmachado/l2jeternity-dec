package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SerializableExtension implements GeneratorExtension {
   Set transientProperties;
   Map transientPropertyInitializers;

   public SerializableExtension(Set var1, Map var2) {
      this.transientProperties = var1;
      this.transientPropertyInitializers = var2;
   }

   public SerializableExtension() {
      this(Collections.EMPTY_SET, null);
   }

   @Override
   public Collection extraGeneralImports() {
      return Collections.EMPTY_SET;
   }

   @Override
   public Collection extraSpecificImports() {
      HashSet var1 = new HashSet();
      var1.add("java.io.IOException");
      var1.add("java.io.Serializable");
      var1.add("java.io.ObjectOutputStream");
      var1.add("java.io.ObjectInputStream");
      return var1;
   }

   @Override
   public Collection extraInterfaceNames() {
      HashSet var1 = new HashSet();
      var1.add("Serializable");
      return var1;
   }

   @Override
   public void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
      var5.println("private static final long serialVersionUID = 1;");
      var5.println("private static final short VERSION = 0x0001;");
      var5.println();
      var5.println("private void writeObject( ObjectOutputStream oos ) throws IOException");
      var5.println("{");
      var5.upIndent();
      var5.println("oos.writeShort( VERSION );");
      int var6 = 0;

      for(int var7 = var3.length; var6 < var7; ++var6) {
         Property var8 = var3[var6];
         if (!this.transientProperties.contains(var8.getName())) {
            Class var9 = var4[var6];
            if (var9 == null || !var9.isPrimitive()) {
               this.writeStoreObject(var8, var9, var5);
            } else if (var9 == Byte.TYPE) {
               var5.println("oos.writeByte(" + var8.getName() + ");");
            } else if (var9 == Character.TYPE) {
               var5.println("oos.writeChar(" + var8.getName() + ");");
            } else if (var9 == Short.TYPE) {
               var5.println("oos.writeShort(" + var8.getName() + ");");
            } else if (var9 == Integer.TYPE) {
               var5.println("oos.writeInt(" + var8.getName() + ");");
            } else if (var9 == Boolean.TYPE) {
               var5.println("oos.writeBoolean(" + var8.getName() + ");");
            } else if (var9 == Long.TYPE) {
               var5.println("oos.writeLong(" + var8.getName() + ");");
            } else if (var9 == Float.TYPE) {
               var5.println("oos.writeFloat(" + var8.getName() + ");");
            } else if (var9 == Double.TYPE) {
               var5.println("oos.writeDouble(" + var8.getName() + ");");
            }
         }
      }

      this.generateExtraSerWriteStatements(var1, var2, var3, var4, var5);
      var5.downIndent();
      var5.println("}");
      var5.println();
      var5.println("private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException");
      var5.println("{");
      var5.upIndent();
      var5.println("short version = ois.readShort();");
      var5.println("switch (version)");
      var5.println("{");
      var5.upIndent();
      var5.println("case VERSION:");
      var5.upIndent();
      var6 = 0;

      for(int var11 = var3.length; var6 < var11; ++var6) {
         Property var12 = var3[var6];
         if (!this.transientProperties.contains(var12.getName())) {
            Class var13 = var4[var6];
            if (var13 == null || !var13.isPrimitive()) {
               this.writeUnstoreObject(var12, var13, var5);
            } else if (var13 == Byte.TYPE) {
               var5.println("this." + var12.getName() + " = ois.readByte();");
            } else if (var13 == Character.TYPE) {
               var5.println("this." + var12.getName() + " = ois.readChar();");
            } else if (var13 == Short.TYPE) {
               var5.println("this." + var12.getName() + " = ois.readShort();");
            } else if (var13 == Integer.TYPE) {
               var5.println("this." + var12.getName() + " = ois.readInt();");
            } else if (var13 == Boolean.TYPE) {
               var5.println("this." + var12.getName() + " = ois.readBoolean();");
            } else if (var13 == Long.TYPE) {
               var5.println("this." + var12.getName() + " = ois.readLong();");
            } else if (var13 == Float.TYPE) {
               var5.println("this." + var12.getName() + " = ois.readFloat();");
            } else if (var13 == Double.TYPE) {
               var5.println("this." + var12.getName() + " = ois.readDouble();");
            }
         } else {
            String var14 = (String)this.transientPropertyInitializers.get(var12.getName());
            if (var14 != null) {
               var5.println("this." + var12.getName() + " = " + var14 + ';');
            }
         }
      }

      this.generateExtraSerInitializers(var1, var2, var3, var4, var5);
      var5.println("break;");
      var5.downIndent();
      var5.println("default:");
      var5.upIndent();
      var5.println("throw new IOException(\"Unsupported Serialized Version: \" + version);");
      var5.downIndent();
      var5.downIndent();
      var5.println("}");
      var5.downIndent();
      var5.println("}");
   }

   protected void writeStoreObject(Property var1, Class var2, IndentedWriter var3) throws IOException {
      var3.println("oos.writeObject( " + var1.getName() + " );");
   }

   protected void writeUnstoreObject(Property var1, Class var2, IndentedWriter var3) throws IOException {
      var3.println("this." + var1.getName() + " = (" + var1.getSimpleTypeName() + ") ois.readObject();");
   }

   protected void generateExtraSerWriteStatements(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
   }

   protected void generateExtraSerInitializers(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
   }
}

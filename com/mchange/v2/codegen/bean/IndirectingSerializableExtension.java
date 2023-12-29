package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.IndentedWriter;
import com.mchange.v2.ser.IndirectPolicy;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

public class IndirectingSerializableExtension extends SerializableExtension {
   protected String findIndirectorExpr;
   protected String indirectorClassName;

   public IndirectingSerializableExtension(String var1) {
      this.indirectorClassName = var1;
      this.findIndirectorExpr = "new " + var1 + "()";
   }

   protected IndirectingSerializableExtension() {
   }

   @Override
   public Collection extraSpecificImports() {
      Collection var1 = super.extraSpecificImports();
      var1.add(this.indirectorClassName);
      var1.add("com.mchange.v2.ser.IndirectlySerialized");
      var1.add("com.mchange.v2.ser.Indirector");
      var1.add("com.mchange.v2.ser.SerializableUtils");
      var1.add("java.io.NotSerializableException");
      return var1;
   }

   protected IndirectPolicy indirectingPolicy(Property var1, Class var2) {
      return Serializable.class.isAssignableFrom(var2) ? IndirectPolicy.DEFINITELY_DIRECT : IndirectPolicy.INDIRECT_ON_EXCEPTION;
   }

   protected void writeInitializeIndirector(Property var1, Class var2, IndentedWriter var3) throws IOException {
   }

   protected void writeExtraDeclarations(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
   }

   @Override
   public void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
      super.generate(var1, var2, var3, var4, var5);
      this.writeExtraDeclarations(var1, var2, var3, var4, var5);
   }

   @Override
   protected void writeStoreObject(Property var1, Class var2, IndentedWriter var3) throws IOException {
      IndirectPolicy var4 = this.indirectingPolicy(var1, var2);
      if (var4 == IndirectPolicy.DEFINITELY_INDIRECT) {
         this.writeIndirectStoreObject(var1, var2, var3);
      } else if (var4 == IndirectPolicy.INDIRECT_ON_EXCEPTION) {
         var3.println("try");
         var3.println("{");
         var3.upIndent();
         var3.println("//test serialize");
         var3.println("SerializableUtils.toByteArray(" + var1.getName() + ");");
         super.writeStoreObject(var1, var2, var3);
         var3.downIndent();
         var3.println("}");
         var3.println("catch (NotSerializableException nse)");
         var3.println("{");
         var3.upIndent();
         this.writeIndirectStoreObject(var1, var2, var3);
         var3.downIndent();
         var3.println("}");
      } else {
         if (var4 != IndirectPolicy.DEFINITELY_DIRECT) {
            throw new InternalError("indirectingPolicy() overridden to return unknown policy: " + var4);
         }

         super.writeStoreObject(var1, var2, var3);
      }
   }

   protected void writeIndirectStoreObject(Property var1, Class var2, IndentedWriter var3) throws IOException {
      var3.println("try");
      var3.println("{");
      var3.upIndent();
      var3.println("Indirector indirector = " + this.findIndirectorExpr + ';');
      this.writeInitializeIndirector(var1, var2, var3);
      var3.println("oos.writeObject( indirector.indirectForm( " + var1.getName() + " ) );");
      var3.downIndent();
      var3.println("}");
      var3.println("catch (IOException indirectionIOException)");
      var3.println("{ throw indirectionIOException; }");
      var3.println("catch (Exception indirectionOtherException)");
      var3.println("{ throw new IOException(\"Problem indirectly serializing " + var1.getName() + ": \" + indirectionOtherException.toString() ); }");
   }

   @Override
   protected void writeUnstoreObject(Property var1, Class var2, IndentedWriter var3) throws IOException {
      IndirectPolicy var4 = this.indirectingPolicy(var1, var2);
      if (var4 != IndirectPolicy.DEFINITELY_INDIRECT && var4 != IndirectPolicy.INDIRECT_ON_EXCEPTION) {
         if (var4 != IndirectPolicy.DEFINITELY_DIRECT) {
            throw new InternalError("indirectingPolicy() overridden to return unknown policy: " + var4);
         }

         super.writeUnstoreObject(var1, var2, var3);
      } else {
         var3.println("// we create an artificial scope so that we can use the name o for all indirectly serialized objects.");
         var3.println("{");
         var3.upIndent();
         var3.println("Object o = ois.readObject();");
         var3.println("if (o instanceof IndirectlySerialized) o = ((IndirectlySerialized) o).getObject();");
         var3.println("this." + var1.getName() + " = (" + var1.getSimpleTypeName() + ") o;");
         var3.downIndent();
         var3.println("}");
      }
   }
}

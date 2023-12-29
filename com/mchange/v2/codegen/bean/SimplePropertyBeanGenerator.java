package com.mchange.v2.codegen.bean;

import com.mchange.v1.lang.ClassUtils;
import com.mchange.v2.codegen.CodegenUtils;
import com.mchange.v2.codegen.IndentedWriter;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SimplePropertyBeanGenerator implements PropertyBeanGenerator {
   private static final MLogger logger = MLog.getLogger(SimplePropertyBeanGenerator.class);
   private boolean inner = false;
   private int java_version = 140;
   private boolean force_unmodifiable = false;
   private String generatorName = this.getClass().getName();
   protected ClassInfo info;
   protected Property[] props;
   protected IndentedWriter iw;
   protected Set generalImports;
   protected Set specificImports;
   protected Set interfaceNames;
   protected Class superclassType;
   protected List interfaceTypes;
   protected Class[] propertyTypes;
   protected List generatorExtensions = new ArrayList();

   public synchronized void setInner(boolean var1) {
      this.inner = var1;
   }

   public synchronized boolean isInner() {
      return this.inner;
   }

   public synchronized void setJavaVersion(int var1) {
      this.java_version = var1;
   }

   public synchronized int getJavaVersion() {
      return this.java_version;
   }

   public synchronized void setGeneratorName(String var1) {
      this.generatorName = var1;
   }

   public synchronized String getGeneratorName() {
      return this.generatorName;
   }

   public synchronized void setForceUnmodifiable(boolean var1) {
      this.force_unmodifiable = var1;
   }

   public synchronized boolean isForceUnmodifiable() {
      return this.force_unmodifiable;
   }

   public synchronized void addExtension(GeneratorExtension var1) {
      this.generatorExtensions.add(var1);
   }

   public synchronized void removeExtension(GeneratorExtension var1) {
      this.generatorExtensions.remove(var1);
   }

   @Override
   public synchronized void generate(ClassInfo var1, Property[] var2, Writer var3) throws IOException {
      this.info = var1;
      this.props = var2;
      Arrays.sort(var2, BeangenUtils.PROPERTY_COMPARATOR);
      this.iw = var3 instanceof IndentedWriter ? (IndentedWriter)var3 : new IndentedWriter(var3);
      this.generalImports = new TreeSet();
      if (var1.getGeneralImports() != null) {
         this.generalImports.addAll(Arrays.asList(var1.getGeneralImports()));
      }

      this.specificImports = new TreeSet();
      if (var1.getSpecificImports() != null) {
         this.specificImports.addAll(Arrays.asList(var1.getSpecificImports()));
      }

      this.interfaceNames = new TreeSet();
      if (var1.getInterfaceNames() != null) {
         this.interfaceNames.addAll(Arrays.asList(var1.getInterfaceNames()));
      }

      this.addInternalImports();
      this.addInternalInterfaces();
      this.resolveTypes();
      if (!this.inner) {
         this.writeHeader();
         this.iw.println();
      }

      this.generateClassJavaDocComment();
      this.writeClassDeclaration();
      this.iw.println('{');
      this.iw.upIndent();
      this.writeCoreBody();
      this.iw.downIndent();
      this.iw.println('}');
   }

   protected void resolveTypes() {
      String[] var1 = this.generalImports.toArray(new String[this.generalImports.size()]);
      String[] var2 = this.specificImports.toArray(new String[this.specificImports.size()]);
      if (this.info.getSuperclassName() != null) {
         try {
            this.superclassType = ClassUtils.forName(this.info.getSuperclassName(), var1, var2);
         } catch (Exception var9) {
            if (logger.isLoggable(MLevel.WARNING)) {
               logger.warning(this.getClass().getName() + " could not resolve superclass '" + this.info.getSuperclassName() + "'.");
            }

            this.superclassType = null;
         }
      }

      this.interfaceTypes = new ArrayList(this.interfaceNames.size());

      for(String var4 : this.interfaceNames) {
         try {
            this.interfaceTypes.add(ClassUtils.forName(var4, var1, var2));
         } catch (Exception var8) {
            if (logger.isLoggable(MLevel.WARNING)) {
               logger.warning(this.getClass().getName() + " could not resolve interface '" + var4 + "'.");
            }

            this.interfaceTypes.add(null);
         }
      }

      this.propertyTypes = new Class[this.props.length];
      int var10 = 0;

      for(int var11 = this.props.length; var10 < var11; ++var10) {
         String var5 = this.props[var10].getSimpleTypeName();

         try {
            this.propertyTypes[var10] = ClassUtils.forName(var5, var1, var2);
         } catch (Exception var7) {
            if (logger.isLoggable(MLevel.WARNING)) {
               logger.log(MLevel.WARNING, this.getClass().getName() + " could not resolve property type '" + var5 + "'.", (Throwable)var7);
            }

            this.propertyTypes[var10] = null;
         }
      }
   }

   protected void addInternalImports() {
      if (this.boundProperties()) {
         this.specificImports.add("java.beans.PropertyChangeEvent");
         this.specificImports.add("java.beans.PropertyChangeSupport");
         this.specificImports.add("java.beans.PropertyChangeListener");
      }

      if (this.constrainedProperties()) {
         this.specificImports.add("java.beans.PropertyChangeEvent");
         this.specificImports.add("java.beans.PropertyVetoException");
         this.specificImports.add("java.beans.VetoableChangeSupport");
         this.specificImports.add("java.beans.VetoableChangeListener");
      }

      for(GeneratorExtension var2 : this.generatorExtensions) {
         this.specificImports.addAll(var2.extraSpecificImports());
         this.generalImports.addAll(var2.extraGeneralImports());
      }
   }

   protected void addInternalInterfaces() {
      for(GeneratorExtension var2 : this.generatorExtensions) {
         this.interfaceNames.addAll(var2.extraInterfaceNames());
      }
   }

   protected void writeCoreBody() throws IOException {
      this.writeJavaBeansChangeSupport();
      this.writePropertyVariables();
      this.writeOtherVariables();
      this.iw.println();
      this.writeGetterSetterPairs();
      if (this.boundProperties()) {
         this.iw.println();
         this.writeBoundPropertyEventSourceMethods();
      }

      if (this.constrainedProperties()) {
         this.iw.println();
         this.writeConstrainedPropertyEventSourceMethods();
      }

      this.writeInternalUtilityFunctions();
      this.writeOtherFunctions();
      this.writeOtherClasses();
      String[] var1 = this.interfaceNames.toArray(new String[this.interfaceNames.size()]);
      String[] var2 = this.generalImports.toArray(new String[this.generalImports.size()]);
      String[] var3 = this.specificImports.toArray(new String[this.specificImports.size()]);
      SimpleClassInfo var4 = new SimpleClassInfo(
         this.info.getPackageName(), this.info.getModifiers(), this.info.getClassName(), this.info.getSuperclassName(), var1, var2, var3
      );

      for(GeneratorExtension var6 : this.generatorExtensions) {
         this.iw.println();
         var6.generate(var4, this.superclassType, this.props, this.propertyTypes, this.iw);
      }
   }

   protected void writeInternalUtilityFunctions() throws IOException {
      this.iw.println("private boolean eqOrBothNull( Object a, Object b )");
      this.iw.println("{");
      this.iw.upIndent();
      this.iw.println("return");
      this.iw.upIndent();
      this.iw.println("a == b ||");
      this.iw.println("(a != null && a.equals(b));");
      this.iw.downIndent();
      this.iw.downIndent();
      this.iw.println("}");
   }

   protected void writeConstrainedPropertyEventSourceMethods() throws IOException {
      this.iw.println("public void addVetoableChangeListener( VetoableChangeListener vcl )");
      this.iw.println("{ vcs.addVetoableChangeListener( vcl ); }");
      this.iw.println();
      this.iw.println("public void removeVetoableChangeListener( VetoableChangeListener vcl )");
      this.iw.println("{ vcs.removeVetoableChangeListener( vcl ); }");
      this.iw.println();
      if (this.java_version >= 140) {
         this.iw.println("public VetoableChangeListener[] getVetoableChangeListeners()");
         this.iw.println("{ return vcs.getVetoableChangeListeners(); }");
      }
   }

   protected void writeBoundPropertyEventSourceMethods() throws IOException {
      this.iw.println("public void addPropertyChangeListener( PropertyChangeListener pcl )");
      this.iw.println("{ pcs.addPropertyChangeListener( pcl ); }");
      this.iw.println();
      this.iw.println("public void addPropertyChangeListener( String propName, PropertyChangeListener pcl )");
      this.iw.println("{ pcs.addPropertyChangeListener( propName, pcl ); }");
      this.iw.println();
      this.iw.println("public void removePropertyChangeListener( PropertyChangeListener pcl )");
      this.iw.println("{ pcs.removePropertyChangeListener( pcl ); }");
      this.iw.println();
      this.iw.println("public void removePropertyChangeListener( String propName, PropertyChangeListener pcl )");
      this.iw.println("{ pcs.removePropertyChangeListener( propName, pcl ); }");
      this.iw.println();
      if (this.java_version >= 140) {
         this.iw.println("public PropertyChangeListener[] getPropertyChangeListeners()");
         this.iw.println("{ return pcs.getPropertyChangeListeners(); }");
      }
   }

   protected void writeJavaBeansChangeSupport() throws IOException {
      if (this.boundProperties()) {
         this.iw.println("protected PropertyChangeSupport pcs = new PropertyChangeSupport( this );");
         this.iw.println();
         this.iw.println("protected PropertyChangeSupport getPropertyChangeSupport()");
         this.iw.println("{ return pcs; }");
      }

      if (this.constrainedProperties()) {
         this.iw.println("protected VetoableChangeSupport vcs = new VetoableChangeSupport( this );");
         this.iw.println();
         this.iw.println("protected VetoableChangeSupport getVetoableChangeSupport()");
         this.iw.println("{ return vcs; }");
      }
   }

   protected void writeOtherVariables() throws IOException {
   }

   protected void writeOtherFunctions() throws IOException {
   }

   protected void writeOtherClasses() throws IOException {
   }

   protected void writePropertyVariables() throws IOException {
      int var1 = 0;

      for(int var2 = this.props.length; var1 < var2; ++var1) {
         this.writePropertyVariable(this.props[var1]);
      }
   }

   protected void writePropertyVariable(Property var1) throws IOException {
      BeangenUtils.writePropertyVariable(var1, this.iw);
   }

   /** @deprecated */
   protected void writePropertyMembers() throws IOException {
      throw new InternalError("writePropertyMembers() deprecated and removed. please us writePropertyVariables().");
   }

   /** @deprecated */
   protected void writePropertyMember(Property var1) throws IOException {
      throw new InternalError("writePropertyMember() deprecated and removed. please us writePropertyVariable().");
   }

   protected void writeGetterSetterPairs() throws IOException {
      int var1 = 0;

      for(int var2 = this.props.length; var1 < var2; ++var1) {
         this.writeGetterSetterPair(this.props[var1], this.propertyTypes[var1]);
         if (var1 != var2 - 1) {
            this.iw.println();
         }
      }
   }

   protected void writeGetterSetterPair(Property var1, Class var2) throws IOException {
      this.writePropertyGetter(var1, var2);
      if (!var1.isReadOnly() && !this.force_unmodifiable) {
         this.iw.println();
         this.writePropertySetter(var1, var2);
      }
   }

   protected void writePropertyGetter(Property var1, Class var2) throws IOException {
      BeangenUtils.writePropertyGetter(var1, this.getGetterDefensiveCopyExpression(var1, var2), this.iw);
   }

   protected void writePropertySetter(Property var1, Class var2) throws IOException {
      BeangenUtils.writePropertySetter(var1, this.getSetterDefensiveCopyExpression(var1, var2), this.iw);
   }

   protected String getGetterDefensiveCopyExpression(Property var1, Class var2) {
      return var1.getDefensiveCopyExpression();
   }

   protected String getSetterDefensiveCopyExpression(Property var1, Class var2) {
      return var1.getDefensiveCopyExpression();
   }

   protected String getConstructorDefensiveCopyExpression(Property var1, Class var2) {
      return var1.getDefensiveCopyExpression();
   }

   protected void writeHeader() throws IOException {
      this.writeBannerComments();
      this.iw.println();
      this.iw.println("package " + this.info.getPackageName() + ';');
      this.iw.println();
      this.writeImports();
   }

   protected void writeBannerComments() throws IOException {
      this.iw.println("/*");
      this.iw.println(" * This class autogenerated by " + this.generatorName + '.');
      this.iw.println(" * " + new Date());
      this.iw.println(" * DO NOT HAND EDIT!");
      this.iw.println(" */");
   }

   protected void generateClassJavaDocComment() throws IOException {
      this.iw.println("/**");
      this.iw.println(" * This class was generated by " + this.generatorName + ".");
      this.iw.println(" */");
   }

   protected void writeImports() throws IOException {
      Iterator var1 = this.generalImports.iterator();

      while(var1.hasNext()) {
         this.iw.println("import " + var1.next() + ".*;");
      }

      var1 = this.specificImports.iterator();

      while(var1.hasNext()) {
         this.iw.println("import " + var1.next() + ";");
      }
   }

   protected void writeClassDeclaration() throws IOException {
      this.iw.print(CodegenUtils.getModifierString(this.info.getModifiers()) + " class " + this.info.getClassName());
      String var1 = this.info.getSuperclassName();
      if (var1 != null) {
         this.iw.print(" extends " + var1);
      }

      if (this.interfaceNames.size() > 0) {
         this.iw.print(" implements ");
         boolean var2 = true;

         for(Iterator var3 = this.interfaceNames.iterator(); var3.hasNext(); this.iw.print((String)var3.next())) {
            if (var2) {
               var2 = false;
            } else {
               this.iw.print(", ");
            }
         }
      }

      this.iw.println();
   }

   boolean boundProperties() {
      return BeangenUtils.hasBoundProperties(this.props);
   }

   boolean constrainedProperties() {
      return BeangenUtils.hasConstrainedProperties(this.props);
   }

   public static void main(String[] var0) {
      try {
         SimpleClassInfo var1 = new SimpleClassInfo("test", 1, var0[0], null, null, new String[]{"java.awt"}, null);
         Property[] var2 = new Property[]{
            new SimpleProperty("number", "int", null, "7", false, true, false),
            new SimpleProperty("fpNumber", "float", null, null, true, true, false),
            new SimpleProperty("location", "Point", "new Point( location.x, location.y )", "new Point( 0, 0 )", false, true, true)
         };
         FileWriter var3 = new FileWriter(var0[0] + ".java");
         SimplePropertyBeanGenerator var4 = new SimplePropertyBeanGenerator();
         var4.addExtension(new SerializableExtension());
         var4.generate(var1, var2, var3);
         var3.flush();
         var3.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }
}

package org.eclipse.jdt.internal.compiler.apt.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipException;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.AccessRule;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class EclipseFileManager implements StandardJavaFileManager {
   private static final String NO_EXTENSION = "";
   static final int HAS_EXT_DIRS = 1;
   static final int HAS_BOOTCLASSPATH = 2;
   static final int HAS_ENDORSED_DIRS = 4;
   static final int HAS_PROCESSORPATH = 8;
   Map<File, Archive> archivesCache;
   Charset charset;
   Locale locale;
   Map<String, Iterable<? extends File>> locations;
   int flags;
   public ResourceBundle bundle;

   public EclipseFileManager(Locale locale, Charset charset) {
      this.locale = locale == null ? Locale.getDefault() : locale;
      this.charset = charset == null ? Charset.defaultCharset() : charset;
      this.locations = new HashMap<>();
      this.archivesCache = new HashMap<>();

      try {
         this.setLocation(StandardLocation.PLATFORM_CLASS_PATH, this.getDefaultBootclasspath());
         Iterable<? extends File> defaultClasspath = this.getDefaultClasspath();
         this.setLocation(StandardLocation.CLASS_PATH, defaultClasspath);
         this.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, defaultClasspath);
      } catch (IOException var5) {
      }

      try {
         this.bundle = Main.ResourceBundleFactory.getBundle(this.locale);
      } catch (MissingResourceException var4) {
         System.out.println("Missing resource : " + "org.eclipse.jdt.internal.compiler.batch.messages".replace('.', '/') + ".properties for locale " + locale);
      }
   }

   @Override
   public void close() throws IOException {
      if (this.locations != null) {
         this.locations.clear();
      }

      for(Archive archive : this.archivesCache.values()) {
         archive.close();
      }

      this.archivesCache.clear();
   }

   private void collectAllMatchingFiles(File file, String normalizedPackageName, Set<Kind> kinds, boolean recurse, ArrayList<JavaFileObject> collector) {
      if (!this.isArchive(file)) {
         File currentFile = new File(file, normalizedPackageName);
         if (!currentFile.exists()) {
            return;
         }

         String path;
         try {
            path = currentFile.getCanonicalPath();
         } catch (IOException var14) {
            return;
         }

         if (File.separatorChar == '/') {
            if (!path.endsWith(normalizedPackageName)) {
               return;
            }
         } else if (!path.endsWith(normalizedPackageName.replace('/', File.separatorChar))) {
            return;
         }

         File[] files = currentFile.listFiles();
         if (files != null) {
            for(File f : files) {
               if (f.isDirectory() && recurse) {
                  this.collectAllMatchingFiles(file, normalizedPackageName + '/' + f.getName(), kinds, recurse, collector);
               } else {
                  Kind kind = this.getKind(f);
                  if (kinds.contains(kind)) {
                     collector.add(new EclipseFileObject(normalizedPackageName + f.getName(), f.toURI(), kind, this.charset));
                  }
               }
            }
         }
      } else {
         Archive archive = this.getArchive(file);
         if (archive == Archive.UNKNOWN_ARCHIVE) {
            return;
         }

         String key = normalizedPackageName;
         if (!normalizedPackageName.endsWith("/")) {
            key = normalizedPackageName + '/';
         }

         if (recurse) {
            for(String packageName : archive.allPackages()) {
               if (packageName.startsWith(key)) {
                  List<String> types = archive.getTypes(packageName);
                  if (types != null) {
                     for(String typeName : types) {
                        Kind kind = this.getKind(this.getExtension(typeName));
                        if (kinds.contains(kind)) {
                           collector.add(archive.getArchiveFileObject(packageName + typeName, this.charset));
                        }
                     }
                  }
               }
            }
         } else {
            List<String> types = archive.getTypes(key);
            if (types != null) {
               for(String typeName : types) {
                  Kind kind = this.getKind(this.getExtension(typeName));
                  if (kinds.contains(kind)) {
                     collector.add(archive.getArchiveFileObject(key + typeName, this.charset));
                  }
               }
            }
         }
      }
   }

   private Iterable<? extends File> concatFiles(Iterable<? extends File> iterable, Iterable<? extends File> iterable2) {
      ArrayList<File> list = new ArrayList<>();
      if (iterable2 == null) {
         return iterable;
      } else {
         Iterator<? extends File> iterator = iterable.iterator();

         while(iterator.hasNext()) {
            list.add(iterator.next());
         }

         iterator = iterable2.iterator();

         while(iterator.hasNext()) {
            list.add(iterator.next());
         }

         return list;
      }
   }

   @Override
   public void flush() throws IOException {
      for(Archive archive : this.archivesCache.values()) {
         archive.flush();
      }
   }

   private Archive getArchive(File f) {
      Archive archive = this.archivesCache.get(f);
      if (archive == null) {
         archive = Archive.UNKNOWN_ARCHIVE;
         if (f.exists()) {
            try {
               archive = new Archive(f);
            } catch (ZipException var3) {
            } catch (IOException var4) {
            }

            if (archive != null) {
               this.archivesCache.put(f, archive);
            }
         }

         this.archivesCache.put(f, archive);
      }

      return archive;
   }

   @Override
   public ClassLoader getClassLoader(Location location) {
      Iterable<? extends File> files = this.getLocation(location);
      if (files == null) {
         return null;
      } else {
         ArrayList<URL> allURLs = new ArrayList<>();

         for(File f : files) {
            try {
               allURLs.add(f.toURI().toURL());
            } catch (MalformedURLException var7) {
               throw new RuntimeException(var7);
            }
         }

         URL[] result = new URL[allURLs.size()];
         return new URLClassLoader(allURLs.toArray(result), this.getClass().getClassLoader());
      }
   }

   private Iterable<? extends File> getPathsFrom(String path) {
      ArrayList<FileSystem.Classpath> paths = new ArrayList<>();
      ArrayList<File> files = new ArrayList<>();

      try {
         this.processPathEntries(4, paths, path, this.charset.name(), false, false);
      } catch (IllegalArgumentException var6) {
         return null;
      }

      for(FileSystem.Classpath classpath : paths) {
         files.add(new File(classpath.getPath()));
      }

      return files;
   }

   Iterable<? extends File> getDefaultBootclasspath() {
      List<File> files = new ArrayList<>();
      String javaversion = System.getProperty("java.version");
      if (javaversion.length() > 3) {
         javaversion = javaversion.substring(0, 3);
      }

      long jdkLevel = CompilerOptions.versionToJdkLevel(javaversion);
      if (jdkLevel < 3276800L) {
         return null;
      } else {
         for(String fileName : org.eclipse.jdt.internal.compiler.util.Util.collectFilesNames()) {
            files.add(new File(fileName));
         }

         return files;
      }
   }

   Iterable<? extends File> getDefaultClasspath() {
      ArrayList<File> files = new ArrayList<>();
      String classProp = System.getProperty("java.class.path");
      if (classProp != null && classProp.length() != 0) {
         StringTokenizer tokenizer = new StringTokenizer(classProp, File.pathSeparator);

         while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            File file = new File(token);
            if (file.exists()) {
               files.add(file);
            }
         }

         return files;
      } else {
         return null;
      }
   }

   private Iterable<? extends File> getEndorsedDirsFrom(String path) {
      ArrayList<FileSystem.Classpath> paths = new ArrayList<>();
      ArrayList<File> files = new ArrayList<>();

      try {
         this.processPathEntries(4, paths, path, this.charset.name(), false, false);
      } catch (IllegalArgumentException var6) {
         return null;
      }

      for(FileSystem.Classpath classpath : paths) {
         files.add(new File(classpath.getPath()));
      }

      return files;
   }

   private Iterable<? extends File> getExtdirsFrom(String path) {
      ArrayList<FileSystem.Classpath> paths = new ArrayList<>();
      ArrayList<File> files = new ArrayList<>();

      try {
         this.processPathEntries(4, paths, path, this.charset.name(), false, false);
      } catch (IllegalArgumentException var6) {
         return null;
      }

      for(FileSystem.Classpath classpath : paths) {
         files.add(new File(classpath.getPath()));
      }

      return files;
   }

   private String getExtension(File file) {
      String name = file.getName();
      return this.getExtension(name);
   }

   private String getExtension(String name) {
      int index = name.lastIndexOf(46);
      return index == -1 ? "" : name.substring(index);
   }

   @Override
   public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
      Iterable<? extends File> files = this.getLocation(location);
      if (files == null) {
         throw new IllegalArgumentException("Unknown location : " + location);
      } else {
         String normalizedFileName = this.normalizedFileName(packageName, relativeName);

         for(File file : files) {
            if (file.isDirectory()) {
               File f = new File(file, normalizedFileName);
               if (f.exists()) {
                  return new EclipseFileObject(packageName + File.separator + relativeName, f.toURI(), this.getKind(f), this.charset);
               }
            } else if (this.isArchive(file)) {
               Archive archive = this.getArchive(file);
               if (archive != Archive.UNKNOWN_ARCHIVE && archive.contains(normalizedFileName)) {
                  return archive.getArchiveFileObject(normalizedFileName, this.charset);
               }
            }
         }

         return null;
      }
   }

   private String normalizedFileName(String packageName, String relativeName) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.normalized(packageName));
      if (sb.length() > 0) {
         sb.append('/');
      }

      sb.append(relativeName.replace('\\', '/'));
      return sb.toString();
   }

   @Override
   public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
      Iterable<? extends File> files = this.getLocation(location);
      if (files == null) {
         throw new IllegalArgumentException("Unknown location : " + location);
      } else {
         Iterator<? extends File> iterator = files.iterator();
         if (iterator.hasNext()) {
            File file = iterator.next();
            String normalizedFileName = this.normalized(packageName) + '/' + relativeName.replace('\\', '/');
            File f = new File(file, normalizedFileName);
            return new EclipseFileObject(packageName + File.separator + relativeName, f.toURI(), this.getKind(f), this.charset);
         } else {
            throw new IllegalArgumentException("location is empty : " + location);
         }
      }
   }

   @Override
   public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
      if (kind != Kind.CLASS && kind != Kind.SOURCE) {
         throw new IllegalArgumentException("Invalid kind : " + kind);
      } else {
         Iterable<? extends File> files = this.getLocation(location);
         if (files == null) {
            throw new IllegalArgumentException("Unknown location : " + location);
         } else {
            String normalizedFileName = this.normalized(className);
            normalizedFileName = normalizedFileName + kind.extension;

            for(File file : files) {
               if (file.isDirectory()) {
                  File f = new File(file, normalizedFileName);
                  if (f.exists()) {
                     return new EclipseFileObject(className, f.toURI(), kind, this.charset);
                  }
               } else if (this.isArchive(file)) {
                  Archive archive = this.getArchive(file);
                  if (archive != Archive.UNKNOWN_ARCHIVE && archive.contains(normalizedFileName)) {
                     return archive.getArchiveFileObject(normalizedFileName, this.charset);
                  }
               }
            }

            return null;
         }
      }
   }

   @Override
   public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
      if (kind != Kind.CLASS && kind != Kind.SOURCE) {
         throw new IllegalArgumentException("Invalid kind : " + kind);
      } else {
         Iterable<? extends File> files = this.getLocation(location);
         if (files != null) {
            Iterator<? extends File> iterator = files.iterator();
            if (iterator.hasNext()) {
               File file = iterator.next();
               String normalizedFileName = this.normalized(className);
               normalizedFileName = normalizedFileName + kind.extension;
               File f = new File(file, normalizedFileName);
               return new EclipseFileObject(className, f.toURI(), kind, this.charset);
            } else {
               throw new IllegalArgumentException("location is empty : " + location);
            }
         } else if (!location.equals(StandardLocation.CLASS_OUTPUT) && !location.equals(StandardLocation.SOURCE_OUTPUT)) {
            throw new IllegalArgumentException("Unknown location : " + location);
         } else if (sibling == null) {
            String normalizedFileName = this.normalized(className);
            normalizedFileName = normalizedFileName + kind.extension;
            File f = new File(System.getProperty("user.dir"), normalizedFileName);
            return new EclipseFileObject(className, f.toURI(), kind, this.charset);
         } else {
            String normalizedFileName = this.normalized(className);
            int index = normalizedFileName.lastIndexOf(47);
            if (index != -1) {
               normalizedFileName = normalizedFileName.substring(index + 1);
            }

            normalizedFileName = normalizedFileName + kind.extension;
            URI uri = sibling.toUri();
            URI uri2 = null;

            try {
               String path = uri.getPath();
               index = path.lastIndexOf(47);
               if (index != -1) {
                  path = path.substring(0, index + 1);
                  path = path + normalizedFileName;
               }

               uri2 = new URI(uri.getScheme(), uri.getHost(), path, uri.getFragment());
            } catch (URISyntaxException var11) {
               throw new IllegalArgumentException("invalid sibling");
            }

            return new EclipseFileObject(className, uri2, kind, this.charset);
         }
      }
   }

   @Override
   public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
      return this.getJavaFileObjectsFromFiles(Arrays.asList(files));
   }

   @Override
   public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
      return this.getJavaFileObjectsFromStrings(Arrays.asList(names));
   }

   @Override
   public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
      ArrayList<JavaFileObject> javaFileArrayList = new ArrayList<>();

      for(File f : files) {
         if (f.isDirectory()) {
            throw new IllegalArgumentException("file : " + f.getAbsolutePath() + " is a directory");
         }

         javaFileArrayList.add(new EclipseFileObject(f.getAbsolutePath(), f.toURI(), this.getKind(f), this.charset));
      }

      return javaFileArrayList;
   }

   @Override
   public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
      ArrayList<File> files = new ArrayList<>();

      for(String name : names) {
         files.add(new File(name));
      }

      return this.getJavaFileObjectsFromFiles(files);
   }

   public Kind getKind(File f) {
      return this.getKind(this.getExtension(f));
   }

   private Kind getKind(String extension) {
      if (Kind.CLASS.extension.equals(extension)) {
         return Kind.CLASS;
      } else if (Kind.SOURCE.extension.equals(extension)) {
         return Kind.SOURCE;
      } else {
         return Kind.HTML.extension.equals(extension) ? Kind.HTML : Kind.OTHER;
      }
   }

   @Override
   public Iterable<? extends File> getLocation(Location location) {
      return this.locations == null ? null : this.locations.get(location.getName());
   }

   private Iterable<? extends File> getOutputDir(String string) {
      if ("none".equals(string)) {
         return null;
      } else {
         File file = new File(string);
         if (file.exists() && !file.isDirectory()) {
            throw new IllegalArgumentException("file : " + file.getAbsolutePath() + " is not a directory");
         } else {
            ArrayList<File> list = new ArrayList<>(1);
            list.add(file);
            return list;
         }
      }
   }

   @Override
   public boolean handleOption(String current, Iterator<String> remaining) {
      try {
         if ("-bootclasspath".equals(current)) {
            if (!remaining.hasNext()) {
               throw new IllegalArgumentException();
            }

            Iterable<? extends File> bootclasspaths = this.getPathsFrom(remaining.next());
            if (bootclasspaths != null) {
               Iterable<? extends File> iterable = this.getLocation(StandardLocation.PLATFORM_CLASS_PATH);
               if ((this.flags & 4) == 0 && (this.flags & 1) == 0) {
                  this.setLocation(StandardLocation.PLATFORM_CLASS_PATH, bootclasspaths);
               } else if ((this.flags & 4) != 0) {
                  this.setLocation(StandardLocation.PLATFORM_CLASS_PATH, this.concatFiles(iterable, bootclasspaths));
               } else {
                  this.setLocation(StandardLocation.PLATFORM_CLASS_PATH, this.prependFiles(iterable, bootclasspaths));
               }
            }

            this.flags |= 2;
            return true;
         }

         if ("-classpath".equals(current) || "-cp".equals(current)) {
            if (remaining.hasNext()) {
               Iterable<? extends File> classpaths = this.getPathsFrom(remaining.next());
               if (classpaths != null) {
                  Iterable<? extends File> iterable = this.getLocation(StandardLocation.CLASS_PATH);
                  if (iterable != null) {
                     this.setLocation(StandardLocation.CLASS_PATH, this.concatFiles(iterable, classpaths));
                  } else {
                     this.setLocation(StandardLocation.CLASS_PATH, classpaths);
                  }

                  if ((this.flags & 8) == 0) {
                     this.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, classpaths);
                  }
               }

               return true;
            }

            throw new IllegalArgumentException();
         }

         if ("-encoding".equals(current)) {
            if (remaining.hasNext()) {
               this.charset = Charset.forName(remaining.next());
               return true;
            }

            throw new IllegalArgumentException();
         }

         if ("-sourcepath".equals(current)) {
            if (remaining.hasNext()) {
               Iterable<? extends File> sourcepaths = this.getPathsFrom(remaining.next());
               if (sourcepaths != null) {
                  this.setLocation(StandardLocation.SOURCE_PATH, sourcepaths);
               }

               return true;
            }

            throw new IllegalArgumentException();
         }

         if ("-extdirs".equals(current)) {
            if (remaining.hasNext()) {
               Iterable<? extends File> iterable = this.getLocation(StandardLocation.PLATFORM_CLASS_PATH);
               this.setLocation(StandardLocation.PLATFORM_CLASS_PATH, this.concatFiles(iterable, this.getExtdirsFrom(remaining.next())));
               this.flags |= 1;
               return true;
            }

            throw new IllegalArgumentException();
         }

         if ("-endorseddirs".equals(current)) {
            if (remaining.hasNext()) {
               Iterable<? extends File> iterable = this.getLocation(StandardLocation.PLATFORM_CLASS_PATH);
               this.setLocation(StandardLocation.PLATFORM_CLASS_PATH, this.prependFiles(iterable, this.getEndorsedDirsFrom(remaining.next())));
               this.flags |= 4;
               return true;
            }

            throw new IllegalArgumentException();
         }

         if ("-d".equals(current)) {
            if (remaining.hasNext()) {
               Iterable<? extends File> outputDir = this.getOutputDir(remaining.next());
               if (outputDir != null) {
                  this.setLocation(StandardLocation.CLASS_OUTPUT, outputDir);
               }

               return true;
            }

            throw new IllegalArgumentException();
         }

         if ("-s".equals(current)) {
            if (remaining.hasNext()) {
               Iterable<? extends File> outputDir = this.getOutputDir(remaining.next());
               if (outputDir != null) {
                  this.setLocation(StandardLocation.SOURCE_OUTPUT, outputDir);
               }

               return true;
            }

            throw new IllegalArgumentException();
         }

         if ("-processorpath".equals(current)) {
            if (remaining.hasNext()) {
               Iterable<? extends File> processorpaths = this.getPathsFrom(remaining.next());
               if (processorpaths != null) {
                  this.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, processorpaths);
               }

               this.flags |= 8;
               return true;
            }

            throw new IllegalArgumentException();
         }
      } catch (IOException var5) {
      }

      return false;
   }

   @Override
   public boolean hasLocation(Location location) {
      return this.locations != null && this.locations.containsKey(location.getName());
   }

   @Override
   public String inferBinaryName(Location location, JavaFileObject file) {
      String name = file.getName();
      JavaFileObject javaFileObject = null;
      int index = name.lastIndexOf(46);
      if (index != -1) {
         name = name.substring(0, index);
      }

      try {
         javaFileObject = this.getJavaFileForInput(location, name, file.getKind());
      } catch (IOException var6) {
      } catch (IllegalArgumentException var7) {
         return null;
      }

      return javaFileObject == null ? null : name.replace('/', '.');
   }

   private boolean isArchive(File f) {
      String extension = this.getExtension(f);
      return extension.equalsIgnoreCase(".jar") || extension.equalsIgnoreCase(".zip");
   }

   @Override
   public boolean isSameFile(FileObject fileObject1, FileObject fileObject2) {
      if (!(fileObject1 instanceof EclipseFileObject)) {
         throw new IllegalArgumentException("Unsupported file object class : " + fileObject1.getClass());
      } else if (!(fileObject2 instanceof EclipseFileObject)) {
         throw new IllegalArgumentException("Unsupported file object class : " + fileObject2.getClass());
      } else {
         return fileObject1.equals(fileObject2);
      }
   }

   @Override
   public int isSupportedOption(String option) {
      return Options.processOptionsFileManager(option);
   }

   @Override
   public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
      Iterable<? extends File> allFilesInLocations = this.getLocation(location);
      if (allFilesInLocations == null) {
         throw new IllegalArgumentException("Unknown location : " + location);
      } else {
         ArrayList<JavaFileObject> collector = new ArrayList<>();
         String normalizedPackageName = this.normalized(packageName);

         for(File file : allFilesInLocations) {
            this.collectAllMatchingFiles(file, normalizedPackageName, kinds, recurse, collector);
         }

         return collector;
      }
   }

   private String normalized(String className) {
      char[] classNameChars = className.toCharArray();
      int i = 0;

      for(int max = classNameChars.length; i < max; ++i) {
         switch(classNameChars[i]) {
            case '.':
               classNameChars[i] = '/';
               break;
            case '\\':
               classNameChars[i] = '/';
         }
      }

      return new String(classNameChars);
   }

   private Iterable<? extends File> prependFiles(Iterable<? extends File> iterable, Iterable<? extends File> iterable2) {
      if (iterable2 == null) {
         return iterable;
      } else {
         ArrayList<File> list = new ArrayList<>();
         Iterator<? extends File> iterator = iterable2.iterator();

         while(iterator.hasNext()) {
            list.add(iterator.next());
         }

         iterator = iterable.iterator();

         while(iterator.hasNext()) {
            list.add(iterator.next());
         }

         return list;
      }
   }

   @Override
   public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
      if (path != null) {
         if (location.isOutputLocation()) {
            int count = 0;

            for(Iterator<? extends File> iterator = path.iterator(); iterator.hasNext(); ++count) {
               iterator.next();
            }

            if (count != 1) {
               throw new IllegalArgumentException("output location can only have one path");
            }
         }

         this.locations.put(location.getName(), path);
      }
   }

   public void setLocale(Locale locale) {
      this.locale = locale == null ? Locale.getDefault() : locale;

      try {
         this.bundle = Main.ResourceBundleFactory.getBundle(this.locale);
      } catch (MissingResourceException var3) {
         System.out.println("Missing resource : " + "org.eclipse.jdt.internal.compiler.batch.messages".replace('.', '/') + ".properties for locale " + locale);
         throw var3;
      }
   }

   public void processPathEntries(
      int defaultSize, ArrayList paths, String currentPath, String customEncoding, boolean isSourceOnly, boolean rejectDestinationPathOnJars
   ) {
      String currentClasspathName = null;
      String currentDestinationPath = null;
      ArrayList currentRuleSpecs = new ArrayList(defaultSize);
      StringTokenizer tokenizer = new StringTokenizer(currentPath, File.pathSeparator + "[]", true);
      ArrayList tokens = new ArrayList();

      while(tokenizer.hasMoreTokens()) {
         tokens.add(tokenizer.nextToken());
      }

      int state = 0;
      String token = null;
      int cursor = 0;
      int tokensNb = tokens.size();
      int bracket = -1;

      while(cursor < tokensNb && state != 99) {
         token = (String)tokens.get(cursor++);
         if (token.equals(File.pathSeparator)) {
            switch(state) {
               case 0:
               case 3:
               case 10:
                  break;
               case 1:
               case 2:
               case 8:
                  state = 3;
                  this.addNewEntry(
                     paths, currentClasspathName, currentRuleSpecs, customEncoding, currentDestinationPath, isSourceOnly, rejectDestinationPathOnJars
                  );
                  currentRuleSpecs.clear();
                  break;
               case 4:
               case 5:
               case 9:
               default:
                  state = 99;
                  break;
               case 6:
                  state = 4;
                  break;
               case 7:
                  throw new IllegalArgumentException(this.bind("configure.incorrectDestinationPathEntry", currentPath));
               case 11:
                  cursor = bracket + 1;
                  state = 5;
            }
         } else if (token.equals("[")) {
            switch(state) {
               case 0:
                  currentClasspathName = "";
               case 1:
                  bracket = cursor - 1;
               case 11:
                  state = 10;
                  break;
               case 2:
                  state = 9;
                  break;
               case 3:
               case 4:
               case 5:
               case 6:
               case 7:
               case 9:
               case 10:
               default:
                  state = 99;
                  break;
               case 8:
                  state = 5;
            }
         } else if (token.equals("]")) {
            switch(state) {
               case 6:
                  state = 2;
                  break;
               case 7:
                  state = 8;
                  break;
               case 8:
               case 9:
               case 11:
               default:
                  state = 99;
                  break;
               case 10:
                  state = 11;
            }
         } else {
            switch(state) {
               case 0:
               case 3:
                  state = 1;
                  currentClasspathName = token;
                  break;
               case 1:
               case 2:
               case 6:
               case 7:
               case 8:
               default:
                  state = 99;
                  break;
               case 5:
                  if (token.startsWith("-d ")) {
                     if (currentDestinationPath != null) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateDestinationPathEntry", currentPath));
                     }

                     currentDestinationPath = token.substring(3).trim();
                     state = 7;
                     break;
                  }
               case 4:
                  if (currentDestinationPath != null) {
                     throw new IllegalArgumentException(this.bind("configure.accessRuleAfterDestinationPath", currentPath));
                  }

                  state = 6;
                  currentRuleSpecs.add(token);
                  break;
               case 9:
                  if (!token.startsWith("-d ")) {
                     state = 99;
                  } else {
                     currentDestinationPath = token.substring(3).trim();
                     state = 7;
                  }
               case 10:
                  break;
               case 11:
                  for(int i = bracket; i < cursor; ++i) {
                     currentClasspathName = currentClasspathName + (String)tokens.get(i);
                  }

                  state = 1;
            }
         }

         if (state == 11 && cursor == tokensNb) {
            cursor = bracket + 1;
            state = 5;
         }
      }

      switch(state) {
         case 1:
         case 2:
         case 8:
            this.addNewEntry(paths, currentClasspathName, currentRuleSpecs, customEncoding, currentDestinationPath, isSourceOnly, rejectDestinationPathOnJars);
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 9:
         case 10:
         case 11:
      }
   }

   protected void addNewEntry(
      ArrayList paths,
      String currentClasspathName,
      ArrayList currentRuleSpecs,
      String customEncoding,
      String destPath,
      boolean isSourceOnly,
      boolean rejectDestinationPathOnJars
   ) {
      int rulesSpecsSize = currentRuleSpecs.size();
      AccessRuleSet accessRuleSet = null;
      if (rulesSpecsSize != 0) {
         AccessRule[] accessRules = new AccessRule[currentRuleSpecs.size()];
         boolean rulesOK = true;
         Iterator i = currentRuleSpecs.iterator();
         int j = 0;

         while(i.hasNext()) {
            String ruleSpec = (String)i.next();
            char key = ruleSpec.charAt(0);
            String pattern = ruleSpec.substring(1);
            if (pattern.length() > 0) {
               switch(key) {
                  case '+':
                     accessRules[j++] = new AccessRule(pattern.toCharArray(), 0);
                     break;
                  case '-':
                     accessRules[j++] = new AccessRule(pattern.toCharArray(), 16777523);
                     break;
                  case '?':
                     accessRules[j++] = new AccessRule(pattern.toCharArray(), 16777523, true);
                     break;
                  case '~':
                     accessRules[j++] = new AccessRule(pattern.toCharArray(), 16777496);
                     break;
                  default:
                     rulesOK = false;
               }
            } else {
               rulesOK = false;
            }
         }

         if (!rulesOK) {
            return;
         }

         accessRuleSet = new AccessRuleSet(accessRules, (byte)0, currentClasspathName);
      }

      if ("none".equals(destPath)) {
         destPath = "none";
      }

      if (!rejectDestinationPathOnJars || destPath == null || !currentClasspathName.endsWith(".jar") && !currentClasspathName.endsWith(".zip")) {
         FileSystem.Classpath currentClasspath = FileSystem.getClasspath(currentClasspathName, customEncoding, isSourceOnly, accessRuleSet, destPath, null);
         if (currentClasspath != null) {
            paths.add(currentClasspath);
         }
      } else {
         throw new IllegalArgumentException(this.bind("configure.unexpectedDestinationPathEntryFile", currentClasspathName));
      }
   }

   private String bind(String id, String binding) {
      return this.bind(id, new String[]{binding});
   }

   private String bind(String id, String[] arguments) {
      if (id == null) {
         return "No message available";
      } else {
         String message = null;

         try {
            message = this.bundle.getString(id);
         } catch (MissingResourceException var4) {
            return "Missing message: " + id + " in: " + "org.eclipse.jdt.internal.compiler.batch.messages";
         }

         return MessageFormat.format(message, arguments);
      }
   }
}

package org.eclipse.jdt.internal.compiler.classfmt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.SignatureWrapper;

public class ExternalAnnotationProvider {
   public static final String ANNOTATION_FILE_EXTENSION = "eea";
   public static final String CLASS_PREFIX = "class ";
   public static final String SUPER_PREFIX = "super ";
   public static final char NULLABLE = '0';
   public static final char NONNULL = '1';
   public static final char NO_ANNOTATION = '@';
   public static final String ANNOTATION_FILE_SUFFIX = ".eea";
   private static final String TYPE_PARAMETER_PREFIX = " <";
   private String typeName;
   String typeParametersAnnotationSource;
   Map<String, String> supertypeAnnotationSources;
   private Map<String, String> methodAnnotationSources;
   private Map<String, String> fieldAnnotationSources;
   ExternalAnnotationProvider.SingleMarkerAnnotation NULLABLE_ANNOTATION;
   ExternalAnnotationProvider.SingleMarkerAnnotation NONNULL_ANNOTATION;

   public ExternalAnnotationProvider(InputStream input, String typeName) throws IOException {
      this.typeName = typeName;
      this.initialize(input);
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void initialize(InputStream input) throws IOException {
      Throwable var2 = null;
      Object var3 = null;

      try {
         LineNumberReader reader = new LineNumberReader(new InputStreamReader(input));

         try {
            assertClassHeader(reader.readLine(), this.typeName);
            String line;
            if ((line = reader.readLine()) == null) {
               return;
            }

            if (line.startsWith(" <")) {
               if ((line = reader.readLine()) == null) {
                  return;
               }

               if (line.startsWith(" <")) {
                  this.typeParametersAnnotationSource = line.substring(" <".length());
                  if ((line = reader.readLine()) == null) {
                     return;
                  }
               }
            }

            String pendingLine;
            do {
               pendingLine = null;
               line = line.trim();
               label312:
               if (!line.isEmpty()) {
                  String rawSig = null;
                  String annotSig = null;
                  String selector = line;
                  boolean isSuper = line.startsWith("super ");
                  if (isSuper) {
                     selector = line.substring("super ".length());
                  }

                  int errLine = -1;

                  try {
                     line = reader.readLine();
                     if (line != null && !line.isEmpty() && line.charAt(0) == ' ') {
                        rawSig = line.substring(1);
                     } else {
                        errLine = reader.getLineNumber();
                     }

                     line = reader.readLine();
                     if (line == null || line.isEmpty()) {
                        break label312;
                     }

                     if (line.charAt(0) != ' ') {
                        pendingLine = line;
                        break label312;
                     }

                     annotSig = line.substring(1);
                  } catch (Exception var18) {
                  }

                  if (rawSig == null || annotSig == null) {
                     if (errLine == -1) {
                        errLine = reader.getLineNumber();
                     }

                     throw new IOException("Illegal format for annotation file at line " + errLine);
                  }

                  annotSig = trimTail(annotSig);
                  if (isSuper) {
                     if (this.supertypeAnnotationSources == null) {
                        this.supertypeAnnotationSources = new HashMap<>();
                     }

                     this.supertypeAnnotationSources.put('L' + selector + rawSig + ';', annotSig);
                  } else if (rawSig.contains("(")) {
                     if (this.methodAnnotationSources == null) {
                        this.methodAnnotationSources = new HashMap<>();
                     }

                     this.methodAnnotationSources.put(selector + rawSig, annotSig);
                  } else {
                     if (this.fieldAnnotationSources == null) {
                        this.fieldAnnotationSources = new HashMap<>();
                     }

                     this.fieldAnnotationSources.put(selector + ':' + rawSig, annotSig);
                  }
               }

               line = pendingLine;
            } while(pendingLine != null || (line = reader.readLine()) != null);
         } finally {
            if (reader != null) {
               reader.close();
            }
         }
      } catch (Throwable var20) {
         if (var2 == null) {
            var2 = var20;
         } else if (var2 != var20) {
            var2.addSuppressed(var20);
         }

         throw var2;
      }
   }

   public static void assertClassHeader(String line, String typeName) throws IOException {
      if (line != null && line.startsWith("class ")) {
         line = line.substring("class ".length());
         if (!trimTail(line).equals(typeName)) {
            throw new IOException("mismatching class name in annotation file, expected " + typeName + ", but header said " + line);
         }
      } else {
         throw new IOException("missing class header in annotation file");
      }
   }

   public static String extractSignature(String line) {
      return line != null && !line.isEmpty() && line.charAt(0) == ' ' ? trimTail(line.substring(1)) : null;
   }

   protected static String trimTail(String line) {
      int tail = line.indexOf(32);
      if (tail == -1) {
         tail = line.indexOf(9);
      }

      return tail != -1 ? line.substring(0, tail) : line;
   }

   public ITypeAnnotationWalker forTypeHeader(LookupEnvironment environment) {
      return (ITypeAnnotationWalker)(this.typeParametersAnnotationSource == null && this.supertypeAnnotationSources == null
         ? ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER
         : new ExternalAnnotationProvider.DispatchingAnnotationWalker(environment));
   }

   public ITypeAnnotationWalker forMethod(char[] selector, char[] signature, LookupEnvironment environment) {
      Map<String, String> sources = this.methodAnnotationSources;
      if (sources != null) {
         String source = sources.get(String.valueOf(CharOperation.concat(selector, signature)));
         if (source != null) {
            return new ExternalAnnotationProvider.MethodAnnotationWalker(source.toCharArray(), 0, environment);
         }
      }

      return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
   }

   public ITypeAnnotationWalker forField(char[] selector, char[] signature, LookupEnvironment environment) {
      if (this.fieldAnnotationSources != null) {
         String source = this.fieldAnnotationSources.get(String.valueOf(CharOperation.concat(selector, signature, ':')));
         if (source != null) {
            return new ExternalAnnotationProvider.FieldAnnotationWalker(source.toCharArray(), 0, environment);
         }
      }

      return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("External Annotations for ").append(this.typeName).append('\n');
      sb.append("Methods:\n");
      if (this.methodAnnotationSources != null) {
         for(Entry<String, String> e : this.methodAnnotationSources.entrySet()) {
            sb.append('\t').append(e.getKey()).append('\n');
         }
      }

      return sb.toString();
   }

   void initAnnotations(final LookupEnvironment environment) {
      if (this.NULLABLE_ANNOTATION == null) {
         this.NULLABLE_ANNOTATION = new ExternalAnnotationProvider.SingleMarkerAnnotation(this) {
            @Override
            public char[] getTypeName() {
               return this.getBinaryTypeName(environment.getNullableAnnotationName());
            }
         };
      }

      if (this.NONNULL_ANNOTATION == null) {
         this.NONNULL_ANNOTATION = new ExternalAnnotationProvider.SingleMarkerAnnotation(this) {
            @Override
            public char[] getTypeName() {
               return this.getBinaryTypeName(environment.getNonNullAnnotationName());
            }
         };
      }
   }

   abstract class BasicAnnotationWalker implements ITypeAnnotationWalker {
      char[] source;
      SignatureWrapper wrapper;
      int pos;
      int prevTypeArgStart;
      int currentTypeBound;
      LookupEnvironment environment;

      BasicAnnotationWalker(char[] source, int pos, LookupEnvironment environment) {
         this.source = source;
         this.pos = pos;
         this.environment = environment;
         ExternalAnnotationProvider.this.initAnnotations(environment);
      }

      SignatureWrapper wrapperWithStart(int start) {
         if (this.wrapper == null) {
            this.wrapper = new SignatureWrapper(this.source);
         }

         this.wrapper.start = start;
         return this.wrapper;
      }

      @Override
      public ITypeAnnotationWalker toReceiver() {
         return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
      }

      @Override
      public ITypeAnnotationWalker toTypeParameter(boolean isClassTypeParameter, int rank) {
         return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
      }

      @Override
      public ITypeAnnotationWalker toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) {
         return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
      }

      @Override
      public ITypeAnnotationWalker toTypeBound(short boundIndex) {
         return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
      }

      @Override
      public ITypeAnnotationWalker toSupertype(short index, char[] superTypeSignature) {
         return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
      }

      @Override
      public ITypeAnnotationWalker toTypeArgument(int rank) {
         if (rank == 0) {
            int start = CharOperation.indexOf('<', this.source, this.pos) + 1;
            this.prevTypeArgStart = start;
            return ExternalAnnotationProvider.this.new MethodAnnotationWalker(this.source, start, this.environment);
         } else {
            int next = this.prevTypeArgStart;
            switch(this.source[next]) {
               case '+':
               case '-':
                  ++next;
               case ',':
               default:
                  next = this.wrapperWithStart(next).computeEnd();
               case '*':
                  this.prevTypeArgStart = ++next;
                  return ExternalAnnotationProvider.this.new MethodAnnotationWalker(this.source, next, this.environment);
            }
         }
      }

      @Override
      public ITypeAnnotationWalker toWildcardBound() {
         switch(this.source[this.pos]) {
            case '+':
            case '-':
               return ExternalAnnotationProvider.this.new MethodAnnotationWalker(this.source, this.pos + 1, this.environment);
            case ',':
            default:
               return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
         }
      }

      @Override
      public ITypeAnnotationWalker toNextArrayDimension() {
         if (this.source[this.pos] == '[') {
            int newPos = this.pos + 1;
            switch(this.source[newPos]) {
               case '0':
               case '1':
                  ++newPos;
               default:
                  return ExternalAnnotationProvider.this.new MethodAnnotationWalker(this.source, newPos, this.environment);
            }
         } else {
            return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
         }
      }

      @Override
      public ITypeAnnotationWalker toNextNestedType() {
         return this;
      }

      @Override
      public IBinaryAnnotation[] getAnnotationsAtCursor(int currentTypeId) {
         if (this.pos != -1 && this.pos < this.source.length - 2) {
            switch(this.source[this.pos]) {
               case '*':
               case '+':
               case '-':
               case 'L':
               case 'T':
               case '[':
                  switch(this.source[this.pos + 1]) {
                     case '0':
                        return new IBinaryAnnotation[]{ExternalAnnotationProvider.this.NULLABLE_ANNOTATION};
                     case '1':
                        return new IBinaryAnnotation[]{ExternalAnnotationProvider.this.NONNULL_ANNOTATION};
                  }
            }
         }

         return NO_ANNOTATIONS;
      }
   }

   class DispatchingAnnotationWalker implements ITypeAnnotationWalker {
      private LookupEnvironment environment;
      private ExternalAnnotationProvider.TypeParametersAnnotationWalker typeParametersWalker;

      public DispatchingAnnotationWalker(LookupEnvironment environment) {
         this.environment = environment;
      }

      @Override
      public ITypeAnnotationWalker toTypeParameter(boolean isClassTypeParameter, int rank) {
         String source = ExternalAnnotationProvider.this.typeParametersAnnotationSource;
         if (source != null) {
            if (this.typeParametersWalker == null) {
               this.typeParametersWalker = ExternalAnnotationProvider.this.new TypeParametersAnnotationWalker(
                  source.toCharArray(), 0, 0, null, this.environment
               );
            }

            return this.typeParametersWalker.toTypeParameter(isClassTypeParameter, rank);
         } else {
            return this;
         }
      }

      @Override
      public ITypeAnnotationWalker toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) {
         return (ITypeAnnotationWalker)(this.typeParametersWalker != null
            ? this.typeParametersWalker.toTypeParameterBounds(isClassTypeParameter, parameterRank)
            : this);
      }

      @Override
      public ITypeAnnotationWalker toSupertype(short index, char[] superTypeSignature) {
         Map<String, String> sources = ExternalAnnotationProvider.this.supertypeAnnotationSources;
         if (sources != null) {
            String source = sources.get(String.valueOf(superTypeSignature));
            if (source != null) {
               return ExternalAnnotationProvider.this.new SuperTypesAnnotationWalker(source.toCharArray(), this.environment);
            }
         }

         return this;
      }

      @Override
      public ITypeAnnotationWalker toField() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toThrows(int rank) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toTypeArgument(int rank) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toMethodParameter(short index) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toTypeBound(short boundIndex) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toMethodReturn() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toReceiver() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toWildcardBound() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toNextArrayDimension() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toNextNestedType() {
         return this;
      }

      @Override
      public IBinaryAnnotation[] getAnnotationsAtCursor(int currentTypeId) {
         return NO_ANNOTATIONS;
      }
   }

   class FieldAnnotationWalker extends ExternalAnnotationProvider.BasicAnnotationWalker {
      public FieldAnnotationWalker(char[] source, int pos, LookupEnvironment environment) {
         super(source, pos, environment);
      }

      @Override
      public ITypeAnnotationWalker toField() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toMethodReturn() {
         throw new UnsupportedOperationException("Field has no method return");
      }

      @Override
      public ITypeAnnotationWalker toMethodParameter(short index) {
         throw new UnsupportedOperationException("Field has no method parameter");
      }

      @Override
      public ITypeAnnotationWalker toThrows(int index) {
         throw new UnsupportedOperationException("Field has no throws");
      }
   }

   public interface IMethodAnnotationWalker extends ITypeAnnotationWalker {
      int getParameterCount();
   }

   class MethodAnnotationWalker extends ExternalAnnotationProvider.BasicAnnotationWalker implements ExternalAnnotationProvider.IMethodAnnotationWalker {
      int prevParamStart;
      ExternalAnnotationProvider.TypeParametersAnnotationWalker typeParametersWalker;

      MethodAnnotationWalker(char[] source, int pos, LookupEnvironment environment) {
         super(source, pos, environment);
      }

      int typeEnd(int start) {
         while(this.source[start] == '[') {
            char an = this.source[++start];
            if (an == '0' || an == '1') {
               ++start;
            }
         }

         return this.wrapperWithStart(start).computeEnd();
      }

      @Override
      public ITypeAnnotationWalker toTypeParameter(boolean isClassTypeParameter, int rank) {
         if (this.source[0] == '<') {
            return (ITypeAnnotationWalker)(this.typeParametersWalker == null
               ? (
                  this.typeParametersWalker = ExternalAnnotationProvider.this.new TypeParametersAnnotationWalker(
                     this.source, this.pos + 1, rank, null, this.environment
                  )
               )
               : this.typeParametersWalker.toTypeParameter(isClassTypeParameter, rank));
         } else {
            return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
         }
      }

      @Override
      public ITypeAnnotationWalker toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) {
         return this.typeParametersWalker != null
            ? this.typeParametersWalker.toTypeParameterBounds(isClassTypeParameter, parameterRank)
            : ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
      }

      @Override
      public ITypeAnnotationWalker toMethodReturn() {
         int close = CharOperation.indexOf(')', this.source);
         if (close != -1) {
            this.pos = close + 1;
            return this;
         } else {
            return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
         }
      }

      @Override
      public ITypeAnnotationWalker toMethodParameter(short index) {
         if (index == 0) {
            int start = CharOperation.indexOf('(', this.source) + 1;
            this.prevParamStart = start;
            this.pos = start;
            return this;
         } else {
            int end = this.typeEnd(this.prevParamStart);
            this.prevParamStart = ++end;
            this.pos = end;
            return this;
         }
      }

      @Override
      public ITypeAnnotationWalker toThrows(int index) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toField() {
         throw new UnsupportedOperationException("Methods have no fields");
      }

      @Override
      public int getParameterCount() {
         int count = 0;

         for(int start = CharOperation.indexOf('(', this.source) + 1; start < this.source.length && this.source[start] != ')'; ++count) {
            start = this.typeEnd(start) + 1;
         }

         return count;
      }
   }

   abstract class SingleMarkerAnnotation implements IBinaryAnnotation {
      @Override
      public IBinaryElementValuePair[] getElementValuePairs() {
         return ElementValuePairInfo.NoMembers;
      }

      protected char[] getBinaryTypeName(char[][] name) {
         return CharOperation.concat('L', CharOperation.concatWith(name, '/'), ';');
      }
   }

   class SuperTypesAnnotationWalker extends ExternalAnnotationProvider.BasicAnnotationWalker {
      SuperTypesAnnotationWalker(char[] source, LookupEnvironment environment) {
         super(source, 0, environment);
      }

      @Override
      public ITypeAnnotationWalker toField() {
         throw new UnsupportedOperationException("Supertype has no field annotations");
      }

      @Override
      public ITypeAnnotationWalker toMethodReturn() {
         throw new UnsupportedOperationException("Supertype has no method return");
      }

      @Override
      public ITypeAnnotationWalker toMethodParameter(short index) {
         throw new UnsupportedOperationException("Supertype has no method parameter");
      }

      @Override
      public ITypeAnnotationWalker toThrows(int index) {
         throw new UnsupportedOperationException("Supertype has no throws");
      }
   }

   public class TypeParametersAnnotationWalker extends ExternalAnnotationProvider.BasicAnnotationWalker {
      int[] rankStarts;
      int currentRank;

      TypeParametersAnnotationWalker(char[] source, int pos, int rank, int[] rankStarts, LookupEnvironment environment) {
         super(source, pos, environment);
         this.currentRank = rank;
         if (rankStarts != null) {
            this.rankStarts = rankStarts;
         } else {
            int length = source.length;
            rankStarts = new int[length];
            int curRank = 0;
            int depth = 0;
            boolean pendingVariable = true;

            label75:
            for(int i = pos; i < length; ++i) {
               switch(this.source[i]) {
                  case ':':
                     if (depth == 0) {
                        pendingVariable = true;
                     }

                     ++i;

                     while(i < length && this.source[i] == '[') {
                        ++i;
                     }

                     if (i < length && this.source[i] == 'L') {
                        for(int currentdepth = depth; i < length && (currentdepth != depth || this.source[i] != ';'); ++i) {
                           if (this.source[i] == '<') {
                              ++currentdepth;
                           }

                           if (this.source[i] == '>') {
                              --currentdepth;
                           }
                        }
                     }

                     --i;
                     break;
                  case ';':
                     if (depth == 0 && i + 1 < length && this.source[i + 1] != ':') {
                        pendingVariable = true;
                     }
                     break;
                  case '<':
                     ++depth;
                     break;
                  case '=':
                  default:
                     if (pendingVariable) {
                        pendingVariable = false;
                        rankStarts[curRank++] = i;
                     }
                     break;
                  case '>':
                     if (--depth < 0) {
                        break label75;
                     }
               }
            }

            System.arraycopy(rankStarts, 0, this.rankStarts = new int[curRank], 0, curRank);
         }
      }

      @Override
      public ITypeAnnotationWalker toTypeParameter(boolean isClassTypeParameter, int rank) {
         if (rank == this.currentRank) {
            return this;
         } else {
            return (ITypeAnnotationWalker)(rank < this.rankStarts.length
               ? ExternalAnnotationProvider.this.new TypeParametersAnnotationWalker(
                  this.source, this.rankStarts[rank], rank, this.rankStarts, this.environment
               )
               : ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER);
         }
      }

      @Override
      public ITypeAnnotationWalker toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) {
         return ExternalAnnotationProvider.this.new TypeParametersAnnotationWalker(
            this.source, this.rankStarts[parameterRank], parameterRank, this.rankStarts, this.environment
         );
      }

      @Override
      public ITypeAnnotationWalker toTypeBound(short boundIndex) {
         int p = this.pos;
         int i = this.currentTypeBound;

         while(true) {
            int colon = CharOperation.indexOf(':', this.source, p);
            if (colon != -1) {
               p = colon + 1;
            }

            if (++i > boundIndex) {
               this.pos = p;
               this.currentTypeBound = boundIndex;
               return this;
            }

            p = this.wrapperWithStart(p).computeEnd() + 1;
         }
      }

      @Override
      public ITypeAnnotationWalker toField() {
         throw new UnsupportedOperationException("Cannot navigate to fields");
      }

      @Override
      public ITypeAnnotationWalker toMethodReturn() {
         throw new UnsupportedOperationException("Cannot navigate to method return");
      }

      @Override
      public ITypeAnnotationWalker toMethodParameter(short index) {
         throw new UnsupportedOperationException("Cannot navigate to method parameter");
      }

      @Override
      public ITypeAnnotationWalker toThrows(int index) {
         throw new UnsupportedOperationException("Cannot navigate to throws");
      }

      @Override
      public IBinaryAnnotation[] getAnnotationsAtCursor(int currentTypeId) {
         if (this.pos != -1 && this.pos < this.source.length - 1) {
            switch(this.source[this.pos]) {
               case '0':
                  return new IBinaryAnnotation[]{ExternalAnnotationProvider.this.NULLABLE_ANNOTATION};
               case '1':
                  return new IBinaryAnnotation[]{ExternalAnnotationProvider.this.NONNULL_ANNOTATION};
            }
         }

         return super.getAnnotationsAtCursor(currentTypeId);
      }
   }
}

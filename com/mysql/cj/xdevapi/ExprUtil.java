package com.mysql.cj.xdevapi;

import com.google.protobuf.ByteString;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import com.mysql.cj.x.protobuf.MysqlxExpr;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class ExprUtil {
   private static SimpleDateFormat javaSqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
   private static SimpleDateFormat javaSqlTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
   private static SimpleDateFormat javaSqlTimeFormat = new SimpleDateFormat("HH:mm:ss.S");
   private static SimpleDateFormat javaUtilDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");

   public static MysqlxExpr.Expr buildLiteralNullScalar() {
      return buildLiteralExpr(nullScalar());
   }

   public static MysqlxExpr.Expr buildLiteralScalar(double d) {
      return buildLiteralExpr(scalarOf(d));
   }

   public static MysqlxExpr.Expr buildLiteralScalar(long l) {
      return buildLiteralExpr(scalarOf(l));
   }

   public static MysqlxExpr.Expr buildLiteralScalar(String str) {
      return buildLiteralExpr(scalarOf(str));
   }

   public static MysqlxExpr.Expr buildLiteralScalar(byte[] bytes) {
      return buildLiteralExpr(scalarOf(bytes));
   }

   public static MysqlxExpr.Expr buildLiteralScalar(boolean b) {
      return buildLiteralExpr(scalarOf(b));
   }

   public static MysqlxExpr.Expr buildLiteralExpr(MysqlxDatatypes.Scalar scalar) {
      return MysqlxExpr.Expr.newBuilder().setType(MysqlxExpr.Expr.Type.LITERAL).setLiteral(scalar).build();
   }

   public static MysqlxDatatypes.Scalar nullScalar() {
      return MysqlxDatatypes.Scalar.newBuilder().setType(MysqlxDatatypes.Scalar.Type.V_NULL).build();
   }

   public static MysqlxDatatypes.Scalar scalarOf(double d) {
      return MysqlxDatatypes.Scalar.newBuilder().setType(MysqlxDatatypes.Scalar.Type.V_DOUBLE).setVDouble(d).build();
   }

   public static MysqlxDatatypes.Scalar scalarOf(long l) {
      return MysqlxDatatypes.Scalar.newBuilder().setType(MysqlxDatatypes.Scalar.Type.V_SINT).setVSignedInt(l).build();
   }

   public static MysqlxDatatypes.Scalar scalarOf(String str) {
      MysqlxDatatypes.Scalar.String sstr = MysqlxDatatypes.Scalar.String.newBuilder().setValue(ByteString.copyFromUtf8(str)).build();
      return MysqlxDatatypes.Scalar.newBuilder().setType(MysqlxDatatypes.Scalar.Type.V_STRING).setVString(sstr).build();
   }

   public static MysqlxDatatypes.Scalar scalarOf(byte[] bytes) {
      MysqlxDatatypes.Scalar.Octets.Builder o = MysqlxDatatypes.Scalar.Octets.newBuilder().setValue(ByteString.copyFrom(bytes));
      return MysqlxDatatypes.Scalar.newBuilder().setType(MysqlxDatatypes.Scalar.Type.V_OCTETS).setVOctets(o).build();
   }

   public static MysqlxDatatypes.Scalar scalarOf(boolean b) {
      return MysqlxDatatypes.Scalar.newBuilder().setType(MysqlxDatatypes.Scalar.Type.V_BOOL).setVBool(b).build();
   }

   public static MysqlxDatatypes.Any buildAny(String str) {
      MysqlxDatatypes.Scalar.String sstr = MysqlxDatatypes.Scalar.String.newBuilder().setValue(ByteString.copyFromUtf8(str)).build();
      MysqlxDatatypes.Scalar s = MysqlxDatatypes.Scalar.newBuilder().setType(MysqlxDatatypes.Scalar.Type.V_STRING).setVString(sstr).build();
      return MysqlxDatatypes.Any.newBuilder().setType(MysqlxDatatypes.Any.Type.SCALAR).setScalar(s).build();
   }

   public static MysqlxDatatypes.Any buildAny(boolean b) {
      return MysqlxDatatypes.Any.newBuilder().setType(MysqlxDatatypes.Any.Type.SCALAR).setScalar(scalarOf(b)).build();
   }

   public static MysqlxCrud.Collection buildCollection(String schemaName, String collectionName) {
      return MysqlxCrud.Collection.newBuilder().setSchema(schemaName).setName(collectionName).build();
   }

   public static MysqlxDatatypes.Scalar argObjectToScalar(Object value) {
      MysqlxExpr.Expr e = argObjectToExpr(value, false);
      if (!e.hasLiteral()) {
         throw new WrongArgumentException("No literal interpretation of argument: " + value);
      } else {
         return e.getLiteral();
      }
   }

   public static MysqlxDatatypes.Any argObjectToScalarAny(Object value) {
      MysqlxDatatypes.Scalar s = argObjectToScalar(value);
      return MysqlxDatatypes.Any.newBuilder().setType(MysqlxDatatypes.Any.Type.SCALAR).setScalar(s).build();
   }

   public static MysqlxExpr.Expr argObjectToExpr(Object value, boolean allowRelationalColumns) {
      if (value == null) {
         return buildLiteralNullScalar();
      } else if (value.getClass() == Boolean.class) {
         return buildLiteralScalar((Boolean)value);
      } else if (value.getClass() == Byte.class) {
         return buildLiteralScalar(((Byte)value).longValue());
      } else if (value.getClass() == Short.class) {
         return buildLiteralScalar(((Short)value).longValue());
      } else if (value.getClass() == Integer.class) {
         return buildLiteralScalar(((Integer)value).longValue());
      } else if (value.getClass() == Long.class) {
         return buildLiteralScalar((Long)value);
      } else if (value.getClass() == Float.class) {
         return buildLiteralScalar(((Float)value).doubleValue());
      } else if (value.getClass() == Double.class) {
         return buildLiteralScalar((Double)value);
      } else if (value.getClass() == String.class) {
         return buildLiteralScalar((String)value);
      } else if (value.getClass() == Expression.class) {
         return new ExprParser(((Expression)value).getExpressionString(), allowRelationalColumns).parse();
      } else if (value.getClass() == Date.class) {
         return buildLiteralScalar(javaSqlDateFormat.format((java.util.Date)value));
      } else if (value.getClass() == Time.class) {
         return buildLiteralScalar(javaSqlTimeFormat.format((java.util.Date)value));
      } else if (value.getClass() == Timestamp.class) {
         return buildLiteralScalar(javaSqlTimestampFormat.format((java.util.Date)value));
      } else if (value.getClass() == java.util.Date.class) {
         return buildLiteralScalar(javaUtilDateFormat.format((java.util.Date)value));
      } else if (DbDoc.class.isAssignableFrom(value.getClass())) {
         return new ExprParser(((DbDoc)value).toString()).parse();
      } else if (value.getClass() == JsonArray.class) {
         return MysqlxExpr.Expr.newBuilder()
            .setType(MysqlxExpr.Expr.Type.ARRAY)
            .setArray(
               MysqlxExpr.Expr.newBuilder()
                  .setType(MysqlxExpr.Expr.Type.ARRAY)
                  .getArrayBuilder()
                  .addAllValue(((JsonArray)value).stream().map(f -> argObjectToExpr(f, true)).collect(Collectors.toList()))
            )
            .build();
      } else if (value.getClass() == JsonString.class) {
         return buildLiteralScalar(((JsonString)value).getString());
      } else if (value.getClass() == JsonNumber.class) {
         return buildLiteralScalar((long)((JsonNumber)value).getInteger().intValue());
      } else {
         throw new FeatureNotAvailableException("TODO: other types: BigDecimal");
      }
   }
}

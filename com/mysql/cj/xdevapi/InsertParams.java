package com.mysql.cj.xdevapi;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class InsertParams {
   private List<MysqlxCrud.Column> projection;
   private List<MysqlxCrud.Insert.TypedRow> rows = new LinkedList<>();

   public void setProjection(String[] projection) {
      this.projection = Arrays.stream(projection).map(p -> new ExprParser(p, true).parseTableInsertField()).collect(Collectors.toList());
   }

   public Object getProjection() {
      return this.projection;
   }

   public void addRow(List<Object> row) {
      this.rows
         .add(
            MysqlxCrud.Insert.TypedRow.newBuilder().addAllField(row.stream().map(f -> ExprUtil.argObjectToExpr(f, true)).collect(Collectors.toList())).build()
         );
   }

   public Object getRows() {
      return this.rows;
   }

   public void setFieldsAndValues(Map<String, Object> fieldsAndValues) {
      this.projection = new ArrayList<>();
      MysqlxCrud.Insert.TypedRow.Builder rowBuilder = MysqlxCrud.Insert.TypedRow.newBuilder();
      fieldsAndValues.entrySet().stream().forEach(e -> {
         this.projection.add(new ExprParser(e.getKey(), true).parseTableInsertField());
         rowBuilder.addField(ExprUtil.argObjectToExpr(e.getValue(), true));
      });
      this.rows.add(rowBuilder.build());
   }
}

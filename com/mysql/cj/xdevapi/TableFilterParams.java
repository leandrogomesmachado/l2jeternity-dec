package com.mysql.cj.xdevapi;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TableFilterParams extends AbstractFilterParams {
   public TableFilterParams(String schemaName, String collectionName) {
      super(schemaName, collectionName, true);
   }

   @Override
   public void setFields(String... projection) {
      this.projection = projection;
      this.fields = new ExprParser(Arrays.stream(projection).collect(Collectors.joining(", ")), true).parseTableSelectProjection();
   }
}

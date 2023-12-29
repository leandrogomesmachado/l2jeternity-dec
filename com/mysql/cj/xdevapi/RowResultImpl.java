package com.mysql.cj.xdevapi;

import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.x.StatementExecuteOk;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.RowList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RowResultImpl extends AbstractDataResult<Row> implements RowResult {
   private ColumnDefinition metadata;

   public RowResultImpl(ColumnDefinition metadata, TimeZone defaultTimeZone, RowList rows, Supplier<StatementExecuteOk> completer) {
      super(rows, completer, new RowFactory(metadata, defaultTimeZone));
      this.metadata = metadata;
   }

   @Override
   public int getColumnCount() {
      return this.metadata.getFields().length;
   }

   @Override
   public List<Column> getColumns() {
      return Arrays.stream(this.metadata.getFields()).map(ColumnImpl::new).collect(Collectors.toList());
   }

   @Override
   public List<String> getColumnNames() {
      return Arrays.stream(this.metadata.getFields()).map(Field::getColumnLabel).collect(Collectors.toList());
   }
}

package com.mysql.cj.xdevapi;

import java.util.List;

public interface RowResult extends FetchResult<Row> {
   int getColumnCount();

   List<Column> getColumns();

   List<String> getColumnNames();
}

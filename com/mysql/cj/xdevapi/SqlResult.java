package com.mysql.cj.xdevapi;

public interface SqlResult extends Result, InsertResult, RowResult {
   boolean nextResult();
}

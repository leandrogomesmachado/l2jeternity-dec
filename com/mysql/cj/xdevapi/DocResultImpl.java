package com.mysql.cj.xdevapi;

import com.mysql.cj.protocol.x.StatementExecuteOk;
import com.mysql.cj.result.RowList;
import java.util.function.Supplier;

public class DocResultImpl extends AbstractDataResult<DbDoc> implements DocResult {
   public DocResultImpl(RowList rows, Supplier<StatementExecuteOk> completer) {
      super(rows, completer, new DbDocFactory());
   }
}

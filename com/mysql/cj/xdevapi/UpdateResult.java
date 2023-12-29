package com.mysql.cj.xdevapi;

import com.mysql.cj.protocol.x.StatementExecuteOk;
import java.util.Iterator;
import java.util.stream.Collectors;

public class UpdateResult implements Result {
   protected StatementExecuteOk ok;

   public UpdateResult(StatementExecuteOk ok) {
      this.ok = ok;
   }

   @Override
   public long getAffectedItemsCount() {
      return this.ok.getRowsAffected();
   }

   @Override
   public int getWarningsCount() {
      return this.ok.getWarnings().size();
   }

   @Override
   public Iterator<Warning> getWarnings() {
      return this.ok.getWarnings().stream().map(w -> new WarningImpl(w)).collect(Collectors.toList()).iterator();
   }
}

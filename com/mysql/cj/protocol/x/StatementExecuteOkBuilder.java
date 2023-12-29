package com.mysql.cj.protocol.x;

import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.Warning;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StatementExecuteOkBuilder {
   private long rowsAffected = 0L;
   private Long lastInsertId = null;
   private List<String> generatedIds = Collections.emptyList();
   private List<Warning> warnings = new ArrayList<>();

   public void addNotice(Notice notice) {
      if (notice.getType() == 1) {
         this.warnings.add(notice);
      } else if (notice.getType() == 3) {
         switch(notice.getParamType()) {
            case 1:
            case 2:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 11:
            default:
               new WrongArgumentException("unhandled SessionStateChanged notice! " + notice).printStackTrace();
               break;
            case 3:
               this.lastInsertId = notice.getValue().getVUnsignedInt();
               break;
            case 4:
               this.rowsAffected = notice.getValue().getVUnsignedInt();
            case 10:
               break;
            case 12:
               this.generatedIds = notice.getValueList().stream().map(v -> v.getVOctets().getValue().toStringUtf8()).collect(Collectors.toList());
         }
      } else {
         new WrongArgumentException("Got an unknown notice: " + notice).printStackTrace();
      }
   }

   public StatementExecuteOk build() {
      return new StatementExecuteOk(this.rowsAffected, this.lastInsertId, this.generatedIds, this.warnings);
   }
}

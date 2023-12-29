package com.mysql.cj.protocol.x;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.x.protobuf.MysqlxNotice;

public class NoticeFactory implements ProtocolEntityFactory<Notice, XMessage> {
   public Notice createFromMessage(XMessage message) {
      MysqlxNotice.Frame notice = (MysqlxNotice.Frame)message.getMessage();
      if (notice.getScope() == MysqlxNotice.Frame.Scope.GLOBAL) {
         return null;
      } else {
         switch(notice.getType()) {
            case 1:
               MysqlxNotice.Warning warn = this.parseNotice(notice.getPayload(), MysqlxNotice.Warning.class);
               return new Notice(warn.getLevel().getNumber(), Integer.toUnsignedLong(warn.getCode()), warn.getMsg());
            case 2:
               MysqlxNotice.SessionVariableChanged svmsg = this.parseNotice(notice.getPayload(), MysqlxNotice.SessionVariableChanged.class);
               return new Notice(svmsg.getParam(), svmsg.getValue());
            case 3:
               MysqlxNotice.SessionStateChanged ssmsg = this.parseNotice(notice.getPayload(), MysqlxNotice.SessionStateChanged.class);
               return new Notice(ssmsg.getParam().getNumber(), ssmsg.getValueList());
            default:
               new WrongArgumentException("Got an unknown notice: " + notice).printStackTrace();
               return null;
         }
      }
   }

   private <T extends GeneratedMessage> T parseNotice(ByteString payload, Class<T> noticeClass) {
      try {
         Parser<T> parser = (Parser)MessageConstants.MESSAGE_CLASS_TO_PARSER.get(noticeClass);
         return (T)parser.parseFrom(payload);
      } catch (InvalidProtocolBufferException var4) {
         throw new CJCommunicationsException(var4);
      }
   }
}

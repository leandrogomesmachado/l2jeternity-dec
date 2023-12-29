package l2e.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.model.items.itemcontainer.Mail;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskBirthday extends Task {
   private static final String NAME = "birthday";
   private static final String QUERY = "SELECT charId, createDate FROM characters WHERE createDate LIKE ?";
   private static final Calendar _today = Calendar.getInstance();
   private int _count = 0;

   @Override
   public String getName() {
      return "birthday";
   }

   @Override
   public void onTimeElapsed(TaskManager.ExecutedTask task) {
      Calendar lastExecDate = Calendar.getInstance();
      long lastActivation = task.getLastActivation();
      if (lastActivation > 0L) {
         lastExecDate.setTimeInMillis(lastActivation);
      }

      String rangeDate = "[" + Util.getDateString(lastExecDate.getTime()) + "] - [" + Util.getDateString(_today.getTime()) + "]";

      while(!_today.before(lastExecDate)) {
         this.checkBirthday(lastExecDate.get(1), lastExecDate.get(2), lastExecDate.get(5));
         lastExecDate.add(5, 1);
      }

      this._log.info("BirthdayManager: " + this._count + " gifts sent. " + rangeDate);
   }

   private void checkBirthday(int year, int month, int day) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT charId, createDate FROM characters WHERE createDate LIKE ?");
      ) {
         statement.setString(1, "%-" + this.getNum(month + 1) + "-" + this.getNum(day));

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               int playerId = rset.getInt("charId");
               Calendar createDate = Calendar.getInstance();
               createDate.setTime(rset.getDate("createDate"));
               int age = year - createDate.get(1);
               if (age > 0) {
                  String text = Config.ALT_BIRTHDAY_MAIL_TEXT;
                  if (text.contains("$c1")) {
                     text = text.replace("$c1", CharNameHolder.getInstance().getNameById(playerId));
                  }

                  if (text.contains("$s1")) {
                     text = text.replace("$s1", String.valueOf(age));
                  }

                  Message msg = new Message(playerId, Config.ALT_BIRTHDAY_MAIL_SUBJECT, text, Message.SenderType.BIRTHDAY);
                  Mail attachments = msg.createAttachments();
                  attachments.addItem("Birthday", Config.ALT_BIRTHDAY_GIFT, 1L, null, null);
                  MailManager.getInstance().sendMessage(msg);
                  ++this._count;
               }
            }
         }
      } catch (SQLException var67) {
         this._log.log(Level.WARNING, "Error checking birthdays. ", (Throwable)var67);
      }

      GregorianCalendar calendar = new GregorianCalendar();
      if (month == 1 && day == 28 && !calendar.isLeapYear(_today.get(1))) {
         this.checkBirthday(year, 1, 29);
      }
   }

   private String getNum(int num) {
      return num <= 9 ? "0" + num : String.valueOf(num);
   }

   @Override
   public void initializate() {
      super.initializate();
      TaskManager.addUniqueTask("birthday", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
   }
}

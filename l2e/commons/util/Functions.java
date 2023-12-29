package l2e.commons.util;

import java.util.Map;
import java.util.Map.Entry;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.model.items.itemcontainer.Mail;
import l2e.gameserver.network.SystemMessageId;

public class Functions {
   private static String _key = "";

   public static void sendSystemMail(Player receiver, String title, String body, Map<Integer, Long> items) {
      if (receiver != null && receiver.isOnline()) {
         if (title != null) {
            if (items.keySet().size() <= 8) {
               Message msg = new Message(receiver.getObjectId(), title, body, Message.SenderType.NEWS_INFORMER);
               if (items != null && !items.isEmpty()) {
                  Mail attachments = msg.createAttachments();

                  for(Entry<Integer, Long> itm : items.entrySet()) {
                     attachments.addItem("reward", itm.getKey(), itm.getValue(), null, null);
                  }
               }

               MailManager.getInstance().sendMessage(msg);
               receiver.sendPacket(SystemMessageId.MAIL_ARRIVED);
            }
         }
      }
   }

   public static boolean sendSystemMail(String receiverName, String title, String body, Map<Integer, Long> items) {
      Player receiver = World.getInstance().getPlayer(receiverName);
      int receiverObjectId = receiver != null ? receiver.getObjectId() : CharNameHolder.getInstance().getIdByName(receiverName);
      return receiverObjectId > 0 && sendSystemMail(receiverName, receiverObjectId, title, body, items);
   }

   public static boolean sendSystemMail(int receiverObjectId, String title, String body, Map<Integer, Long> items) {
      String receiverName = CharNameHolder.getInstance().getNameById(receiverObjectId);
      return !receiverName.equals("") && sendSystemMail(receiverName, receiverObjectId, title, body, items);
   }

   public static boolean sendSystemMail(String receiverName, int receiverObjectId, String title, String body, Map<Integer, Long> items) {
      if (title != null && receiverObjectId > 0) {
         if (items.keySet().size() > 8) {
            return false;
         } else {
            Message msg = new Message(receiverObjectId, title, body, Message.SenderType.NEWS_INFORMER);
            if (items != null && !items.isEmpty()) {
               Mail attachments = msg.createAttachments();

               for(Entry<Integer, Long> itm : items.entrySet()) {
                  attachments.addItem("reward", itm.getKey(), itm.getValue(), null, null);
               }
            }

            MailManager.getInstance().sendMessage(msg);
            Player receiver = World.getInstance().getPlayer(receiverName);
            if (receiver != null) {
               receiver.sendPacket(SystemMessageId.MAIL_ARRIVED);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static void setBuffKey(String val) {
      _key = val;
   }

   public static String getBuffKey() {
      return _key;
   }
}

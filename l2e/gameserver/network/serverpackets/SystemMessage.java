package l2e.gameserver.network.serverpackets;

import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.components.AbstractMessage;

public final class SystemMessage extends AbstractMessage<SystemMessage> {
   private SystemMessage(SystemMessageId smId) {
      super(smId);
   }

   public static final SystemMessage sendString(String text) {
      if (text == null) {
         throw new NullPointerException();
      } else {
         SystemMessage sm = getSystemMessage(SystemMessageId.S1);
         sm.addString(text);
         return sm;
      }
   }

   public static final SystemMessage getSystemMessage(SystemMessageId smId) {
      SystemMessage sm = smId.getStaticSystemMessage();
      if (sm != null) {
         return sm;
      } else {
         sm = new SystemMessage(smId);
         if (smId.getParamCount() == 0) {
            smId.setStaticSystemMessage(sm);
         }

         return sm;
      }
   }

   public static SystemMessage getSystemMessage(int id) {
      return getSystemMessage(SystemMessageId.getSystemMessageId(id));
   }

   private SystemMessage(int id) {
      this(SystemMessageId.getSystemMessageId(id));
   }

   @Override
   protected final void writeImpl() {
      this.writeInfo();
   }
}

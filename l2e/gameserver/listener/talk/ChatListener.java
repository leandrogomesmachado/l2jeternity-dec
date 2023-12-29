package l2e.gameserver.listener.talk;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.ChatEvent;
import l2e.gameserver.network.clientpackets.Say2;

public abstract class ChatListener extends AbstractListener {
   public ChatListener() {
      this.register();
   }

   public abstract void onTalk(ChatEvent var1);

   @Override
   public void register() {
      Say2.addChatListener(this);
   }

   @Override
   public void unregister() {
      Say2.removeChatListener(this);
   }

   public static ChatListener.ChatTargetType getTargetType(String type) {
      ChatListener.ChatTargetType targetType = ChatListener.ChatTargetType.ALL;

      try {
         targetType = ChatListener.ChatTargetType.valueOf(type);
      } catch (Exception var3) {
         log.info("Invalid ChatTargetType:" + type);
         var3.getMessage();
      }

      return targetType;
   }

   public static enum ChatTargetType {
      ALL,
      SHOUT,
      TELL,
      PARTY,
      CLAN,
      GM,
      PETITION_PLAYER,
      PETITION_GM,
      TRADE,
      ALLIANCE,
      ANNOUNCEMENT,
      BOAT,
      L2FRIEND,
      MSNCHAT,
      PARTYMATCH_ROOM,
      PARTYROOM_COMMANDER,
      PARTYROOM_ALL,
      HERO_VOICE,
      CRITICAL_ANNOUNCE,
      SCREEN_ANNOUNCE,
      BATTLEFIELD,
      MPCC_ROOM;
   }
}

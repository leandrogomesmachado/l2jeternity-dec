package l2e.gameserver.listener.talk;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.ChatEvent;
import l2e.gameserver.network.clientpackets.Say2;

public abstract class ChatFilterListener extends AbstractListener {
   public ChatFilterListener() {
      this.register();
   }

   public abstract String onTalk(ChatEvent var1);

   @Override
   public void register() {
      Say2.addChatFilterListener(this);
   }

   @Override
   public void unregister() {
      Say2.removeChatFilterListener(this);
   }
}

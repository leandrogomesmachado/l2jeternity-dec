package l2e.gameserver.handler.chathandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.handler.chathandlers.impl.ChatAll;
import l2e.gameserver.handler.chathandlers.impl.ChatAlliance;
import l2e.gameserver.handler.chathandlers.impl.ChatBattlefield;
import l2e.gameserver.handler.chathandlers.impl.ChatClan;
import l2e.gameserver.handler.chathandlers.impl.ChatHeroVoice;
import l2e.gameserver.handler.chathandlers.impl.ChatMpccRoom;
import l2e.gameserver.handler.chathandlers.impl.ChatParty;
import l2e.gameserver.handler.chathandlers.impl.ChatPartyMatchRoom;
import l2e.gameserver.handler.chathandlers.impl.ChatPartyRoomAll;
import l2e.gameserver.handler.chathandlers.impl.ChatPartyRoomCommander;
import l2e.gameserver.handler.chathandlers.impl.ChatPetition;
import l2e.gameserver.handler.chathandlers.impl.ChatShout;
import l2e.gameserver.handler.chathandlers.impl.ChatTell;
import l2e.gameserver.handler.chathandlers.impl.ChatTrade;

public class ChatHandler {
   private static Logger _log = Logger.getLogger(ChatHandler.class.getName());
   private final Map<Integer, IChatHandler> _handlers = new HashMap<>();

   protected ChatHandler() {
      this.registerHandler(new ChatAll());
      this.registerHandler(new ChatAlliance());
      this.registerHandler(new ChatBattlefield());
      this.registerHandler(new ChatClan());
      this.registerHandler(new ChatHeroVoice());
      this.registerHandler(new ChatMpccRoom());
      this.registerHandler(new ChatParty());
      this.registerHandler(new ChatPartyMatchRoom());
      this.registerHandler(new ChatPartyRoomAll());
      this.registerHandler(new ChatPartyRoomCommander());
      this.registerHandler(new ChatPetition());
      this.registerHandler(new ChatShout());
      this.registerHandler(new ChatTell());
      this.registerHandler(new ChatTrade());
      _log.info("Loaded " + this._handlers.size() + " ChatHandlers.");
   }

   public void registerHandler(IChatHandler handler) {
      int[] ids = handler.getChatTypeList();

      for(int id : ids) {
         if (this._handlers.containsKey(id)) {
            _log.fine(
               "VoicedCommand: dublicate bypass registered! First handler: "
                  + this._handlers.get(id).getClass().getSimpleName()
                  + " second: "
                  + handler.getClass().getSimpleName()
            );
            this._handlers.remove(id);
         }

         this._handlers.put(id, handler);
      }
   }

   public synchronized void removeHandler(IChatHandler handler) {
      int[] ids = handler.getChatTypeList();

      for(int id : ids) {
         this._handlers.remove(id);
      }
   }

   public IChatHandler getHandler(Integer chatType) {
      return this._handlers.get(chatType);
   }

   public int size() {
      return this._handlers.size();
   }

   public static ChatHandler getInstance() {
      return ChatHandler.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ChatHandler _instance = new ChatHandler();
   }
}

package l2e.gameserver.network.clientpackets;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.handler.chathandlers.ChatHandler;
import l2e.gameserver.handler.chathandlers.IChatHandler;
import l2e.gameserver.listener.events.ChatEvent;
import l2e.gameserver.listener.talk.ChatFilterListener;
import l2e.gameserver.listener.talk.ChatListener;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public final class Say2 extends GameClientPacket {
   private static Logger _logChat = Logger.getLogger("chat");
   private static List<ChatListener> chatListeners = new LinkedList<>();
   private static List<ChatFilterListener> chatFilterListeners = new LinkedList<>();
   public static final int ALL = 0;
   public static final int SHOUT = 1;
   public static final int TELL = 2;
   public static final int PARTY = 3;
   public static final int CLAN = 4;
   public static final int GM = 5;
   public static final int PETITION_PLAYER = 6;
   public static final int PETITION_GM = 7;
   public static final int TRADE = 8;
   public static final int ALLIANCE = 9;
   public static final int ANNOUNCEMENT = 10;
   public static final int BOAT = 11;
   public static final int L2FRIEND = 12;
   public static final int MSNCHAT = 13;
   public static final int PARTYMATCH_ROOM = 14;
   public static final int PARTYROOM_COMMANDER = 15;
   public static final int PARTYROOM_ALL = 16;
   public static final int HERO_VOICE = 17;
   public static final int CRITICAL_ANNOUNCE = 18;
   public static final int SCREEN_ANNOUNCE = 19;
   public static final int BATTLEFIELD = 20;
   public static final int MPCC_ROOM = 21;
   public static final int NPC_ALL = 22;
   public static final int NPC_SHOUT = 23;
   private static final String[] CHAT_NAMES = new String[]{
      "ALL",
      "SHOUT",
      "TELL",
      "PARTY",
      "CLAN",
      "GM",
      "PETITION_PLAYER",
      "PETITION_GM",
      "TRADE",
      "ALLIANCE",
      "ANNOUNCEMENT",
      "BOAT",
      "L2FRIEND",
      "MSNCHAT",
      "PARTYMATCH_ROOM",
      "PARTYROOM_COMMANDER",
      "PARTYROOM_ALL",
      "HERO_VOICE",
      "CRITICAL_ANNOUNCE",
      "SCREEN_ANNOUNCE",
      "BATTLEFIELD",
      "MPCC_ROOM"
   };
   private static final String[] WALKER_COMMAND_LIST = new String[]{
      "USESKILL",
      "USEITEM",
      "BUYITEM",
      "SELLITEM",
      "SAVEITEM",
      "LOADITEM",
      "MSG",
      "DELAY",
      "LABEL",
      "JMP",
      "CALL",
      "RETURN",
      "MOVETO",
      "NPCSEL",
      "NPCDLG",
      "DLGSEL",
      "CHARSTATUS",
      "POSOUTRANGE",
      "POSINRANGE",
      "GOHOME",
      "SAY",
      "EXIT",
      "PAUSE",
      "STRINDLG",
      "STRNOTINDLG",
      "CHANGEWAITTYPE",
      "FORCEATTACK",
      "ISMEMBER",
      "REQUESTJOINPARTY",
      "REQUESTOUTPARTY",
      "QUITPARTY",
      "MEMBERSTATUS",
      "CHARBUFFS",
      "ITEMCOUNT",
      "FOLLOWTELEPORT"
   };
   private String _text;
   private int _type;
   private String _target;

   @Override
   protected void readImpl() {
      this._text = this.readS();
      this._type = this.readD();
      this._target = this._type == 2 ? this.readS() : null;
   }

   @Override
   protected void runImpl() {
      if (Config.DEBUG) {
         _log.info("Say2: Msg Type = '" + this._type + "' Text = '" + this._text + "'.");
      }

      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.isntAfk();
         if (this._type < 0 || this._type >= CHAT_NAMES.length) {
            _log.warning("Say2: Invalid type: " + this._type + " Player : " + activeChar.getName() + " text: " + this._text);
            activeChar.sendActionFailed();
            activeChar.logout();
         } else if (this._text.isEmpty()) {
            _log.warning(activeChar.getName() + ": sending empty text. Possible packet hack!");
            activeChar.sendActionFailed();
            activeChar.logout();
         } else if (activeChar.isGM()
            || (this._text.indexOf(8) < 0 || this._text.length() <= 500) && (this._text.indexOf(8) >= 0 || this._text.length() <= 105)) {
            if (Config.L2WALKER_PROTECTION && this._type == 2 && this.checkBot(this._text)) {
               Util.handleIllegalPlayerAction(activeChar, "Client Emulator Detect: " + activeChar.getName() + " using l2walker.");
            } else if (!activeChar.isCursedWeaponEquipped() || this._type != 8 && this._type != 1) {
               if (activeChar.isChatBanned() && this._text.charAt(0) != '.') {
                  if (activeChar.getFirstEffect(EffectType.CHAT_BLOCK) != null) {
                     activeChar.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_CHATTING_NOT_ALLOWED);
                  } else {
                     for(int chatId : Config.BAN_CHAT_CHANNELS) {
                        if (this._type == chatId) {
                           activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
                        }
                     }
                  }
               } else if (!activeChar.isJailed() || !Config.JAIL_DISABLE_CHAT || this._type != 2 && this._type != 1 && this._type != 8 && this._type != 17) {
                  if (this._type == 6 && activeChar.isGM()) {
                     this._type = 7;
                  }

                  if (Config.LOG_CHAT) {
                     LogRecord record = new LogRecord(Level.INFO, this._text);
                     record.setLoggerName("chat");
                     if (this._type == 2) {
                        record.setParameters(new Object[]{CHAT_NAMES[this._type], "[" + activeChar.getName() + " to " + this._target + "]"});
                     } else {
                        record.setParameters(new Object[]{CHAT_NAMES[this._type], "[" + activeChar.getName() + "]"});
                     }

                     _logChat.log(record);
                  }

                  if (this._text.indexOf(8) < 0 || this.parseAndPublishItem(activeChar)) {
                     this.fireChatListeners(activeChar);
                     boolean blockBroadCast = this.checkBroadCastText();
                     if (Config.USE_SAY_FILTER && !blockBroadCast) {
                        this.checkText();
                     }

                     this.fireChatFilters(activeChar);
                     if (this._text.charAt(0) != '-' || !Config.ALLOW_CUSTOM_CHAT || this._type != 0) {
                        IChatHandler handler = ChatHandler.getInstance().getHandler(this._type);
                        if (handler != null) {
                           handler.handleChat(this._type, activeChar, this._target, this._text, blockBroadCast);
                        } else {
                           _log.info("No handler registered for ChatType: " + this._type + " Player: " + this.getClient());
                        }
                     } else if (activeChar.getCustomChatStatus() < Config.CHECK_CHAT_VALID) {
                        activeChar.sendMessage(new ServerMessage("CustomChat.CANT_USE", activeChar.getLang()).toString());
                     } else if (activeChar.getChatMsg() == 0) {
                        activeChar.sendMessage(new ServerMessage("CustomChat.LIMIT", activeChar.getLang()).toString());
                     } else {
                        String text = this._text.substring(1);
                        if (blockBroadCast) {
                           activeChar.sendPacket(new CreatureSay(0, 20, activeChar.getName(), text));
                        } else {
                           for(Player player : World.getInstance().getAllPlayers()) {
                              player.sendPacket(new CreatureSay(0, 20, activeChar.getName(), text));
                           }
                        }

                        activeChar.setChatMsg(activeChar.getChatMsg() - 1);
                     }
                  }
               } else {
                  activeChar.sendMessage("You can not chat with players outside of the jail.");
               }
            } else {
               activeChar.sendPacket(SystemMessageId.SHOUT_AND_TRADE_CHAT_CANNOT_BE_USED_WHILE_POSSESSING_CURSED_WEAPON);
            }
         } else {
            activeChar.sendPacket(SystemMessageId.DONT_SPAM);
         }
      }
   }

   private boolean checkBot(String text) {
      for(String botCommand : WALKER_COMMAND_LIST) {
         if (text.startsWith(botCommand)) {
            return true;
         }
      }

      return false;
   }

   private boolean checkBroadCastText() {
      if (!Config.USE_BROADCAST_SAY_FILTER) {
         return false;
      } else {
         for(String pattern : Config.BROADCAST_FILTER_LIST) {
            int index = this._text.indexOf(pattern);
            if (index != -1) {
               return true;
            }
         }

         return false;
      }
   }

   private void checkText() {
      String filteredText = this._text;

      for(String pattern : Config.FILTER_LIST) {
         filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
      }

      this._text = filteredText;
   }

   private boolean parseAndPublishItem(Player owner) {
      int pos1 = -1;

      while((pos1 = this._text.indexOf(8, pos1)) > -1) {
         int pos = this._text.indexOf("ID=", pos1);
         if (pos == -1) {
            return false;
         }

         StringBuilder result = new StringBuilder(9);
         pos += 3;

         while(Character.isDigit(this._text.charAt(pos))) {
            result.append(this._text.charAt(pos++));
         }

         int id = Integer.parseInt(result.toString());
         GameObject item = World.getInstance().findObject(id);
         if (!(item instanceof ItemInstance)) {
            _log.info(this.getClient() + " trying publish object which is not item! Object:" + item);
            return false;
         }

         if (owner.getInventory().getItemByObjectId(id) == null) {
            _log.info(this.getClient() + " trying publish item which doesnt own! ID:" + id);
            return false;
         }

         ((ItemInstance)item).publish();
         pos1 = this._text.indexOf(8, pos) + 1;
         if (pos1 == 0) {
            _log.info(this.getClient() + " sent invalid publish item msg! ID:" + id);
            return false;
         }
      }

      return true;
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }

   private void fireChatListeners(Player activeChar) {
      if (!chatListeners.isEmpty()) {
         ChatEvent event = null;
         event = new ChatEvent();
         event.setOrigin(activeChar);
         event.setTarget(this._target);
         event.setTargetType(ChatListener.getTargetType(CHAT_NAMES[this._type]));
         event.setText(this._text);

         for(ChatListener listener : chatListeners) {
            listener.onTalk(event);
         }
      }
   }

   private void fireChatFilters(Player activeChar) {
      if (!chatFilterListeners.isEmpty()) {
         ChatEvent event = null;
         event = new ChatEvent();
         event.setOrigin(activeChar);
         event.setTarget(this._target);
         event.setTargetType(ChatListener.getTargetType(CHAT_NAMES[this._type]));
         event.setText(this._text);

         for(ChatFilterListener listener : chatFilterListeners) {
            this._text = listener.onTalk(event);
         }
      }
   }

   public static void addChatListener(ChatListener listener) {
      if (!chatListeners.contains(listener)) {
         chatListeners.add(listener);
      }
   }

   public static void removeChatListener(ChatListener listener) {
      chatListeners.remove(listener);
   }

   public static void addChatFilterListener(ChatFilterListener listener) {
      if (!chatFilterListeners.contains(listener)) {
         chatFilterListeners.add(listener);
      }
   }

   public static void removeChatFilterListener(ChatFilterListener listener) {
      chatFilterListeners.remove(listener);
   }
}

package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.listener.player.QuestionMarkListener;
import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.listener.CharListenerList;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.JoinParty;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class FindParty implements IVoicedCommandHandler {
   private static final String[] COMMANDS = new String[]{"findparty", "fp", "party", "invite", "partylist"};
   private static final FindParty.OnPartyQuestionMarkClicked _listener = new FindParty.OnPartyQuestionMarkClicked();
   private static final Map<Integer, FindParty.FindPartyRequest> _requests = new ConcurrentHashMap<>();

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (!Config.ALLOW_FIND_PARTY) {
         return false;
      } else if (command.startsWith("partylist")) {
         int i = 0;
         CreatureSay[] packets = new CreatureSay[_requests.size() + 2];
         packets[i++] = new CreatureSay(
            activeChar.getObjectId(),
            20,
            ServerStorage.getInstance().getString(activeChar.getLang(), "FindParty.PARTY_REQUEST"),
            ServerStorage.getInstance().getString(activeChar.getLang(), "FindParty.PARTY_LIST")
         );

         for(FindParty.FindPartyRequest request : _requests.values()) {
            if (target == null || target.isEmpty() || request.getMessage().toLowerCase().contains(target.toLowerCase())) {
               Player partyLeader = World.getInstance().getPlayer(request.getObjectId());
               if (partyLeader != null) {
                  int freeSlots = 8;
                  if (partyLeader.getParty() != null) {
                     freeSlots = 9 - partyLeader.getParty().getMembers().size();
                  }

                  if (freeSlots > 0) {
                     packets[i++] = new CreatureSay(
                        activeChar.getObjectId(),
                        3,
                        ServerStorage.getInstance().getString(activeChar.getLang(), "FindParty.FIND_PARTY"),
                        "\b\tType=1 \tID="
                           + partyLeader.getObjectId()
                           + " \tColor=0 \tUnderline=0 \tTitle=\u001b\u001b\b"
                           + partyLeader.getName()
                           + " ("
                           + freeSlots
                           + "/"
                           + 9
                           + ") "
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "FindParty.FREE_SLOTS")
                           + ". "
                           + request.getMessage()
                     );
                  }
               }
            }
         }

         packets[i++] = new CreatureSay(
            activeChar.getObjectId(),
            20,
            ServerStorage.getInstance().getString(activeChar.getLang(), "FindParty.PARTY_REQUEST"),
            ServerStorage.getInstance().getString(activeChar.getLang(), "FindParty.PARTY_LIST_END")
         );
         activeChar.sendPacket(packets);
         return true;
      } else {
         if (command.startsWith("invite")) {
            Player playerToInvite = null;
            if (activeChar.isInParty() && !activeChar.getParty().isLeader(activeChar) && activeChar.getParty().getMemberCount() >= 9) {
               playerToInvite = World.getInstance().getPlayer(target);
            }

            if (playerToInvite != null) {
               for(Player ptMem : activeChar.getParty()) {
                  if (activeChar.getParty().getLeader() == ptMem) {
                     ptMem.sendPacket(
                        new CreatureSay(
                           activeChar.getObjectId(),
                           3,
                           ServerStorage.getInstance().getString(ptMem.getLang(), "FindParty.PARTY_REQUEST"),
                           ""
                              + ServerStorage.getInstance().getString(ptMem.getLang(), "FindParty.PL_INVITE")
                              + " "
                              + playerToInvite.getName()
                              + " "
                              + ServerStorage.getInstance().getString(ptMem.getLang(), "FindParty.TO_PARTY")
                              + ". \b\tType=1 \tID="
                              + playerToInvite.getObjectId()
                              + " \tColor=0 \tUnderline=0 \tTitle=\u001b\u001b\b"
                        )
                     );
                  } else {
                     ptMem.sendPacket(
                        new CreatureSay(
                           activeChar.getObjectId(),
                           3,
                           ServerStorage.getInstance().getString(ptMem.getLang(), "FindParty.PARTY_REQUEST"),
                           ""
                              + ServerStorage.getInstance().getString(ptMem.getLang(), "FindParty.PL_INVITE")
                              + " "
                              + playerToInvite.getName()
                              + " "
                              + ServerStorage.getInstance().getString(ptMem.getLang(), "FindParty.TO_PARTY")
                              + "."
                        )
                     );
                  }
               }
            }
         } else if (command.startsWith("party") || command.startsWith("fp") || command.startsWith("findparty")) {
            if (activeChar.isInParty() && !activeChar.getParty().isLeader(activeChar)) {
               activeChar.sendMessage(new ServerMessage("FindParty.ONLY_LEADER", activeChar.getLang()).toString());
               return true;
            }

            if (activeChar.getLevel() < Config.FIND_PARTY_MIN_LEVEL) {
               ServerMessage msg = new ServerMessage("FindParty.MIN_LEVEL", activeChar.getLang());
               msg.add(Config.FIND_PARTY_MIN_LEVEL);
               activeChar.sendMessage(msg.toString());
               return true;
            }

            int partyRequestObjId = 0;

            for(Entry<Integer, FindParty.FindPartyRequest> entry : _requests.entrySet()) {
               if (entry.getValue().getObjectId() == activeChar.getObjectId()) {
                  partyRequestObjId = entry.getKey();
                  break;
               }
            }

            if (partyRequestObjId == 0) {
               partyRequestObjId = IdFactory.getInstance().getNextId();
            }

            int freeSlots = 8;
            if (activeChar.getParty() != null) {
               freeSlots = 9 - activeChar.getParty().getMembers().size();
            }

            if (freeSlots <= 0) {
               activeChar.sendMessage(new ServerMessage("FindParty.PARTY_FULL", activeChar.getLang()).toString());
               return true;
            }

            if (target != null && !target.isEmpty()) {
               target = String.valueOf(target.charAt(0)).toUpperCase() + target.substring(1);
            }

            FindParty.FindPartyRequest request = _requests.get(partyRequestObjId);
            if (request == null) {
               request = new FindParty.FindPartyRequest(activeChar, target);
            } else {
               long delay = System.currentTimeMillis() - request.getRequestStartTime();
               if (delay < (long)(Config.FIND_PARTY_FLOOD_TIME * 1000)) {
                  ServerMessage msg = new ServerMessage("FindParty.FLOOD_MSG", activeChar.getLang());
                  msg.add(Config.FIND_PARTY_FLOOD_TIME * 1000 / 1000);
                  msg.add(((long)(Config.FIND_PARTY_FLOOD_TIME * 1000) - delay) / 1000L);
                  activeChar.sendMessage(msg.toString());
                  return true;
               }

               if (target != null && !target.isEmpty()) {
                  request.update(target);
               } else {
                  request.update();
               }
            }

            _requests.put(partyRequestObjId, request);

            for(Player player : World.getInstance().getAllPlayers()) {
               if (player != null && (player.canJoinParty(activeChar) || activeChar.isInParty() && activeChar.getParty().getMembers().contains(player))) {
                  player.sendPacket(
                     new CreatureSay(
                        activeChar.getObjectId(),
                        3,
                        ServerStorage.getInstance().getString(player.getLang(), "FindParty.FIND_PARTY"),
                        "\b\tType=1 \tID="
                           + partyRequestObjId
                           + " \tColor=0 \tUnderline=0 \tTitle=\u001b\u001b\b"
                           + activeChar.getName()
                           + " ("
                           + freeSlots
                           + "/"
                           + 9
                           + ") "
                           + ServerStorage.getInstance().getString(player.getLang(), "FindParty.FREE_SLOTS")
                           + ". "
                           + request.getMessage()
                     )
                  );
               }
            }
         }

         return false;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return COMMANDS;
   }

   static {
      CharListenerList.addGlobal(_listener);
      ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
         @Override
         public void run() {
            synchronized(FindParty._requests) {
               for(Entry<Integer, FindParty.FindPartyRequest> entry : FindParty._requests.entrySet()) {
                  if (entry.getValue().getRequestStartTime() + (long)(Config.FIND_PARTY_REFRESH_TIME * 1000) < System.currentTimeMillis()) {
                     FindParty._requests.remove(entry.getKey());
                  }
               }
            }
         }
      }, 60000L, 60000L);
   }

   private static class FindPartyRequest {
      private final int _objectId;
      private long _requestStartTimeMilis;
      private String _message;

      public FindPartyRequest(Player player, String msg) {
         this._objectId = player.getObjectId();
         this._requestStartTimeMilis = System.currentTimeMillis();
         this._message = msg == null ? "" : msg;
      }

      public void update() {
         this._requestStartTimeMilis = System.currentTimeMillis();
      }

      public void update(String newMsg) {
         this._requestStartTimeMilis = System.currentTimeMillis();
         this._message = newMsg;
      }

      public int getObjectId() {
         return this._objectId;
      }

      public long getRequestStartTime() {
         return this._requestStartTimeMilis;
      }

      public String getMessage() {
         return this._message;
      }
   }

   private static class OnPartyQuestionMarkClicked implements QuestionMarkListener {
      private OnPartyQuestionMarkClicked() {
      }

      @Override
      public void onQuestionMarkClicked(Player player, int targetObjId) {
         int requestorObjId = FindParty._requests.containsKey(targetObjId) ? FindParty._requests.get(targetObjId).getObjectId() : 0;
         if (requestorObjId > 0) {
            if (player.getObjectId() != requestorObjId) {
               Player partyLeader = World.getInstance().getPlayer(requestorObjId);
               if (partyLeader == null) {
                  player.sendMessage(new ServerMessage("FindParty.LEADER_OFF", player.getLang()).toString());
               } else {
                  long delay = System.currentTimeMillis() - player.getQuickVarL("partyrequestsent", 0L);
                  if (delay < (long)(Config.FIND_PARTY_FLOOD_TIME * 1000)) {
                     ServerMessage msg = new ServerMessage("FindParty.FLOOD_MSG", player.getLang());
                     msg.add(Config.FIND_PARTY_FLOOD_TIME * 1000 / 1000);
                     msg.add(((long)(Config.FIND_PARTY_FLOOD_TIME * 1000) - delay) / 1000L);
                     player.sendMessage(msg.toString());
                     return;
                  }

                  player.addQuickVar("partyrequestsent", System.currentTimeMillis());
                  CreatureSay packetLeader = new CreatureSay(
                     player.getObjectId(),
                     20,
                     player.getName(),
                     ""
                        + ServerStorage.getInstance().getString(partyLeader.getLang(), "FindParty.LEVEL")
                        + " "
                        + player.getLevel()
                        + ", "
                        + ServerStorage.getInstance().getString(partyLeader.getLang(), "FindParty.CLASS")
                        + " "
                        + Util.className(partyLeader, player.getClassId().getId())
                        + ". "
                        + ServerStorage.getInstance().getString(partyLeader.getLang(), "FindParty.INVITE")
                        + " \b\tType=1 \tID="
                        + player.getObjectId()
                        + " \tColor=0 \tUnderline=0 \tTitle=\u001b\u001b\b"
                  );
                  partyLeader.sendPacket(packetLeader);
                  ServerMessage msg = new ServerMessage("FindParty.REQUEST_SENT", player.getLang());
                  msg.add(partyLeader.getName());
                  player.sendMessage(msg.toString());
               }
            }
         } else {
            Player target = World.getInstance().getPlayer(targetObjId);
            if (target != null) {
               this.requestParty(player, target);
            } else {
               player.sendMessage(new ServerMessage("FindParty.NOT_VALID", player.getLang()).toString());
            }
         }
      }

      private void requestParty(Player partyLeader, Player target) {
         if (partyLeader.isOutOfControl()) {
            partyLeader.sendActionFailed();
         } else if (partyLeader.isProcessingRequest()) {
            partyLeader.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
         } else if (target == null) {
            partyLeader.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
         } else if (target == partyLeader || !target.isVisibleFor(partyLeader)) {
            partyLeader.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            partyLeader.sendActionFailed();
         } else if (target.isProcessingRequest()) {
            partyLeader.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addPcName(target));
         } else if (partyLeader.isPartyBanned()) {
            partyLeader.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_PARTY_NOT_ALLOWED);
            partyLeader.sendActionFailed();
         } else if (target.isPartyBanned()) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_REPORTED_AND_CANNOT_PARTY);
            sm.addCharName(target);
            partyLeader.sendPacket(sm);
         } else if (target.getUCState() <= 0
            && (
               !AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()
                  || !AerialCleftEvent.getInstance().isPlayerParticipant(target.getObjectId())
            )) {
            if (target.isInParty()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_IN_PARTY);
               sm.addString(target.getName());
               partyLeader.sendPacket(sm);
            } else if (BlockedList.isBlocked(target, partyLeader)) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
               sm.addCharName(target);
               partyLeader.sendPacket(sm);
            } else if (target.getPartyInviteRefusal()) {
               partyLeader.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(target.getName()));
            } else if (target.isCursedWeaponEquipped() || partyLeader.isCursedWeaponEquipped()) {
               partyLeader.sendPacket(SystemMessageId.INCORRECT_TARGET);
            } else if (target.isInFightEvent() && !target.getFightEvent().canJoinParty(partyLeader, target)) {
               partyLeader.sendPacket(SystemMessageId.INCORRECT_TARGET);
            } else if (!target.isJailed() && !partyLeader.isJailed()) {
               if (!target.isInOlympiadMode() && !partyLeader.isInOlympiadMode()
                  || target.isInOlympiadMode() == partyLeader.isInOlympiadMode()
                     && target.getOlympiadGameId() == partyLeader.getOlympiadGameId()
                     && target.getOlympiadSide() == partyLeader.getOlympiadSide()) {
                  if (partyLeader.isInParty()) {
                     if (partyLeader.getParty().getMemberCount() >= 9) {
                        partyLeader.sendPacket(SystemMessageId.PARTY_FULL);
                        return;
                     }

                     if (Config.PARTY_LEADER_ONLY_CAN_INVITE && !partyLeader.getParty().isLeader(partyLeader)) {
                        partyLeader.sendPacket(SystemMessageId.ONLY_LEADER_CAN_INVITE);
                        return;
                     }

                     if (partyLeader.getParty().isInDimensionalRift()) {
                        partyLeader.sendMessage(new ServerMessage("FindParty.RIFT", partyLeader.getLang()).toString());
                        partyLeader.sendActionFailed();
                        return;
                     }
                  }

                  int itemDistribution = partyLeader.getParty() == null ? 0 : partyLeader.getParty().getLootDistribution();
                  Party party = partyLeader.getParty();
                  if (party == null) {
                     partyLeader.setParty(party = new Party(partyLeader, itemDistribution));
                  }

                  target.joinParty(party);
                  partyLeader.sendPacket(new JoinParty(1));
               } else {
                  partyLeader.sendPacket(SystemMessageId.A_USER_CURRENTLY_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_SEND_PARTY_AND_FRIEND_INVITATIONS);
               }
            } else {
               partyLeader.sendMessage(new ServerMessage("FindParty.JAIL", partyLeader.getLang()).toString());
            }
         } else {
            partyLeader.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         }
      }
   }
}

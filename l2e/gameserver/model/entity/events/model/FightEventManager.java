package l2e.gameserver.model.entity.events.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import l2e.commons.collections.MultiValueSet;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.FightEventParser;
import l2e.gameserver.handler.communityhandlers.CommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventGameRoom;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ShowTutorialMark;
import l2e.gameserver.network.serverpackets.TutorialCloseHtml;
import l2e.gameserver.network.serverpackets.TutorialShowHtml;

public class FightEventManager {
   private static final Logger _log = Logger.getLogger(FightEventManager.class.getName());
   private static FightEventManager _instance;
   public static final Location RETURN_LOC = new Location(83208, 147672, -3494, 0);
   public static final String BYPASS = "_fightEvent";
   private final Map<Integer, AbstractFightEvent> _activeEvents = new ConcurrentHashMap<>();
   private final List<FightEventGameRoom> _rooms = new CopyOnWriteArrayList<>();
   private final boolean _shutDown = false;
   private AbstractFightEvent _nextEvent = null;
   private ScheduledFuture<?> _eventTask1 = null;
   private ScheduledFuture<?> _eventTask2 = null;
   private ScheduledFuture<?> _eventTask3 = null;
   private ScheduledFuture<?> _eventTask4 = null;
   private ScheduledFuture<?> _eventTask5 = null;
   private ScheduledFuture<?> _eventTask6 = null;
   private ScheduledFuture<?> _eventTask7 = null;
   private ScheduledFuture<?> _eventTask8 = null;
   private ScheduledFuture<?> _eventTask9 = null;
   private ScheduledFuture<?> _eventTask10 = null;

   public FightEventManager() {
      this.startAutoEventsTasks();
   }

   public void reload() {
      this.startAutoEventsTasks();
   }

   public boolean serverShuttingDown() {
      return false;
   }

   public void signForEvent(Player player, AbstractFightEvent event) {
      FightEventGameRoom roomFound = null;

      for(FightEventGameRoom room : this.getEventRooms(event)) {
         if (room.getSlotsLeft() > 0) {
            roomFound = room;
            break;
         }
      }

      if (roomFound == null) {
         AbstractFightEvent duplicatedEvent = this.prepareNewEvent(event);
         roomFound = this.createRoom(duplicatedEvent);
      }

      roomFound.addAlonePlayer(player);
      ServerMessage msg = new ServerMessage("FightEvents.JUST_PARTICIPATE", player.getLang());
      msg.add(player.getEventName(event.getId()));
      player.sendMessage(msg.toString());
   }

   public void trySignForEvent(Player player, AbstractFightEvent event, boolean checkConditions) {
      if (!checkConditions || this.canPlayerParticipate(player, true, false)) {
         if (!this.isRegistrationOpened(event)) {
            ServerMessage msg = new ServerMessage("FightEvents.CANT_PARTICIPATE", player.getLang());
            msg.add(player.getEventName(event.getId()));
            player.sendMessage(msg.toString());
         } else if (this.isPlayerRegistered(player, event.getId())) {
            player.sendMessage(new ServerMessage("FightEvents.ALREADY_REG", player.getLang()).toString());
         } else if (Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS > 0
            && !DoubleSessionManager.getInstance().tryAddPlayer(event.getId(), player, Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS)) {
            ServerMessage msg = new ServerMessage("FightEvents.MAX_IP", player.getLang());
            msg.add(Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS);
            player.sendMessage(msg.toString());
         } else {
            this.signForEvent(player, event);
         }
      }
   }

   public void unsignFromEvent(Player player, int eventId) {
      for(FightEventGameRoom room : this._rooms) {
         if (room != null && room.containsPlayer(player) && room.getGame().getId() == eventId) {
            room.leaveRoom(player);
            if (Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS > 0) {
               DoubleSessionManager.getInstance().removePlayer(room.getGame().getId(), player);
            }

            player.sendMessage(new ServerMessage("FightEvents.UNREGISTER", player.getLang()).toString());
         }
      }
   }

   public void unsignFromAllEvents(Player player) {
      for(FightEventGameRoom room : this._rooms) {
         if (room != null && room.containsPlayer(player)) {
            room.leaveRoom(player);
            if (Config.DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS > 0) {
               DoubleSessionManager.getInstance().removePlayer(room.getGame().getId(), player);
            }

            player.sendMessage(new ServerMessage("FightEvents.UNREGISTER", player.getLang()).toString());
         }
      }
   }

   public boolean isRegistrationOpened(AbstractFightEvent event) {
      for(FightEventGameRoom room : this._rooms) {
         if (room.getGame() != null && room.getGame().getId() == event.getId()) {
            return true;
         }
      }

      return false;
   }

   public boolean isPlayerRegistered(Player player, int eventId) {
      if (player == null) {
         return false;
      } else if (player.isInFightEvent()) {
         return true;
      } else {
         for(FightEventGameRoom room : this._rooms) {
            if (room != null && room.getGame().getId() == eventId) {
               for(Player iPlayer : room.getAllPlayers()) {
                  if (iPlayer.equals(player)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public void startEventCountdown(AbstractFightEvent event) {
      if (Config.ALLOW_FIGHT_EVENTS && this.getEventById(event.getId()) == null) {
         if (!FightEventParser.getInstance().getDisabledEvents().contains(event.getId())) {
            this._nextEvent = event;
            AbstractFightEvent duplicatedEvent = this.prepareNewEvent(event);
            this.createRoom(duplicatedEvent);
            DoubleSessionManager.getInstance().registerEvent(event.getId());
            this.sendToAllMsg(duplicatedEvent, "FightEvents.OPEN_REG", true);
            if (Config.ALLOW_REG_CONFIRM_DLG) {
               this.sendEventInvitations(event);
            }

            FightEventNpcManager.getInstance().trySpawnRegNpc();
            this.setEventTask(duplicatedEvent);
         }
      }
   }

   private void notifyConditions(AbstractFightEvent event) {
      for(FightEventGameRoom room : this.getEventRooms(event)) {
         for(Player player : room.getAllPlayers()) {
            this.canPlayerParticipate(player, true, false);
         }
      }
   }

   private void startEvent(AbstractFightEvent event) {
      List<FightEventGameRoom> eventRooms = this.getEventRooms(event);
      if (Config.FIGHT_EVENTS_EQUALIZE_ROOMS) {
         this.equalizeRooms(eventRooms);
      }

      this.clearEventIdTask(event.getId());

      for(FightEventGameRoom room : eventRooms) {
         this._rooms.remove(room);
         FightEventNpcManager.getInstance().tryUnspawnRegNpc();
         if (room.getPlayersCount() < 2) {
            _log.info(event.getNameEn() + ": Removing room because it doesnt have enough players");
            _log.info(event.getNameEn() + ": Player Counts: " + room.getPlayersCount());
            this.sendToAllMsg(event, "FightEvents.CANCEL", true);

            for(Player player : room.getAllPlayers()) {
               if (player != null) {
                  room.leaveRoom(player);
               }
            }

            DoubleSessionManager.getInstance().clear(event.getId());
            this.removeEventId(event.getId());
         } else {
            this.sendToAllMsg(event, "FightEvents.STARTED", true);
            room.getGame().prepareEvent(room);
         }
      }
   }

   private void equalizeRooms(Collection<FightEventGameRoom> eventRooms) {
      double players = 0.0;

      for(FightEventGameRoom room : eventRooms) {
         players += (double)room.getPlayersCount();
      }

      double average = players / (double)eventRooms.size();
      List<Player> playersToChange = new ArrayList<>();

      for(FightEventGameRoom room : eventRooms) {
         int before = room.getPlayersCount();
         int toRemove = room.getPlayersCount() - (int)Math.ceil(average);

         for(int i = 0; i < toRemove; ++i) {
            Player player = room.getAllPlayers().iterator().next();
            room.leaveRoom(player);
            playersToChange.add(player);
         }

         _log.info(
            "Equalizing FC Room, before:" + before + " toRemove:" + toRemove + " after:" + room.getPlayersCount() + " to Change:" + playersToChange.size()
         );
      }

      for(FightEventGameRoom room : eventRooms) {
         int before = room.getPlayersCount();
         int toAdd = Math.min((int)Math.floor(average) - before, playersToChange.size());

         for(int i = 0; i < toAdd; ++i) {
            Player player = playersToChange.remove(0);
            room.addAlonePlayer(player);
         }

         _log.info("Equalizing FC Room, Before: " + before + " Final:" + room.getPlayersCount());
      }
   }

   private List<FightEventGameRoom> getEventRooms(AbstractFightEvent event) {
      List<FightEventGameRoom> eventRooms = new ArrayList<>();

      for(FightEventGameRoom room : this._rooms) {
         if (room.getGame() != null && room.getGame().getId() == event.getId()) {
            eventRooms.add(room);
         }
      }

      return eventRooms;
   }

   private void sendEventInvitations(AbstractFightEvent event) {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (this.canPlayerParticipate(player, false, true) && player.getEvent(AbstractFightEvent.class) == null) {
            ServerMessage msg = new ServerMessage("FightEvents.WANT_JOIN", player.getLang());
            msg.add(player.getEventName(event.getId()));
            player.sendConfirmDlg(new FightEventManager.AnswerEventInvitation(player, event), 60000, msg.toString());
         }
      }
   }

   public FightEventGameRoom createRoom(AbstractFightEvent event) {
      FightEventGameRoom newRoom = new FightEventGameRoom(event);
      this._rooms.add(newRoom);
      return newRoom;
   }

   public AbstractFightEvent getNextEvent() {
      return this._nextEvent;
   }

   private void sendErrorMessageToPlayer(Player player, String msg) {
      player.sendPacket(new CreatureSay(player.getObjectId(), 15, new ServerMessage("FightEvents.ERROR", player.getLang()).toString(), msg));
      player.sendMessage(msg);
   }

   public void sendToAllMsg(AbstractFightEvent event, String msg, boolean printName) {
      for(Player player : World.getInstance().getAllPlayers()) {
         ServerMessage message = new ServerMessage(msg, player.getLang());
         if (printName) {
            message.add(player.getEventName(event.getId()));
         }

         player.sendPacket(new CreatureSay(0, 18, player.getEventName(event.getId()), message.toString()));
      }
   }

   private AbstractFightEvent prepareNewEvent(AbstractFightEvent event) {
      MultiValueSet<String> set = event.getSet();
      AbstractFightEvent duplicatedEvent = null;

      try {
         Class<AbstractFightEvent> eventClass = Class.forName(set.getString("eventClass"));
         Constructor<AbstractFightEvent> constructor = eventClass.getConstructor(MultiValueSet.class);
         duplicatedEvent = constructor.newInstance(set);
         duplicatedEvent.clearSet();
         this._activeEvents.put(duplicatedEvent.getId(), duplicatedEvent);
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException var6) {
         var6.printStackTrace();
      }

      return duplicatedEvent;
   }

   private void startAutoEventsTasks() {
      AbstractFightEvent closestEvent = null;
      long closestEventTime = Long.MAX_VALUE;

      for(AbstractFightEvent event : FightEventParser.getInstance().getEvents().valueCollection()) {
         if (event != null && event.isAutoTimed() && !FightEventParser.getInstance().getDisabledEvents().contains(event.getId())) {
            Calendar nextEventDate = this.getClosestEventDate(event.getAutoStartTimes());
            ThreadPoolManager.getInstance()
               .schedule(new FightEventManager.EventRunThread(event), nextEventDate.getTimeInMillis() - System.currentTimeMillis());
            _log.info(event.getNameEn() + " will start in: " + TimeUtils.toSimpleFormat(nextEventDate.getTimeInMillis()));
            if (closestEventTime > nextEventDate.getTimeInMillis()) {
               closestEvent = event;
               closestEventTime = nextEventDate.getTimeInMillis();
            }
         }
      }

      this._nextEvent = closestEvent;
   }

   private Calendar getClosestEventDate(int[][] dates) {
      Calendar currentTime = Calendar.getInstance();
      Calendar nextStartTime = null;
      Calendar testStartTime = null;

      for(int[] hourMin : dates) {
         testStartTime = Calendar.getInstance();
         testStartTime.setLenient(true);
         testStartTime.set(11, hourMin[0]);
         testStartTime.set(12, hourMin[1]);
         if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis()) {
            testStartTime.add(5, 1);
         }

         if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis()) {
            nextStartTime = testStartTime;
         }
      }

      return nextStartTime;
   }

   public boolean canPlayerParticipate(Player player, boolean sendMessage, boolean justMostImportant) {
      if (player == null) {
         return false;
      } else if (player.getLevel() < Config.FIGHT_EVENTS_MIN_LVL) {
         ServerMessage msg = new ServerMessage("FightEvents.LOW_LEVEL", player.getLang());
         msg.add(Config.FIGHT_EVENTS_MIN_LVL);
         this.sendErrorMessageToPlayer(player, msg.toString());
         return false;
      } else if (player.isDead() || player.isAlikeDead()) {
         this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_DEAD", player.getLang()).toString());
         return false;
      } else if (player.isBlocked()) {
         this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_BLOCKED", player.getLang()).toString());
         return false;
      } else if (player.getCursedWeaponEquippedId() > 0) {
         if (sendMessage) {
            this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_CURSE", player.getLang()).toString());
         }

         return false;
      } else if (OlympiadManager.getInstance().isRegistered(player)) {
         if (sendMessage) {
            this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_OLY", player.getLang()).toString());
         }

         return false;
      } else if (!player.isOnEvent() && !player.isInOlympiadMode()) {
         if (player.inObserverMode()) {
            if (sendMessage) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_OBSERVE", player.getLang()).toString());
            }

            return false;
         } else if (player.isJailed()) {
            if (sendMessage) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_JAIL", player.getLang()).toString());
            }

            return false;
         } else if (player.isInOfflineMode()) {
            if (sendMessage) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_OFFLINE", player.getLang()).toString());
            }

            return false;
         } else if (player.isInStoreMode()) {
            if (sendMessage) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_STORE", player.getLang()).toString());
            }

            return false;
         } else if (player.getReflectionId() > 0) {
            if (sendMessage) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_REF", player.getLang()).toString());
            }

            return false;
         } else if (player.isInDuel()) {
            if (sendMessage) {
               this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_DUEL", player.getLang()).toString());
            }

            return false;
         } else {
            if (!justMostImportant) {
               if (player.isDead() || player.isAlikeDead()) {
                  if (sendMessage) {
                     this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_DEAD", player.getLang()).toString());
                  }

                  return false;
               }

               if (!player.isInsideZone(ZoneId.PEACE) && player.getPvpFlag() > 0) {
                  if (sendMessage) {
                     this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_PVP", player.getLang()).toString());
                  }

                  return false;
               }

               if (player.isInCombat()) {
                  if (sendMessage) {
                     this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_COMBAT", player.getLang()).toString());
                  }

                  return false;
               }

               if (player.getKarma() > 0) {
                  if (sendMessage) {
                     this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_PK", player.getLang()).toString());
                  }

                  return false;
               }
            }

            return true;
         }
      } else {
         if (sendMessage) {
            this.sendErrorMessageToPlayer(player, new ServerMessage("FightEvents.CANT_OLY1", player.getLang()).toString());
         }

         return false;
      }
   }

   public void requestEventPlayerMenuBypass(Player player, String bypass) {
      player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
      AbstractFightEvent event = player.getFightEvent();
      if (event != null) {
         FightEventPlayer fPlayer = event.getFightEventPlayer(player);
         if (fPlayer != null) {
            fPlayer.setShowTutorial(false);
            if (bypass.startsWith("_fightEvent")) {
               StringTokenizer st = new StringTokenizer(bypass, " ");
               st.nextToken();
               String action = st.nextToken();
               switch(action) {
                  case "leave":
                     this.askQuestion(player, new ServerMessage("FightEvents.WANT_TO_LEAVE", player.getLang()).toString());
                     break;
                  case "buffer":
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler("_bbsbuffer");
                     if (handler != null) {
                        handler.onBypassCommand("_bbsbuffer", player);
                     }
               }
            }
         }
      }
   }

   public void sendEventPlayerMenu(Player player) {
      AbstractFightEvent event = player.getFightEvent();
      if (event != null && event.getFightEventPlayer(player) != null) {
         FightEventPlayer fPlayer = event.getFightEventPlayer(player);
         fPlayer.setShowTutorial(true);
         StringBuilder builder = new StringBuilder();
         builder.append("<html><head><title>").append(player.getEventName(event.getId())).append("</title></head>");
         builder.append("<body>");
         builder.append("<br1><img src=\"L2UI.squaregray\" width=\"290\" height=\"1\">");
         builder.append("<table height=20 fixwidth=\"290\" bgcolor=29241d>");
         builder.append("\t<tr>");
         builder.append("\t\t<td height=20 width=290>");
         builder.append("\t\t\t<center><font name=\"hs12\" color=913d3d>").append(player.getEventName(event.getId())).append("</font></center>");
         builder.append("\t\t</td>");
         builder.append("\t</tr>");
         builder.append("</table>");
         builder.append("<br1><img src=\"L2UI.squaregray\" width=\"290\" height=\"1\">");
         builder.append("<br>");
         builder.append("<table fixwidth=290 bgcolor=29241d>");
         builder.append("\t<tr>");
         builder.append("\t\t<td valign=top width=280>");
         builder.append("\t\t\t<font color=388344>").append(player.getEventDescr(event.getId())).append("<br></font>");
         builder.append("\t\t</td>");
         builder.append("\t</tr>");
         builder.append("</table>");
         builder.append("<br1><img src=\"L2UI.squaregray\" width=\"290\" height=\"1\">");
         builder.append("<br>");
         builder.append("<table width=270>");
         builder.append("\t<tr>");
         builder.append("\t\t<td>");
         builder.append(
               "\t\t\t<center><button value = \"" + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.BUFFER") + "\" action=\"bypass -h "
            )
            .append("_fightEvent")
            .append(" buffer\" back=\"l2ui_ct1.button.OlympiadWnd_DF_Back_Down\" width=200 height=30 fore=\"l2ui_ct1.button.OlympiadWnd_DF_Back\"></center>");
         builder.append("\t\t</td>");
         builder.append("\t</tr>");
         builder.append("\t<tr>");
         builder.append("\t\t<td>");
         builder.append(
               "\t\t\t<center><button value = \""
                  + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.LEAVE_EVENT")
                  + "\" action=\"bypass -h "
            )
            .append("_fightEvent")
            .append(" leave\" back=\"l2ui_ct1.button.OlympiadWnd_DF_Back_Down\" width=200 height=30 fore=\"l2ui_ct1.button.OlympiadWnd_DF_Back\"></center>");
         builder.append("\t\t</td>");
         builder.append("\t</tr>");
         builder.append("\t<tr>");
         builder.append("\t\t<td>");
         builder.append(
               "\t\t\t<center><button value = \"" + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.CLOSE") + "\" action=\"bypass -h "
            )
            .append("_fightEvent")
            .append(" close\" back=\"l2ui_ct1.button.OlympiadWnd_DF_Info_Down\" width=200 height=30 fore=\"l2ui_ct1.button.OlympiadWnd_DF_Info\"></center>");
         builder.append("\t\t</td>");
         builder.append("\t</tr>");
         builder.append("</table>");
         builder.append("</body></html>");
         player.sendPacket(new TutorialShowHtml(builder.toString()));
         player.sendPacket(new ShowTutorialMark(false, 100));
      }
   }

   private void leaveEvent(Player player) {
      AbstractFightEvent event = player.getFightEvent();
      if (event != null) {
         if (event.leaveEvent(player, true)) {
            player.sendMessage(new ServerMessage("FightEvents.LEFT_EVENT", player.getLang()).toString());
         }
      }
   }

   private void askQuestion(Player player, String question) {
      player.sendConfirmDlg(new FightEventManager.AskQuestionAnswerListener(player), 0, question);
   }

   public AbstractFightEvent getEventById(int id) {
      return this._activeEvents.get(id);
   }

   public Map<Integer, AbstractFightEvent> getActiveEvents() {
      return this._activeEvents;
   }

   public void setEventTask(AbstractFightEvent event) {
      switch(event.getId()) {
         case 1:
            this._eventTask1 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
            break;
         case 2:
            this._eventTask2 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
            break;
         case 3:
            this._eventTask3 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
            break;
         case 4:
            this._eventTask4 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
            break;
         case 5:
            this._eventTask5 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
            break;
         case 6:
            this._eventTask6 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
            break;
         case 7:
            this._eventTask7 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
            break;
         case 8:
            this._eventTask8 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
            break;
         case 9:
            this._eventTask9 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
            break;
         case 10:
            this._eventTask10 = ThreadPoolManager.getInstance().schedule(new FightEventManager.EventTask(event), 120000L);
      }
   }

   public boolean getActiveEventTask(int id) {
      switch(id) {
         case 1:
            return this._eventTask1 != null;
         case 2:
            return this._eventTask2 != null;
         case 3:
            return this._eventTask3 != null;
         case 4:
            return this._eventTask4 != null;
         case 5:
            return this._eventTask5 != null;
         case 6:
            return this._eventTask6 != null;
         case 7:
            return this._eventTask7 != null;
         case 8:
            return this._eventTask8 != null;
         case 9:
            return this._eventTask9 != null;
         case 10:
            return this._eventTask10 != null;
         default:
            return false;
      }
   }

   public void clearEventIdTask(int objId) {
      switch(objId) {
         case 1:
            if (this._eventTask1 != null) {
               this._eventTask1.cancel(false);
               this._eventTask1 = null;
            }
            break;
         case 2:
            if (this._eventTask2 != null) {
               this._eventTask2.cancel(false);
               this._eventTask2 = null;
            }
            break;
         case 3:
            if (this._eventTask3 != null) {
               this._eventTask3.cancel(false);
               this._eventTask3 = null;
            }
            break;
         case 4:
            if (this._eventTask4 != null) {
               this._eventTask4.cancel(false);
               this._eventTask4 = null;
            }
            break;
         case 5:
            if (this._eventTask5 != null) {
               this._eventTask5.cancel(false);
               this._eventTask5 = null;
            }
            break;
         case 6:
            if (this._eventTask6 != null) {
               this._eventTask6.cancel(false);
               this._eventTask6 = null;
            }
            break;
         case 7:
            if (this._eventTask7 != null) {
               this._eventTask7.cancel(false);
               this._eventTask7 = null;
            }
            break;
         case 8:
            if (this._eventTask8 != null) {
               this._eventTask8.cancel(false);
               this._eventTask8 = null;
            }
            break;
         case 9:
            if (this._eventTask9 != null) {
               this._eventTask9.cancel(false);
               this._eventTask9 = null;
            }
            break;
         case 10:
            if (this._eventTask10 != null) {
               this._eventTask10.cancel(false);
               this._eventTask10 = null;
            }
      }
   }

   public void cleanEventId(int eventId) {
      for(FightEventGameRoom room : this._rooms) {
         if (room.getGame() != null && room.getGame().getId() == eventId) {
            room.cleanUp();
            this._rooms.remove(room);
         }
      }
   }

   public void prepareStartEventId(int eventId) {
      this.clearEventIdTask(eventId);
      AbstractFightEvent event = this.getEventById(eventId);
      if (event != null) {
         this.startEvent(event);
      }
   }

   public void removeEventId(int eventId) {
      if (this._activeEvents.containsKey(eventId)) {
         this._activeEvents.remove(eventId);
      }
   }

   public static FightEventManager getInstance() {
      if (_instance == null) {
         _instance = new FightEventManager();
      }

      return _instance;
   }

   private class AnswerEventInvitation implements OnAnswerListener {
      private final Player _player;
      private final AbstractFightEvent _event;

      private AnswerEventInvitation(Player player, AbstractFightEvent event) {
         this._player = player;
         this._event = event;
      }

      @Override
      public void sayYes() {
         FightEventManager.this.trySignForEvent(this._player, this._event, false);
      }

      @Override
      public void sayNo() {
      }
   }

   private class AskQuestionAnswerListener implements OnAnswerListener {
      private final Player _player;

      private AskQuestionAnswerListener(Player player) {
         this._player = player;
      }

      @Override
      public void sayYes() {
         FightEventManager.this.leaveEvent(this._player);
      }

      @Override
      public void sayNo() {
      }
   }

   public static enum CLASSES {
      FIGHTERS(
         13113,
         ClassId.fighter,
         ClassId.warrior,
         ClassId.gladiator,
         ClassId.warlord,
         ClassId.knight,
         ClassId.rogue,
         ClassId.elvenFighter,
         ClassId.elvenKnight,
         ClassId.elvenScout,
         ClassId.darkFighter,
         ClassId.palusKnight,
         ClassId.assassin,
         ClassId.orcFighter,
         ClassId.orcRaider,
         ClassId.destroyer,
         ClassId.orcMonk,
         ClassId.tyrant,
         ClassId.dwarvenFighter,
         ClassId.scavenger,
         ClassId.bountyHunter,
         ClassId.artisan,
         ClassId.warsmith,
         ClassId.maleSoldier,
         ClassId.femaleSoldier,
         ClassId.trooper,
         ClassId.warder,
         ClassId.berserker,
         ClassId.maleSoulbreaker,
         ClassId.femaleSoulbreaker,
         ClassId.inspector,
         ClassId.duelist,
         ClassId.dreadnought,
         ClassId.titan,
         ClassId.grandKhavatari,
         ClassId.maestro,
         ClassId.doombringer,
         ClassId.maleSoulhound,
         ClassId.femaleSoulhound
      ),
      TANKS(
         13112,
         ClassId.paladin,
         ClassId.darkAvenger,
         ClassId.templeKnight,
         ClassId.shillienKnight,
         ClassId.phoenixKnight,
         ClassId.hellKnight,
         ClassId.evaTemplar,
         ClassId.shillienTemplar,
         ClassId.trickster
      ),
      ARCHERS(
         13114,
         ClassId.hawkeye,
         ClassId.silverRanger,
         ClassId.phantomRanger,
         ClassId.arbalester,
         ClassId.sagittarius,
         ClassId.moonlightSentinel,
         ClassId.ghostSentinel,
         ClassId.fortuneSeeker
      ),
      DAGGERS(13114, ClassId.treasureHunter, ClassId.plainsWalker, ClassId.abyssWalker, ClassId.adventurer, ClassId.windRider, ClassId.ghostHunter),
      MAGES(
         13116,
         ClassId.mage,
         ClassId.wizard,
         ClassId.sorceror,
         ClassId.necromancer,
         ClassId.elvenMage,
         ClassId.elvenWizard,
         ClassId.spellsinger,
         ClassId.darkMage,
         ClassId.darkWizard,
         ClassId.spellhowler,
         ClassId.orcMage,
         ClassId.orcShaman,
         ClassId.archmage,
         ClassId.soultaker,
         ClassId.mysticMuse,
         ClassId.stormScreamer
      ),
      SUMMONERS(
         13118, ClassId.warlock, ClassId.elementalSummoner, ClassId.phantomSummoner, ClassId.arcanaLord, ClassId.elementalMaster, ClassId.spectralMaster
      ),
      HEALERS(13115, ClassId.bishop, ClassId.elder, ClassId.shillenElder, ClassId.cardinal, ClassId.evaSaint, ClassId.shillienSaint, ClassId.dominator),
      SUPPORTS(
         13117,
         ClassId.cleric,
         ClassId.prophet,
         ClassId.swordSinger,
         ClassId.oracle,
         ClassId.bladedancer,
         ClassId.shillienOracle,
         ClassId.overlord,
         ClassId.warcryer,
         ClassId.hierophant,
         ClassId.swordMuse,
         ClassId.spectralDancer,
         ClassId.doomcryer,
         ClassId.judicator
      );

      private int _transformId;
      private ClassId[] _classes;

      private CLASSES(int transformId, ClassId... ids) {
         this._transformId = transformId;
         this._classes = ids;
      }

      public ClassId[] getClasses() {
         return this._classes;
      }

      public int getTransformId() {
         return this._transformId;
      }
   }

   private class EventRunThread extends RunnableImpl {
      private final AbstractFightEvent _event;

      private EventRunThread(AbstractFightEvent event) {
         this._event = event;
      }

      @Override
      public void runImpl() throws Exception {
         FightEventManager.this.startEventCountdown(this._event);
         if (this._event.isAutoTimed()) {
            Thread.sleep(60000L);
            Calendar nextEventDate = FightEventManager.this.getClosestEventDate(this._event.getAutoStartTimes());
            ThreadPoolManager.getInstance()
               .schedule(FightEventManager.this.new EventRunThread(this._event), nextEventDate.getTimeInMillis() - System.currentTimeMillis());
            FightEventManager._log
               .info("Next event " + this._event.getNameEn() + " will start in: " + TimeUtils.toSimpleFormat(nextEventDate.getTimeInMillis()));
         }
      }
   }

   private class EventTask extends RunnableImpl {
      AbstractFightEvent _event;

      private EventTask(AbstractFightEvent event) {
         this._event = event;
      }

      @Override
      public void runImpl() {
         FightEventManager.this.sendToAllMsg(this._event, "FightEvents.LAST_3MIN", true);

         try {
            Thread.sleep(180000L);
         } catch (InterruptedException var4) {
            var4.printStackTrace();
         }

         FightEventManager.this.sendToAllMsg(this._event, "FightEvents.LAST_1MIN", true);
         FightEventManager.this.notifyConditions(this._event);

         try {
            Thread.sleep(45000L);
         } catch (InterruptedException var3) {
            var3.printStackTrace();
         }

         FightEventManager.this.sendToAllMsg(this._event, "FightEvents.LAST_15SEC", true);
         FightEventManager.this.notifyConditions(this._event);

         try {
            Thread.sleep(15000L);
         } catch (InterruptedException var2) {
            var2.printStackTrace();
         }

         FightEventManager.this.startEvent(this._event);
      }
   }
}

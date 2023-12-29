package l2e.scripts.clanhallsiege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import l2e.commons.util.Util;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.guard.SpecialGuardAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.model.entity.clanhall.SiegeStatus;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.zone.type.ResidenceHallTeleportZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class WildBeastReserveSiege extends ClanHallSiegeEngine {
   private static String qn = "WildBeastReserveSiege";
   private static final String SQL_LOAD_ATTACKERS = "SELECT * FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
   private static final String SQL_SAVE_ATTACKER = "INSERT INTO siegable_hall_flagwar_attackers_members VALUES (?,?,?)";
   private static final String SQL_LOAD_MEMEBERS = "SELECT object_id FROM siegable_hall_flagwar_attackers_members WHERE clan_id = ?";
   private static final String SQL_SAVE_CLAN = "INSERT INTO siegable_hall_flagwar_attackers VALUES(?,?,?,?)";
   private static final String SQL_SAVE_NPC = "UPDATE siegable_hall_flagwar_attackers SET npc = ? WHERE clan_id = ?";
   private static final String SQL_CLEAR_CLAN = "DELETE FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
   private static final String SQL_CLEAR_CLAN_ATTACKERS = "DELETE FROM siegable_hall_flagwar_attackers_members WHERE hall_id = ?";
   private static int ROYAL_FLAG = 35606;
   private static int FLAG_RED = 35607;
   private static int ALLY_1 = 35618;
   private static int ALLY_2 = 35619;
   private static int ALLY_3 = 35620;
   private static int ALLY_4 = 35621;
   private static int ALLY_5 = 35622;
   private static int TELEPORT_1 = 35612;
   private static int MESSENGER = 35627;
   protected static int[] OUTTER_DOORS_TO_OPEN = new int[2];
   protected static int[] INNER_DOORS_TO_OPEN = new int[2];
   private static Location[] FLAG_COORDS = new Location[7];
   private static ResidenceHallTeleportZone[] TELE_ZONES = new ResidenceHallTeleportZone[6];
   private static int QUEST_REWARD = 8293;
   private static int STONE = 8084;
   private static Location CENTER = new Location(57762, -92696, -1359, 0);
   protected static Map<Integer, WildBeastReserveSiege.ClanData> _data = new HashMap<>();
   private Clan _winner;
   private boolean _firstPhase;

   public WildBeastReserveSiege(int questId, String name, String descr, int hallId) {
      super(questId, name, descr, hallId);
      this.addStartNpc(MESSENGER);
      this.addFirstTalkId(MESSENGER);
      this.addTalkId(MESSENGER);

      for(int i = 0; i < 6; ++i) {
         this.addFirstTalkId(TELEPORT_1 + i);
      }

      this.addKillId(ALLY_1);
      this.addKillId(ALLY_2);
      this.addKillId(ALLY_3);
      this.addKillId(ALLY_4);
      this.addKillId(ALLY_5);
      this.addSpawnId(new int[]{ALLY_1});
      this.addSpawnId(new int[]{ALLY_2});
      this.addSpawnId(new int[]{ALLY_3});
      this.addSpawnId(new int[]{ALLY_4});
      this.addSpawnId(new int[]{ALLY_5});
      this._winner = ClanHolder.getInstance().getClan(this._hall.getOwnerId());
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (player.getQuestState(qn) == null) {
         this.newQuestState(player);
      }

      if (npc.getId() == MESSENGER) {
         if (!this.checkIsAttacker(player.getClan())) {
            Clan clan = ClanHolder.getInstance().getClan(this._hall.getOwnerId());
            NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
            html.setFile(player, "data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35627-00.htm");
            html.replace("%clanName%", clan == null ? "no owner" : clan.getName());
            player.sendPacket(html);
            return "";
         } else {
            return "35627-01.htm";
         }
      } else {
         int index = npc.getId() - TELEPORT_1;
         if (index == 0 && this._firstPhase) {
            return "35612-00.htm";
         } else {
            TELE_ZONES[index].checkTeleporTask();
            return "35612-01.htm";
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      Clan clan = player.getClan();
      if (event.startsWith("Register")) {
         if (!this._hall.isRegistering()) {
            if (!this._hall.isInSiege()) {
               this.sendRegistrationPageDate(player);
               return null;
            }

            htmltext = "35627-02.htm";
         } else if (clan != null && player.isClanLeader()) {
            if (this.getAttackers().size() >= 5) {
               htmltext = "35627-04.htm";
            } else if (this.checkIsAttacker(clan)) {
               htmltext = "35627-05.htm";
            } else if (this._hall.getOwnerId() == clan.getId()) {
               htmltext = "35627-06.htm";
            } else {
               String[] arg = event.split(" ");
               if (arg.length >= 2) {
                  if (arg[1].equals("wQuest")) {
                     if (player.destroyItemByItemId("Beast Farm Siege", QUEST_REWARD, 1L, player, true)) {
                        this.registerClan(clan);
                        htmltext = this.getFlagHtml(_data.get(clan.getId()).flag);
                     } else {
                        htmltext = "35627-07.htm";
                     }
                  } else if (arg[1].equals("wFee") && this.canPayRegistration()) {
                     if (player.reduceAdena(qn + " Siege", 200000L, player, true)) {
                        this.registerClan(clan);
                        htmltext = this.getFlagHtml(_data.get(clan.getId()).flag);
                     } else {
                        htmltext = "35627-08.htm";
                     }
                  }
               }
            }
         } else {
            htmltext = "35627-03.htm";
         }
      } else if (event.startsWith("Select_NPC")) {
         if (!player.isClanLeader()) {
            htmltext = "35627-09.htm";
         } else if (!_data.containsKey(clan.getId())) {
            htmltext = "35627-10.htm";
         } else {
            String[] var = event.split(" ");
            if (var.length >= 2) {
               int id = 0;

               try {
                  id = Integer.parseInt(var[1]);
               } catch (Exception var10) {
                  this._log.warning(qn + "->select_clan_npc->Wrong mahum warrior id: " + var[1]);
               }

               if (id > 0 && (htmltext = this.getAllyHtml(id)) != null) {
                  _data.get(clan.getId()).npc = id;
                  this.saveNpc(id, clan.getId());
               }
            } else {
               this._log.warning(qn + " Siege: Not enough parameters to save clan npc for clan: " + clan.getName());
            }
         }
      } else if (event.startsWith("View")) {
         WildBeastReserveSiege.ClanData cd = null;
         if (clan == null) {
            htmltext = "35627-10.htm";
         } else if ((cd = _data.get(clan.getId())) == null) {
            htmltext = "35627-03.htm";
         } else if (cd.npc == 0) {
            htmltext = "35627-11.htm";
         } else {
            htmltext = this.getAllyHtml(cd.npc);
         }
      } else if (event.startsWith("RegisterMember")) {
         if (clan == null) {
            htmltext = "35627-10.htm";
         } else if (!this._hall.isRegistering()) {
            htmltext = "35627-02.htm";
         } else if (!_data.containsKey(clan.getId())) {
            htmltext = "35627-03.htm";
         } else if (_data.get(clan.getId()).players.size() >= 18) {
            htmltext = "35627-12.htm";
         } else {
            WildBeastReserveSiege.ClanData data = _data.get(clan.getId());
            data.players.add(player.getObjectId());
            this.saveMember(clan.getId(), player.getObjectId());
            if (data.npc == 0) {
               htmltext = "35627-11.htm";
            } else {
               htmltext = "35627-13.htm";
            }
         }
      } else if (event.startsWith("Attackers")) {
         if (this._hall.isRegistering()) {
            this.sendRegistrationPageDate(player);
            return null;
         }

         htmltext = HtmCache.getInstance().getHtm(null, "data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35627-14.htm");
         int i = 0;

         for(Entry<Integer, WildBeastReserveSiege.ClanData> clanData : _data.entrySet()) {
            Clan attacker = ClanHolder.getInstance().getClan(clanData.getKey());
            if (attacker != null) {
               htmltext = htmltext.replaceAll("%clan" + i + "%", clan.getName());
               htmltext = htmltext.replaceAll("%clanMem" + i + "%", String.valueOf(clanData.getValue().players.size()));
               ++i;
            }
         }

         if (_data.size() < 5) {
            for(int c = _data.size(); c < 5; ++c) {
               htmltext = htmltext.replaceAll("%clan" + c + "%", "Empty pos. ");
               htmltext = htmltext.replaceAll("%clanMem" + c + "%", "Empty pos. ");
            }
         }
      } else if (event.startsWith("CheckQuest")) {
         if (clan == null || clan.getLevel() < 4) {
            htmltext = "35627-23.htm";
         } else if (!player.isClanLeader()) {
            htmltext = "35627-24.htm";
         } else if (clan.getHideoutId() <= 0 && clan.getFortId() <= 0 && clan.getCastleId() <= 0) {
            if (player.getInventory().getItemByItemId(QUEST_REWARD) != null) {
               htmltext = "35627-26.htm";
            } else if (player.getInventory().getInventoryItemCount(STONE, -1) >= 10L) {
               htmltext = "35627-22a.htm";
            } else {
               htmltext = "35627-22.htm";
            }
         } else {
            htmltext = "35627-25.htm";
         }
      }

      return htmltext;
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (this._hall.isInSiege()) {
         int npcId = npc.getId();

         for(int keys : _data.keySet()) {
            if (_data.get(keys).npc == npcId) {
               this.removeParticipant(keys, true);
            }
         }

         synchronized(this) {
            List<Integer> clanIds = new ArrayList<>(_data.keySet());
            if (!this._firstPhase) {
               this._missionAccomplished = true;
               this._winner = ClanHolder.getInstance().getClan(clanIds.get(0));
               this.removeParticipant(clanIds.get(0), false);
               this.endSiege();
            } else if ((clanIds.size() != 1 || this._hall.getOwnerId() > 0) && _data.get(clanIds.get(0)).npc != 0) {
               if (_data.size() == 2 && this._hall.getOwnerId() > 0) {
                  this.cancelSiegeTask();
                  this._firstPhase = false;
                  this._hall.getSiegeZone().setIsActive(false);

                  for(int doorId : INNER_DOORS_TO_OPEN) {
                     this._hall.openCloseDoor(doorId, true);
                  }

                  for(WildBeastReserveSiege.ClanData data : _data.values()) {
                     this.doUnSpawns(data);
                  }

                  ThreadPoolManager.getInstance().schedule(new Runnable() {
                     @Override
                     public void run() {
                        for(int doorId : WildBeastReserveSiege.INNER_DOORS_TO_OPEN) {
                           WildBeastReserveSiege.this._hall.openCloseDoor(doorId, false);
                        }

                        for(Entry<Integer, WildBeastReserveSiege.ClanData> e : WildBeastReserveSiege._data.entrySet()) {
                           WildBeastReserveSiege.this.doSpawns(e.getKey(), e.getValue());
                        }

                        WildBeastReserveSiege.this._hall.getSiegeZone().setIsActive(true);
                     }
                  }, 300000L);
               }
            } else {
               this._missionAccomplished = true;
               this.cancelSiegeTask();
               this.endSiege();
            }
         }
      }

      return null;
   }

   @Override
   public String onSpawn(Npc npc) {
      npc.getAI().setIntention(CtrlIntention.MOVING, CENTER);
      return null;
   }

   @Override
   public Clan getWinner() {
      return this._winner;
   }

   @Override
   public void prepareOwner() {
      if (this._hall.getOwnerId() > 0) {
         this.registerClan(ClanHolder.getInstance().getClan(this._hall.getOwnerId()));
      }

      this._hall.banishForeigners();
      SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
      msg.addString(Util.clanHallName(null, this._hall.getId()));
      Announcements.getInstance().announceToAll(msg);
      this._hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
      this._siegeTask = ThreadPoolManager.getInstance().schedule(new ClanHallSiegeEngine.SiegeStarts(), 3600000L);
   }

   @Override
   public void startSiege() {
      if (this.getAttackers().size() < 2) {
         this.onSiegeEnds();
         this.getAttackers().clear();
         this._hall.updateNextSiege();
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
         sm.addString(Util.clanHallName(null, this._hall.getId()));
         Announcements.getInstance().announceToAll(sm);
      } else {
         for(int door : OUTTER_DOORS_TO_OPEN) {
            this._hall.openCloseDoor(door, true);
         }

         if (this._hall.getOwnerId() > 0) {
            Clan owner = ClanHolder.getInstance().getClan(this._hall.getOwnerId());
            Location loc = this._hall.getZone().getSpawns().get(0);

            for(ClanMember pc : owner.getMembers()) {
               if (pc != null) {
                  Player player = pc.getPlayerInstance();
                  if (player != null && player.isOnline()) {
                     player.teleToLocation(loc, false);
                  }
               }
            }
         }

         ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               for(int door : WildBeastReserveSiege.OUTTER_DOORS_TO_OPEN) {
                  WildBeastReserveSiege.this._hall.openCloseDoor(door, false);
               }

               WildBeastReserveSiege.this._hall.getZone().banishNonSiegeParticipants();
               WildBeastReserveSiege.super.startSiege();
            }
         }, 300000L);
      }
   }

   @Override
   public void onSiegeStarts() {
      for(Entry<Integer, WildBeastReserveSiege.ClanData> clan : _data.entrySet()) {
         try {
            WildBeastReserveSiege.ClanData data = clan.getValue();
            this.doSpawns(clan.getKey(), data);
            this.fillPlayerList(data);
         } catch (Exception var4) {
            this.endSiege();
            this._log.warning(qn + ": Problems in siege initialization!");
            var4.printStackTrace();
         }
      }
   }

   @Override
   public void endSiege() {
      if (this._hall.getOwnerId() > 0) {
         Clan clan = ClanHolder.getInstance().getClan(this._hall.getOwnerId());
         clan.setHideoutId(0);
         this._hall.free();
      }

      super.endSiege();
   }

   @Override
   public void onSiegeEnds() {
      if (_data.size() > 0) {
         for(int clanId : _data.keySet()) {
            if (this._hall.getOwnerId() == clanId) {
               this.removeParticipant(clanId, false);
            } else {
               this.removeParticipant(clanId, true);
            }
         }
      }

      this.clearTables();
   }

   @Override
   public final Location getInnerSpawnLoc(Player player) {
      Location loc = null;
      if (player.getId() == this._hall.getOwnerId()) {
         loc = this._hall.getZone().getSpawns().get(0);
      } else {
         WildBeastReserveSiege.ClanData cd = _data.get(player.getId());
         if (cd != null) {
            int index = cd.flag - FLAG_RED;
            if (index < 0 || index > 4) {
               throw new ArrayIndexOutOfBoundsException();
            }

            loc = this._hall.getZone().getChallengerSpawns().get(index);
         }
      }

      return loc;
   }

   @Override
   public final boolean canPlantFlag() {
      return false;
   }

   @Override
   public final boolean doorIsAutoAttackable() {
      return false;
   }

   void doSpawns(int clanId, WildBeastReserveSiege.ClanData data) {
      try {
         NpcTemplate mahumTemplate = NpcsParser.getInstance().getTemplate(data.npc);
         NpcTemplate flagTemplate = NpcsParser.getInstance().getTemplate(data.flag);
         if (flagTemplate == null) {
            this._log.warning(qn + ": Flag L2NpcTemplate[" + data.flag + "] does not exist!");
            throw new NullPointerException();
         }

         if (mahumTemplate == null) {
            this._log.warning(qn + ": Ally L2NpcTemplate[" + data.npc + "] does not exist!");
            throw new NullPointerException();
         }

         int index = 0;
         if (this._firstPhase) {
            index = data.flag - FLAG_RED;
         } else {
            index = clanId == this._hall.getOwnerId() ? 5 : 6;
         }

         Location loc = FLAG_COORDS[index];
         data.flagInstance = new Spawner(flagTemplate);
         data.flagInstance.setLocation(loc);
         data.flagInstance.setRespawnDelay(10000);
         data.flagInstance.setAmount(1);
         data.flagInstance.init();
         data.warrior = new Spawner(mahumTemplate);
         data.warrior.setLocation(loc);
         data.warrior.setRespawnDelay(10000);
         data.warrior.setAmount(1);
         data.warrior.init();
         ((SpecialGuardAI)data.warrior.getLastSpawn().getAI()).getAlly().addAll(data.players);
      } catch (Exception var7) {
         this._log.warning(qn + ": Couldnt make clan spawns: " + var7.getMessage());
         var7.printStackTrace();
      }
   }

   private void fillPlayerList(WildBeastReserveSiege.ClanData data) {
      for(int objId : data.players) {
         Player plr = World.getInstance().getPlayer(objId);
         if (plr != null) {
            data.playersInstance.add(plr);
         }
      }
   }

   private void registerClan(Clan clan) {
      int clanId = clan.getId();
      SiegeClan sc = new SiegeClan(clanId, SiegeClan.SiegeClanType.ATTACKER);
      this.getAttackers().put(clanId, sc);
      WildBeastReserveSiege.ClanData data = new WildBeastReserveSiege.ClanData();
      data.flag = ROYAL_FLAG + _data.size();
      data.players.add(clan.getLeaderId());
      _data.put(clanId, data);
      this.saveClan(clanId, data.flag);
      this.saveMember(clanId, clan.getLeaderId());
   }

   private final void doUnSpawns(WildBeastReserveSiege.ClanData data) {
      if (data.flagInstance != null) {
         data.flagInstance.stopRespawn();
         data.flagInstance.getLastSpawn().deleteMe();
      }

      if (data.warrior != null) {
         data.warrior.stopRespawn();
         data.warrior.getLastSpawn().deleteMe();
      }
   }

   private final void removeParticipant(int clanId, boolean teleport) {
      WildBeastReserveSiege.ClanData dat = _data.remove(clanId);
      if (dat != null) {
         if (dat.flagInstance != null) {
            dat.flagInstance.stopRespawn();
            if (dat.flagInstance.getLastSpawn() != null) {
               dat.flagInstance.getLastSpawn().deleteMe();
            }
         }

         if (dat.warrior != null) {
            dat.warrior.stopRespawn();
            if (dat.warrior.getLastSpawn() != null) {
               dat.warrior.getLastSpawn().deleteMe();
            }
         }

         dat.players.clear();
         if (teleport) {
            for(Player pc : dat.playersInstance) {
               if (pc != null) {
                  pc.teleToLocation(TeleportWhereType.TOWN, true);
               }
            }
         }

         dat.playersInstance.clear();
      }
   }

   public boolean canPayRegistration() {
      return true;
   }

   private void sendRegistrationPageDate(Player player) {
      NpcHtmlMessage msg = new NpcHtmlMessage(5);
      msg.setFile(player, "data/scripts/clanhallsiege/" + qn + "/" + player.getLang() + "/35627-15.htm");
      msg.replace("%nextSiege%", this._hall.getSiegeDate().getTime().toString());
      player.sendPacket(msg);
   }

   public String getFlagHtml(int flag) {
      String result = "35627-15a.htm";
      switch(flag) {
         case 35607:
            result = "35627-16.htm";
            break;
         case 35608:
            result = "35627-17.htm";
            break;
         case 35609:
            result = "35627-18.htm";
            break;
         case 35610:
            result = "35627-19.htm";
            break;
         case 35611:
            result = "35627-20.htm";
      }

      return result;
   }

   public String getAllyHtml(int ally) {
      String result = null;
      switch(ally) {
         case 35618:
            result = "35627-16a.htm";
            break;
         case 35619:
            result = "35627-17a.htm";
            break;
         case 35620:
            result = "35627-18a.htm";
            break;
         case 35621:
            result = "35627-19a.htm";
            break;
         case 35622:
            result = "35627-20a.htm";
      }

      return result;
   }

   @Override
   public final void loadAttackers() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM siegable_hall_flagwar_attackers WHERE hall_id = ?");
         statement.setInt(1, this._hall.getId());
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            int clanId = rset.getInt("clan_id");
            if (ClanHolder.getInstance().getClan(clanId) == null) {
               this._log.warning(qn + ": Loaded an unexistent clan as attacker! Clan Id: " + clanId);
            } else {
               int flag = rset.getInt("flag");
               int npc = rset.getInt("npc");
               SiegeClan sc = new SiegeClan(clanId, SiegeClan.SiegeClanType.ATTACKER);
               this.getAttackers().put(clanId, sc);
               WildBeastReserveSiege.ClanData data = new WildBeastReserveSiege.ClanData();
               data.flag = flag;
               data.npc = npc;
               _data.put(clanId, data);
               this.loadAttackerMembers(clanId);
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var20) {
         this._log.warning(qn + ".loadAttackers()->" + var20.getMessage());
         var20.printStackTrace();
      }
   }

   private final void loadAttackerMembers(int clanId) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         ArrayList<Integer> listInstance = _data.get(clanId).players;
         if (listInstance == null) {
            this._log.warning(qn + ": Tried to load unregistered clan: " + clanId + "[clan Id]");
            return;
         }

         PreparedStatement statement = con.prepareStatement("SELECT object_id FROM siegable_hall_flagwar_attackers_members WHERE clan_id = ?");
         statement.setInt(1, clanId);
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            listInstance.add(rset.getInt("object_id"));
         }

         rset.close();
         statement.close();
      } catch (Exception var18) {
         this._log.warning(qn + ".loadAttackerMembers()->" + var18.getMessage());
         var18.printStackTrace();
      }
   }

   private final void saveClan(int clanId, int flag) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("INSERT INTO siegable_hall_flagwar_attackers VALUES(?,?,?,?)");
         statement.setInt(1, this._hall.getId());
         statement.setInt(2, flag);
         statement.setInt(3, 0);
         statement.setInt(4, clanId);
         statement.execute();
         statement.close();
      } catch (Exception var16) {
         this._log.warning(qn + ".saveClan()->" + var16.getMessage());
         var16.printStackTrace();
      }
   }

   private final void saveNpc(int npc, int clanId) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE siegable_hall_flagwar_attackers SET npc = ? WHERE clan_id = ?");
         statement.setInt(1, npc);
         statement.setInt(2, clanId);
         statement.execute();
         statement.close();
      } catch (Exception var16) {
         this._log.warning(qn + ".saveNpc()->" + var16.getMessage());
         var16.printStackTrace();
      }
   }

   private final void saveMember(int clanId, int objectId) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("INSERT INTO siegable_hall_flagwar_attackers_members VALUES (?,?,?)");
         statement.setInt(1, this._hall.getId());
         statement.setInt(2, clanId);
         statement.setInt(3, objectId);
         statement.execute();
         statement.close();
      } catch (Exception var16) {
         this._log.warning(qn + ".saveMember()->" + var16.getMessage());
         var16.printStackTrace();
      }
   }

   private void clearTables() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement stat1 = con.prepareStatement("DELETE FROM siegable_hall_flagwar_attackers WHERE hall_id = ?");
         stat1.setInt(1, this._hall.getId());
         stat1.execute();
         stat1.close();
         PreparedStatement stat2 = con.prepareStatement("DELETE FROM siegable_hall_flagwar_attackers_members WHERE hall_id = ?");
         stat2.setInt(1, this._hall.getId());
         stat2.execute();
         stat2.close();
      } catch (Exception var15) {
         this._log.warning(qn + ".clearTables()->" + var15.getMessage());
      }
   }

   public static void main(String[] args) {
      new WildBeastReserveSiege(-1, qn, "clanhallsiege", 63);
   }

   static {
      FLAG_COORDS[0] = new Location(56963, -92211, -1303, 60611);
      FLAG_COORDS[1] = new Location(58090, -91641, -1303, 47274);
      FLAG_COORDS[2] = new Location(58908, -92556, -1303, 34450);
      FLAG_COORDS[3] = new Location(58336, -93600, -1303, 21100);
      FLAG_COORDS[4] = new Location(57152, -93360, -1303, 8400);
      FLAG_COORDS[5] = new Location(59116, -93251, -1302, 31000);
      FLAG_COORDS[6] = new Location(56432, -92864, -1303, 64000);
      OUTTER_DOORS_TO_OPEN[0] = 21150003;
      OUTTER_DOORS_TO_OPEN[1] = 21150004;
      INNER_DOORS_TO_OPEN[0] = 21150001;
      INNER_DOORS_TO_OPEN[1] = 21150002;

      for(ResidenceHallTeleportZone teleZone : ZoneManager.getInstance().getAllZones(ResidenceHallTeleportZone.class)) {
         if (teleZone.getResidenceId() == 63) {
            int id = teleZone.getResidenceZoneId();
            if (id >= 0 && id < 6) {
               TELE_ZONES[id] = teleZone;
            }
         }
      }
   }

   class ClanData {
      int flag = 0;
      int npc = 0;
      ArrayList<Integer> players = new ArrayList<>(18);
      ArrayList<Player> playersInstance = new ArrayList<>(18);
      Spawner warrior = null;
      Spawner flagInstance = null;
   }
}

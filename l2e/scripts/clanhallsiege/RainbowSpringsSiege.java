package l2e.scripts.clanhallsiege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import l2e.commons.util.Rnd;
import l2e.commons.util.TimeUtils;
import l2e.commons.util.Util;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ChestInstance;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.model.entity.clanhall.SiegeStatus;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.apache.commons.lang.ArrayUtils;

public final class RainbowSpringsSiege extends ClanHallSiegeEngine {
   private static final String qn = "RainbowSpringsSiege";
   private static final int WAR_DECREES = 8034;
   private static final int RAINBOW_NECTAR = 8030;
   private static final int RAINBOW_MWATER = 8031;
   private static final int RAINBOW_WATER = 8032;
   private static final int RAINBOW_SULFUR = 8033;
   private static final int MESSENGER = 35604;
   private static final int CARETAKER = 35603;
   private static final int CHEST = 35593;
   private static final int ENRAGED_YETI = 35592;
   protected static Map<Integer, Long> _warDecreesCount = new HashMap<>();
   protected static List<Clan> _acceptedClans = new ArrayList<>();
   protected ArrayList<Integer> _playersOnArena = new ArrayList<>();
   protected final List<Npc> chests = new ArrayList<>();
   private static final int ItemA = 8035;
   private static final int ItemB = 8036;
   private static final int ItemC = 8037;
   private static final int ItemD = 8038;
   private static final int ItemE = 8039;
   private static final int ItemF = 8040;
   private static final int ItemG = 8041;
   private static final int ItemH = 8042;
   private static final int ItemI = 8043;
   private static final int ItemK = 8045;
   private static final int ItemL = 8046;
   private static final int ItemN = 8047;
   private static final int ItemO = 8048;
   private static final int ItemP = 8049;
   private static final int ItemR = 8050;
   private static final int ItemS = 8051;
   private static final int ItemT = 8052;
   private static final int ItemU = 8053;
   private static final int ItemW = 8054;
   private static final int ItemY = 8055;
   protected static int _generated;
   protected Future<?> _task = null;
   protected Future<?> _chesttask = null;
   private Clan _winner;
   protected static final RainbowSpringsSiege.Word[] WORLD_LIST = new RainbowSpringsSiege.Word[8];
   private static final int[] GOURDS = new int[]{35588, 35589, 35590, 35591};
   private static Spawner[] _gourds = new Spawner[4];
   private static Npc[] _yetis = new Npc[4];
   protected Npc _chest1;
   protected Npc _chest2;
   protected Npc _chest3;
   protected Npc _chest4;
   private static final int[] YETIS = new int[]{35596, 35597, 35598, 35599};
   private static final int[][] ARENAS = new int[][]{{151562, -127080, -2214}, {153141, -125335, -2214}, {153892, -127530, -2214}, {155657, -125752, -2214}};
   private static final int[][] YETIS_SPAWN = new int[][]{
      {151560, -127075, -2221}, {153129, -125337, -2221}, {153884, -127534, -2221}, {156657, -125753, -2221}
   };
   protected static final int[][] CHESTS_SPAWN = new int[][]{
      {151560, -127075, -2221}, {153129, -125337, -2221}, {153884, -127534, -2221}, {155657, -125753, -2221}
   };
   protected final int[] arenaChestsCnt = new int[]{0, 0, 0, 0};
   private static final Skill[] DEBUFFS = new Skill[]{SkillsParser.getInstance().getInfo(4991, 1)};

   public RainbowSpringsSiege(int questId, String name, String descr, int hallId) {
      super(questId, name, descr, hallId);
      this.addFirstTalkId(35604);
      this.addFirstTalkId(35603);
      this.addFirstTalkId(YETIS);
      this.addTalkId(35604);
      this.addTalkId(35603);
      this.addTalkId(YETIS);

      for(int squashes : GOURDS) {
         this.addSpawnId(new int[]{squashes});
         this.addKillId(squashes);
      }

      this.addSpawnId(new int[]{35592});
      this.addSkillSeeId(YETIS);
      this.addKillId(35593);
      _generated = -1;
      this._winner = ClanHolder.getInstance().getClan(this._hall.getOwnerId());
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (player.getQuestState("RainbowSpringsSiege") == null) {
         this.newQuestState(player);
      }

      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      int npcId = npc.getId();
      if (npcId == 35604) {
         String main = this._hall.getOwnerId() > 0 ? "35604-01.htm" : "35604-00.htm";
         html.setFile(player, "data/scripts/clanhallsiege/RainbowSpringsSiege/" + player.getLang() + "/" + main);
         html.replace("%nextSiege%", TimeUtils.toSimpleFormat(this._hall.getSiegeDate()));
         if (this._hall.getOwnerId() > 0) {
            html.replace("%owner%", ClanHolder.getInstance().getClan(this._hall.getOwnerId()).getName());
         }

         player.sendPacket(html);
      } else if (npcId == 35603) {
         String main = !this._hall.isInSiege() && this._hall.isWaitingBattle() ? "35603-01.htm" : "35603-00.htm";
         html.setFile(player, "data/scripts/clanhallsiege/RainbowSpringsSiege/" + player.getLang() + "/" + main);
         player.sendPacket(html);
      } else if (Util.contains(YETIS, npcId)) {
         Clan clan = player.getClan();
         if (_acceptedClans.contains(clan)) {
            int index = _acceptedClans.indexOf(clan);
            if (npcId == YETIS[index]) {
               if (!player.isClanLeader()) {
                  html.setFile(player, "data/scripts/clanhallsiege/RainbowSpringsSiege/" + player.getLang() + "/35596-00.htm");
               } else {
                  html.setFile(player, "data/scripts/clanhallsiege/RainbowSpringsSiege/" + player.getLang() + "/35596-01.htm");
               }
            } else {
               html.setFile(player, "data/scripts/clanhallsiege/RainbowSpringsSiege/" + player.getLang() + "/35596-06.htm");
            }
         } else {
            html.setFile(player, "data/scripts/clanhallsiege/RainbowSpringsSiege/" + player.getLang() + "/35596-06.htm");
         }

         player.sendPacket(html);
      }

      player.setLastQuestNpcObject(npc.getObjectId());
      return "";
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext;
      htmltext = event;
      Clan clan = player.getClan();
      label370:
      switch(npc.getId()) {
         case 35603:
            switch(event) {
               case "GoToArena":
                  Party party = player.getParty();
                  if (clan == null) {
                     htmltext = "35603-07.htm";
                  } else if (!player.isClanLeader()) {
                     htmltext = "35603-02.htm";
                  } else if (!player.isInParty()) {
                     htmltext = "35603-03.htm";
                  } else {
                     if (party.getLeaderObjectId() == player.getObjectId()) {
                        int clanId = player.getId();
                        boolean nonClanMemberInParty = false;

                        for(Player member : party.getMembers()) {
                           if (member.getId() != clanId) {
                              nonClanMemberInParty = true;
                              break;
                           }
                        }

                        if (nonClanMemberInParty) {
                           htmltext = "35603-05.htm";
                        } else if (party.getMemberCount() < 5) {
                           htmltext = "35603-06.htm";
                        }

                        if (clan.getCastleId() <= 0 && clan.getFortId() <= 0 && clan.getHideoutId() <= 0) {
                           if (clan.getLevel() < Config.CHS_CLAN_MINLEVEL) {
                              htmltext = "35603-09.htm";
                           } else if (!_acceptedClans.contains(clan)) {
                              htmltext = "35603-10.htm";
                           } else {
                              this.portToArena(player, _acceptedClans.indexOf(clan));
                           }
                        } else {
                           htmltext = "35603-08.htm";
                        }

                        return null;
                     }

                     htmltext = "35603-04.htm";
                  }
               default:
                  break label370;
            }
         case 35604:
            switch(event) {
               case "Register":
                  if (!player.isClanLeader()) {
                     htmltext = "35604-07.htm";
                  } else if (clan.getCastleId() > 0 || clan.getFortId() > 0 || clan.getHideoutId() > 0) {
                     htmltext = "35604-09.htm";
                  } else if (!this._hall.isRegistering()) {
                     htmltext = "35604-11.htm";
                  } else if (_warDecreesCount.containsKey(clan.getId())) {
                     htmltext = "35604-10.htm";
                  } else if (this.getAttackers().size() >= 4) {
                     htmltext = "35604-18.htm";
                  } else if (clan.getLevel() >= 3 && clan.getMembersCount() >= 5) {
                     ItemInstance warDecrees = player.getInventory().getItemByItemId(8034);
                     if (warDecrees == null) {
                        htmltext = "35604-05.htm";
                     } else {
                        long count = warDecrees.getCount();
                        _warDecreesCount.put(clan.getId(), count);
                        player.destroyItem("Rainbow Springs Registration", warDecrees, npc, true);
                        this.registerClan(clan, count, true);
                        htmltext = "35604-06.htm";
                     }
                  } else {
                     htmltext = "35604-08.htm";
                  }
                  break;
               case "Cancel":
                  if (!player.isClanLeader()) {
                     htmltext = "35604-08.htm";
                  } else if (!_warDecreesCount.containsKey(clan.getId())) {
                     htmltext = "35604-12.htm";
                  } else if (!this._hall.isRegistering()) {
                     htmltext = "35604-13.htm";
                  } else {
                     this.registerClan(clan, 0L, false);
                     htmltext = "35604-17.htm";
                  }
                  break;
               case "Unregister":
                  if (this._hall.isRegistering()) {
                     if (_warDecreesCount.containsKey(clan.getId())) {
                        player.addItem("Rainbow Spring unregister", 8034, _warDecreesCount.get(clan.getId()) / 2L, npc, true);
                        _warDecreesCount.remove(clan.getId());
                        htmltext = "35604-14.htm";
                     } else {
                        htmltext = "35604-16.htm";
                     }
                  } else if (this._hall.isWaitingBattle()) {
                     _acceptedClans.remove(clan);
                     htmltext = "35604-16.htm";
                  }
            }
      }

      if (!event.startsWith("getItem")) {
         if (event.startsWith("seeItem")) {
            NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
            html.setFile(player, "data/scripts/clanhallsiege/RainbowSpringsSiege/" + player.getLang() + "/35596-05.htm");
            if (_generated == -1) {
               html.replace("%word%", "<fstring>" + NpcStringId.UNDECIDED + "</fstring>");
            } else {
               html.replace("%word%", WORLD_LIST[_generated].getName());
            }

            player.sendPacket(html);
            return null;
         } else {
            return htmltext;
         }
      } else {
         NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
         boolean has = true;
         if (_generated == -1) {
            has = false;
         } else {
            RainbowSpringsSiege.Word word = WORLD_LIST[_generated];
            if (_generated == 0) {
               if (player.getInventory().getInventoryItemCount(8036, -1) >= 2L
                  && player.getInventory().getInventoryItemCount(8035, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8055, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8038, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8053, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8037, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8045, -1) >= 1L) {
                  has = true;
               } else {
                  has = false;
               }
            }

            if (_generated == 1) {
               if (player.getInventory().getInventoryItemCount(8035, -1) >= 2L
                  && player.getInventory().getInventoryItemCount(8046, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8036, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8052, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8050, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8048, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8051, -1) >= 1L) {
                  has = true;
               } else {
                  has = false;
               }
            }

            if (_generated == 2) {
               if (player.getInventory().getInventoryItemCount(8049, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8039, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8046, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8043, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8037, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8035, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8047, -1) >= 1L) {
                  has = true;
               } else {
                  has = false;
               }
            }

            if (_generated == 3) {
               if (player.getInventory().getInventoryItemCount(8045, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8043, -1) >= 2L
                  && player.getInventory().getInventoryItemCount(8047, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8041, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8040, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8051, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8042, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8039, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8050, -1) >= 1L) {
                  has = true;
               } else {
                  has = false;
               }
            }

            if (_generated == 4) {
               if (player.getInventory().getInventoryItemCount(8037, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8055, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8041, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8047, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8053, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8051, -1) >= 1L) {
                  has = true;
               } else {
                  has = false;
               }
            }

            if (_generated == 5) {
               if (player.getInventory().getInventoryItemCount(8052, -1) >= 2L
                  && player.getInventory().getInventoryItemCount(8050, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8043, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8048, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8047, -1) >= 1L) {
                  has = true;
               } else {
                  has = false;
               }
            }

            if (_generated == 6) {
               if (player.getInventory().getInventoryItemCount(8050, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8035, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8043, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8047, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8036, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8048, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8054, -1) >= 1L) {
                  has = true;
               } else {
                  has = false;
               }
            }

            if (_generated == 7) {
               if (player.getInventory().getInventoryItemCount(8051, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8049, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8050, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8043, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8047, -1) >= 1L
                  && player.getInventory().getInventoryItemCount(8041, -1) >= 1L) {
                  has = true;
               } else {
                  has = false;
               }
            }

            if (has) {
               for(int[] itemInfo : word.getItems()) {
                  player.destroyItemByItemId("Rainbow Item", itemInfo[0], (long)itemInfo[1], player, true);
               }

               int rnd = Rnd.get(100);
               if (_generated >= 0 && _generated <= 5) {
                  if (rnd < 70) {
                     this.addItem(player, 8030);
                  } else if (rnd < 80) {
                     this.addItem(player, 8031);
                  } else if (rnd < 90) {
                     this.addItem(player, 8032);
                  } else {
                     this.addItem(player, 8033);
                  }
               } else if (rnd < 10) {
                  this.addItem(player, 8030);
               } else if (rnd < 40) {
                  this.addItem(player, 8031);
               } else if (rnd < 70) {
                  this.addItem(player, 8032);
               } else {
                  this.addItem(player, 8033);
               }
            }

            if (!has) {
               html.setFile(player, "data/scripts/clanhallsiege/RainbowSpringsSiege/" + player.getLang() + "/35596-02.htm");
            } else {
               html.setFile(player, "data/scripts/clanhallsiege/RainbowSpringsSiege/" + player.getLang() + "/35596-04.htm");
            }

            player.sendPacket(html);
         }

         return null;
      }
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      Clan clan = caster.getClan();
      if (clan != null && _acceptedClans.contains(clan)) {
         int index = _acceptedClans.indexOf(clan);
         int warIndex = Integer.MIN_VALUE;
         if (Util.contains(targets, npc) && npc.isInsideRadius(caster, 60, false, false)) {
            switch(skill.getId()) {
               case 2240:
                  if (getRandom(100) < 10) {
                     addSpawn(35592, caster.getX() + 10, caster.getY() + 10, caster.getZ(), 0, false, 0L, false);
                  }

                  reduceGourdHp(index, caster);
                  break;
               case 2241:
                  warIndex = this.rndEx(_acceptedClans.size(), index);
                  if (warIndex == Integer.MIN_VALUE) {
                     return null;
                  }

                  increaseGourdHp(warIndex);
                  break;
               case 2242:
                  warIndex = this.rndEx(_acceptedClans.size(), index);
                  if (warIndex == Integer.MIN_VALUE) {
                     return null;
                  }

                  moveGourds(warIndex);
                  break;
               case 2243:
                  warIndex = this.rndEx(_acceptedClans.size(), index);
                  if (warIndex == Integer.MIN_VALUE) {
                     return null;
                  }

                  this.castDebuffsOnEnemies(caster, warIndex);
            }
         }

         return super.onSkillSee(npc, caster, skill, targets, isSummon);
      } else {
         return null;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      Clan clan = killer.getClan();
      int index = _acceptedClans.indexOf(clan);
      if (clan != null && _acceptedClans.contains(clan)) {
         if (npc.getId() == 35593) {
            this.chestDie(npc);
            if (this.chests.contains(npc)) {
               this.chests.remove(npc);
            }

            int chance = Rnd.get(100);
            if (chance <= 5) {
               ((ChestInstance)npc).dropItem(killer, 8035, 1L);
            } else if (chance > 5 && chance <= 10) {
               ((ChestInstance)npc).dropItem(killer, 8036, 1L);
            } else if (chance > 10 && chance <= 15) {
               ((ChestInstance)npc).dropItem(killer, 8037, 1L);
            } else if (chance > 15 && chance <= 20) {
               ((ChestInstance)npc).dropItem(killer, 8038, 1L);
            } else if (chance > 20 && chance <= 25) {
               ((ChestInstance)npc).dropItem(killer, 8039, 1L);
            } else if (chance > 25 && chance <= 30) {
               ((ChestInstance)npc).dropItem(killer, 8040, 1L);
            } else if (chance > 30 && chance <= 35) {
               ((ChestInstance)npc).dropItem(killer, 8041, 1L);
            } else if (chance > 35 && chance <= 40) {
               ((ChestInstance)npc).dropItem(killer, 8042, 1L);
            } else if (chance > 40 && chance <= 45) {
               ((ChestInstance)npc).dropItem(killer, 8043, 1L);
            } else if (chance > 45 && chance <= 50) {
               ((ChestInstance)npc).dropItem(killer, 8045, 1L);
            } else if (chance > 50 && chance <= 55) {
               ((ChestInstance)npc).dropItem(killer, 8046, 1L);
            } else if (chance > 55 && chance <= 60) {
               ((ChestInstance)npc).dropItem(killer, 8047, 1L);
            } else if (chance > 60 && chance <= 65) {
               ((ChestInstance)npc).dropItem(killer, 8048, 1L);
            } else if (chance > 65 && chance <= 70) {
               ((ChestInstance)npc).dropItem(killer, 8049, 1L);
            } else if (chance > 70 && chance <= 75) {
               ((ChestInstance)npc).dropItem(killer, 8050, 1L);
            } else if (chance > 75 && chance <= 80) {
               ((ChestInstance)npc).dropItem(killer, 8051, 1L);
            } else if (chance > 80 && chance <= 85) {
               ((ChestInstance)npc).dropItem(killer, 8052, 1L);
            } else if (chance > 85 && chance <= 90) {
               ((ChestInstance)npc).dropItem(killer, 8053, 1L);
            } else if (chance > 90 && chance <= 95) {
               ((ChestInstance)npc).dropItem(killer, 8054, 1L);
            } else if (chance > 95) {
               ((ChestInstance)npc).dropItem(killer, 8055, 1L);
            }
         }

         if (npc.getId() == GOURDS[index]) {
            this._missionAccomplished = true;
            this._winner = ClanHolder.getInstance().getClan(clan.getId());
            synchronized(this) {
               this.cancelSiegeTask();
               this.endSiege();
               ThreadPoolManager.getInstance().schedule(new Runnable() {
                  @Override
                  public void run() {
                     for(int id : RainbowSpringsSiege.this._playersOnArena) {
                        Player pl = World.getInstance().getPlayer(id);
                        if (pl != null) {
                           pl.teleToLocation(TeleportWhereType.TOWN, true);
                        }
                     }

                     RainbowSpringsSiege.this._playersOnArena = new ArrayList<>();
                  }
               }, 120000L);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Override
   public final String onSpawn(Npc npc) {
      if (npc.getId() == 35592) {
         npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), NpcStringId.OOOH_WHO_POURED_NECTAR_ON_MY_HEAD_WHILE_I_WAS_SLEEPING));
      }

      if (ArrayUtils.contains(GOURDS, npc.getId())) {
         npc.setIsParalyzed(true);
      }

      return super.onSpawn(npc);
   }

   @Override
   public void startSiege() {
      if (_acceptedClans != null && !_acceptedClans.isEmpty() && _acceptedClans.size() >= 2) {
         this.spawnGourds();
         this.spawnYetis();
      } else {
         this.onSiegeEnds();
         _acceptedClans.clear();
         this._hall.updateNextSiege();
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
         sm.addString(Util.clanHallName(null, this._hall.getId()));
         Announcements.getInstance().announceToAll(sm);
      }
   }

   @Override
   public void prepareOwner() {
      if (this._hall.getOwnerId() > 0) {
         this.registerClan(ClanHolder.getInstance().getClan(this._hall.getOwnerId()), 10000L, true);
      }

      this._hall.banishForeigners();
      SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
      msg.addString(Util.clanHallName(null, this._hall.getId()));
      Announcements.getInstance().announceToAll(msg);
      this._hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
      this._siegeTask = ThreadPoolManager.getInstance().schedule(new ClanHallSiegeEngine.SiegeStarts(), 3600000L);
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
      this.unSpawnGourds();
      this.unSpawnYetis();
      this.unSpawnChests();
      this.clearTables();
   }

   protected void portToArena(Player leader, int arena) {
      if (arena >= 0 && arena <= 3) {
         for(Player pc : leader.getParty().getMembers()) {
            if (pc != null) {
               pc.stopAllEffects();
               if (pc.hasSummon()) {
                  pc.getSummon().unSummon(pc);
               }

               this._playersOnArena.add(pc.getObjectId());
               pc.teleToLocation(ARENAS[arena][0], ARENAS[arena][1], ARENAS[arena][2], true);
            }
         }
      } else {
         this._log.warning("RainbowSpringsSiege: Wrong arena id passed: " + arena);
      }
   }

   protected void spawnYetis() {
      if (_acceptedClans != null && !_acceptedClans.isEmpty()) {
         for(int i = 0; i < _acceptedClans.size(); ++i) {
            if (_yetis[i] == null) {
               try {
                  _yetis[i] = addSpawn(YETIS[i], YETIS_SPAWN[i][0], YETIS_SPAWN[i][1], YETIS_SPAWN[i][2], 0, false, 0L, false);
                  _yetis[i].setHeading(1);
                  this._task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RainbowSpringsSiege.GenerateTask(_yetis[i]), 10000L, 300000L);
               } catch (Exception var3) {
                  var3.printStackTrace();
               }
            }
         }
      }
   }

   protected void spawnGourds() {
      if (_acceptedClans != null && !_acceptedClans.isEmpty() && _acceptedClans.size() >= 2) {
         for(int i = 0; i < _acceptedClans.size(); ++i) {
            try {
               _gourds[i] = new Spawner(NpcsParser.getInstance().getTemplate(GOURDS[i]));
               _gourds[i].setX(ARENAS[i][0] + 150);
               _gourds[i].setY(ARENAS[i][1] + 150);
               _gourds[i].setZ(ARENAS[i][2]);
               _gourds[i].setHeading(1);
               _gourds[i].setAmount(1);
            } catch (Exception var3) {
               var3.printStackTrace();
            }

            SpawnParser.getInstance().addNewSpawn(_gourds[i]);
            _gourds[i].init();
         }

         this._chesttask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RainbowSpringsSiege.ChestsSpawn(), 5000L, 5000L);
      }
   }

   protected void unSpawnYetis() {
      for(int i = 0; i < _acceptedClans.size(); ++i) {
         _yetis[i].deleteMe();
         if (this._task != null) {
            this._task.cancel(false);
            this._task = null;
         }
      }
   }

   protected void unSpawnGourds() {
      for(int i = 0; i < _acceptedClans.size(); ++i) {
         _gourds[i].getLastSpawn().deleteMe();
         SpawnParser.getInstance().deleteSpawn(_gourds[i]);
      }
   }

   protected void unSpawnChests() {
      if (!this.chests.isEmpty()) {
         for(Npc chest : this.chests) {
            if (chest != null) {
               chest.deleteMe();
               if (this._chesttask != null) {
                  this._chesttask.cancel(false);
                  this._chesttask = null;
               }
            }
         }
      }
   }

   private static void moveGourds(int index) {
      Spawner[] tempArray = _gourds;

      for(int i = 0; i < index; ++i) {
         Spawner oldSpawn = _gourds[index - 1 - i];
         Spawner curSpawn = tempArray[i];
         _gourds[index - 1 - i] = curSpawn;
         int newX = oldSpawn.getX();
         int newY = oldSpawn.getY();
         int newZ = oldSpawn.getZ();
         curSpawn.getLastSpawn().teleToLocation(newX, newY, newZ, true);
      }
   }

   private static void reduceGourdHp(int index, Player player) {
      Spawner gourd = _gourds[index];
      gourd.getLastSpawn().reduceCurrentHp(1000.0, player, null);
   }

   private static void increaseGourdHp(int index) {
      Spawner gourd = _gourds[index];
      Npc gourdNpc = gourd.getLastSpawn();
      gourdNpc.setCurrentHp(gourdNpc.getCurrentHp() + 1000.0);
   }

   private void castDebuffsOnEnemies(Player player, int myArena) {
      if (_acceptedClans.contains(player.getClan())) {
         int index = _acceptedClans.indexOf(player.getClan());
         if (this._playersOnArena.contains(player.getObjectId())) {
            for(Player pl : player.getParty().getMembers()) {
               if (index == myArena && pl != null) {
                  for(Skill sk : DEBUFFS) {
                     sk.getEffects(pl, pl, false);
                  }
               }
            }
         }
      }
   }

   private void registerClan(Clan clan, long count, boolean register) {
      if (register) {
         SiegeClan sc = new SiegeClan(clan.getId(), SiegeClan.SiegeClanType.ATTACKER);
         this.getAttackers().put(clan.getId(), sc);
         int spotLeft = 4;

         for(int i = 0; i < 4; ++i) {
            long counter = 0L;
            Clan fightclan = null;

            for(int clanId : _warDecreesCount.keySet()) {
               Clan actingClan = ClanHolder.getInstance().getClan(clanId);
               if (actingClan != null && actingClan.getDissolvingExpiryTime() <= 0L) {
                  long counts = _warDecreesCount.get(clanId);
                  if (counts > counter) {
                     counter = counts;
                     fightclan = actingClan;
                  }
               } else {
                  _warDecreesCount.remove(clanId);
               }
            }

            if (fightclan != null && _acceptedClans.size() < 4) {
               _acceptedClans.add(clan);
            }
         }

         updateAttacker(clan.getId(), count, false);
      } else {
         updateAttacker(clan.getId(), 0L, true);
      }
   }

   private static void updateAttacker(int clanId, long count, boolean remove) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement;
         if (remove) {
            statement = con.prepareStatement("DELETE FROM rainbowsprings_attacker_list WHERE clanId = ?");
            statement.setInt(1, clanId);
         } else {
            statement = con.prepareStatement("INSERT INTO rainbowsprings_attacker_list VALUES (?,?)");
            statement.setInt(1, clanId);
            statement.setLong(2, count);
         }

         statement.execute();
         statement.close();
      } catch (Exception var17) {
         var17.printStackTrace();
      }
   }

   @Override
   public final void loadAttackers() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM rainbowsprings_attacker_list");
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            int clanId = rset.getInt("clanId");
            long count = rset.getLong("war_decrees_count");
            _warDecreesCount.put(clanId, count);

            for(int clan : _warDecreesCount.keySet()) {
               Clan loadClan = ClanHolder.getInstance().getClan(clan);
               _acceptedClans.add(loadClan);
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var21) {
         this._log.warning("RainbowSpringsSiege.loadAttackers()->" + var21.getMessage());
         var21.printStackTrace();
      }
   }

   private void clearTables() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement stat1 = con.prepareStatement("DELETE FROM rainbowsprings_attacker_list");
         stat1.execute();
         stat1.close();
      } catch (Exception var14) {
         this._log.warning("RainbowSpringsSiege.clearTables()->" + var14.getMessage());
      }
   }

   private void addItem(Player player, int itemId) {
      player.getInventory().addItem("Rainbow Item", itemId, 1L, player, null);
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
      sm.addItemName(itemId);
      player.sendPacket(sm);
   }

   protected void chestDie(Npc npc) {
      for(int i = 0; i < _acceptedClans.size(); ++i) {
         this.arenaChestsCnt[i]--;
      }
   }

   private int rndEx(int size, int ex) {
      int rnd = Integer.MIN_VALUE;

      for(int i = 0; i < 127; ++i) {
         rnd = Rnd.get(size);
         if (rnd != ex) {
            break;
         }
      }

      return rnd;
   }

   @Override
   public Clan getWinner() {
      return this._winner;
   }

   public static void main(String[] args) {
      new RainbowSpringsSiege(-1, "RainbowSpringsSiege", "clanhallsiege", 62);
   }

   static {
      WORLD_LIST[0] = new RainbowSpringsSiege.Word(
         "BABYDUCK",
         new int[]{8036, 2},
         new int[]{8035, 1},
         new int[]{8055, 1},
         new int[]{8038, 1},
         new int[]{8053, 1},
         new int[]{8037, 1},
         new int[]{8045, 1}
      );
      WORLD_LIST[1] = new RainbowSpringsSiege.Word(
         "ALBATROS",
         new int[]{8035, 2},
         new int[]{8046, 1},
         new int[]{8036, 1},
         new int[]{8052, 1},
         new int[]{8050, 1},
         new int[]{8048, 1},
         new int[]{8051, 1}
      );
      WORLD_LIST[2] = new RainbowSpringsSiege.Word(
         "PELICAN", new int[]{8049, 1}, new int[]{8039, 1}, new int[]{8046, 1}, new int[]{8043, 1}, new int[]{8037, 1}, new int[]{8035, 1}, new int[]{8047, 1}
      );
      WORLD_LIST[3] = new RainbowSpringsSiege.Word(
         "KINGFISHER",
         new int[]{8045, 1},
         new int[]{8043, 1},
         new int[]{8047, 1},
         new int[]{8041, 1},
         new int[]{8040, 1},
         new int[]{8043, 1},
         new int[]{8051, 1},
         new int[]{8042, 1},
         new int[]{8039, 1},
         new int[]{8050, 1}
      );
      WORLD_LIST[4] = new RainbowSpringsSiege.Word(
         "CYGNUS", new int[]{8037, 1}, new int[]{8055, 1}, new int[]{8041, 1}, new int[]{8047, 1}, new int[]{8053, 1}, new int[]{8051, 1}
      );
      WORLD_LIST[5] = new RainbowSpringsSiege.Word("TRITON", new int[]{8052, 2}, new int[]{8050, 1}, new int[]{8043, 1}, new int[]{8047, 1});
      WORLD_LIST[6] = new RainbowSpringsSiege.Word(
         "RAINBOW", new int[]{8050, 1}, new int[]{8035, 1}, new int[]{8043, 1}, new int[]{8047, 1}, new int[]{8036, 1}, new int[]{8048, 1}, new int[]{8054, 1}
      );
      WORLD_LIST[7] = new RainbowSpringsSiege.Word(
         "SPRING", new int[]{8051, 1}, new int[]{8049, 1}, new int[]{8050, 1}, new int[]{8043, 1}, new int[]{8047, 1}, new int[]{8041, 1}
      );
   }

   protected final class ChestsSpawn implements Runnable {
      @Override
      public void run() {
         for(int i = 0; i < RainbowSpringsSiege._acceptedClans.size(); ++i) {
            if (RainbowSpringsSiege.this.arenaChestsCnt[i] < 4) {
               Npc chest = Quest.addSpawn(
                  35593,
                  RainbowSpringsSiege.CHESTS_SPAWN[i][0] + Quest.getRandom(-400, 400),
                  RainbowSpringsSiege.CHESTS_SPAWN[i][1] + Quest.getRandom(-400, 400),
                  RainbowSpringsSiege.CHESTS_SPAWN[i][2],
                  0,
                  false,
                  0L,
                  false
               );
               if (chest != null) {
                  RainbowSpringsSiege.this.chests.add(chest);
               }

               RainbowSpringsSiege.this.arenaChestsCnt[i]++;
            }

            if (RainbowSpringsSiege.this.arenaChestsCnt[i] < 4) {
               Npc chest = Quest.addSpawn(
                  35593,
                  RainbowSpringsSiege.CHESTS_SPAWN[i][0] + Quest.getRandom(-400, 400),
                  RainbowSpringsSiege.CHESTS_SPAWN[i][1] + Quest.getRandom(-400, 400),
                  RainbowSpringsSiege.CHESTS_SPAWN[i][2],
                  0,
                  false,
                  0L,
                  false
               );
               if (chest != null) {
                  RainbowSpringsSiege.this.chests.add(chest);
               }

               RainbowSpringsSiege.this.arenaChestsCnt[i]++;
            }

            if (RainbowSpringsSiege.this.arenaChestsCnt[i] < 4) {
               Npc chest = Quest.addSpawn(
                  35593,
                  RainbowSpringsSiege.CHESTS_SPAWN[i][0] + Quest.getRandom(-400, 400),
                  RainbowSpringsSiege.CHESTS_SPAWN[i][1] + Quest.getRandom(-400, 400),
                  RainbowSpringsSiege.CHESTS_SPAWN[i][2],
                  0,
                  false,
                  0L,
                  false
               );
               if (chest != null) {
                  RainbowSpringsSiege.this.chests.add(chest);
               }

               RainbowSpringsSiege.this.arenaChestsCnt[i]++;
            }

            if (RainbowSpringsSiege.this.arenaChestsCnt[i] < 4) {
               Npc chest = Quest.addSpawn(
                  35593,
                  RainbowSpringsSiege.CHESTS_SPAWN[i][0] + Quest.getRandom(-400, 400),
                  RainbowSpringsSiege.CHESTS_SPAWN[i][1] + Quest.getRandom(-400, 400),
                  RainbowSpringsSiege.CHESTS_SPAWN[i][2],
                  0,
                  false,
                  0L,
                  false
               );
               if (chest != null) {
                  RainbowSpringsSiege.this.chests.add(chest);
               }

               RainbowSpringsSiege.this.arenaChestsCnt[i]++;
            }
         }
      }
   }

   protected final class GenerateTask implements Runnable {
      protected final Npc _npc;

      protected GenerateTask(Npc npc) {
         this._npc = npc;
      }

      @Override
      public void run() {
         RainbowSpringsSiege._generated = Quest.getRandom(RainbowSpringsSiege.WORLD_LIST.length);
         RainbowSpringsSiege.Word word = RainbowSpringsSiege.WORLD_LIST[RainbowSpringsSiege._generated];
         ExShowScreenMessage msg = new ExShowScreenMessage(word.getName(), 5000);
         int region = MapRegionManager.getInstance().getMapRegionLocId(this._npc.getX(), this._npc.getY());

         for(Player player : World.getInstance().getAllPlayers()) {
            if (region == MapRegionManager.getInstance().getMapRegionLocId(player.getX(), player.getY()) && Util.checkIfInRange(750, this._npc, player, false)
               )
             {
               player.sendPacket(msg);
            }
         }
      }
   }

   private static class Word {
      private final String _name;
      private final int[][] _items;

      public Word(String name, int[]... items) {
         this._name = name;
         this._items = items;
      }

      public String getName() {
         return this._name;
      }

      public int[][] getItems() {
         return this._items;
      }
   }
}

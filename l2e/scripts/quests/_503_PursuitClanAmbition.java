package l2e.scripts.quests;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _503_PursuitClanAmbition extends Quest {
   private static final String qn = "_503_PursuitClanAmbition";
   private static int ImpGraveKepperStat = 1;
   private static final int G_Let_Martien = 3866;
   private static final int Th_Wyrm_Eggs = 3842;
   private static final int Drake_Eggs = 3841;
   private static final int Bl_Wyrm_Eggs = 3840;
   private static final int Mi_Drake_Eggs = 3839;
   private static final int Brooch = 3843;
   private static final int Bl_Anvil_Coin = 3871;
   private static final int G_Let_Balthazar = 3867;
   private static final int Spiteful_Soul_Energy = 14855;
   private static final int G_Let_Rodemai = 3868;
   private static final int Imp_Keys = 3847;
   private static final int Scepter_Judgement = 3869;
   private static final int Proof_Aspiration = 3870;
   private static final int[] EggList = new int[]{3839, 3840, 3841, 3842};
   private static final int[] NPC = new int[]{30645, 30758, 30759, 30760, 30761, 30762, 30763, 30512, 30764, 30868, 30765, 30766};
   private static final String[] STATS = new String[]{"cond", "Fritz", "Lutz", "Kurtz", "ImpGraveKeeper"};
   private static final TIntObjectHashMap<_503_PursuitClanAmbition.dropList> drop = new TIntObjectHashMap<>();

   public _503_PursuitClanAmbition(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30760);

      for(int npcId : NPC) {
         this.addTalkId(npcId);
      }

      drop.put(20282, new _503_PursuitClanAmbition.dropList(2, 10, 20, new int[]{3842}));
      drop.put(20243, new _503_PursuitClanAmbition.dropList(2, 10, 15, new int[]{3842}));
      drop.put(20137, new _503_PursuitClanAmbition.dropList(2, 10, 20, new int[]{3841}));
      drop.put(20285, new _503_PursuitClanAmbition.dropList(2, 10, 25, new int[]{3841}));
      drop.put(27178, new _503_PursuitClanAmbition.dropList(2, 10, 100, new int[]{3840}));
      drop.put(20974, new _503_PursuitClanAmbition.dropList(5, 10, 20, new int[]{14855}));
      drop.put(20668, new _503_PursuitClanAmbition.dropList(10, 0, 15, new int[0]));
      drop.put(27179, new _503_PursuitClanAmbition.dropList(10, 6, 80, new int[]{3847}));
      drop.put(27181, new _503_PursuitClanAmbition.dropList(10, 0, 100, new int[0]));
      this.addAttackId(27181);
      this.addKillId(20974);

      for(int mobId : drop.keys()) {
         this.addKillId(mobId);
      }

      this.questItemIds = new int[]{14855};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_503_PursuitClanAmbition");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30760-08.htm")) {
            st.giveItems(3866, 1L);
            st.set("cond", "1");

            for(String var : STATS) {
               st.set(var, "1");
            }

            st.setState((byte)1);
         } else if (event.equalsIgnoreCase("30760-12.htm")) {
            st.giveItems(3867, 1L);
            st.set("cond", "4");
         } else if (event.equalsIgnoreCase("30760-16.htm")) {
            st.giveItems(3868, 1L);
            st.set("cond", "7");
         } else if (event.equalsIgnoreCase("30760-20.htm")) {
            this.exit(true, st);
         } else if (event.equalsIgnoreCase("30760-22.htm")) {
            st.set("cond", "13");
         } else if (event.equalsIgnoreCase("30760-23.htm")) {
            this.exit(true, st);
         } else if (event.equalsIgnoreCase("30645-03.htm")) {
            st.takeItems(3866, -1L);
            st.set("cond", "2");
            this.suscribeMembers(st);

            for(Player i : player.getClan().getOnlineMembers(player.getObjectId())) {
               st = this.newQuestState(i);
               st.setState((byte)1);
            }
         } else if (event.equalsIgnoreCase("30763-03.htm")) {
            if (st.getInt("Kurtz") == 1) {
               st.giveItems(3839, 6L);
               st.giveItems(3843, 1L);
               st.set("Kurtz", "2");
               return "30763-02.htm";
            }
         } else if (event.equalsIgnoreCase("30762-03.htm")) {
            addSpawn(27178, npc.getX() + 50, npc.getY() + 50, npc.getZ(), 0, false, 120000L);
            addSpawn(27178, npc.getX() - 50, npc.getY() - 50, npc.getZ(), 0, false, 120000L);
            if (st.getInt("Lutz") == 1) {
               st.giveItems(3839, 4L);
               st.giveItems(3840, 3L);
               st.set("Lutz", "2");
               return "30762-02.htm";
            }
         } else if (event.equalsIgnoreCase("30761-03.htm")) {
            addSpawn(27178, npc.getX() + 50, npc.getY() + 50, npc.getZ(), 0, false, 120000L);
            addSpawn(27178, npc.getX() - 50, npc.getY() - 50, npc.getZ(), 0, false, 120000L);
            if (st.getInt("Fritz") == 1) {
               st.giveItems(3840, 3L);
               st.set("Fritz", "2");
               return "30761-02.htm";
            }
         } else if (event.equalsIgnoreCase("30512-03.htm")) {
            st.takeItems(3843, -1L);
            st.giveItems(3871, 1L);
            st.set("Kurtz", "3");
         } else if (event.equalsIgnoreCase("30764-03.htm")) {
            st.takeItems(3867, -1L);
            st.set("cond", "5");
            st.set("Kurtz", "3");
         } else if (event.equalsIgnoreCase("30764-05.htm")) {
            st.takeItems(3867, -1L);
            st.set("cond", "5");
         } else if (event.equalsIgnoreCase("30764-06.htm")) {
            st.takeItems(3871, -1L);
            st.set("Kurtz", "4");
         } else if (event.equalsIgnoreCase("30868-04.htm")) {
            st.takeItems(3868, -1L);
            st.set("cond", "8");
         } else if (event.equalsIgnoreCase("30868-06a.htm")) {
            st.set("cond", "10");
         } else if (event.equalsIgnoreCase("30868-10.htm")) {
            st.set("cond", "12");
         } else if (event.equalsIgnoreCase("30766-04.htm")) {
            st.set("cond", "9");
            Npc spawnedNpc = addSpawn(30766, 160622, 21230, -3710, 0, false, 90000L);
            spawnedNpc.broadcastPacket(new NpcSay(spawnedNpc.getObjectId(), 0, spawnedNpc.getId(), NpcStringId.BLOOD_AND_HONOR), 2000);
            spawnedNpc = st.addSpawn(30759, 160665, 21209, -3710, 0, false, 90000);
            spawnedNpc.broadcastPacket(new NpcSay(spawnedNpc.getObjectId(), 0, spawnedNpc.getId(), NpcStringId.AMBITION_AND_POWER), 2000);
            spawnedNpc = st.addSpawn(30758, 160665, 21291, -3710, 0, false, 90000);
            spawnedNpc.broadcastPacket(new NpcSay(spawnedNpc.getObjectId(), 0, spawnedNpc.getId(), NpcStringId.WAR_AND_DEATH), 2000);
         } else if (event.equalsIgnoreCase("30766-08.htm")) {
            st.takeItems(3869, -1L);
            this.exit(false, st);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_503_PursuitClanAmbition");
      if (st == null) {
         return htmltext;
      } else {
         boolean isLeader = player.isClanLeader();
         int npcId = npc.getId();
         int kurtz = st.getInt("Kurtz");
         int lutz = st.getInt("Lutz");
         int fritz = st.getInt("Fritz");
         switch(st.getState()) {
            case 0:
               if (npcId == 30760) {
                  for(String var : STATS) {
                     st.set(var, "0");
                  }

                  if (player.getClan() != null && player.getClan().getLevel() >= 5) {
                     return htmltext;
                  }

                  if (player.getClan() != null) {
                     if (isLeader) {
                        int clanLevel = player.getClan().getLevel();
                        if (st.getQuestItemsCount(3870) > 0L) {
                           htmltext = "30760-03.htm";
                           st.exitQuest(true);
                        } else if (clanLevel == 4) {
                           htmltext = "30760-04.htm";
                        } else {
                           htmltext = "30760-02.htm";
                           st.exitQuest(true);
                        }
                     } else {
                        htmltext = "30760-04t.htm";
                        st.exitQuest(true);
                     }
                  } else {
                     htmltext = "30760-01.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (isLeader) {
                  int cond = st.getInt("cond");
                  if (cond == 0) {
                     st.set("cond", "1");
                  }

                  if (st.get("Kurtz") == null) {
                     st.set("Kurtz", "1");
                  }

                  if (st.get("Lutz") == null) {
                     st.set("Lutz", "1");
                  }

                  if (st.get("Fritz") == null) {
                     st.set("Fritz", "1");
                  }

                  if (npcId == 30760) {
                     if (cond == 1) {
                        htmltext = "30760-09.htm";
                     } else if (cond == 2) {
                        htmltext = "30760-10.htm";
                     } else if (cond == 3) {
                        htmltext = "30760-11.htm";
                     } else if (cond == 4) {
                        htmltext = "30760-13.htm";
                     } else if (cond == 5) {
                        htmltext = "30760-14.htm";
                     } else if (cond == 6) {
                        htmltext = "30760-15.htm";
                     } else if (cond == 7) {
                        htmltext = "30760-17.htm";
                     } else if (cond == 12) {
                        htmltext = "30760-19.htm";
                     } else if (cond == 13) {
                        htmltext = "30760-24.htm";
                     } else {
                        htmltext = "30760-18.htm";
                     }
                  } else if (npcId == 30645) {
                     if (cond == 1) {
                        htmltext = "30645-02.htm";
                     } else if (cond == 2) {
                        if (this.checkEggs(st) && kurtz > 1 && lutz > 1 && fritz > 1) {
                           htmltext = "30645-05.htm";
                           st.set("cond", "3");

                           for(int item : EggList) {
                              st.takeItems(item, -1L);
                           }
                        } else {
                           htmltext = "30645-04.htm";
                        }
                     } else if (cond == 3) {
                        htmltext = "30645-07.htm";
                     } else {
                        htmltext = "30645-08.htm";
                     }
                  } else if (npcId == 30762 && cond == 2) {
                     htmltext = "30762-01.htm";
                  } else if (npcId == 30763 && cond == 2) {
                     htmltext = "30763-01.htm";
                  } else if (npcId == 30761 && cond == 2) {
                     htmltext = "30761-01.htm";
                  } else if (npcId == 30512) {
                     if (kurtz == 1) {
                        htmltext = "30512-01.htm";
                     } else if (kurtz == 2) {
                        htmltext = "30512-02.htm";
                     } else {
                        htmltext = "30512-04.htm";
                     }
                  } else if (npcId == 30764) {
                     if (cond == 4) {
                        if (kurtz > 2) {
                           htmltext = "30764-04.htm";
                        } else {
                           htmltext = "30764-02.htm";
                        }
                     } else if (cond == 5) {
                        if (st.getQuestItemsCount(14855) > 9L) {
                           htmltext = "30764-08.htm";
                           st.takeItems(14855, -1L);
                           st.takeItems(3843, -1L);
                           st.set("cond", "6");
                        } else {
                           htmltext = "30764-07.htm";
                        }
                     } else if (cond == 6) {
                        htmltext = "30764-09.htm";
                     }
                  } else if (npcId == 30868) {
                     if (cond == 7) {
                        htmltext = "30868-02.htm";
                     } else if (cond == 8) {
                        htmltext = "30868-05.htm";
                     } else if (cond == 9) {
                        htmltext = "30868-06.htm";
                     } else if (cond == 10) {
                        htmltext = "30868-08.htm";
                     } else if (cond == 11) {
                        htmltext = "30868-09.htm";
                     } else if (cond == 12) {
                        htmltext = "30868-11.htm";
                     }
                  } else if (npcId == 30766) {
                     if (cond == 8) {
                        htmltext = "30766-02.htm";
                     } else if (cond == 9) {
                        htmltext = "30766-05.htm";
                     } else if (cond == 10) {
                        htmltext = "30766-06.htm";
                     } else if (cond == 11 || cond == 12 || cond == 13) {
                        htmltext = "30766-07.htm";
                     }
                  } else if (npcId == 30765) {
                     if (cond == 10) {
                        if (st.getQuestItemsCount(3847) < 6L) {
                           htmltext = "30765-03a.htm";
                        } else if (st.getInt("ImpGraveKeeper") == 3) {
                           htmltext = "30765-02.htm";
                           st.set("cond", "11");
                           st.takeItems(3847, 6L);
                           st.giveItems(3869, 1L);
                        } else {
                           htmltext = "30765-02a.htm";
                        }
                     } else {
                        htmltext = "30765-03b.htm";
                     }
                  } else if (npcId == 30759) {
                     htmltext = "30759-01.htm";
                  } else if (npcId == 30758) {
                     htmltext = "30758-01.htm";
                  }
               } else {
                  int cond = this.getLeaderVar(st, "cond");
                  if (npcId != 30645 || cond != 1 && cond != 2 && cond != 3) {
                     if (npcId == 30868) {
                        if (cond == 9 || cond == 10) {
                           htmltext = "30868-07.htm";
                        } else if (cond == 7) {
                           htmltext = "30868-01.htm";
                        }
                     } else if (npcId == 30764 && cond == 4) {
                        htmltext = "30764-01.htm";
                     } else if (npcId == 30766 && cond == 8) {
                        htmltext = "30766-01.htm";
                     } else if (npcId == 30512 && cond < 6 && cond > 2) {
                        htmltext = "30512-01a.htm";
                     } else if (npcId == 30765 && cond == 10) {
                        htmltext = "30765-01.htm";
                     } else if (npcId == 30760) {
                        if (cond == 3) {
                           htmltext = "30760-11t.htm";
                        } else if (cond == 4) {
                           htmltext = "30760-15t.htm";
                        } else if (cond == 12) {
                           htmltext = "30760-19t.htm";
                        } else if (cond == 13) {
                           htmltext = "30766-24t.htm";
                        }
                     }
                  } else {
                     htmltext = "30645-01.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onAttack(Npc npc, Player player, int damage, boolean isSummon, Skill skill) {
      if (npc.getMaxHp() / 2.0 > npc.getCurrentHp() && getRandom(100) < 4) {
         if (ImpGraveKepperStat == 1) {
            for(int i = 0; i < 19; ++i) {
               int x = (int)(100.0 * Math.cos((double)i * 0.785));
               int y = (int)(100.0 * Math.sin((double)i * 0.785));
               addSpawn(27180, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0L);
            }

            ImpGraveKepperStat = 2;
         } else {
            Collection<Player> plrs = World.getInstance().getAroundPlayers(npc);
            if (!plrs.isEmpty()) {
               Player playerToTP = (Player)plrs.toArray()[getRandom(plrs.size())];
               playerToTP.teleToLocation(185462, 20342, -3250, true);
            }
         }
      }

      return super.onAttack(npc, player, damage, isSummon, skill);
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      int npcId = npc.getId();
      QuestState leader_st = null;
      if (player.isClanLeader()) {
         leader_st = player.getQuestState("_503_PursuitClanAmbition");
      } else {
         Clan clan = player.getClan();
         if (clan != null && clan.getLeader() != null) {
            Player leader = clan.getLeader().getPlayerInstance();
            if (leader != null && player.isInsideRadius(leader, 2000, false, false)) {
               leader_st = leader.getQuestState("_503_PursuitClanAmbition");
            }
         }
      }

      if (leader_st != null) {
         if (leader_st.getState() != 1) {
            return super.onKill(npc, player, isSummon);
         }

         _503_PursuitClanAmbition.dropList droplist;
         synchronized(drop) {
            droplist = drop.get(npcId);
         }

         int cond = leader_st.getInt("cond");
         if (cond == droplist.cond && getRandom(100) < droplist.chance) {
            if (droplist.items.length > 0) {
               this.giveItem(droplist.items[0], droplist.maxcount, leader_st);
            } else if (npcId == 27181) {
               Npc spawnedNpc = leader_st.addSpawn(30765, 120000);
               npc.broadcastPacket(
                  new NpcSay(spawnedNpc.getObjectId(), 0, spawnedNpc.getId(), NpcStringId.CURSE_OF_THE_GODS_ON_THE_ONE_THAT_DEFILES_THE_PROPERTY_OF_THE_EMPIRE),
                  2000
               );
               leader_st.set("ImpGraveKeeper", "3");
               ImpGraveKepperStat = 1;
            } else {
               leader_st.addSpawn(27179);
            }
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   private void exit(boolean complete, QuestState st) {
      if (complete) {
         st.giveItems(3870, 1L);
         st.addExpAndSp(0, 250000);

         for(String var : STATS) {
            st.unset(var);
         }

         st.exitQuest(false);
      } else {
         st.exitQuest(true);
      }

      st.takeItems(3869, -1L);

      try {
         for(Player i : st.getPlayer().getClan().getOnlineMembers(0)) {
            if (i != null) {
               QuestState qs = i.getQuestState("_503_PursuitClanAmbition");
               if (qs != null) {
                  qs.exitQuest(true);
               }
            }
         }

         this.offlineMemberExit(st);
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   private void offlineMemberExit(QuestState st) {
      int clan = st.getPlayer().getClan().getId();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement offline = con.prepareStatement(
            "DELETE FROM character_quests WHERE name = ? and charId IN (SELECT charId FROM characters WHERE clanId =? AND online=0)"
         );
      ) {
         offline.setString(1, "_503_PursuitClanAmbition");
         offline.setInt(2, clan);
         offline.execute();
      } catch (Exception var35) {
         var35.printStackTrace();
      }
   }

   private void suscribeMembers(QuestState st) {
      int clan = st.getPlayer().getClan().getId();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement offline = con.prepareStatement("SELECT charId FROM characters WHERE clanid=? AND online=0");
      ) {
         PreparedStatement insertion = con.prepareStatement("REPLACE INTO character_quests (charId,name,var,value) VALUES (?,?,?,?)");
         offline.setInt(1, clan);
         ResultSet rs = offline.executeQuery();

         while(rs.next()) {
            int charId = rs.getInt("charId");

            try {
               insertion.setInt(1, charId);
               insertion.setString(2, "_503_PursuitClanAmbition");
               insertion.setString(3, "<state>");
               insertion.setString(4, "Started");
               insertion.executeUpdate();
            } catch (Exception var36) {
               var36.printStackTrace();
            }
         }
      } catch (Exception var41) {
         var41.printStackTrace();
      }
   }

   private int getLeaderVar(QuestState st, String var) {
      int val = -1;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         Clan clan = st.getPlayer().getClan();
         if (clan == null) {
            return -1;
         } else {
            Player leader = clan.getLeader().getPlayerInstance();
            if (leader == null) {
               PreparedStatement offline = con.prepareStatement("SELECT value FROM character_quests WHERE charId=? AND var=? AND name=?");
               offline.setInt(1, st.getPlayer().getClan().getLeaderId());
               offline.setString(2, var);
               offline.setString(3, "_503_PursuitClanAmbition");
               ResultSet rs = offline.executeQuery();

               while(rs.next()) {
                  val = rs.getInt("value");
               }

               return val;
            } else {
               return leader.getQuestState("_503_PursuitClanAmbition").getInt(var);
            }
         }
      } catch (Exception var22) {
         System.out.println("Pursuit of Clan Ambition: cannot read quest states offline clan leader");
         return val;
      }
   }

   private boolean checkEggs(QuestState st) {
      int count = 0;

      for(int item : EggList) {
         if (st.getQuestItemsCount(item) > 9L) {
            ++count;
         }
      }

      return count > 3;
   }

   private void giveItem(int item, int maxcount, QuestState st) {
      long count = st.getQuestItemsCount(item);
      if (count < (long)maxcount) {
         st.giveItems(item, 1L);
         if (count == (long)(maxcount - 1)) {
            st.playSound("ItemSound.quest_middle");
         } else {
            st.playSound("ItemSound.quest_itemget");
         }
      }
   }

   public static void main(String[] args) {
      new _503_PursuitClanAmbition(503, "_503_PursuitClanAmbition", "");
   }

   private class dropList {
      public int cond;
      public int maxcount;
      public int chance;
      public int[] items;

      protected dropList(int _cond, int _maxcount, int _chance, int[] _items) {
         this.cond = _cond;
         this.maxcount = _maxcount;
         this.chance = _chance;
         this.items = _items;
      }
   }
}

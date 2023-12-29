package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcSay;

public abstract class SagasSuperClass extends Quest {
   protected int[] NPC;
   protected int[] Items;
   protected int[] Mob;
   protected int[] classid;
   protected int[] prevclass;
   protected Location[] npcSpawnLocations;
   protected String[] Text;
   private static final Map<Npc, Integer> _spawnList = new HashMap<>();
   private static int[][] QuestClass = new int[][]{
      {127},
      {128, 129},
      {130},
      {5},
      {20},
      {21},
      {2},
      {3},
      {46},
      {48},
      {51},
      {52},
      {8},
      {23},
      {36},
      {9},
      {24},
      {37},
      {16},
      {17},
      {30},
      {12},
      {27},
      {40},
      {14},
      {28},
      {41},
      {13},
      {6},
      {34},
      {33},
      {43},
      {55},
      {57}
   };

   public SagasSuperClass(int id, String name, String descr) {
      super(id, name, descr);
   }

   public void registerNPCs() {
      this.addStartNpc(this.NPC[0]);
      this.addAttackId(this.Mob[2]);
      this.addAttackId(this.Mob[1]);
      this.addSkillSeeId(new int[]{this.Mob[1]});
      this.addFirstTalkId(this.NPC[4]);

      for(int npc : this.NPC) {
         this.addTalkId(npc);
      }

      for(int mobid : this.Mob) {
         this.addKillId(mobid);
      }

      this.questItemIds = (int[])this.Items.clone();
      this.questItemIds[0] = 0;
      this.questItemIds[2] = 0;

      for(int Archon_Minion = 21646; Archon_Minion < 21652; ++Archon_Minion) {
         this.addKillId(Archon_Minion);
      }

      int[] Archon_Hellisha_Norm = new int[]{18212, 18214, 18215, 18216, 18218};

      for(int element : Archon_Hellisha_Norm) {
         this.addKillId(element);
      }

      for(int Guardian_Angel = 27214; Guardian_Angel < 27217; ++Guardian_Angel) {
         this.addKillId(Guardian_Angel);
      }
   }

   private static void cast(Npc npc, Creature target, int skillId, int level) {
      target.broadcastPacket(new MagicSkillUse(target, target, skillId, level, 6000, 1));
      target.broadcastPacket(new MagicSkillUse(npc, npc, skillId, level, 6000, 1));
   }

   private static void autoChat(Npc npc, String text) {
      npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), text), 2000);
   }

   private static void addSpawn(QuestState st, Npc mob) {
      _spawnList.put(mob, st.getPlayer().getObjectId());
   }

   private static Npc FindSpawn(Player player, Npc npc) {
      return _spawnList.containsKey(npc) && _spawnList.get(npc) == player.getObjectId() ? npc : null;
   }

   private static void DeleteSpawn(QuestState st, Npc npc) {
      if (_spawnList.containsKey(npc)) {
         _spawnList.remove(npc);
         npc.deleteMe();
      }
   }

   private QuestState findRightState(Npc npc) {
      Player player = null;
      QuestState st = null;
      if (_spawnList.containsKey(npc)) {
         player = World.getInstance().getPlayer(_spawnList.get(npc));
         if (player != null) {
            st = player.getQuestState(this.getName());
         }
      }

      return st;
   }

   private void giveHalishaMark(QuestState st2) {
      if (st2.getInt("spawned") == 0) {
         if (st2.getQuestItemsCount(this.Items[3]) >= 700L) {
            st2.takeItems(this.Items[3], 20L);
            int xx = st2.getPlayer().getX();
            int yy = st2.getPlayer().getY();
            int zz = st2.getPlayer().getZ();
            Npc Archon = st2.addSpawn(this.Mob[1], xx, yy, zz);
            addSpawn(st2, Archon);
            st2.set("spawned", "1");
            st2.startQuestTimer("Archon Hellisha has despawned", 600000L, Archon);
            autoChat(Archon, this.Text[13].replace("PLAYERNAME", st2.getPlayer().getName()));
            ((Attackable)Archon).addDamageHate(st2.getPlayer(), 0, 99999);
            Archon.getAI().setIntention(CtrlIntention.ATTACK, st2.getPlayer(), null);
         } else {
            st2.giveItems(this.Items[3], (long)getRandom(1, 4));
         }
      }
   }

   private QuestState findQuest(Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null) {
         if (this.getId() == 68) {
            for(int q = 0; q < 2; ++q) {
               if (player.getClassId().getId() == QuestClass[1][q]) {
                  return st;
               }
            }
         } else if (player.getClassId().getId() == QuestClass[this.getId() - 67][0]) {
            return st;
         }
      }

      return null;
   }

   public int getClassId(Player player) {
      return player.getClassId().getId() == 129 ? this.classid[1] : this.classid[0];
   }

   public int getPrevClass(Player player) {
      if (player.getClassId().getId() == 129) {
         return this.prevclass.length == 1 ? -1 : this.prevclass[1];
      } else {
         return this.prevclass[0];
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      String htmltext = null;
      if (st != null) {
         switch(event) {
            case "0-011.htm":
            case "0-012.htm":
            case "0-013.htm":
            case "0-014.htm":
            case "0-015.htm":
               htmltext = event;
               break;
            case "accept":
               st.startQuest();
               giveItems(player, this.Items[10], 1L);
               htmltext = "0-03.htm";
               break;
            case "0-1":
               if (player.getLevel() < 76) {
                  htmltext = "0-02.htm";
                  if (st.isCreated()) {
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "0-05.htm";
               }
               break;
            case "0-2":
               if (player.getLevel() < 76) {
                  takeItems(player, this.Items[10], -1L);
                  st.setCond(20, true);
                  htmltext = "0-08.htm";
               } else {
                  takeItems(player, this.Items[10], -1L);
                  addExpAndSp(player, 2299404L, 0);
                  this.giveAdena(player, 5000000L, true);
                  giveItems(player, 6622, 1L);
                  int Class = this.getClassId(player);
                  int prevClass = this.getPrevClass(player);
                  player.setClassId(Class);
                  if (!player.isSubClassActive() && player.getBaseClass() == prevClass) {
                     player.setBaseClass(Class);
                  }

                  player.broadcastCharInfo();
                  cast(npc, player, 4339, 1);
                  st.exitQuest(false);
                  htmltext = "0-07.htm";
               }
               break;
            case "1-3":
               st.setCond(3);
               htmltext = "1-05.htm";
               break;
            case "1-4":
               st.setCond(4);
               takeItems(player, this.Items[0], 1L);
               if (this.Items[11] != 0) {
                  takeItems(player, this.Items[11], 1L);
               }

               giveItems(player, this.Items[1], 1L);
               htmltext = "1-06.htm";
               break;
            case "2-1":
               st.setCond(2);
               htmltext = "2-05.htm";
               break;
            case "2-2":
               st.setCond(5);
               takeItems(player, this.Items[1], 1L);
               giveItems(player, this.Items[4], 1L);
               htmltext = "2-06.htm";
               break;
            case "3-5":
               htmltext = "3-07.htm";
               break;
            case "3-6":
               st.setCond(11);
               htmltext = "3-02.htm";
               break;
            case "3-7":
               st.setCond(12);
               htmltext = "3-03.htm";
               break;
            case "3-8":
               st.setCond(13);
               takeItems(player, this.Items[2], 1L);
               giveItems(player, this.Items[7], 1L);
               htmltext = "3-08.htm";
               break;
            case "4-1":
               htmltext = "4-010.htm";
               break;
            case "4-2":
               giveItems(player, this.Items[9], 1L);
               st.setCond(18, true);
               htmltext = "4-011.htm";
               break;
            case "4-3":
               giveItems(player, this.Items[9], 1L);
               st.setCond(18, true);
               autoChat(npc, this.Text[13].replace("PLAYERNAME", player.getName()));
               st.set("Quest0", "0");
               this.cancelQuestTimer("Mob_2 has despawned", npc, player);
               DeleteSpawn(st, npc);
               return null;
            case "5-1":
               st.setCond(6, true);
               takeItems(player, this.Items[4], 1L);
               cast(npc, player, 4546, 1);
               htmltext = "5-02.htm";
               break;
            case "6-1":
               st.setCond(8, true);
               takeItems(player, this.Items[5], 1L);
               cast(npc, player, 4546, 1);
               htmltext = "6-03.htm";
               break;
            case "7-1":
               if (st.getInt("spawned") == 1) {
                  htmltext = "7-03.htm";
               } else if (st.getInt("spawned") == 0) {
                  Npc Mob_1 = st.addSpawn(this.Mob[0], this.npcSpawnLocations[0].getX(), this.npcSpawnLocations[0].getY(), this.npcSpawnLocations[0].getZ());
                  st.set("spawned", "1");
                  st.startQuestTimer("Mob_1 Timer 1", 500L, Mob_1);
                  st.startQuestTimer("Mob_1 has despawned", 300000L, Mob_1);
                  addSpawn(st, Mob_1);
                  htmltext = "7-02.htm";
               } else {
                  htmltext = "7-04.htm";
               }
               break;
            case "7-2":
               st.setCond(10, true);
               takeItems(player, this.Items[6], 1L);
               cast(npc, player, 4546, 1);
               htmltext = "7-06.htm";
               break;
            case "8-1":
               st.setCond(14, true);
               takeItems(player, this.Items[7], 1L);
               cast(npc, player, 4546, 1);
               htmltext = "8-02.htm";
               break;
            case "9-1":
               st.setCond(17, true);
               takeItems(player, this.Items[8], 1L);
               cast(npc, player, 4546, 1);
               htmltext = "9-03.htm";
               break;
            case "10-1":
               if (st.getInt("Quest0") == 0) {
                  Npc Mob_3 = st.addSpawn(this.Mob[2], this.npcSpawnLocations[1].getX(), this.npcSpawnLocations[1].getY(), this.npcSpawnLocations[1].getZ());
                  Npc Mob_2 = st.addSpawn(this.NPC[4], this.npcSpawnLocations[2].getX(), this.npcSpawnLocations[2].getY(), this.npcSpawnLocations[2].getZ());
                  addSpawn(st, Mob_3);
                  addSpawn(st, Mob_2);
                  st.set("Mob_2", String.valueOf(Mob_2.getObjectId()));
                  st.set("Quest0", "1");
                  st.set("Quest1", "45");
                  st.startRepeatingQuestTimer("Mob_3 Timer 1", 500L, Mob_3);
                  st.startQuestTimer("Mob_3 has despawned", 59000L, Mob_3);
                  st.startQuestTimer("Mob_2 Timer 1", 500L, Mob_2);
                  st.startQuestTimer("Mob_2 has despawned", 60000L, Mob_2);
                  htmltext = "10-02.htm";
               } else if (st.getInt("Quest1") == 45) {
                  htmltext = "10-03.htm";
               } else {
                  htmltext = "10-04.htm";
               }
               break;
            case "10-2":
               st.setCond(19, true);
               takeItems(player, this.Items[9], 1L);
               cast(npc, player, 4546, 1);
               htmltext = "10-06.htm";
               break;
            case "11-9":
               st.setCond(15);
               htmltext = "11-03.htm";
               break;
            case "Mob_1 Timer 1":
               autoChat(npc, this.Text[0].replace("PLAYERNAME", player.getName()));
               return null;
            case "Mob_1 has despawned":
               autoChat(npc, this.Text[1].replace("PLAYERNAME", player.getName()));
               st.set("spawned", "0");
               DeleteSpawn(st, npc);
               return null;
            case "Archon Hellisha has despawned":
               autoChat(npc, this.Text[6].replace("PLAYERNAME", player.getName()));
               st.set("spawned", "0");
               DeleteSpawn(st, npc);
               return null;
            case "Mob_3 Timer 1":
               Npc Mob_2 = FindSpawn(player, (Npc)World.getInstance().findObject(st.getInt("Mob_2")));
               if (World.getInstance().getAroundNpc(npc).contains(Mob_2)) {
                  ((Attackable)npc).addDamageHate(Mob_2, 0, 99999);
                  npc.getAI().setIntention(CtrlIntention.ATTACK, Mob_2, null);
                  Mob_2.getAI().setIntention(CtrlIntention.ATTACK, npc, null);
                  autoChat(npc, this.Text[14].replace("PLAYERNAME", player.getName()));
                  this.cancelQuestTimer("Mob_3 Timer 1", npc, player);
               }

               return null;
            case "Mob_3 has despawned":
               autoChat(npc, this.Text[15].replace("PLAYERNAME", player.getName()));
               st.set("Quest0", "2");
               DeleteSpawn(st, npc);
               return null;
            case "Mob_2 Timer 1":
               autoChat(npc, this.Text[7].replace("PLAYERNAME", player.getName()));
               st.startQuestTimer("Mob_2 Timer 2", 1500L, npc);
               if (st.getInt("Quest1") == 45) {
                  st.set("Quest1", "0");
               }

               return null;
            case "Mob_2 Timer 2":
               autoChat(npc, this.Text[8].replace("PLAYERNAME", player.getName()));
               st.startQuestTimer("Mob_2 Timer 3", 10000L, npc);
               return null;
            case "Mob_2 Timer 3":
               if (st.getInt("Quest0") == 0) {
                  st.startQuestTimer("Mob_2 Timer 3", 13000L, npc);
                  if (getRandom(2) == 0) {
                     autoChat(npc, this.Text[9].replace("PLAYERNAME", player.getName()));
                  } else {
                     autoChat(npc, this.Text[10].replace("PLAYERNAME", player.getName()));
                  }
               }

               return null;
            case "Mob_2 has despawned":
               st.set("Quest1", String.valueOf(st.getInt("Quest1") + 1));
               if (st.getInt("Quest0") != 1 && st.getInt("Quest0") != 2 && st.getInt("Quest1") <= 3) {
                  st.startQuestTimer("Mob_2 has despawned", 1000L, npc);
               } else {
                  st.set("Quest0", "0");
                  if (st.getInt("Quest0") == 1) {
                     autoChat(npc, this.Text[11].replace("PLAYERNAME", player.getName()));
                  } else {
                     autoChat(npc, this.Text[12].replace("PLAYERNAME", player.getName()));
                  }

                  DeleteSpawn(st, npc);
               }

               return null;
         }
      }

      return htmltext;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st != null) {
         int npcId = npc.getId();
         if (npcId == this.NPC[0] && st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (player.getClassId().getId() == this.getPrevClass(player)) {
            switch(st.getCond()) {
               case 0:
                  if (npcId == this.NPC[0]) {
                     htmltext = "0-01.htm";
                  }
                  break;
               case 1:
                  if (npcId == this.NPC[0]) {
                     htmltext = "0-04.htm";
                  } else if (npcId == this.NPC[2]) {
                     htmltext = "2-01.htm";
                  }
                  break;
               case 2:
                  if (npcId == this.NPC[2]) {
                     htmltext = "2-02.htm";
                  } else if (npcId == this.NPC[1]) {
                     htmltext = "1-01.htm";
                  }
                  break;
               case 3:
                  if (npcId == this.NPC[1] && hasQuestItems(player, this.Items[0])) {
                     if (this.Items[11] != 0 && !hasQuestItems(player, this.Items[11])) {
                        htmltext = "1-02.htm";
                     } else {
                        htmltext = "1-03.htm";
                     }
                  }
                  break;
               case 4:
                  if (npcId == this.NPC[1]) {
                     htmltext = "1-04.htm";
                  } else if (npcId == this.NPC[2]) {
                     htmltext = "2-03.htm";
                  }
                  break;
               case 5:
                  if (npcId == this.NPC[2]) {
                     htmltext = "2-04.htm";
                  } else if (npcId == this.NPC[5]) {
                     htmltext = "5-01.htm";
                  }
                  break;
               case 6:
                  if (npcId == this.NPC[5]) {
                     htmltext = "5-03.htm";
                  } else if (npcId == this.NPC[6]) {
                     htmltext = "6-01.htm";
                  }
                  break;
               case 7:
                  if (npcId == this.NPC[6]) {
                     htmltext = "6-02.htm";
                  }
                  break;
               case 8:
                  if (npcId == this.NPC[6]) {
                     htmltext = "6-04.htm";
                  } else if (npcId == this.NPC[7]) {
                     htmltext = "7-01.htm";
                  }
                  break;
               case 9:
                  if (npcId == this.NPC[7]) {
                     htmltext = "7-05.htm";
                  }
                  break;
               case 10:
                  if (npcId == this.NPC[7]) {
                     htmltext = "7-07.htm";
                  } else if (npcId == this.NPC[3]) {
                     htmltext = "3-01.htm";
                  }
                  break;
               case 11:
               case 12:
                  if (npcId == this.NPC[3]) {
                     if (hasQuestItems(player, this.Items[2])) {
                        htmltext = "3-05.htm";
                     } else {
                        htmltext = "3-04.htm";
                     }
                  }
                  break;
               case 13:
                  if (npcId == this.NPC[3]) {
                     htmltext = "3-06.htm";
                  } else if (npcId == this.NPC[8]) {
                     htmltext = "8-01.htm";
                  }
                  break;
               case 14:
                  if (npcId == this.NPC[8]) {
                     htmltext = "8-03.htm";
                  } else if (npcId == this.NPC[11]) {
                     htmltext = "11-01.htm";
                  }
                  break;
               case 15:
                  if (npcId == this.NPC[11]) {
                     htmltext = "11-02.htm";
                  } else if (npcId == this.NPC[9]) {
                     htmltext = "9-01.htm";
                  }
                  break;
               case 16:
                  if (npcId == this.NPC[9]) {
                     htmltext = "9-02.htm";
                  }
                  break;
               case 17:
                  if (npcId == this.NPC[9]) {
                     htmltext = "9-04.htm";
                  } else if (npcId == this.NPC[10]) {
                     htmltext = "10-01.htm";
                  }
                  break;
               case 18:
                  if (npcId == this.NPC[10]) {
                     htmltext = "10-05.htm";
                  }
                  break;
               case 19:
                  if (npcId == this.NPC[10]) {
                     htmltext = "10-07.htm";
                  } else if (npcId == this.NPC[0]) {
                     htmltext = "0-06.htm";
                  }
                  break;
               case 20:
                  if (npcId == this.NPC[0]) {
                     if (player.getLevel() >= 76) {
                        htmltext = "0-09.htm";
                        if (this.getClassId(player) < 131 || this.getClassId(player) > 135) {
                           st.exitQuest(false);
                           addExpAndSp(player, 2299404L, 0);
                           this.giveAdena(player, 5000000L, true);
                           giveItems(player, 6622, 1L);
                           int classId = this.getClassId(player);
                           int prevClass = this.getPrevClass(player);
                           player.setClassId(classId);
                           if (!player.isSubClassActive() && player.getBaseClass() == prevClass) {
                              player.setBaseClass(classId);
                           }

                           player.broadcastCharInfo();
                           cast(npc, player, 4339, 1);
                        }
                     } else {
                        htmltext = "0-010.htm";
                     }
                  }
            }
         }
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      String htmltext = "";
      QuestState st = player.getQuestState(this.getName());
      int npcId = npc.getId();
      if (st != null && npcId == this.NPC[4]) {
         int cond = st.getCond();
         if (cond == 17) {
            QuestState st2 = this.findRightState(npc);
            if (st2 != null) {
               player.setLastQuestNpcObject(npc.getObjectId());
               int tab = st.getInt("Tab");
               int quest0 = st.getInt("Quest0");
               if (st == st2) {
                  if (tab == 1) {
                     if (quest0 == 0) {
                        htmltext = "4-04.htm";
                     } else if (quest0 == 1) {
                        htmltext = "4-06.htm";
                     }
                  } else if (quest0 == 0) {
                     htmltext = "4-01.htm";
                  } else if (quest0 == 1) {
                     htmltext = "4-03.htm";
                  }
               } else if (tab == 1) {
                  if (quest0 == 0) {
                     htmltext = "4-05.htm";
                  } else if (quest0 == 1) {
                     htmltext = "4-07.htm";
                  }
               } else if (quest0 == 0) {
                  htmltext = "4-02.htm";
               }
            }
         } else if (cond == 18) {
            htmltext = "4-08.htm";
         }
      }

      if (htmltext == "") {
         npc.showChatWindow(player);
      }

      return htmltext;
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon) {
      QuestState st2 = this.findRightState(npc);
      if (st2 != null) {
         int cond = st2.getCond();
         QuestState st = player.getQuestState(this.getName());
         int npcId = npc.getId();
         if (npcId == this.Mob[2] && st == st2 && cond == 17) {
            int quest0 = st.getInt("Quest0") + 1;
            if (quest0 == 1) {
               autoChat(npc, this.Text[16].replace("PLAYERNAME", player.getName()));
            }

            if (quest0 > 15) {
               quest0 = 1;
               autoChat(npc, this.Text[17].replace("PLAYERNAME", player.getName()));
               this.cancelQuestTimer("Mob_3 has despawned", npc, st2.getPlayer());
               st.set("Tab", "1");
               DeleteSpawn(st, npc);
            }

            st.set("Quest0", Integer.toString(quest0));
         } else if (npcId == this.Mob[1] && cond == 15 && (st != st2 || st == st2 && player.isInParty())) {
            autoChat(npc, this.Text[5].replace("PLAYERNAME", player.getName()));
            this.cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
            st2.set("spawned", "0");
            DeleteSpawn(st2, npc);
         }
      }

      return super.onAttack(npc, player, damage, isSummon);
   }

   @Override
   public String onSkillSee(Npc npc, Player player, Skill skill, GameObject[] targets, boolean isSummon) {
      if (_spawnList.containsKey(npc) && _spawnList.get(npc) != player.getObjectId()) {
         Player quest_player = (Player)World.getInstance().findObject(_spawnList.get(npc));
         if (quest_player == null) {
            return null;
         }

         for(GameObject obj : targets) {
            if (obj == quest_player || obj == npc) {
               QuestState st2 = this.findRightState(npc);
               if (st2 == null) {
                  return null;
               }

               autoChat(npc, this.Text[5].replace("PLAYERNAME", player.getName()));
               this.cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
               st2.set("spawned", "0");
               DeleteSpawn(st2, npc);
            }
         }
      }

      return super.onSkillSee(npc, player, skill, targets, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      int npcId = npc.getId();
      QuestState st = player.getQuestState(this.getName());

      for(int Archon_Minion = 21646; Archon_Minion < 21652; ++Archon_Minion) {
         if (npcId == Archon_Minion) {
            Party party = player.getParty();
            if (party != null) {
               List<QuestState> PartyQuestMembers = new ArrayList<>();

               for(Player player1 : party.getMembers()) {
                  QuestState st1 = this.findQuest(player1);
                  if (st1 != null && player1.isInsideRadius(player, Config.ALT_PARTY_RANGE2, false, false) && st1.isCond(15)) {
                     PartyQuestMembers.add(st1);
                  }
               }

               if (PartyQuestMembers.size() > 0) {
                  QuestState st2 = PartyQuestMembers.get(getRandom(PartyQuestMembers.size()));
                  this.giveHalishaMark(st2);
               }
            } else {
               QuestState st1 = this.findQuest(player);
               if (st1 != null && st1.isCond(15)) {
                  this.giveHalishaMark(st1);
               }
            }

            return super.onKill(npc, player, isSummon);
         }
      }

      int[] Archon_Hellisha_Norm = new int[]{18212, 18214, 18215, 18216, 18218};

      for(int element : Archon_Hellisha_Norm) {
         if (npcId == element) {
            QuestState st1 = this.findQuest(player);
            if (st1 != null && st1.isCond(15)) {
               autoChat(npc, this.Text[4].replace("PLAYERNAME", st1.getPlayer().getName()));
               st1.giveItems(this.Items[8], 1L);
               st1.takeItems(this.Items[3], -1L);
               st1.setCond(16, true);
            }

            return super.onKill(npc, player, isSummon);
         }
      }

      for(int Guardian_Angel = 27214; Guardian_Angel < 27217; ++Guardian_Angel) {
         if (npcId == Guardian_Angel) {
            QuestState st1 = this.findQuest(player);
            if (st1 != null && st1.isCond(6)) {
               int kills = st1.getInt("kills");
               if (kills < 9) {
                  st1.set("kills", Integer.toString(kills + 1));
               } else {
                  st1.giveItems(this.Items[5], 1L);
                  st.setCond(7, true);
               }
            }

            return super.onKill(npc, player, isSummon);
         }
      }

      if (st != null) {
         QuestState st2 = this.findRightState(npc);
         if (st2 != null) {
            int cond = st.getCond();
            if (npcId == this.Mob[0] && cond == 8) {
               if (!player.isInParty() && st == st2) {
                  autoChat(npc, this.Text[12].replace("PLAYERNAME", player.getName()));
                  giveItems(player, this.Items[6], 1L);
                  st.setCond(9, true);
               }

               this.cancelQuestTimer("Mob_1 has despawned", npc, st2.getPlayer());
               st2.set("spawned", "0");
               DeleteSpawn(st2, npc);
            } else if (npcId == this.Mob[1] && cond == 15) {
               if (!player.isInParty()) {
                  if (st == st2) {
                     autoChat(npc, this.Text[4].replace("PLAYERNAME", player.getName()));
                     giveItems(player, this.Items[8], 1L);
                     takeItems(player, this.Items[3], -1L);
                     st.setCond(16, true);
                  } else {
                     autoChat(npc, this.Text[5].replace("PLAYERNAME", player.getName()));
                  }
               }

               this.cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
               st2.set("spawned", "0");
               DeleteSpawn(st2, npc);
            } else if (npcId == this.Mob[2] && st == st2 && cond == 17) {
               autoChat(npc, this.Text[17].replace("PLAYERNAME", player.getName()));
               this.cancelQuestTimer("Mob_3 has despawned", npc, st2.getPlayer());
               st.set("Tab", "1");
               DeleteSpawn(st, npc);
               st.set("Quest0", "1");
            }
         }
      } else if (npcId == this.Mob[0]) {
         st = this.findRightState(npc);
         if (st != null) {
            this.cancelQuestTimer("Mob_1 has despawned", npc, player);
            st.set("spawned", "0");
            DeleteSpawn(st, npc);
         }
      } else if (npcId == this.Mob[1]) {
         st = this.findRightState(npc);
         if (st != null) {
            this.cancelQuestTimer("Archon Hellisha has despawned", npc, player);
            st.set("spawned", "0");
            DeleteSpawn(st, npc);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
   }
}

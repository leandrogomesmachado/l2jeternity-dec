package l2e.scripts.quests;

import gnu.trove.map.hash.TIntIntHashMap;
import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _501_ProofOfClanAlliance extends Quest {
   private static final String qn = "_501_ProofOfClanAlliance";
   private static final int SIR_KRISTOF_RODEMAI = 30756;
   private static final int STATUE_OF_OFFERING = 30757;
   private static final int WITCH_ATHREA = 30758;
   private static final int WITCH_KALIS = 30759;
   private static final int POISON_OF_DEATH = 4082;
   private static final int DYING = 4083;
   private static final int HERB_OF_HARIT = 3832;
   private static final int HERB_OF_VANOR = 3833;
   private static final int HERB_OF_OEL_MAHUM = 3834;
   private static final int BLOOD_OF_EVA = 3835;
   private static final int SYMBOL_OF_LOYALTY = 3837;
   private static final int PROOF_OF_ALLIANCE = 3874;
   private static final int VOUCHER_OF_FAITH = 3873;
   private static final int ANTIDOTE_RECIPE = 3872;
   private static final int POTION_OF_RECOVERY = 3889;
   private static final int[] CHESTS = new int[]{27173, 27178};
   private static final int[][] CHEST_LOCS = new int[][]{
      {102273, 103433, -3512},
      {102190, 103379, -3524},
      {102107, 103325, -3533},
      {102024, 103271, -3500},
      {102327, 103350, -3511},
      {102244, 103296, -3518},
      {102161, 103242, -3529},
      {102078, 103188, -3500},
      {102381, 103267, -3538},
      {102298, 103213, -3532},
      {102215, 103159, -3520},
      {102132, 103105, -3513},
      {102435, 103184, -3515},
      {102352, 103130, -3522},
      {102269, 103076, -3533},
      {102186, 103022, -3541}
   };
   private static TIntIntHashMap MOBS = new TIntIntHashMap();
   private static boolean isArthea = false;

   public _501_ProofOfClanAlliance(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30756);
      this.addStartNpc(30757);
      this.addTalkId(30756);
      this.addTalkId(30757);
      this.addTalkId(30758);
      this.addTalkId(30759);

      for(int i : CHESTS) {
         this.addKillId(i);
      }

      this.addKillId(20685);
      this.addKillId(20644);
      this.addKillId(20576);
      MOBS.putIfAbsent(20685, 3833);
      MOBS.putIfAbsent(20644, 3832);
      MOBS.putIfAbsent(20576, 3834);
      isArthea = false;
      this.questItemIds = new int[]{3833, 3832, 3834, 3835, 3837, 3872, 3873, 3889, 3872};
   }

   private QuestState getLeaderQuestState(Player player) {
      if (player.isClanLeader()) {
         return player.getQuestState("_501_ProofOfClanAlliance");
      } else {
         Clan clan = player.getClan();
         if (clan == null) {
            return null;
         } else {
            Player leader = clan.getLeader().getPlayerInstance();
            return leader == null ? null : leader.getQuestState("_501_ProofOfClanAlliance");
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState leaderst = null;
      QuestState st = player.getQuestState("_501_ProofOfClanAlliance");
      if (st == null) {
         return event;
      } else if (event.equalsIgnoreCase("chest_timer")) {
         isArthea = false;
         return "";
      } else {
         if (player.isClanLeader()) {
            leaderst = st;
         } else {
            leaderst = this.getLeaderQuestState(player);
         }

         if (leaderst == null) {
            return null;
         } else {
            if (player.isClanLeader()) {
               if (event.equalsIgnoreCase("30756-07.htm")) {
                  st.playSound("ItemSound.quest_accept");
                  st.set("cond", "1");
                  st.setState((byte)1);
                  st.set("part", "1");
               } else if (event.equalsIgnoreCase("30759-03.htm")) {
                  st.set("part", "2");
                  st.set("cond", "2");
                  st.set("dead_list", " ");
               } else if (event.equalsIgnoreCase("30759-07.htm")) {
                  st.takeItems(3837, 1L);
                  st.takeItems(3837, 1L);
                  st.takeItems(3837, 1L);
                  st.giveItems(3872, 1L);
                  st.set("part", "3");
                  st.set("cond", "3");
                  st.addNotifyOfDeath(player);
                  SkillsParser.getInstance().getInfo(4082, 1).getEffects(npc, player, false);
               }
            } else if (event.equalsIgnoreCase("30757-05.htm")) {
               if (player.isClanLeader()) {
                  return "пїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅ!";
               }

               if (getRandom(10) > 5) {
                  st.giveItems(3837, 1L);
                  String[] deadlist = leaderst.get("dead_list").split(" ");
                  leaderst.set("dead_list", joinStringArray(setNewValToArray(deadlist, player.getName().toLowerCase()), " "));
                  return "30757-06.htm";
               }

               Skill skill = SkillsParser.getInstance().getInfo(4083, 1);
               npc.setTarget(player);
               npc.doCast(skill);
               this.startQuestTimer(player.getName(), 4000L, npc, player, false);
            } else if (event.equalsIgnoreCase(player.getName())) {
               if (player.isDead()) {
                  st.giveItems(3837, 1L);
                  String[] deadlist = leaderst.get("dead_list").split(" ");
                  leaderst.set("dead_list", joinStringArray(setNewValToArray(deadlist, player.getName().toLowerCase()), " "));
               }
            } else if (event.equalsIgnoreCase("30758-03.htm")) {
               if (isArthea) {
                  return "30758-04.htm";
               }

               isArthea = true;
               leaderst.set("part", "4");

               for(int[] element : CHEST_LOCS) {
                  int rand = getRandom(5);
                  addSpawn(CHESTS[0] + rand, element[0], element[1], element[2], 0, false, 300000L);
                  this.startQuestTimer("chest_timer", 60000L, npc, player, false);
               }
            } else if (event.equalsIgnoreCase("30758-07.htm") && st.getQuestItemsCount(57) >= 10000L && !isArthea) {
               st.takeItems(57, 10000L);
               return "30758-08.htm";
            }

            return event;
         }
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_501_ProofOfClanAlliance");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         byte id = st.getState();
         Clan clan = talker.getClan();
         int part = st.getInt("part");
         switch(npcId) {
            case 30756:
               if (id == 0) {
                  if (!talker.isClanLeader() || clan == null) {
                     return returningString("05", npcId);
                  }

                  int level = clan.getLevel();
                  if (level <= 2) {
                     return returningString("01", npcId);
                  }

                  if (level >= 4) {
                     return returningString("02", npcId);
                  }

                  if (level == 3) {
                     if (st.hasQuestItems(3874)) {
                        return returningString("03", npcId);
                     }

                     return returningString("04", npcId);
                  }
               } else if (id == 1) {
                  if (st.hasQuestItems(3873) && part == 6) {
                     st.playSound("ItemSound.quest_finish");
                     st.takeItems(3873, 1L);
                     st.giveItems(3874, 1L);
                     st.addExpAndSp(0, 120000);
                     st.exitQuest(false);
                     return returningString("09", npcId);
                  }

                  return returningString("10", npcId);
               }
               break;
            case 30757:
               QuestState leaderst = this.getLeaderQuestState(talker);
               if (leaderst == null) {
                  return "";
               }

               byte sId = leaderst.getState();
               switch(sId) {
                  case 1:
                     if (leaderst.getInt("part") != 2) {
                        return "";
                     }

                     if (!talker.isClanLeader() && leaderst != st) {
                        if (talker.getLevel() >= 40) {
                           String[] dlist = leaderst.get("dead_list").split(" ");
                           if (dlist.length < 3) {
                              for(String str : dlist) {
                                 if (talker.getName().equalsIgnoreCase(str)) {
                                    return returningString("03", npcId);
                                 }
                              }

                              return returningString("01", npcId);
                           }

                           return returningString("03", npcId);
                        }

                        return returningString("04", npcId);
                     }

                     return returningString("02", npcId);
                  default:
                     return returningString("08", npcId);
               }
            case 30758:
               QuestState leader_st = this.getLeaderQuestState(talker);
               if (leader_st == null) {
                  return "";
               }

               byte s_Id = leader_st.getState();
               switch(s_Id) {
                  case 1:
                     int partA = leader_st.getInt("part");
                     if (partA == 3 && leader_st.hasQuestItems(3872) && !leader_st.hasQuestItems(3835)) {
                        return returningString("01", npcId);
                     }

                     if (partA == 5) {
                        return returningString("10", npcId);
                     }

                     if (partA == 4) {
                        if (leader_st.getInt("chest_wins") >= 4) {
                           st.giveItems(3835, 1L);
                           leader_st.set("part", "5");
                           return returningString("09", npcId);
                        }

                        return returningString("06", npcId);
                     }

                     return null;
                  default:
                     return null;
               }
            case 30759:
               if (id == 0) {
                  QuestState leaderst = this.getLeaderQuestState(talker);
                  if (leaderst == null) {
                     return "";
                  }

                  if (talker.isClanLeader() || leaderst == st) {
                     return "пїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅ, пїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅ! пїЅ пїЅпїЅ пїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅ!";
                  }

                  if (leaderst.getState() == 1) {
                     return returningString("12", npcId);
                  }
               } else if (id == 1) {
                  long symbol = st.getQuestItemsCount(3837);
                  if (part == 1) {
                     return returningString("01", npcId);
                  }

                  if (part == 2 && symbol < 3L) {
                     return returningString("05", npcId);
                  }

                  if (symbol == 3L) {
                     return returningString("06", npcId);
                  }

                  if (part == 5
                     && st.hasQuestItems(3832)
                     && st.hasQuestItems(3833)
                     && st.hasQuestItems(3834)
                     && st.hasQuestItems(3835)
                     && this.isAffected(talker, 4082)) {
                     st.giveItems(3873, 1L);
                     st.giveItems(3889, 1L);
                     st.takeItems(3832, -1L);
                     st.takeItems(3833, -1L);
                     st.takeItems(3834, -1L);
                     st.takeItems(3835, -1L);
                     st.set("part", "6");
                     st.set("cond", "4");
                     return returningString("08", npcId);
                  }

                  if (part == 3 || part == 4 || part == 5) {
                     if (!this.isAffected(talker, 4082)) {
                        st.set("part", "1");
                        st.takeItems(3872, -1L);
                        return returningString("09", npcId);
                     }

                     return returningString("10", npcId);
                  }

                  if (part == 6) {
                     return returningString("11", npcId);
                  }
               }
         }

         return null;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState leaderst = this.getLeaderQuestState(killer);
      if (leaderst == null) {
         return null;
      } else if (leaderst.getState() != 1) {
         return null;
      } else {
         int part = leaderst.getInt("part");
         int npcId = npc.getId();
         if (MOBS.containsKey(npcId)) {
            QuestState st = killer.getQuestState("_501_ProofOfClanAlliance");
            if (st == null) {
               st = this.newQuestState(killer);
            }

            if (st == leaderst) {
               return null;
            }

            if (part >= 3 && part < 6 && getRandom(10) == 0) {
               st.giveItems(MOBS.get(npcId), 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         for(int i : CHESTS) {
            QuestState st = killer.getQuestState("_501_ProofOfClanAlliance");
            if (st == null) {
               st = this.newQuestState(killer);
            }

            if (npcId == i) {
               if (Rnd.chance(25)) {
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.BINGO), 2000);
                  int wins = leaderst.getInt("chest_wins");
                  if (wins < 4) {
                     leaderst.set("chest_wins", String.valueOf(++wins));
                  }

                  if (wins >= 4) {
                     st.playSound("ItemSound.quest_middle");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               }

               return null;
            }
         }

         return null;
      }
   }

   @Override
   public String onDeath(Creature killer, Creature victim, QuestState qs) {
      if (qs.getPlayer().equals(victim)) {
         qs.exitQuest(true);
      }

      return null;
   }

   private boolean isAffected(Player player, int skillId) {
      return player.getFirstEffect(skillId) != null;
   }

   private static String joinStringArray(String[] s, String sep) {
      String ts = "";

      for(int i = 0; i < s.length; ++i) {
         if (i == s.length - 1) {
            ts = ts + s[i];
         } else {
            ts = ts + s[i] + sep;
         }
      }

      return ts;
   }

   public static String[] setNewValToArray(String[] s, String s1) {
      String[] ts = new String[s.length + 1];

      for(int i = 0; i < s.length; ++i) {
         ts[i] = s[i];
      }

      ts[s.length] = s1;
      return ts;
   }

   private static String returningString(String s, int npcId) {
      return npcId + "-" + s + ".htm";
   }

   public static void main(String[] args) {
      new _501_ProofOfClanAlliance(501, "_501_ProofOfClanAlliance", "");
   }
}

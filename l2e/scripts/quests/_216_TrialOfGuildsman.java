package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _216_TrialOfGuildsman extends Quest {
   private static final String qn = "_216_TrialOfGuildsman";
   private static final int VALKON = 30103;
   private static final int NORMAN = 30210;
   private static final int ALTRAN = 30283;
   private static final int PINTER = 30298;
   private static final int DUNING = 30688;
   private static final int[] TALKERS = new int[]{30103, 30210, 30283, 30298, 30688};
   private static final int MANDRAGORA_SPROUT = 20154;
   private static final int MANDRAGORA_SAPLING = 20155;
   private static final int MANDRAGORA_BLOSSOM = 20156;
   private static final int SILENOS = 20168;
   private static final int STRAIN = 20200;
   private static final int GHOUL = 20201;
   private static final int DEAD_SEEKER = 20202;
   private static final int MANDRAGORA_SPROUT2 = 20223;
   private static final int BREKA_ORC = 20267;
   private static final int BREKA_ORC_ARCHER = 20268;
   private static final int BREKA_ORC_SHAMAN = 20269;
   private static final int BREKA_ORC_OVERLORD = 20270;
   private static final int BREKA_ORC_WARRIOR = 20271;
   private static final int ANT = 20079;
   private static final int ANT_CAPTAIN = 20080;
   private static final int ANT_OVERSEER = 20081;
   private static final int GRANITE_GOLEM = 20083;
   private static final int[] MOBS = new int[]{
      20154, 20155, 20156, 20168, 20200, 20201, 20202, 20223, 20267, 20268, 20269, 20270, 20271, 20079, 20080, 20081, 20083
   };
   private static final int VALKONS_RECOMMEND = 3120;
   private static final int MANDRAGORA_BERRY = 3121;
   private static final int ALLTRANS_INSTRUCTIONS = 3122;
   private static final int ALLTRANS_RECOMMEND1 = 3123;
   private static final int ALLTRANS_RECOMMEND2 = 3124;
   private static final int NORMANS_INSTRUCTIONS = 3125;
   private static final int NORMANS_RECEIPT = 3126;
   private static final int DUNINGS_INSTRUCTIONS = 3127;
   private static final int DUNINGS_KEY = 3128;
   private static final int NORMANS_LIST = 3129;
   private static final int GRAY_BONE_POWDER = 3130;
   private static final int GRANITE_WHETSTONE = 3131;
   private static final int RED_PIGMENT = 3132;
   private static final int BRAIDED_YARN = 3133;
   private static final int JOURNEYMAN_GEM = 3134;
   private static final int PINTERS_INSTRUCTIONS = 3135;
   private static final int AMBER_BEAD = 3136;
   private static final int AMBER_LUMP = 3137;
   private static final int JOURNEYMAN_DECO_BEADS = 3138;
   private static final int JOURNEYMAN_RING = 3139;
   private static final int RP_JOURNEYMAN_RING = 3024;
   private static final int RP_AMBER_BEAD = 3025;
   private static final int[] QUESTITEMS = new int[]{3024, 3122, 3024, 3120, 3121, 3123, 3128, 3125, 3129, 3126, 3124, 3135, 3025, 3136, 3127};
   private static final int MARK_OF_GUILDSMAN = 3119;
   private static final int[] CLASSES = new int[]{56, 54};

   public _216_TrialOfGuildsman(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30103);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int mobId : MOBS) {
         this.addKillId(mobId);
      }

      this.questItemIds = QUESTITEMS;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_216_TrialOfGuildsman");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30103-06.htm";
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3120, 1L);
            st.takeItems(57, 2000L);
         } else if (event.equalsIgnoreCase("30103_1")) {
            htmltext = "30103-04.htm";
         } else if (event.equalsIgnoreCase("30103_2")) {
            htmltext = st.getQuestItemsCount(57) >= 2000L ? "30103-05.htm" : "30103-05a.htm";
         } else if (event.equalsIgnoreCase("30103_3")) {
            htmltext = "30103-09a.htm";
            st.set("cond", "0");
            st.set("onlyone", "1");
            st.takeItems(3139, -1L);
            st.takeItems(3122, 1L);
            st.takeItems(3024, 1L);
            st.addExpAndSp(514739, 33384);
            st.giveItems(57, 93803L);
            st.giveItems(3119, 1L);
            player.sendPacket(new SocialAction(player.getObjectId(), 3));
            st.exitQuest(false);
            st.playSound("ItemSound.quest_finish");
         } else if (event.equalsIgnoreCase("30103_4")) {
            st.addExpAndSp(514739, 33384);
            st.giveItems(57, 93803L);
            if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
               st.giveItems(7562, 85L);
               player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
            }

            htmltext = "30103-09b.htm";
            st.set("cond", "0");
            st.set("onlyone", "1");
            st.exitQuest(false);
            st.playSound("ItemSound.quest_finish");
            st.takeItems(3139, -1L);
            st.takeItems(3122, 1L);
            st.takeItems(3024, 1L);
            st.giveItems(3119, 1L);
            player.sendPacket(new SocialAction(player.getObjectId(), 3));
         } else if (event.equalsIgnoreCase("30283_1")) {
            htmltext = "30283-03.htm";
            st.giveItems(3122, 1L);
            st.takeItems(3120, 1L);
            st.giveItems(3024, 1L);
            st.takeItems(3121, 1L);
            st.giveItems(3123, 1L);
            st.giveItems(3124, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "5");
         } else if (event.equalsIgnoreCase("30210_1")) {
            htmltext = "30210-02.htm";
         } else if (event.equalsIgnoreCase("30210_2")) {
            htmltext = "30210-03.htm";
         } else if (event.equalsIgnoreCase("30210_3")) {
            htmltext = "30210-04.htm";
            st.giveItems(3125, 1L);
            st.takeItems(3123, 1L);
            st.giveItems(3126, 1L);
         } else if (event.equalsIgnoreCase("30210_4")) {
            htmltext = "30210-08.htm";
         } else if (event.equalsIgnoreCase("30210_5")) {
            htmltext = "30210-09.htm";
         } else if (event.equalsIgnoreCase("30210_6")) {
            htmltext = "30210-10.htm";
            st.takeItems(3128, st.getQuestItemsCount(3128));
            st.giveItems(3129, 1L);
            st.takeItems(3125, 1L);
         } else if (event.equalsIgnoreCase("30688_1")) {
            htmltext = "30688-02.htm";
            st.giveItems(3127, 1L);
            st.takeItems(3126, 1L);
         } else if (event.equalsIgnoreCase("30298_1")) {
            htmltext = "30298-03.htm";
         } else if (event.equalsIgnoreCase("30298_2")) {
            if (player.getClassId().getId() == 54) {
               htmltext = "30298-04.htm";
               st.giveItems(3135, 1L);
               st.takeItems(3124, 1L);
            } else {
               htmltext = "30298-05.htm";
               st.giveItems(3025, 1L);
               st.takeItems(3124, 1L);
               st.giveItems(3135, 1L);
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_216_TrialOfGuildsman");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30103 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30103 && st.getInt("cond") == 0 && st.getInt("onlyone") == 0) {
               if (Util.contains(CLASSES, talker.getClassId().getId())) {
                  if (talker.getLevel() < 35) {
                     htmltext = "30103-02.htm";
                     st.exitQuest(true);
                  } else {
                     htmltext = "30103-03.htm";
                  }
               } else {
                  htmltext = "30103-01.htm";
                  st.exitQuest(true);
               }
            } else if (npcId == 30103 && st.getInt("cond") == 0 && st.getInt("onlyone") == 1) {
               htmltext = Quest.getAlreadyCompletedMsg(talker);
            } else if (npcId == 30103 && st.getInt("cond") >= 1 && st.getQuestItemsCount(3120) == 1L) {
               htmltext = "30103-07.htm";
               st.set("cond", "3");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30103 && st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) == 1L) {
               htmltext = st.getQuestItemsCount(3139) < 7L ? "30103-08.htm" : "30103-09.htm";
            } else if (npcId == 30283 && st.getInt("cond") >= 1 && st.getQuestItemsCount(3120) == 1L && st.getQuestItemsCount(3121) == 0L) {
               htmltext = "30283-01.htm";
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30283 && st.getInt("cond") >= 1 && st.getQuestItemsCount(3120) == 1L && st.getQuestItemsCount(3121) == 1L) {
               htmltext = "30283-02.htm";
            } else if (npcId == 30283 && st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) == 1L) {
               htmltext = st.getQuestItemsCount(3139) < 7L ? "30283-04.htm" : "30283-05.htm";
            } else if (npcId == 30210 && st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) == 1L && st.getQuestItemsCount(3123) == 1L) {
               htmltext = "30210-01.htm";
            } else if (npcId == 30210
               && st.getInt("cond") >= 1
               && st.getQuestItemsCount(3122) > 0L
               && st.getQuestItemsCount(3125) > 0L
               && st.getQuestItemsCount(3126) > 0L) {
               htmltext = "30210-05.htm";
            } else if (npcId == 30210
               && st.getInt("cond") >= 1
               && st.getQuestItemsCount(3122) > 0L
               && st.getQuestItemsCount(3125) > 0L
               && st.getQuestItemsCount(3127) > 0L) {
               htmltext = "30210-06.htm";
            } else if (npcId == 30210
               && st.getInt("cond") >= 1
               && st.getQuestItemsCount(3122) > 0L
               && st.getQuestItemsCount(3125) > 0L
               && st.getQuestItemsCount(3128) >= 30L) {
               htmltext = "30210-07.htm";
            } else if (npcId == 30210 && st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3129) > 0L) {
               if (st.getQuestItemsCount(3130) >= 70L
                  && st.getQuestItemsCount(3131) >= 70L
                  && st.getQuestItemsCount(3132) >= 70L
                  && st.getQuestItemsCount(3133) >= 70L) {
                  htmltext = "30210-12.htm";
                  st.takeItems(3129, 1L);
                  st.takeItems(3130, st.getQuestItemsCount(3130));
                  st.takeItems(3131, st.getQuestItemsCount(3131));
                  st.takeItems(3132, st.getQuestItemsCount(3132));
                  st.takeItems(3133, st.getQuestItemsCount(3133));
                  st.giveItems(3134, 7L);
                  if (st.getQuestItemsCount(3138) >= 7L) {
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "6");
                  }
               } else {
                  htmltext = "30210-11.htm";
               }
            } else if (npcId != 30210
               || st.getInt("cond") < 1
               || st.getQuestItemsCount(3125) != 0L
               || st.getQuestItemsCount(3129) != 0L
               || st.getQuestItemsCount(3122) != 1L
               || st.getQuestItemsCount(3134) <= 0L && st.getQuestItemsCount(3139) <= 0L) {
               if (npcId == 30688
                  && st.getInt("cond") >= 1
                  && st.getQuestItemsCount(3122) > 0L
                  && st.getQuestItemsCount(3125) > 0L
                  && st.getQuestItemsCount(3126) > 0L) {
                  htmltext = "30688-01.htm";
               } else if (npcId == 30688
                  && st.getInt("cond") >= 1
                  && st.getQuestItemsCount(3122) > 0L
                  && st.getQuestItemsCount(3125) > 0L
                  && st.getQuestItemsCount(3127) > 0L) {
                  htmltext = "30688-03.htm";
               } else if (npcId == 30688
                  && st.getInt("cond") >= 1
                  && st.getQuestItemsCount(3122) > 0L
                  && st.getQuestItemsCount(3125) > 0L
                  && st.getQuestItemsCount(3128) >= 30L) {
                  htmltext = "30688-04.htm";
               } else if (npcId == 30688
                  && st.getInt("cond") >= 1
                  && st.getQuestItemsCount(3126) == 0L
                  && st.getQuestItemsCount(3127) == 0L
                  && st.getQuestItemsCount(3128) == 0L
                  && st.getQuestItemsCount(3122) == 1L) {
                  htmltext = "30688-01.htm";
               } else if (npcId == 30298 && st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3124) > 0L) {
                  htmltext = talker.getLevel() < 35 ? "30298-01.htm" : "30298-02.htm";
               } else if (npcId == 30298 && st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) > 0L && st.getQuestItemsCount(3135) > 0L) {
                  if (st.getQuestItemsCount(3136) < 70L) {
                     htmltext = "30298-06.htm";
                  } else {
                     htmltext = "30298-07.htm";
                     st.takeItems(3135, 1L);
                     st.takeItems(3136, st.getQuestItemsCount(3136));
                     st.takeItems(3025, st.getQuestItemsCount(3025));
                     st.takeItems(3137, st.getQuestItemsCount(3137));
                     st.giveItems(3138, 7L);
                     if (st.getQuestItemsCount(3134) >= 7L) {
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "6");
                     }
                  }
               } else if (npcId == 30298
                  && st.getInt("cond") >= 1
                  && st.getQuestItemsCount(3122) == 1L
                  && st.getQuestItemsCount(3135) == 0L
                  && (st.getQuestItemsCount(3138) > 0L || st.getQuestItemsCount(3139) > 0L)) {
                  htmltext = "30298-08.htm";
               }
            } else {
               htmltext = "30210-13.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_216_TrialOfGuildsman");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (npcId == 20223) {
            if (st.getInt("cond") >= 1 && st.getQuestItemsCount(3120) == 1L && st.getQuestItemsCount(3121) == 0L) {
               st.giveItems(3121, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "4");
            }
         } else if (npcId == 20154 || npcId == 20155 || npcId == 20156) {
            if (st.getInt("cond") >= 1 && st.getQuestItemsCount(3120) == 1L && st.getQuestItemsCount(3121) == 0L) {
               st.giveItems(3121, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "4");
            }
         } else if (npcId == 20267 || npcId == 20268 || npcId == 20269 || npcId == 20270 || npcId == 20271) {
            if (st.getInt("cond") >= 1
               && st.getQuestItemsCount(3122) == 1L
               && st.getQuestItemsCount(3125) == 1L
               && st.getQuestItemsCount(3127) == 1L
               && st.getQuestItemsCount(3128) <= 29L) {
               if (st.getQuestItemsCount(3128) == 29L) {
                  st.giveItems(3128, 1L);
                  st.takeItems(3127, 1L);
                  st.playSound("ItemSound.quest_middle");
               } else {
                  st.giveItems(3128, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20201 || npcId == 20200) {
            if (st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) == 1L && st.getQuestItemsCount(3129) == 1L && st.getQuestItemsCount(3130) < 70L) {
               st.giveItems(3130, 5L);
               st.playSound(st.getQuestItemsCount(3130) >= 70L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if (npcId == 20083) {
            if (st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) == 1L && st.getQuestItemsCount(3129) == 1L && st.getQuestItemsCount(3131) < 70L) {
               st.giveItems(3131, 7L);
               st.playSound(st.getQuestItemsCount(3131) >= 70L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if (npcId == 20202) {
            if (st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) == 1L && st.getQuestItemsCount(3129) == 1L && st.getQuestItemsCount(3132) < 70L) {
               st.giveItems(3132, 7L);
               st.playSound(st.getQuestItemsCount(3132) >= 70L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if (npcId == 20168) {
            if (st.getInt("cond") >= 1 && st.getQuestItemsCount(3122) == 1L && st.getQuestItemsCount(3129) == 1L && st.getQuestItemsCount(3133) < 70L) {
               st.giveItems(3133, 10L);
               st.playSound(st.getQuestItemsCount(3133) >= 70L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
         } else if ((npcId == 20079 || npcId == 20080 || npcId == 20081)
            && st.getInt("cond") >= 1
            && st.getQuestItemsCount(3122) == 1L
            && st.getQuestItemsCount(3135) == 1L
            && st.getQuestItemsCount(3136) < 70L) {
            st.giveItems(3136, 5L);
            st.playSound("ItemSound.quest_itemget");
            st.playSound(st.getQuestItemsCount(3136) >= 70L ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _216_TrialOfGuildsman(216, "_216_TrialOfGuildsman", "");
   }
}

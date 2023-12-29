package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _218_TestimonyOfLife extends Quest {
   private static final String qn = "_218_TestimonyOfLife";
   private static final int CARDIEN = 30460;
   private static final int ASTERIOS = 30154;
   private static final int PUSHKIN = 30300;
   private static final int THALIA = 30371;
   private static final int ADONIUS = 30375;
   private static final int ARKENIA = 30419;
   private static final int ISAEL_SILVERSHADOW = 30655;
   private static final int[] TALKERS = new int[]{30460, 30154, 30300, 30371, 30375, 30419, 30655};
   private static final int HARPY = 20145;
   private static final int WYRM = 20176;
   private static final int MARSH_SPIDER = 20233;
   private static final int UNICORN_OF_EVA = 27077;
   private static final int GUARDIAN_BASILISK = 20550;
   private static final int LETO_LIZARDMAN_SHAMAN = 20581;
   private static final int LETO_LIZARDMAN_OVERLORD = 20582;
   private static final int ANT_RECRUIT = 20082;
   private static final int ANT_PATROL = 20084;
   private static final int ANT_GUARD = 20086;
   private static final int ANT_SOLDIER = 20087;
   private static final int ANT_WARRIOR_CAPTAIN = 20088;
   private static final int[] MOBS = new int[]{20145, 20176, 20233, 27077, 20550, 20581, 20582, 20082, 20084, 20086, 20087, 20088};
   private static final int CARDIENS_LETTER = 3141;
   private static final int CAMOMILE_CHARM = 3142;
   private static final int HIERARCHS_LETTER = 3143;
   private static final int MOONFLOWER_CHARM = 3144;
   private static final int GRAIL_DIAGRAM = 3145;
   private static final int THALIAS_LETTER1 = 3146;
   private static final int THALIAS_LETTER2 = 3147;
   private static final int THALIAS_INSTRUCTIONS = 3148;
   private static final int PUSHKINS_LIST = 3149;
   private static final int PURE_MITHRIL_CUP = 3150;
   private static final int ARKENIAS_CONTRACT = 3151;
   private static final int ARKENIAS_INSTRUCTIONS = 3152;
   private static final int ADONIUS_LIST = 3153;
   private static final int ANDARIEL_SCRIPTURE_COPY = 3154;
   private static final int STARDUST = 3155;
   private static final int ISAELS_INSTRUCTIONS = 3156;
   private static final int ISAELS_LETTER = 3157;
   private static final int GRAIL_OF_PURITY = 3158;
   private static final int TEARS_OF_UNICORN = 3159;
   private static final int WATER_OF_LIFE = 3160;
   private static final int PURE_MITHRIL_ORE = 3161;
   private static final int ANT_SOLDIER_ACID = 3162;
   private static final int WYRMS_TALON1 = 3163;
   private static final int SPIDER_ICHOR = 3164;
   private static final int HARPYS_DOWN = 3165;
   private static final int TALINS_SPEAR_BLADE = 3166;
   private static final int TALINS_SPEAR_SHAFT = 3167;
   private static final int TALINS_RUBY = 3168;
   private static final int TALINS_AQUAMARINE = 3169;
   private static final int TALINS_AMETHYST = 3170;
   private static final int TALINS_PERIDOT = 3171;
   private static final int TALINS_SPEAR = 3026;
   private static final int[] QUESTITEMS = new int[]{
      3142,
      3141,
      3160,
      3144,
      3143,
      3155,
      3150,
      3148,
      3157,
      3159,
      3145,
      3149,
      3146,
      3151,
      3154,
      3152,
      3153,
      3147,
      3166,
      3167,
      3168,
      3169,
      3170,
      3171,
      3156,
      3158
   };
   private static final int MARK_OF_LIFE = 3140;

   public _218_TestimonyOfLife(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30460);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int mobId : MOBS) {
         this.addKillId(mobId);
      }

      this.questItemIds = QUESTITEMS;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_218_TestimonyOfLife");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30460-04.htm";
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3141, 1L);
         } else if (event.equalsIgnoreCase("30154_1")) {
            htmltext = "30154-02.htm";
         } else if (event.equalsIgnoreCase("30154_2")) {
            htmltext = "30154-03.htm";
         } else if (event.equalsIgnoreCase("30154_3")) {
            htmltext = "30154-04.htm";
         } else if (event.equalsIgnoreCase("30154_4")) {
            htmltext = "30154-05.htm";
         } else if (event.equalsIgnoreCase("30154_5")) {
            htmltext = "30154-06.htm";
         } else if (event.equalsIgnoreCase("30154_6")) {
            htmltext = "30154-07.htm";
            st.set("cond", "2");
            st.takeItems(3141, 1L);
            st.giveItems(3144, 1L);
            st.giveItems(3143, 1L);
         } else if (event.equalsIgnoreCase("30371_1")) {
            htmltext = "30371-02.htm";
         } else if (event.equalsIgnoreCase("30371_2")) {
            htmltext = "30371-03.htm";
            st.set("cond", "3");
            st.takeItems(3143, 1L);
            st.giveItems(3145, 1L);
         } else if (event.equalsIgnoreCase("30371_3")) {
            if (player.getLevel() < 37) {
               htmltext = "30371-10.htm";
               st.set("cond", "13");
               st.takeItems(3155, 1L);
               st.giveItems(3148, 1L);
            } else {
               htmltext = "30371-11.htm";
               st.set("cond", "14");
               st.takeItems(3155, 1L);
               st.giveItems(3147, 1L);
            }
         } else if (event.equalsIgnoreCase("30300_1")) {
            htmltext = "30300-02.htm";
         } else if (event.equalsIgnoreCase("30300_2")) {
            htmltext = "30300-03.htm";
         } else if (event.equalsIgnoreCase("30300_3")) {
            htmltext = "30300-04.htm";
         } else if (event.equalsIgnoreCase("30300_4")) {
            htmltext = "30300-05.htm";
         } else if (event.equalsIgnoreCase("30300_5")) {
            htmltext = "30300-06.htm";
            st.set("cond", "4");
            st.takeItems(3145, 1L);
            st.giveItems(3149, 1L);
         } else if (event.equalsIgnoreCase("30300_6")) {
            htmltext = "30300-09.htm";
         } else if (event.equalsIgnoreCase("30300_7")) {
            htmltext = "30300-10.htm";
            st.set("cond", "6");
            st.takeItems(3161, st.getQuestItemsCount(3161));
            st.takeItems(3162, st.getQuestItemsCount(3162));
            st.takeItems(3163, st.getQuestItemsCount(3163));
            st.takeItems(3149, 1L);
            st.giveItems(3150, 1L);
         } else if (event.equalsIgnoreCase("30419_1")) {
            htmltext = "30419-02.htm";
         } else if (event.equalsIgnoreCase("30419_2")) {
            htmltext = "30419-03.htm";
         } else if (event.equalsIgnoreCase("30419_3")) {
            htmltext = "30419-04.htm";
            st.set("cond", "8");
            st.takeItems(3146, 1L);
            st.giveItems(3151, 1L);
            st.giveItems(3152, 1L);
         } else if (event.equalsIgnoreCase("30375_1")) {
            htmltext = "30375-02.htm";
            st.set("cond", "9");
            st.takeItems(3152, 1L);
            st.giveItems(3153, 1L);
         } else if (event.equalsIgnoreCase("30655_1")) {
            htmltext = "30655-02.htm";
            st.set("cond", "15");
            st.takeItems(3147, 1L);
            st.giveItems(3156, 1L);
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_218_TestimonyOfLife");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30460 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30460) {
               if (st.getInt("cond") == 0) {
                  if (id == 2) {
                     htmltext = getAlreadyCompletedMsg(talker);
                  } else if (talker.getRace().ordinal() == 1) {
                     if (talker.getLevel() < 37) {
                        htmltext = "30460-02.htm";
                        st.exitQuest(true);
                     } else {
                        htmltext = "30460-03.htm";
                     }
                  } else {
                     htmltext = "30460-01.htm";
                  }
               } else if (st.getQuestItemsCount(3141) > 0L) {
                  htmltext = "30460-05.htm";
               } else if (st.getQuestItemsCount(3144) > 0L) {
                  htmltext = "30460-06.htm";
               } else if (st.getQuestItemsCount(3142) > 0L && getGameTicks() != st.getInt("id")) {
                  htmltext = "30460-07.htm";
                  st.set("id", String.valueOf(getGameTicks()));
                  st.set("cond", "22");
                  st.takeItems(3142, 1L);
                  st.addExpAndSp(943416, 62959);
                  st.giveItems(57, 171144L);
                  if (talker.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                     st.giveItems(7562, 102L);
                     talker.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                  }

                  st.giveItems(3140, 1L);
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
               }
            } else if (npcId == 30154) {
               if (st.getQuestItemsCount(3141) > 0L) {
                  htmltext = "30154-01.htm";
               } else if (st.getQuestItemsCount(3160) > 0L) {
                  htmltext = "30154-09.htm";
                  st.set("cond", "21");
                  st.takeItems(3160, 1L);
                  st.takeItems(3144, 1L);
                  st.giveItems(3142, 1L);
               } else if (st.getQuestItemsCount(3144) > 0L) {
                  htmltext = "30154-08.htm";
               } else if (st.getQuestItemsCount(3142) > 0L) {
                  htmltext = "30154-10.htm";
               }
            } else if (npcId == 30371) {
               if (st.getQuestItemsCount(3143) > 0L) {
                  htmltext = "30371-01.htm";
               } else if (st.getQuestItemsCount(3145) > 0L) {
                  htmltext = "30371-04.htm";
               } else if (st.getQuestItemsCount(3149) > 0L) {
                  htmltext = "30371-05.htm";
               } else if (st.getQuestItemsCount(3150) > 0L) {
                  htmltext = "30371-06.htm";
                  st.set("cond", "7");
                  st.takeItems(3150, 1L);
                  st.giveItems(3146, 1L);
               } else if (st.getQuestItemsCount(3146) > 0L) {
                  htmltext = "30371-07.htm";
               } else if (st.getQuestItemsCount(3151) > 0L) {
                  htmltext = "30371-08.htm";
               } else if (st.getQuestItemsCount(3155) > 0L) {
                  htmltext = "30371-09.htm";
               } else if (st.getQuestItemsCount(3148) > 0L) {
                  if (talker.getLevel() < 37) {
                     htmltext = "30371-12.htm";
                     st.set("cond", "13");
                  } else {
                     st.set("cond", "14");
                     st.takeItems(3148, 1L);
                     st.giveItems(3147, 1L);
                  }
               } else if (st.getQuestItemsCount(3147) > 0L) {
                  htmltext = "30371-14.htm";
               } else if (st.getQuestItemsCount(3156) > 0L) {
                  htmltext = "30371-15.htm";
               } else if (st.getQuestItemsCount(3157) > 0L) {
                  htmltext = "30371-16.htm";
                  st.set("cond", "18");
                  st.takeItems(3157, 1L);
                  st.giveItems(3158, 1L);
               } else if (st.getQuestItemsCount(3158) > 0L) {
                  htmltext = "30371-17.htm";
               } else if (st.getQuestItemsCount(3159) > 0L) {
                  htmltext = "30371-18.htm";
                  st.set("cond", "20");
                  st.takeItems(3159, 1L);
                  st.giveItems(3160, 1L);
               } else if (st.getQuestItemsCount(3160) > 0L) {
                  htmltext = "30371-19.htm";
               }
            } else if (npcId == 30300) {
               if (st.getQuestItemsCount(3145) > 0L) {
                  htmltext = "30300-01.htm";
               } else if (st.getQuestItemsCount(3149) > 0L) {
                  htmltext = st.getInt("cond") == 5 ? "30300-08.htm" : "30300-07.htm";
               } else if (st.getQuestItemsCount(3150) > 0L) {
                  htmltext = "30300-11.htm";
               } else if (st.getInt("cond") > 5) {
                  htmltext = "30300-12.htm";
               }
            } else if (npcId == 30419) {
               if (st.getQuestItemsCount(3146) > 0L) {
                  htmltext = "30419-01.htm";
               } else if (st.getQuestItemsCount(3152) > 0L || st.getQuestItemsCount(3153) > 0L) {
                  htmltext = "30419-05.htm";
               } else if (st.getQuestItemsCount(3154) > 0L) {
                  htmltext = "30419-06.htm";
                  st.set("cond", "12");
                  st.takeItems(3151, 1L);
                  st.takeItems(3154, 1L);
                  st.giveItems(3155, 1L);
               } else if (st.getQuestItemsCount(3155) > 0L) {
                  htmltext = "30419-07.htm";
               } else {
                  htmltext = "30419-08.htm";
               }
            } else if (npcId == 30375) {
               if (st.getQuestItemsCount(3152) > 0L) {
                  htmltext = "30375-01.htm";
               } else if (st.getQuestItemsCount(3153) > 0L) {
                  if (st.getInt("cond") == 10) {
                     htmltext = "30375-04.htm";
                     st.set("cond", "11");
                     st.takeItems(3164, st.getQuestItemsCount(3164));
                     st.takeItems(3165, st.getQuestItemsCount(3165));
                     st.takeItems(3153, 1L);
                     st.giveItems(3154, 1L);
                  } else {
                     htmltext = "30375-03.htm";
                  }
               } else if (st.getQuestItemsCount(3154) > 0L) {
                  htmltext = "30375-05.htm";
               } else {
                  htmltext = "30375-06.htm";
               }
            } else if (npcId == 30655) {
               if (st.getQuestItemsCount(3147) > 0L) {
                  htmltext = "30655-01.htm";
               } else if (st.getQuestItemsCount(3156) > 0L) {
                  if (st.getQuestItemsCount(3166) > 0L
                     && st.getQuestItemsCount(3167) > 0L
                     && st.getQuestItemsCount(3168) > 0L
                     && st.getQuestItemsCount(3169) > 0L
                     && st.getQuestItemsCount(3170) > 0L
                     && st.getQuestItemsCount(3171) > 0L) {
                     htmltext = "30655-04.htm";
                     st.set("cond", "17");
                     st.takeItems(3166, 1L);
                     st.takeItems(3167, 1L);
                     st.takeItems(3168, 1L);
                     st.takeItems(3169, 1L);
                     st.takeItems(3170, 1L);
                     st.takeItems(3171, 1L);
                     st.takeItems(3156, 1L);
                     st.giveItems(3157, 1L);
                     st.giveItems(3026, 1L);
                  } else {
                     htmltext = "30655-03.htm";
                  }
               } else if (st.getQuestItemsCount(3026) > 0L && st.getQuestItemsCount(3157) > 0L) {
                  htmltext = "30655-05.htm";
               } else if (st.getQuestItemsCount(3158) > 0L || st.getQuestItemsCount(3142) > 0L) {
                  htmltext = "30655-06.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_218_TestimonyOfLife");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (npcId == 20550) {
            if (st.getQuestItemsCount(3149) > 0L && st.getQuestItemsCount(3161) < 10L && st.getRandom(100) < 50) {
               st.giveItems(3161, 1L);
               if (st.getQuestItemsCount(3161) < 10L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(3163) >= 20L && st.getQuestItemsCount(3162) >= 20L) {
                     st.set("cond", "5");
                  }
               }
            }
         } else if (npcId == 20176) {
            if (st.getQuestItemsCount(3149) > 0L && st.getQuestItemsCount(3163) < 20L && st.getRandom(100) < 50) {
               st.giveItems(3163, 1L);
               if (st.getQuestItemsCount(3163) < 20L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(3161) >= 10L && st.getQuestItemsCount(3162) >= 20L) {
                     st.set("cond", "5");
                  }
               }
            }
         } else if (npcId == 20082 || npcId == 20084 || npcId == 20086 || npcId == 20087 || npcId == 20088) {
            if (st.getQuestItemsCount(3149) > 0L && st.getQuestItemsCount(3162) < 20L) {
               int chance = 80;
               if (npcId == 20087 || npcId == 20088) {
                  chance = 50;
               }

               if (st.getRandom(100) < chance) {
                  st.giveItems(3162, 1L);
                  if (st.getQuestItemsCount(3162) < 20L) {
                     st.playSound("ItemSound.quest_itemget");
                  } else {
                     st.playSound("ItemSound.quest_middle");
                     if (st.getQuestItemsCount(3161) >= 10L && st.getQuestItemsCount(3163) >= 20L) {
                        st.set("cond", "5");
                     }
                  }
               }
            }
         } else if (npcId == 20233) {
            if (st.getQuestItemsCount(3153) > 0L && st.getQuestItemsCount(3164) < 20L && st.getRandom(100) < 50) {
               st.giveItems(3164, 1L);
               if (st.getQuestItemsCount(3164) < 20L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(3165) >= 20L) {
                     st.set("cond", "10");
                  }
               }
            }
         } else if (npcId == 20145) {
            if (st.getQuestItemsCount(3153) > 0L && st.getQuestItemsCount(3165) < 20L && st.getRandom(100) < 50) {
               st.giveItems(3165, 1L);
               if (st.getQuestItemsCount(3165) < 20L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.playSound("ItemSound.quest_middle");
                  if (st.getQuestItemsCount(3164) >= 20L) {
                     st.set("cond", "10");
                  }
               }
            }
         } else if (npcId == 27077) {
            if (st.getQuestItemsCount(3026) > 0L && st.getQuestItemsCount(3158) > 0L && st.getQuestItemsCount(3159) == 0L) {
               st.takeItems(3158, 1L);
               st.takeItems(3026, 1L);
               st.giveItems(3159, 1L);
               st.set("cond", "19");
            }
         } else if ((npcId == 20581 || npcId == 20582) && st.getQuestItemsCount(3156) > 0L && st.getRandom(100) < 50) {
            for(int id : new int[]{3166, 3167, 3168, 3169, 3170}) {
               if (st.getQuestItemsCount(id) == 0L) {
                  st.giveItems(id, 1L);
                  st.playSound("ItemSound.quest_itemget");
                  return super.onKill(npc, killer, isSummon);
               }
            }

            if (st.getQuestItemsCount(3171) == 0L) {
               st.giveItems(3171, 1L);
               st.playSound("ItemSound.quest_itemget");
               st.set("cond", "16");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _218_TestimonyOfLife(218, "_218_TestimonyOfLife", "");
   }
}

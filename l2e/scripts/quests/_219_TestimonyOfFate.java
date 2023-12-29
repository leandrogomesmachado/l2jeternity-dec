package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _219_TestimonyOfFate extends Quest {
   private static final String qn = "_219_TestimonyOfFate";
   private static final int Kaira = 30476;
   private static final int Metheus = 30614;
   private static final int Ixia = 30463;
   private static final int AldersSpirit = 30613;
   private static final int Roa = 30114;
   private static final int Norman = 30210;
   private static final int Thifiell = 30358;
   private static final int Arkenia = 30419;
   private static final int BloodyPixy = 31845;
   private static final int BlightTreant = 31850;
   private static final int[] NPCS = new int[]{30476, 30614, 30463, 30613, 30114, 30210, 30358, 30419, 31845, 31850};
   private static final int KairasLetter = 3173;
   private static final int MetheussFuneralJar = 3174;
   private static final int KasandrasRemains = 3175;
   private static final int HerbalismTextbook = 3176;
   private static final int IxiasList = 3177;
   private static final int MedusasIchor = 3178;
   private static final int MarshSpiderFluids = 3179;
   private static final int DeadSeekerDung = 3180;
   private static final int TyrantsBlood = 3181;
   private static final int NightshadeRoot = 3182;
   private static final int Belladonna = 3183;
   private static final int AldersSkull1 = 3184;
   private static final int AldersSkull2 = 3185;
   private static final int AldersReceipt = 3186;
   private static final int RevelationsManuscript = 3187;
   private static final int KairasRecommendation = 3189;
   private static final int KairasInstructions = 3188;
   private static final int PalusCharm = 3190;
   private static final int ThifiellsLetter = 3191;
   private static final int ArkeniasNote = 3192;
   private static final int PixyGarnet = 3193;
   private static final int BlightTreantSeed = 3199;
   private static final int GrandissSkull = 3194;
   private static final int KarulBugbearSkull = 3195;
   private static final int BrekaOverlordSkull = 3196;
   private static final int LetoOverlordSkull = 3197;
   private static final int BlackWillowLeaf = 3200;
   private static final int RedFairyDust = 3198;
   private static final int BlightTreantSap = 3201;
   private static final int ArkeniasLetter = 1246;
   private static final int MarkofFate = 3172;
   private static final int HangmanTree = 20144;
   private static final int Medusa = 20158;
   private static final int MarshSpider = 20233;
   private static final int DeadSeeker = 20202;
   private static final int Tyrant = 20192;
   private static final int TyrantKingpin = 20193;
   private static final int MarshStakatoWorker = 20230;
   private static final int MarshStakato = 20157;
   private static final int MarshStakatoSoldier = 20232;
   private static final int MarshStakatoDrone = 20234;
   private static final int Grandis = 20554;
   private static final int KarulBugbear = 20600;
   private static final int BrekaOrcOverlord = 20270;
   private static final int LetoLizardmanOverlord = 20582;
   private static final int BlackWillowLurker = 27079;

   public _219_TestimonyOfFate(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30476);

      for(int i : NPCS) {
         this.addTalkId(i);
      }

      this.addKillId(new int[]{20144, 20157, 20158, 20192, 20193, 20202, 20230, 20232, 20233, 20234, 20270, 20554, 20582, 20600, 27079});
      this.questItemIds = new int[]{
         3173,
         3174,
         3175,
         3177,
         3183,
         3184,
         3185,
         3186,
         3187,
         3189,
         3188,
         3191,
         3190,
         3192,
         3193,
         3199,
         3198,
         3201,
         1246,
         3178,
         3179,
         3180,
         3181,
         3182,
         3194,
         3195,
         3196,
         3197,
         3200
      };
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_219_TestimonyOfFate");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30476-05.htm";
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3173, 1L);
         } else if (event.equalsIgnoreCase("30476_1")) {
            htmltext = "30476-04.htm";
         } else if (event.equalsIgnoreCase("30476_2")) {
            if (player.getLevel() >= 38) {
               st.set("cond", "15");
               htmltext = "30476-12.htm";
               st.giveItems(3189, 1L);
               st.takeItems(3187, 1L);
            } else {
               st.set("cond", "14");
               htmltext = "30476-13.htm";
               st.giveItems(3188, 1L);
               st.takeItems(3187, 1L);
            }
         } else if (event.equalsIgnoreCase("30114_1")) {
            htmltext = "30114-02.htm";
         } else if (event.equalsIgnoreCase("30114_2")) {
            htmltext = "30114-03.htm";
         } else if (event.equalsIgnoreCase("30114_3")) {
            htmltext = "30114-04.htm";
            st.takeItems(3185, 1L);
            st.giveItems(3186, 1L);
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30419_1")) {
            htmltext = "30419-02.htm";
            st.takeItems(3191, 1L);
            st.giveItems(3192, 1L);
            st.set("cond", "17");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("31845_1")) {
            htmltext = "31845-02.htm";
            st.giveItems(3193, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("31850_1")) {
            htmltext = "31850-02.htm";
            st.giveItems(3199, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30419_2")) {
            htmltext = "30419-05.htm";
            st.takeItems(3192, 1L);
            st.takeItems(3198, 1L);
            st.takeItems(3201, 1L);
            st.giveItems(1246, 1L);
            st.set("cond", "18");
            st.playSound("ItemSound.quest_middle");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_219_TestimonyOfFate");
      if (st == null) {
         return htmltext;
      } else {
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         int cond = st.getInt("cond");
         int npcId = npc.getId();
         if (npcId == 30476) {
            if (st.getQuestItemsCount(3172) != 0L) {
               return htmltext;
            }

            if (cond == 0) {
               if (player.getRace().ordinal() == 2 && player.getLevel() >= 37) {
                  htmltext = "30476-03.htm";
               } else if (player.getRace().ordinal() == 2) {
                  htmltext = "30476-02.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30476-01.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 2 && st.getQuestItemsCount(3173) == 1L) {
               htmltext = "30476-06.htm";
            } else if (cond == 9 || cond == 10) {
               htmltext = "30476-09.htm";
               st.takeItems(3184, 1L);
               if (st.getQuestItemsCount(3185) == 0L) {
                  st.giveItems(3185, 1L);
               }

               st.set("cond", "10");
               st.playSound("Itemsound.quest_middle");
               st.addSpawn(30613, 78977, 149036, -3597, 30000);
            } else if (cond == 13) {
               htmltext = "30476-11.htm";
            } else if (cond == 14) {
               if (st.getQuestItemsCount(3188) != 0L && player.getLevel() < 38) {
                  htmltext = "30476-14.htm";
               } else if (st.getQuestItemsCount(3188) != 0L && player.getLevel() >= 38) {
                  st.giveItems(3189, 1L);
                  st.takeItems(3188, 1L);
                  htmltext = "30476-15.htm";
                  st.set("cond", "15");
                  st.playSound("Itemsound.quest_middle");
               }
            } else if (cond == 15) {
               htmltext = "30476-16.htm";
            } else if (cond != 16 && cond != 17) {
               if (st.getQuestItemsCount(3174) > 0L || st.getQuestItemsCount(3175) > 0L) {
                  htmltext = "30476-07.htm";
               } else if (st.getQuestItemsCount(3176) > 0L || st.getQuestItemsCount(3177) > 0L) {
                  htmltext = "30476-08.htm";
               } else if (st.getQuestItemsCount(3185) > 0L || st.getQuestItemsCount(3186) > 0L) {
                  htmltext = "30476-10.htm";
               }
            } else {
               htmltext = "30476-17.htm";
            }
         } else if (npcId == 30614) {
            if (cond == 1) {
               htmltext = "30614-01.htm";
               st.takeItems(3173, 1L);
               st.giveItems(3174, 1L);
               st.set("cond", "2");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 2) {
               htmltext = "30614-02.htm";
            } else if (cond == 3) {
               st.takeItems(3175, 1L);
               st.giveItems(3176, 1L);
               htmltext = "30614-03.htm";
               st.set("cond", "5");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 8) {
               st.takeItems(3183, 1L);
               st.giveItems(3184, 1L);
               htmltext = "30614-05.htm";
               st.set("cond", "9");
               st.playSound("Itemsound.quest_middle");
            } else if (st.getQuestItemsCount(3176) > 0L || st.getQuestItemsCount(3177) > 0L) {
               htmltext = "30614-04.htm";
            } else if (st.getQuestItemsCount(3184) > 0L
               || st.getQuestItemsCount(3185) > 0L
               || st.getQuestItemsCount(3186) > 0L
               || st.getQuestItemsCount(3187) > 0L
               || st.getQuestItemsCount(3188) > 0L
               || st.getQuestItemsCount(3189) > 0L) {
               htmltext = "30614-06.htm";
            }
         } else if (npcId == 30463) {
            if (cond == 5) {
               st.takeItems(3176, 1L);
               st.giveItems(3177, 1L);
               htmltext = "30463-01.htm";
               st.set("cond", "6");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 6) {
               htmltext = "30463-02.htm";
            } else if (cond == 7
               && st.getQuestItemsCount(3178) >= 10L
               && st.getQuestItemsCount(3179) >= 10L
               && st.getQuestItemsCount(3180) >= 10L
               && st.getQuestItemsCount(3181) >= 10L
               && st.getQuestItemsCount(3182) >= 10L) {
               st.takeItems(3178, st.getQuestItemsCount(3178));
               st.takeItems(3179, st.getQuestItemsCount(3179));
               st.takeItems(3180, st.getQuestItemsCount(3180));
               st.takeItems(3181, st.getQuestItemsCount(3181));
               st.takeItems(3182, st.getQuestItemsCount(3182));
               st.takeItems(3177, 1L);
               st.giveItems(3183, 1L);
               htmltext = "30463-03.htm";
               st.set("cond", "8");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 7) {
               htmltext = "30463-02.htm";
               st.set("cond", "6");
            } else if (cond == 8) {
               htmltext = "30463-04.htm";
            } else if (st.getQuestItemsCount(3184) > 0L
               || st.getQuestItemsCount(3185) > 0L
               || st.getQuestItemsCount(3186) > 0L
               || st.getQuestItemsCount(3187) > 0L
               || st.getQuestItemsCount(3188) > 0L
               || st.getQuestItemsCount(3189) > 0L) {
               htmltext = "30614-06.htm";
            }
         } else if (npcId == 30613) {
            htmltext = "30613-02.htm";
            st.set("cond", "11");
         } else if (npcId == 30114) {
            if (cond == 11) {
               htmltext = "30114-01.htm";
            } else if (cond == 12) {
               htmltext = "30114-05.htm";
            } else if (st.getQuestItemsCount(3187) > 0L || st.getQuestItemsCount(3188) > 0L || st.getQuestItemsCount(3189) > 0L) {
               htmltext = "30114-06.htm";
            }
         } else if (npcId == 30210) {
            if (cond == 12) {
               st.takeItems(3186, 1L);
               st.giveItems(3187, 1L);
               htmltext = "30210-01.htm";
               st.set("cond", "13");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 13) {
               htmltext = "30210-02.htm";
            }
         } else if (npcId == 30358) {
            if (cond == 15) {
               st.takeItems(3189, 1L);
               st.giveItems(3191, 1L);
               st.giveItems(3190, 1L);
               htmltext = "30358-01.htm";
               st.set("cond", "16");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 16) {
               htmltext = "30358-02.htm";
            } else if (cond == 17) {
               htmltext = "30358-03.htm";
            } else if (cond == 18) {
               st.takeItems(1246, 1L);
               st.takeItems(3190, 1L);
               st.giveItems(3172, 1L);
               st.addExpAndSp(1365470, 91124);
               st.giveItems(57, 247708L);
               if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                  st.giveItems(7562, 16L);
                  player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
               }

               htmltext = "30358-04.htm";
               st.playSound("ItemSound.quest_finish");
               st.unset("cond");
               st.setState((byte)2);
               st.exitQuest(false);
            }
         } else if (npcId == 30419) {
            if (cond == 16) {
               htmltext = "30419-01.htm";
            } else if (cond == 17) {
               if (st.getQuestItemsCount(3198) < 1L || st.getQuestItemsCount(3201) < 1L) {
                  htmltext = "30419-03.htm";
               } else if (st.getQuestItemsCount(3198) >= 1L && st.getQuestItemsCount(3201) >= 1L) {
                  htmltext = "30419-04.htm";
               }
            } else if (cond == 18) {
               htmltext = "30419-06.htm";
            }
         } else if (npcId == 31845 && cond == 17) {
            if (st.getQuestItemsCount(3198) == 0L && st.getQuestItemsCount(3193) == 0L) {
               htmltext = "31845-01.htm";
            } else if (st.getQuestItemsCount(3198) != 0L
               || st.getQuestItemsCount(3193) <= 0L
               || st.getQuestItemsCount(3194) >= 10L
                  && st.getQuestItemsCount(3195) >= 10L
                  && st.getQuestItemsCount(3196) >= 10L
                  && st.getQuestItemsCount(3197) >= 10L) {
               if (st.getQuestItemsCount(3198) == 0L
                  && st.getQuestItemsCount(3193) > 0L
                  && st.getQuestItemsCount(3194) >= 10L
                  && st.getQuestItemsCount(3195) >= 10L
                  && st.getQuestItemsCount(3196) >= 10L
                  && st.getQuestItemsCount(3197) >= 10L) {
                  st.takeItems(3194, st.getQuestItemsCount(3194));
                  st.takeItems(3195, st.getQuestItemsCount(3195));
                  st.takeItems(3196, st.getQuestItemsCount(3196));
                  st.takeItems(3197, st.getQuestItemsCount(3197));
                  st.takeItems(3193, 1L);
                  st.giveItems(3198, 1L);
                  htmltext = "31845-04.htm";
               } else if (st.getQuestItemsCount(3198) != 0L) {
                  htmltext = "31845-05.htm";
               }
            } else {
               htmltext = "31845-03.htm";
            }
         } else if (npcId == 31850 && cond == 17) {
            if (st.getQuestItemsCount(3201) == 0L && st.getQuestItemsCount(3199) == 0L) {
               htmltext = "31850-01.htm";
            } else if (st.getQuestItemsCount(3201) == 0L && st.getQuestItemsCount(3199) > 0L && st.getQuestItemsCount(3200) == 0L) {
               htmltext = "31850-03.htm";
            } else if (st.getQuestItemsCount(3201) == 0L && st.getQuestItemsCount(3199) > 0L && st.getQuestItemsCount(3200) > 0L) {
               st.takeItems(3200, st.getQuestItemsCount(3200));
               st.takeItems(3199, 1L);
               st.giveItems(3201, 1L);
               htmltext = "31850-04.htm";
            } else if (st.getQuestItemsCount(3201) > 0L) {
               htmltext = "31850-05.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = this.getQuestState(player, false);
      if (st != null && st.isStarted() && Util.checkIfInRange(1500, npc, player, true)) {
         switch(npc.getId()) {
            case 20144:
               if (hasQuestItems(player, 3174) && !hasQuestItems(player, 3175)) {
                  takeItems(player, 3174, 1L);
                  giveItems(player, 3175, 1L);
                  st.setCond(3, true);
               }
            case 20157:
            case 20230:
            case 20232:
            case 20234:
               if (hasQuestItems(player, 3177) && getQuestItemsCount(player, 3182) < 10L) {
                  if (getQuestItemsCount(player, 3182) == 9L) {
                     giveItems(player, 3182, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(player, 3178) >= 10L
                        && getQuestItemsCount(player, 3179) >= 10L
                        && getQuestItemsCount(player, 3180) >= 10L
                        && getQuestItemsCount(player, 3181) >= 10L) {
                        st.setCond(7);
                     }
                  } else {
                     giveItems(player, 3182, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20158:
               if (hasQuestItems(player, 3177) && getQuestItemsCount(player, 3178) < 10L) {
                  if (getQuestItemsCount(player, 3178) == 9L) {
                     giveItems(player, 3178, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(player, 3179) >= 10L
                        && getQuestItemsCount(player, 3180) >= 10L
                        && getQuestItemsCount(player, 3181) >= 10L
                        && getQuestItemsCount(player, 3182) >= 10L) {
                        st.setCond(7);
                     }
                  } else {
                     giveItems(player, 3178, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20192:
            case 20193:
               if (hasQuestItems(player, 3177) && getQuestItemsCount(player, 3181) < 10L) {
                  if (getQuestItemsCount(player, 3181) == 9L) {
                     giveItems(player, 3181, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(player, 3178) >= 10L
                        && getQuestItemsCount(player, 3179) >= 10L
                        && getQuestItemsCount(player, 3180) >= 10L
                        && getQuestItemsCount(player, 3182) >= 10L) {
                        st.setCond(7);
                     }
                  } else {
                     giveItems(player, 3181, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20202:
               if (hasQuestItems(player, 3177) && getQuestItemsCount(player, 3180) < 10L) {
                  if (getQuestItemsCount(player, 3180) == 9L) {
                     giveItems(player, 3180, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(player, 3178) >= 10L
                        && getQuestItemsCount(player, 3179) >= 10L
                        && getQuestItemsCount(player, 3181) >= 10L
                        && getQuestItemsCount(player, 3182) >= 10L) {
                        st.setCond(7);
                     }
                  } else {
                     giveItems(player, 3180, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20233:
               if (hasQuestItems(player, 3177) && getQuestItemsCount(player, 3179) < 10L) {
                  if (getQuestItemsCount(player, 3179) == 9L) {
                     giveItems(player, 3179, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(player, 3178) >= 10L
                        && getQuestItemsCount(player, 3180) >= 10L
                        && getQuestItemsCount(player, 3181) >= 10L
                        && getQuestItemsCount(player, 3182) >= 10L) {
                        st.setCond(7);
                     }
                  } else {
                     giveItems(player, 3179, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20270:
               if (hasQuestItems(player, new int[]{3190, 3192, 3193}) && !hasQuestItems(player, 3198) && getQuestItemsCount(player, 3196) < 10L) {
                  if (getQuestItemsCount(player, 3196) == 9L) {
                     giveItems(player, 3196, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     giveItems(player, 3196, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20554:
               if (hasQuestItems(player, new int[]{3190, 3192, 3193}) && !hasQuestItems(player, 3198) && getQuestItemsCount(player, 3194) < 10L) {
                  if (getQuestItemsCount(player, 3194) == 9L) {
                     giveItems(player, 3194, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     giveItems(player, 3194, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20582:
               if (hasQuestItems(player, new int[]{3190, 3192, 3193}) && !hasQuestItems(player, 3198) && getQuestItemsCount(player, 3197) < 10L) {
                  if (getQuestItemsCount(player, 3197) == 9L) {
                     giveItems(player, 3197, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     giveItems(player, 3197, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20600:
               if (hasQuestItems(player, new int[]{3190, 3192, 3193}) && !hasQuestItems(player, 3198) && getQuestItemsCount(player, 3195) < 10L) {
                  if (getQuestItemsCount(player, 3195) == 9L) {
                     giveItems(player, 3195, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     giveItems(player, 3195, 1L);
                     playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 27079:
               if (hasQuestItems(player, new int[]{3190, 3192, 3199}) && !hasQuestItems(player, new int[]{3201, 3200})) {
                  giveItems(player, 3200, 1L);
                  playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
               }
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _219_TestimonyOfFate(219, "_219_TestimonyOfFate", "");
   }
}

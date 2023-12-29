package l2e.scripts.quests;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _337_AudienceWithTheLandDragon extends Quest {
   private static final String qn = "_337_AudienceWithTheLandDragon";
   private static final int GABRIELLE = 30753;
   private static final int ORVEN = 30857;
   private static final int KENDRA = 30851;
   private static final int CHAKIRIS = 30705;
   private static final int KAIENA = 30720;
   private static final int MOKE = 30498;
   private static final int HELTON = 30678;
   private static final int GILMORE = 30754;
   private static final int THEODRIC = 30755;
   private static final int BLOOD_QUEEN = 18001;
   private static final int SACRIFICE_OF_THE_SACRIFICED = 27171;
   private static final int HARIT_LIZARDMAN_SHAMAN = 20644;
   private static final int HARIT_LIZARDMAN_MATRIARCH = 20645;
   private static final int HARIT_LIZARDMAN_ZEALOT = 27172;
   private static final int KRANROT = 20650;
   private static final int HAMRUT = 20649;
   private static final int MARSH_DRAKE = 20680;
   private static final int MARSH_STALKER = 20679;
   private static final int ABYSSAL_JEWEL_1 = 27165;
   private static final int JEWEL_GUARDIAN_MARA = 27168;
   private static final int ABYSSAL_JEWEL_2 = 27166;
   private static final int JEWEL_GUARDIAN_MUSFEL = 27169;
   private static final int CAVE_MAIDEN = 20134;
   private static final int CAVE_KEEPER = 20246;
   private static final int ABYSSAL_JEWEL_3 = 27167;
   private static final int JEWEL_GUARDIAN_PYTON = 27170;
   private static final int FEATHER_OF_GABRIELLE = 3852;
   private static final int MARK_OF_WATCHMAN = 3864;
   private static final int REMAINS_OF_SACRIFIED = 3857;
   private static final int TOTEM_OF_LAND_DRAGON = 3858;
   private static final int KRANROT_SKIN = 3855;
   private static final int HAMRUT_LEG = 3856;
   private static final int MARSH_DRAKE_TALONS = 3854;
   private static final int MARSH_STALKER_HORN = 3853;
   private static final int FIRST_FRAGMENT_OF_ABYSS_JEWEL = 3859;
   private static final int MARA_FANG = 3862;
   private static final int SECOND_FRAGMENT_OF_ABYSS_JEWEL = 3860;
   private static final int MUSFEL_FANG = 3863;
   private static final int HERALD_OF_SLAYER = 3890;
   private static final int THIRD_FRAGMENT_OF_ABYSS_JEWEL = 3861;
   private static final int PORTAL_STONE = 3865;
   private static final int[][] DROPS_ON_KILL = new int[][]{
      {27171, 1, 1, 5, 3857},
      {27172, 1, 2, 25, 3858},
      {20650, 1, 3, 20, 3855},
      {20649, 1, 3, 20, 3856},
      {20680, 1, 4, 20, 3854},
      {20679, 1, 4, 20, 3853},
      {27168, 2, 5, 20, 3862},
      {27169, 2, 6, 20, 3863}
   };
   private static final int[][] DROP_ON_ATTACK = new int[][]{{27165, 2, 5, 3859, 20, 27168}, {27166, 2, 6, 3860, 20, 27169}, {27167, 4, 7, 3861, 3, 27170}};

   public _337_AudienceWithTheLandDragon(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30753);
      this.addTalkId(new int[]{30753, 30857, 30851, 30705, 30720, 30498, 30678, 30754, 30755});
      this.addAttackId(new int[]{27165, 27166, 27167});
      this.addKillId(new int[]{18001, 27171, 20644, 20645, 27172, 20650, 20649, 20680, 20679, 27168, 27169, 20134, 20246, 27170});
      this.questItemIds = new int[]{3852, 3864, 3857, 3858, 3855, 3856, 3854, 3853, 3859, 3862, 3860, 3863, 3890, 3861};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_337_AudienceWithTheLandDragon");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30753-05.htm")) {
            st.set("drop1", "1");
            st.set("drop2", "1");
            st.set("drop3", "1");
            st.set("drop4", "1");
            st.giveItems(3852, 1L);
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30753-09.htm")) {
            if (st.getQuestItemsCount(3864) >= 4L) {
               st.set("drop5", "2");
               st.set("drop6", "2");
               st.takeItems(3864, 4L);
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = null;
            }
         } else if (event.equalsIgnoreCase("30755-05.htm")) {
            if (st.hasQuestItems(3861)) {
               st.takeItems(3861, 1L);
               st.takeItems(3890, 1L);
               st.giveItems(3865, 1L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = null;
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_337_AudienceWithTheLandDragon");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 50) {
                  st.exitQuest(true);
                  htmltext = "30753-02.htm";
               } else {
                  htmltext = "30753-01.htm";
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               switch(npc.getId()) {
                  case 30498:
                     if (cond == 2) {
                        switch(st.getInt("drop5")) {
                           case 0:
                              if (st.getQuestItemsCount(3864) < 2L) {
                                 htmltext = "30498-04.htm";
                              } else {
                                 htmltext = "30498-05.htm";
                              }
                              break;
                           case 1:
                              if (st.hasQuestItems(3859) && st.hasQuestItems(3862)) {
                                 st.takeItems(3859, 1L);
                                 st.takeItems(3862, 1L);
                                 st.giveItems(3864, 1L);
                                 st.unset("drop5");
                                 st.playSound("ItemSound.quest_middle");
                                 htmltext = "30498-03.htm";
                              } else {
                                 htmltext = "30498-02.htm";
                              }
                              break;
                           case 2:
                              st.set("drop5", "1");
                              htmltext = "30498-01.htm";
                        }
                     }
                     break;
                  case 30678:
                     if (cond == 2) {
                        switch(st.getInt("drop6")) {
                           case 0:
                              if (st.getQuestItemsCount(3864) < 2L) {
                                 htmltext = "30678-04.htm";
                              } else {
                                 htmltext = "30678-05.htm";
                              }
                              break;
                           case 1:
                              if (st.hasQuestItems(3860) && st.hasQuestItems(3863)) {
                                 st.takeItems(3860, 1L);
                                 st.takeItems(3863, 1L);
                                 st.giveItems(3864, 1L);
                                 st.unset("drop6");
                                 st.playSound("ItemSound.quest_middle");
                                 htmltext = "30678-03.htm";
                              } else {
                                 htmltext = "30678-02.htm";
                              }
                              break;
                           case 2:
                              st.set("drop6", "1");
                              htmltext = "30678-01.htm";
                        }
                     }
                     break;
                  case 30705:
                     if (cond == 1) {
                        if (st.getInt("drop3") == 1) {
                           if (st.hasQuestItems(3855) && st.hasQuestItems(3856)) {
                              st.takeItems(3855, 1L);
                              st.takeItems(3856, 1L);
                              st.giveItems(3864, 1L);
                              st.unset("drop3");
                              st.playSound("ItemSound.quest_middle");
                              htmltext = "30705-02.htm";
                           } else {
                              htmltext = "30705-01.htm";
                           }
                        } else if (st.getQuestItemsCount(3864) < 4L) {
                           htmltext = "30705-03.htm";
                        } else {
                           htmltext = "30705-04.htm";
                        }
                     }
                     break;
                  case 30720:
                     if (cond == 1) {
                        if (st.getInt("drop4") == 1) {
                           if (st.hasQuestItems(3854) && st.hasQuestItems(3853)) {
                              st.takeItems(3854, 1L);
                              st.takeItems(3853, 1L);
                              st.giveItems(3864, 1L);
                              st.unset("drop4");
                              st.playSound("ItemSound.quest_middle");
                              htmltext = "30720-02.htm";
                           } else {
                              htmltext = "30720-01.htm";
                           }
                        } else if (st.getQuestItemsCount(3864) < 4L) {
                           htmltext = "30720-03.htm";
                        } else {
                           htmltext = "30720-04.htm";
                        }
                     }
                     break;
                  case 30753:
                     if (cond == 1) {
                        if (st.getQuestItemsCount(3864) < 4L) {
                           htmltext = "30753-06.htm";
                        } else {
                           htmltext = "30753-08.htm";
                        }
                     } else if (cond == 2) {
                        if (st.getQuestItemsCount(3864) < 2L) {
                           htmltext = "30753-10.htm";
                        } else {
                           st.takeItems(3852, 1L);
                           st.takeItems(3864, 1L);
                           st.giveItems(3890, 1L);
                           st.set("cond", "3");
                           st.playSound("ItemSound.quest_middle");
                           htmltext = "30753-11.htm";
                        }
                     } else if (cond == 3) {
                        htmltext = "30753-12.htm";
                     } else if (cond == 4) {
                        htmltext = "30753-13.htm";
                     }
                     break;
                  case 30754:
                     if (cond == 1 || cond == 2) {
                        htmltext = "30754-01.htm";
                     } else if (cond == 3) {
                        st.set("drop7", "1");
                        st.set("cond", "4");
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "30754-02.htm";
                     } else if (cond == 4) {
                        if (st.hasQuestItems(3861)) {
                           htmltext = "30754-05.htm";
                        } else {
                           htmltext = "30754-04.htm";
                        }
                     }
                     break;
                  case 30755:
                     if (cond == 1 || cond == 2) {
                        htmltext = "30755-01.htm";
                     } else if (cond == 3) {
                        htmltext = "30755-02.htm";
                     } else if (cond == 4) {
                        if (st.hasQuestItems(3861)) {
                           htmltext = "30755-04.htm";
                        } else {
                           htmltext = "30755-03.htm";
                        }
                     }
                     break;
                  case 30851:
                     if (cond == 1) {
                        if (st.getInt("drop2") == 1) {
                           if (st.hasQuestItems(3858)) {
                              st.takeItems(3858, 1L);
                              st.giveItems(3864, 1L);
                              st.unset("drop2");
                              st.playSound("ItemSound.quest_middle");
                              htmltext = "30851-02.htm";
                           } else {
                              htmltext = "30851-01.htm";
                           }
                        } else if (st.getQuestItemsCount(3864) < 4L) {
                           htmltext = "30851-03.htm";
                        } else {
                           htmltext = "30851-04.htm";
                        }
                     }
                     break;
                  case 30857:
                     if (cond == 1) {
                        if (st.getInt("drop1") == 1) {
                           if (st.hasQuestItems(3857)) {
                              st.takeItems(3857, 1L);
                              st.giveItems(3864, 1L);
                              st.unset("drop1");
                              st.playSound("ItemSound.quest_middle");
                              htmltext = "30857-02.htm";
                           } else {
                              htmltext = "30857-01.htm";
                           }
                        } else if (st.getQuestItemsCount(3864) < 4L) {
                           htmltext = "30857-03.htm";
                        } else {
                           htmltext = "30857-04.htm";
                        }
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      QuestState st = attacker.getQuestState("_337_AudienceWithTheLandDragon");
      if (st == null) {
         return null;
      } else if (st.getState() != 1) {
         return null;
      } else {
         int npcId = npc.getId();

         for(int[] npcInfo : DROP_ON_ATTACK) {
            if (npcInfo[0] == npcId) {
               if (npcInfo[1] == st.getInt("cond")) {
                  if (!(npc.getCurrentHp() < npc.getMaxHp() * 0.4) || !npc.isScriptValue(0) && !npc.isScriptValue(1)) {
                     if (npc.getCurrentHp() < npc.getMaxHp() * 0.8 && npc.isScriptValue(0) && st.getInt("drop" + npcInfo[2]) == 1) {
                        npc.setScriptValue(1);

                        for(int i = 0; i < npcInfo[4]; ++i) {
                           Npc mob = addSpawn(
                              npcInfo[5],
                              npc.getX() + getRandom(-150, 150),
                              npc.getY() + getRandom(-150, 150),
                              npc.getZ(),
                              npc.getHeading(),
                              true,
                              60000L,
                              false
                           );
                           mob.setRunning();
                           ((Attackable)mob).addDamageHate(attacker, 0, 500);
                           mob.getAI().setIntention(CtrlIntention.ATTACK, attacker);
                        }
                     }
                  } else if (st.getInt("drop" + npcInfo[2]) == 1) {
                     npc.setScriptValue(npc.isScriptValue(0) ? 1 : 2);
                     int itemId = npcInfo[3];
                     if (!st.hasQuestItems(itemId)) {
                        st.giveItems(itemId, 1L);
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
               }
               break;
            }
         }

         return super.onAttack(npc, attacker, damage, isSummon);
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_337_AudienceWithTheLandDragon");
      if (st == null) {
         return null;
      } else if (st.getState() != 1) {
         return null;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         switch(npcId) {
            case 18001:
               if (cond == 1 && getRandom(100) < 25 && st.getInt("drop1") == 1 && !st.hasQuestItems(3857)) {
                  for(int i = 0; i < 10; ++i) {
                     addSpawn(27171, npc.getX() + getRandom(-100, 100), npc.getY() + getRandom(-100, 100), npc.getZ(), npc.getHeading(), true, 60000L, false);
                  }
               }
               break;
            case 20134:
            case 20246:
               if (cond == 4 && getRandom(100) < 15 && !st.hasQuestItems(3861)) {
                  addSpawn(27167, npc.getX() + getRandom(-50, 50), npc.getY() + getRandom(-50, 50), npc.getZ(), npc.getHeading(), true, 60000L, false);
               }
               break;
            case 20644:
            case 20645:
               if (cond == 1 && getRandom(100) < 15 && st.getInt("drop2") == 1 && !st.hasQuestItems(3858)) {
                  for(int i = 0; i < 3; ++i) {
                     addSpawn(27172, npc.getX() + getRandom(-50, 50), npc.getY() + getRandom(-50, 50), npc.getZ(), npc.getHeading(), true, 60000L, false);
                  }
               }
               break;
            case 20649:
            case 20650:
            case 20679:
            case 20680:
            case 27168:
            case 27169:
            case 27171:
            case 27172:
               for(int[] npcInfo : DROPS_ON_KILL) {
                  if (npcInfo[0] == npcId) {
                     if (npcInfo[1] == cond && st.getInt("drop" + npcInfo[2]) == 1 && getRandom(100) < npcInfo[3]) {
                        int itemId = npcInfo[4];
                        if (!st.hasQuestItems(itemId)) {
                           st.giveItems(itemId, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        }
                     }
                     break;
                  }
               }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _337_AudienceWithTheLandDragon(337, "_337_AudienceWithTheLandDragon", "");
   }
}

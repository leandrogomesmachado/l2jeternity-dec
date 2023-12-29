package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import org.apache.commons.lang.ArrayUtils;

public final class _255_Tutorial extends Quest {
   public static final String qn = "_255_Tutorial";
   private static final String[][] QTEXMTWO = new String[][]{
      {"0", "tutorial_voice_001a", "tutorial_human_fighter001.htm"},
      {"10", "tutorial_voice_001b", "tutorial_human_mage001.htm"},
      {"18", "tutorial_voice_001c", "tutorial_elven_fighter001.htm"},
      {"25", "tutorial_voice_001d", "tutorial_elven_mage001.htm"},
      {"31", "tutorial_voice_001e", "tutorial_delf_fighter001.htm"},
      {"38", "tutorial_voice_001f", "tutorial_delf_mage001.htm"},
      {"44", "tutorial_voice_001g", "tutorial_orc_fighter001.htm"},
      {"49", "tutorial_voice_001h", "tutorial_orc_mage001.htm"},
      {"53", "tutorial_voice_001i", "tutorial_dwarven_fighter001.htm"},
      {"123", "tutorial_voice_001k", "tutorial_kamael_male001.htm"},
      {"124", "tutorial_voice_001j", "tutorial_kamael_female001.htm"}
   };
   private static final String[][] CEE_A = new String[][]{
      {"0", "tutorial_human_fighter007.htm", "-71424", "258336", "-3109"},
      {"10", "tutorial_human_mage007.htm", "-91036", "248044", "-3568"},
      {"18", "tutorial_elf007.htm", "46112", "41200", "-3504"},
      {"25", "tutorial_elf007.htm", "46112", "41200", "-3504"},
      {"31", "tutorial_delf007.htm", "28384", "11056", "-4233"},
      {"38", "tutorial_delf007.htm", "28384", "11056", "-4233"},
      {"44", "tutorial_orc007.htm", "-56736", "-113680", "-672"},
      {"49", "tutorial_orc007.htm", "-56736", "-113680", "-672"},
      {"53", "tutorial_dwarven_fighter007.htm", "108567", "-173994", "-406"},
      {"123", "tutorial_kamael007.htm", "-125872", "38016", "1251"},
      {"124", "tutorial_kamael007.htm", "-125872", "38016", "1251"}
   };
   private static final String[][] QMC_A = new String[][]{
      {"0", "tutorial_fighter017.htm", "-83165", "242711", "-3720"},
      {"10", "tutorial_mage017.htm", "-85247", "244718", "-3720"},
      {"18", "tutorial_fighter017.htm", "45610", "52206", "-2792"},
      {"25", "tutorial_mage017.htm", "45610", "52206", "-2792"},
      {"31", "tutorial_fighter017.htm", "10344", "14445", "-4242"},
      {"38", "tutorial_mage017.htm", "10344", "14445", "-4242"},
      {"44", "tutorial_orc_fighter017.htm", "-46324", "-114384", "-200"},
      {"49", "tutorial_orc_mage017.htm", "-46305", "-112763", "-200"},
      {"53", "tutorial_dwarven017.htm", "115447", "-182672", "-1440"},
      {"123", "tutorial_fighter017.htm", "-118132", "42788", "723"},
      {"124", "tutorial_fighter017.htm", "-118132", "42788", "723"}
   };
   private static final String[][] QMC_B = new String[][]{
      {"0", "tutorial_human009.htm"},
      {"10", "tutorial_human009.htm"},
      {"18", "tutorial_elf009.htm"},
      {"25", "tutorial_elf009.htm"},
      {"31", "tutorial_delf009.htm"},
      {"38", "tutorial_delf009.htm"},
      {"44", "tutorial_orc009.htm"},
      {"49", "tutorial_orc009.htm"},
      {"53", "tutorial_dwarven009.htm"},
      {"123", "tutorial_kamael009.htm"},
      {"124", "tutorial_kamael009.htm"}
   };
   private static final String[][] QMC_C = new String[][]{
      {"0", "tutorial_21.htm"},
      {"10", "tutorial_21a.htm"},
      {"18", "tutorial_21b.htm"},
      {"25", "tutorial_21c.htm"},
      {"31", "tutorial_21g.htm"},
      {"38", "tutorial_21h.htm"},
      {"44", "tutorial_21d.htm"},
      {"49", "tutorial_21e.htm"},
      {"53", "tutorial_21f.htm"}
   };
   private static final String[][] TCL_A = new String[][]{
      {"1", "tutorial_22w.htm"},
      {"4", "tutorial_22.htm"},
      {"7", "tutorial_22b.htm"},
      {"11", "tutorial_22c.htm"},
      {"15", "tutorial_22d.htm"},
      {"19", "tutorial_22e.htm"},
      {"22", "tutorial_22f.htm"},
      {"26", "tutorial_22g.htm"},
      {"29", "tutorial_22h.htm"},
      {"32", "tutorial_22n.htm"},
      {"35", "tutorial_22o.htm"},
      {"39", "tutorial_22p.htm"},
      {"42", "tutorial_22q.htm"},
      {"45", "tutorial_22i.htm"},
      {"47", "tutorial_22j.htm"},
      {"50", "tutorial_22k.htm"},
      {"54", "tutorial_22l.htm"},
      {"56", "tutorial_22m.htm"}
   };
   private static final String[][] TCL_B = new String[][]{
      {"4", "tutorial_22aa.htm"},
      {"7", "tutorial_22ba.htm"},
      {"11", "tutorial_22ca.htm"},
      {"15", "tutorial_22da.htm"},
      {"19", "tutorial_22ea.htm"},
      {"22", "tutorial_22fa.htm"},
      {"26", "tutorial_22ga.htm"},
      {"32", "tutorial_22na.htm"},
      {"35", "tutorial_22oa.htm"},
      {"39", "tutorial_22pa.htm"},
      {"50", "tutorial_22ka.htm"}
   };
   private static final String[][] TCL_C = new String[][]{
      {"4", "tutorial_22ab.htm"},
      {"7", "tutorial_22bb.htm"},
      {"11", "tutorial_22cb.htm"},
      {"15", "tutorial_22db.htm"},
      {"19", "tutorial_22eb.htm"},
      {"22", "tutorial_22fb.htm"},
      {"26", "tutorial_22gb.htm"},
      {"32", "tutorial_22nb.htm"},
      {"35", "tutorial_22ob.htm"},
      {"39", "tutorial_22pb.htm"},
      {"50", "tutorial_22kb.htm"}
   };

   public _255_Tutorial(int questId, String name, String descr) {
      super(questId, name, descr);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if (Config.DISABLE_TUTORIAL) {
         return "";
      } else {
         QuestState st = player.getQuestState("_255_Tutorial");
         String htmltext = "";
         int classId = player.getClassId().getId();
         int playerLevel = player.getLevel();
         int Ex = st.getInt("Ex");
         if (event.startsWith("UC")) {
            if (playerLevel < 6 && st.getInt("onlyone") == 0) {
               int uc = st.getInt("ucMemo");
               switch(uc) {
                  case 0:
                     st.set("ucMemo", "0");
                     st.startQuestTimer("QT", 10000L);
                     st.set("ucMemo", "0");
                     st.set("Ex", "-2");
                     break;
                  case 1:
                     st.showQuestionMark(false, 1);
                     st.playTutorialVoice("tutorial_voice_006");
                     st.playSound("ItemSound.quest_tutorial");
                     break;
                  case 2:
                     if (Ex == 2) {
                        st.showQuestionMark(false, 3);
                        st.playSound("ItemSound.quest_tutorial");
                     }

                     if (st.getQuestItemsCount(6353) == 1L) {
                        st.showQuestionMark(false, 5);
                        st.playSound("ItemSound.quest_tutorial");
                     }
                     break;
                  case 3:
                     st.showQuestionMark(false, 12);
                     st.playSound("ItemSound.quest_tutorial");
                     st.onTutorialClientEvent(0);
               }
            } else {
               switch(playerLevel) {
                  case 18:
                     if (player.getQuestState("_10276_MutatedKaneusGludio") == null) {
                        st.showQuestionMark(true, 33);
                        st.playSound("ItemSound.quest_tutorial");
                     }
                     break;
                  case 28:
                     if (player.getQuestState("_10277_MutatedKaneusDion") == null
                        || player.getQuestState("_10278_MutatedKaneusHeine") == null
                        || player.getQuestState("_10279_MutatedKaneusOren") == null
                        || player.getQuestState("_10280_MutatedKaneusSchuttgart") == null
                        || player.getQuestState("_10281_MutatedKaneusRune") == null) {
                        st.showQuestionMark(true, 33);
                        st.playSound("ItemSound.quest_tutorial");
                     }
                     break;
                  case 79:
                     if (player.getQuestState("_192_SevenSignSeriesOfDoubt") == null) {
                        st.showQuestionMark(true, 33);
                        st.playSound("ItemSound.quest_tutorial");
                     }
               }
            }
         } else if (event.startsWith("QT")) {
            switch(Ex) {
               case -4:
                  st.playTutorialVoice("tutorial_voice_008");
                  st.set("Ex", "-5");
                  break;
               case -3:
                  st.playTutorialVoice("tutorial_voice_002");
                  st.set("Ex", "0");
                  break;
               case -2:
                  String voice = "";

                  for(String[] element : QTEXMTWO) {
                     if (classId == Integer.valueOf(element[0])) {
                        voice = element[1];
                        htmltext = element[2];
                     }

                     st.playTutorialVoice(voice);
                  }

                  if (st.getQuestItemsCount(5588) == 0L) {
                     st.giveItems(5588, 1L);
                  }

                  st.set("Ex", "-3");
                  st.startQuestTimer("QT", 30000L);
            }
         } else if (event.startsWith("TE")) {
            int eventId = 0;

            try {
               eventId = Integer.valueOf(event.substring(2));
            } catch (IndexOutOfBoundsException var21) {
            } catch (NumberFormatException var22) {
            }

            switch(eventId) {
               case 0:
                  st.closeTutorialHtml();
                  break;
               case 1:
                  st.closeTutorialHtml();
                  st.playTutorialVoice("tutorial_voice_006");
                  st.showQuestionMark(false, 1);
                  st.playSound("ItemSound.quest_tutorial");
                  st.startQuestTimer("QT", 30000L);
                  st.set("Ex", "-4");
                  break;
               case 2:
                  st.playTutorialVoice("tutorial_voice_003");
                  htmltext = "tutorial_02.htm";
                  st.onTutorialClientEvent(1);
                  st.set("Ex", "-5");
                  break;
               case 3:
                  htmltext = "tutorial_03.htm";
                  st.onTutorialClientEvent(2);
               case 4:
               case 6:
               case 9:
               case 11:
               case 13:
               case 14:
               case 15:
               case 16:
               case 17:
               case 18:
               case 19:
               case 20:
               case 21:
               case 22:
               default:
                  break;
               case 5:
                  htmltext = "tutorial_05.htm";
                  st.onTutorialClientEvent(8);
                  break;
               case 7:
                  htmltext = "tutorial_100.htm";
                  st.onTutorialClientEvent(0);
                  break;
               case 8:
                  htmltext = "tutorial_101.htm";
                  st.onTutorialClientEvent(0);
                  break;
               case 10:
                  htmltext = "tutorial_103.htm";
                  st.onTutorialClientEvent(0);
                  break;
               case 12:
                  st.closeTutorialHtml();
                  break;
               case 23:
                  for(String[] element : TCL_B) {
                     if (classId == Integer.valueOf(element[0])) {
                        htmltext = element[1];
                     }
                  }
                  break;
               case 24:
                  for(String[] element : TCL_C) {
                     if (classId == Integer.valueOf(element[0])) {
                        htmltext = element[1];
                     }
                  }
                  break;
               case 25:
                  htmltext = "tutorial_22cc.htm";
                  break;
               case 26:
                  for(String[] element : TCL_A) {
                     if (classId == Integer.valueOf(element[0])) {
                        htmltext = element[1];
                     }
                  }
                  break;
               case 27:
                  htmltext = "tutorial_29.htm";
                  break;
               case 28:
                  htmltext = "tutorial_28.htm";
            }
         } else if (event.startsWith("CE")) {
            int eventId = 0;

            try {
               eventId = Integer.valueOf(event.substring(2));
            } catch (IndexOutOfBoundsException var19) {
            } catch (NumberFormatException var20) {
            }

            label389:
            switch(eventId) {
               case 1:
                  if (playerLevel < 6) {
                     st.playTutorialVoice("tutorial_voice_004");
                     htmltext = "tutorial_03.htm";
                     st.playSound("ItemSound.quest_tutorial");
                     st.onTutorialClientEvent(2);
                  }
                  break;
               case 2:
                  if (playerLevel < 6) {
                     st.playTutorialVoice("tutorial_voice_005");
                     htmltext = "tutorial_05.htm";
                     st.playSound("ItemSound.quest_tutorial");
                     st.onTutorialClientEvent(8);
                  }
                  break;
               case 8:
                  if (playerLevel < 6) {
                     int x = 0;
                     int y = 0;
                     int z = 0;

                     for(String[] element : CEE_A) {
                        if (classId == Integer.valueOf(element[0])) {
                           htmltext = element[1];
                           x = Integer.valueOf(element[2]);
                           y = Integer.valueOf(element[3]);
                           z = Integer.valueOf(element[4]);
                        }
                     }

                     st.addRadar(x, y, z);
                     st.playTutorialVoice("tutorial_voice_007");
                     st.set("ucMemo", "1");
                     st.set("Ex", "-5");
                  }
                  break;
               case 30:
                  if (playerLevel < 10 && st.getInt("Die") == 0) {
                     st.playTutorialVoice("tutorial_voice_016");
                     st.playSound("ItemSound.quest_tutorial");
                     st.set("Die", "1");
                     st.showQuestionMark(false, 8);
                     st.onTutorialClientEvent(0);
                  }
                  break;
               case 40:
                  if (playerLevel == 5 && player.getClassId().level() == 0) {
                     if (st.getInt("lvl") < 5 && (!player.getClassId().isMage() || classId == 49)) {
                        st.playTutorialVoice("tutorial_voice_014");
                        st.showQuestionMark(false, 9);
                        st.playSound("ItemSound.quest_tutorial");
                        st.set("lvl", "5");
                     }
                  } else if (playerLevel == 6 && st.getInt("lvl") < 6 && player.getClassId().level() == 0) {
                     st.playTutorialVoice("tutorial_voice_020");
                     st.playSound("ItemSound.quest_tutorial");
                     st.showQuestionMark(false, 24);
                     st.set("lvl", "6");
                  } else if (playerLevel == 7 && player.getClassId().isMage() && classId != 49) {
                     if (st.getInt("lvl") < 7 && player.getClassId().level() == 0) {
                        st.playTutorialVoice("tutorial_voice_019");
                        st.playSound("ItemSound.quest_tutorial");
                        st.set("lvl", "7");
                        st.showQuestionMark(false, 11);
                     }
                  } else {
                     switch(playerLevel) {
                        case 10:
                           if (st.getInt("lvl") < 10) {
                              st.playTutorialVoice("tutorial_voice_030");
                              st.playSound("ItemSound.quest_tutorial");
                              st.set("lvl", "10");
                              st.showQuestionMark(false, 27);
                           }
                           break label389;
                        case 15:
                           if (st.getInt("lvl") < 15) {
                              st.playSound("ItemSound.quest_tutorial");
                              st.set("lvl", "15");
                              st.showQuestionMark(false, 17);
                           }
                           break label389;
                        case 18:
                           if (st.getInt("lvl") < 18) {
                              st.playSound("ItemSound.quest_tutorial");
                              st.set("lvl", "18");
                              st.showQuestionMark(false, 33);
                           }
                           break label389;
                        case 19:
                           if (st.getInt("lvl") < 19) {
                              int race = player.getRace().ordinal();
                              if (race != 5 && player.getClassId().level() == 0) {
                                 int[] classIds = new int[]{0, 10, 18, 25, 31, 38, 44, 49, 52};
                                 if (ArrayUtils.contains(classIds, classId)) {
                                    st.playSound("ItemSound.quest_tutorial");
                                    st.set("lvl", "19");
                                    st.showQuestionMark(false, 35);
                                 }
                              }
                           }
                           break label389;
                        case 28:
                           if (st.getInt("lvl") < 28) {
                              st.playSound("ItemSound.quest_tutorial");
                              st.set("lvl", "28");
                              st.showQuestionMark(false, 33);
                           }
                           break label389;
                        case 35:
                           if (st.getInt("lvl") < 35) {
                              int race = player.getRace().ordinal();
                              if (race != 5 && player.getClassId().level() == 1) {
                                 int[] classIds = new int[]{1, 4, 7, 11, 15, 19, 22, 26, 29, 32, 35, 39, 42, 45, 47, 50, 54, 56};
                                 if (ArrayUtils.contains(classIds, classId)) {
                                    st.playSound("ItemSound.quest_tutorial");
                                    st.set("lvl", "35");
                                    st.showQuestionMark(false, 34);
                                 }
                              }
                           }
                           break label389;
                        case 38:
                           if (st.getInt("lvl") < 38) {
                              st.playSound("ItemSound.quest_tutorial");
                              st.set("lvl", "38");
                              st.showQuestionMark(false, 33);
                           }
                           break label389;
                        case 48:
                           if (st.getInt("lvl") < 48) {
                              st.playSound("ItemSound.quest_tutorial");
                              st.set("lvl", "48");
                              st.showQuestionMark(false, 33);
                           }
                           break label389;
                        case 58:
                           if (st.getInt("lvl") < 58) {
                              st.playSound("ItemSound.quest_tutorial");
                              st.set("lvl", "58");
                              st.showQuestionMark(false, 33);
                           }
                           break label389;
                        case 68:
                           if (st.getInt("lvl") < 68) {
                              st.playSound("ItemSound.quest_tutorial");
                              st.set("lvl", "68");
                              st.showQuestionMark(false, 33);
                           }
                           break label389;
                        case 79:
                           if (st.getInt("lvl") < 79) {
                              st.playSound("ItemSound.quest_tutorial");
                              st.set("lvl", "79");
                              st.showQuestionMark(false, 33);
                           }
                     }
                  }
                  break;
               case 45:
                  if (playerLevel < 10 && st.getInt("HP") == 0) {
                     st.playTutorialVoice("tutorial_voice_017");
                     st.playSound("ItemSound.quest_tutorial");
                     st.set("HP", "1");
                     st.showQuestionMark(false, 10);
                     st.onTutorialClientEvent(800000);
                  }
                  break;
               case 57:
                  if (playerLevel < 6 && st.getInt("Adena") == 0) {
                     st.playTutorialVoice("tutorial_voice_012");
                     st.playSound("ItemSound.quest_tutorial");
                     st.set("Adena", "1");
                     st.showQuestionMark(false, 23);
                  }
                  break;
               case 6353:
                  if (playerLevel < 6 && st.getInt("Gemstone") == 0) {
                     st.playTutorialVoice("tutorial_voice_013");
                     st.playSound("ItemSound.quest_tutorial");
                     st.set("Gemstone", "1");
                     st.showQuestionMark(false, 5);
                  }
                  break;
               case 800000:
                  if (playerLevel < 6 && st.getInt("sit") == 0) {
                     st.playTutorialVoice("tutorial_voice_018");
                     st.playSound("ItemSound.quest_tutorial");
                     st.set("sit", "1");
                     st.onTutorialClientEvent(0);
                     htmltext = "tutorial_21z.htm";
                  }
            }
         } else if (event.startsWith("QM")) {
            int eventId = 0;

            try {
               eventId = Integer.valueOf(event.substring(2));
            } catch (IndexOutOfBoundsException var17) {
            } catch (NumberFormatException var18) {
            }

            int x = 0;
            int y = 0;
            int z = 0;
            label364:
            switch(eventId) {
               case 1:
                  st.playTutorialVoice("tutorial_voice_007");
                  st.set("Ex", "-5");

                  for(String[] element : CEE_A) {
                     if (classId == Integer.valueOf(element[0])) {
                        htmltext = element[1];
                        x = Integer.valueOf(element[2]);
                        y = Integer.valueOf(element[3]);
                        z = Integer.valueOf(element[4]);
                     }
                  }

                  st.addRadar(x, y, z);
               case 2:
               case 4:
               case 6:
               case 14:
               case 15:
               case 16:
               case 18:
               case 19:
               case 20:
               case 21:
               case 22:
               case 25:
               case 28:
               case 29:
               case 30:
               case 31:
               case 32:
               default:
                  break;
               case 3:
                  htmltext = "tutorial_09.htm";
                  break;
               case 5:
                  for(String[] element : CEE_A) {
                     if (classId == Integer.valueOf(element[0])) {
                        htmltext = element[1];
                        x = Integer.valueOf(element[2]);
                        y = Integer.valueOf(element[3]);
                        z = Integer.valueOf(element[4]);
                     }
                  }

                  st.addRadar(x, y, z);
                  htmltext = "tutorial_11.htm";
                  break;
               case 7:
                  htmltext = "tutorial_15.htm";
                  st.set("ucMemo", "3");
                  break;
               case 8:
                  htmltext = "tutorial_18.htm";
                  break;
               case 9:
                  for(String[] element : QMC_A) {
                     if (classId == Integer.valueOf(element[0])) {
                        htmltext = element[1];
                        x = Integer.valueOf(element[2]);
                        y = Integer.valueOf(element[3]);
                        z = Integer.valueOf(element[4]);
                     }
                  }

                  st.addRadar(x, y, z);
                  break;
               case 10:
                  htmltext = "tutorial_19.htm";
                  break;
               case 11:
                  for(String[] element : QMC_A) {
                     if (classId == Integer.valueOf(element[0])) {
                        htmltext = element[1];
                        x = Integer.valueOf(element[2]);
                        y = Integer.valueOf(element[3]);
                        z = Integer.valueOf(element[4]);
                     }
                  }

                  st.addRadar(x, y, z);
                  break;
               case 12:
                  htmltext = "tutorial_15.htm";
                  st.set("ucMemo", "4");
                  break;
               case 13:
                  htmltext = "tutorial_30.htm";
                  break;
               case 17:
                  htmltext = "tutorial_27.htm";
                  break;
               case 23:
                  htmltext = "tutorial_24.htm";
                  break;
               case 24:
                  for(String[] element : QMC_B) {
                     if (classId == Integer.valueOf(element[0])) {
                        htmltext = element[1];
                     }
                  }
                  break;
               case 26:
                  if (player.getClassId().isMage() && classId != 49) {
                     htmltext = "tutorial_newbie004b.htm";
                  } else {
                     htmltext = "tutorial_newbie004a.htm";
                  }
                  break;
               case 27:
                  htmltext = "tutorial_20.htm";
                  break;
               case 33:
                  switch(playerLevel) {
                     case 18:
                        htmltext = "tutorial_kama_18.htm";
                        break label364;
                     case 28:
                        htmltext = "tutorial_kama_28.htm";
                        break label364;
                     case 38:
                        htmltext = "tutorial_kama_38.htm";
                        break label364;
                     case 48:
                        htmltext = "tutorial_kama_48.htm";
                        break label364;
                     case 58:
                        htmltext = "tutorial_kama_58.htm";
                        break label364;
                     case 68:
                        htmltext = "tutorial_kama_68.htm";
                        break label364;
                     case 79:
                        htmltext = "tutorial_epic_quest.htm";
                     default:
                        break label364;
                  }
               case 34:
                  htmltext = "tutorial_28.htm";
                  break;
               case 35:
                  for(String[] element : QMC_C) {
                     if (classId == Integer.valueOf(element[0])) {
                        htmltext = element[1];
                     }
                  }
            }
         }

         if (htmltext.isEmpty()) {
            return "";
         } else {
            st.showTutorialHTML(htmltext);
            return "";
         }
      }
   }

   public static void main(String[] args) {
      new _255_Tutorial(255, "_255_Tutorial", "");
   }
}

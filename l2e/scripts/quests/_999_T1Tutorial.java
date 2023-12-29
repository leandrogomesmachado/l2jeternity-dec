package l2e.scripts.quests;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.HashMap;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.strings.server.ServerStorage;

public class _999_T1Tutorial extends Quest {
   private static String qnTutorial = "_255_Tutorial";
   private static String qn = "_999_T1Tutorial";
   private static int RECOMMENDATION_01 = 1067;
   private static int RECOMMENDATION_02 = 1068;
   private static int LEAF_OF_MOTHERTREE = 1069;
   private static int BLOOD_OF_JUNDIN = 1070;
   private static int LICENSE_OF_MINER = 1498;
   private static int VOUCHER_OF_FLAME = 1496;
   private static int SOULSHOT_NOVICE = 5789;
   private static int SPIRITSHOT_NOVICE = 5790;
   private static int BLUE_GEM = 6353;
   private static int DIPLOMA = 9881;
   private static final int[] NPCS = new int[]{
      30008, 30009, 30017, 30019, 30129, 30131, 30573, 30575, 30370, 30528, 30530, 30400, 30401, 30402, 30403, 30404, 32133, 32134
   };
   private static final HashMap<Object, Object[]> Event = new HashMap<>();
   private static final TIntObjectHashMap<_999_T1Tutorial.Talk> Talks = new TIntObjectHashMap<>();

   public _999_T1Tutorial(int questId, String name, String descr) {
      super(questId, name, descr);
      Event.put("32133_02", new Object[]{"32133-03.htm", -119692, 44504, 380, DIPLOMA, 123, SOULSHOT_NOVICE, 200, 124, SOULSHOT_NOVICE, 200});
      Event.put("30008_02", new Object[]{"30008-03.htm", 0, 0, 0, RECOMMENDATION_01, 0, SOULSHOT_NOVICE, 200, 0, 0, 0});
      Event.put("30008_04", new Object[]{"30008-04.htm", -84058, 243239, -3730, 0, 0, 0, 0, 0, 0, 0});
      Event.put("30017_02", new Object[]{"30017-03.htm", 0, 0, 0, RECOMMENDATION_02, 10, SPIRITSHOT_NOVICE, 100, 0, 0, 0});
      Event.put("30017_04", new Object[]{"30017-04.htm", -84058, 243239, -3730, 0, 10, 0, 0, 0, 0, 0});
      Event.put("30370_02", new Object[]{"30370-03.htm", 0, 0, 0, LEAF_OF_MOTHERTREE, 25, SPIRITSHOT_NOVICE, 100, 18, SOULSHOT_NOVICE, 200});
      Event.put("30370_04", new Object[]{"30370-04.htm", 45491, 48359, -3086, 0, 25, 0, 0, 18, 0, 0});
      Event.put("30129_02", new Object[]{"30129-03.htm", 0, 0, 0, BLOOD_OF_JUNDIN, 38, SPIRITSHOT_NOVICE, 100, 31, SOULSHOT_NOVICE, 200});
      Event.put("30129_04", new Object[]{"30129-04.htm", 12116, 16666, -4610, 0, 38, 0, 0, 31, 0, 0});
      Event.put("30528_02", new Object[]{"30528-03.htm", 0, 0, 0, LICENSE_OF_MINER, 53, SOULSHOT_NOVICE, 200, 0, 0, 0});
      Event.put("30528_04", new Object[]{"30528-04.htm", 115642, -178046, -941, 0, 53, 0, 0, 0, 0, 0});
      Event.put("30573_02", new Object[]{"30573-03.htm", 0, 0, 0, VOUCHER_OF_FLAME, 49, SPIRITSHOT_NOVICE, 100, 44, SOULSHOT_NOVICE, 200});
      Event.put("30573_04", new Object[]{"30573-04.htm", -45067, -113549, -235, 0, 49, 0, 0, 44, 0, 0});
      Talks.put(30017, new _999_T1Tutorial.Talk(0, new String[]{"30017-01.htm", "30017-02.htm", "30017-04.htm"}, 0, 0));
      Talks.put(30008, new _999_T1Tutorial.Talk(0, new String[]{"30008-01.htm", "30008-02.htm", "30008-04.htm"}, 0, 0));
      Talks.put(30370, new _999_T1Tutorial.Talk(1, new String[]{"30370-01.htm", "30370-02.htm", "30370-04.htm"}, 0, 0));
      Talks.put(30129, new _999_T1Tutorial.Talk(2, new String[]{"30129-01.htm", "30129-02.htm", "30129-04.htm"}, 0, 0));
      Talks.put(30573, new _999_T1Tutorial.Talk(3, new String[]{"30573-01.htm", "30573-02.htm", "30573-04.htm"}, 0, 0));
      Talks.put(30528, new _999_T1Tutorial.Talk(4, new String[]{"30528-01.htm", "30528-02.htm", "30528-04.htm"}, 0, 0));
      Talks.put(30018, new _999_T1Tutorial.Talk(0, new String[]{"30131-01.htm", "", "30019-03a.htm", "30019-04.htm"}, 1, RECOMMENDATION_02));
      Talks.put(30019, new _999_T1Tutorial.Talk(0, new String[]{"30131-01.htm", "", "30019-03a.htm", "30019-04.htm"}, 1, RECOMMENDATION_02));
      Talks.put(30020, new _999_T1Tutorial.Talk(0, new String[]{"30131-01.htm", "", "30019-03a.htm", "30019-04.htm"}, 1, RECOMMENDATION_02));
      Talks.put(30021, new _999_T1Tutorial.Talk(0, new String[]{"30131-01.htm", "", "30019-03a.htm", "30019-04.htm"}, 1, RECOMMENDATION_02));
      Talks.put(30009, new _999_T1Tutorial.Talk(0, new String[]{"30530-01.htm", "30009-03.htm", "", "30009-04.htm"}, 1, RECOMMENDATION_01));
      Talks.put(30011, new _999_T1Tutorial.Talk(0, new String[]{"30530-01.htm", "30009-03.htm", "", "30009-04.htm"}, 1, RECOMMENDATION_01));
      Talks.put(30012, new _999_T1Tutorial.Talk(0, new String[]{"30530-01.htm", "30009-03.htm", "", "30009-04.htm"}, 1, RECOMMENDATION_01));
      Talks.put(30056, new _999_T1Tutorial.Talk(0, new String[]{"30530-01.htm", "30009-03.htm", "", "30009-04.htm"}, 1, RECOMMENDATION_01));
      Talks.put(30400, new _999_T1Tutorial.Talk(1, new String[]{"30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm"}, 1, LEAF_OF_MOTHERTREE));
      Talks.put(30401, new _999_T1Tutorial.Talk(1, new String[]{"30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm"}, 1, LEAF_OF_MOTHERTREE));
      Talks.put(30402, new _999_T1Tutorial.Talk(1, new String[]{"30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm"}, 1, LEAF_OF_MOTHERTREE));
      Talks.put(30403, new _999_T1Tutorial.Talk(1, new String[]{"30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm"}, 1, LEAF_OF_MOTHERTREE));
      Talks.put(30131, new _999_T1Tutorial.Talk(2, new String[]{"30131-01.htm", "30131-03.htm", "30131-03a.htm", "30131-04.htm"}, 1, BLOOD_OF_JUNDIN));
      Talks.put(30404, new _999_T1Tutorial.Talk(2, new String[]{"30131-01.htm", "30131-03.htm", "30131-03a.htm", "30131-04.htm"}, 1, BLOOD_OF_JUNDIN));
      Talks.put(30574, new _999_T1Tutorial.Talk(3, new String[]{"30575-01.htm", "30575-03.htm", "30575-03a.htm", "30575-04.htm"}, 1, VOUCHER_OF_FLAME));
      Talks.put(30575, new _999_T1Tutorial.Talk(3, new String[]{"30575-01.htm", "30575-03.htm", "30575-03a.htm", "30575-04.htm"}, 1, VOUCHER_OF_FLAME));
      Talks.put(30530, new _999_T1Tutorial.Talk(4, new String[]{"30530-01.htm", "30530-03.htm", "", "30530-04.htm"}, 1, LICENSE_OF_MINER));
      Talks.put(32133, new _999_T1Tutorial.Talk(5, new String[]{"32133-01.htm", "32133-02.htm", "32133-04.htm"}, 0, 0));
      Talks.put(32134, new _999_T1Tutorial.Talk(5, new String[]{"32134-01.htm", "32134-03.htm", "", "32134-04.htm"}, 1, DIPLOMA));

      for(int startNpc : NPCS) {
         this.addStartNpc(startNpc);
      }

      for(int FirstTalkId : NPCS) {
         this.addFirstTalkId(FirstTalkId);
      }

      for(int TalkId : NPCS) {
         this.addTalkId(TalkId);
      }

      this.addKillId(18342);
      this.addKillId(20001);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (Config.DISABLE_TUTORIAL) {
         return null;
      } else {
         QuestState st = player.getQuestState(qn);
         QuestState qs = player.getQuestState(qnTutorial);
         if (qs == null) {
            return event;
         } else {
            int Ex = qs.getInt("Ex");
            if (event.equalsIgnoreCase("TimerEx_NewbieHelper")) {
               if (Ex == 0) {
                  if (player.getClassId().isMage()) {
                     st.playTutorialVoice("tutorial_voice_009b");
                  } else {
                     st.playTutorialVoice("tutorial_voice_009a");
                  }

                  qs.set("Ex", "1");
               } else if (Ex == 3) {
                  st.playTutorialVoice("tutorial_voice_010a");
                  qs.set("Ex", "4");
               }

               return null;
            } else if (event.equalsIgnoreCase("TimerEx_GrandMaster")) {
               if (Ex >= 4) {
                  st.showQuestionMark(false, 7);
                  st.playSound("ItemSound.quest_tutorial");
                  st.playTutorialVoice("tutorial_voice_025");
               }

               return null;
            } else {
               String htmltext;
               if (event.equalsIgnoreCase("isle")) {
                  st.addRadar(-119692, 44504, 380);
                  player.teleToLocation(-120050, 44500, 360, true);
                  htmltext = "<html><body>"
                     + npc.getName()
                     + ":<br>"
                     + ServerStorage.getInstance().getString(player.getLang(), "999quest.ISLE")
                     + "</body></html>";
               } else {
                  Object[] map = (Object[])Event.get(event);
                  htmltext = (String)map[0];
                  int radarX = map[1];
                  int radarY = map[2];
                  int radarZ = map[3];
                  int item = map[4];
                  int classId1 = map[5];
                  int gift1 = map[6];
                  int count1 = map[7];
                  int classId2 = map[8];
                  int gift2 = map[9];
                  int count2 = map[10];
                  if (radarX != 0) {
                     st.addRadar(radarX, radarY, radarZ);
                  }

                  if (st.getQuestItemsCount(item) > 0L && st.getInt("onlyone") == 0) {
                     st.addExpAndSp(0, 50);
                     st.startQuestTimer("TimerEx_GrandMaster", 60000L);
                     st.takeItems(item, 1L);
                     st.set("step", "3");
                     if (Ex <= 3) {
                        qs.set("Ex", "4");
                     }

                     if (player.getClassId().getId() == classId1) {
                        st.giveItems(gift1, (long)count1);
                        if (gift1 == SPIRITSHOT_NOVICE) {
                           st.playTutorialVoice("tutorial_voice_027");
                        } else {
                           st.playTutorialVoice("tutorial_voice_026");
                        }
                     } else if (player.getClassId().getId() == classId2 && gift2 != 0) {
                        st.giveItems(gift2, (long)count2);
                        st.playTutorialVoice("tutorial_voice_026");
                     }

                     st.set("step", "4");
                     st.set("onlyone", "1");
                  }
               }

               return htmltext;
            }
         }
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (Config.DISABLE_TUTORIAL) {
         npc.showChatWindow(player);
         return null;
      } else {
         String htmltext = "";
         QuestState st = player.getQuestState(qn);
         if (st == null) {
            st = this.newQuestState(player);
         }

         QuestState qs = player.getQuestState(qnTutorial);
         if (qs != null && !qs.isCompleted()) {
            int onlyone = st.getInt("onlyone");
            int Ex = qs.getInt("Ex");
            int step = st.getInt("step");
            _999_T1Tutorial.Talk talk = Talks.get(npc.getId());
            if ((player.getLevel() >= 10 || onlyone != 0) && talk.npcTyp == 1) {
               htmltext = "30575-05.htm";
            } else if (onlyone == 0 && player.getLevel() < 10) {
               if (talk != null && player.getRace().ordinal() == talk.raceId) {
                  htmltext = talk.htmlfiles[0];
                  if (talk.npcTyp == 1) {
                     if (step == 0 && Ex < 0) {
                        qs.set("Ex", "0");
                        st.startQuestTimer("TimerEx_NewbieHelper", 30000L);
                        if (player.getClassId().isMage()) {
                           st.set("step", "1");
                           st.setState((byte)1);
                        } else {
                           htmltext = "30530-01.htm";
                           st.set("step", "1");
                           st.setState((byte)1);
                        }
                     } else if (step == 1 && st.getQuestItemsCount(talk.item) == 0L && Ex <= 2) {
                        if (st.getQuestItemsCount(BLUE_GEM) != 0L) {
                           st.takeItems(BLUE_GEM, -1L);
                           st.giveItems(talk.item, 1L);
                           st.set("step", "2");
                           qs.set("Ex", "3");
                           st.startQuestTimer("TimerEx_NewbieHelper", 30000L);
                           qs.set("ucMemo", "3");
                           if (player.getClassId().isMage()) {
                              st.playTutorialVoice("tutorial_voice_027");
                              st.giveItems(SPIRITSHOT_NOVICE, 100L);
                              htmltext = talk.htmlfiles[2];
                              if (htmltext.equals("")) {
                                 htmltext = "<html><body>"
                                    + ServerStorage.getInstance().getString(player.getLang(), "999quest.CAN_NOT_HELP")
                                    + "</body></html>";
                              }
                           } else {
                              st.playTutorialVoice("tutorial_voice_026");
                              st.giveItems(SOULSHOT_NOVICE, 200L);
                              htmltext = talk.htmlfiles[1];
                              if (htmltext.equals("")) {
                                 htmltext = "<html><body>"
                                    + ServerStorage.getInstance().getString(player.getLang(), "999quest.CAN_NOT_HELP1")
                                    + "</body></html>";
                              }
                           }
                        } else {
                           if (player.getClassId().isMage()) {
                              htmltext = "30131-02.htm";
                           }

                           if (player.getRace().ordinal() == 3) {
                              htmltext = "30575-02.htm";
                           } else {
                              htmltext = "30530-02.htm";
                           }
                        }
                     } else if (step == 2) {
                        htmltext = talk.htmlfiles[3];
                     }
                  } else if (talk.npcTyp == 0) {
                     if (step == 1) {
                        htmltext = talk.htmlfiles[0];
                     } else if (step == 2) {
                        htmltext = talk.htmlfiles[1];
                     } else if (step == 3) {
                        htmltext = talk.htmlfiles[2];
                     }
                  }
               }
            } else if (step == 4 && player.getLevel() < 10) {
               htmltext = npc.getId() + "-04.htm";
            }

            if (htmltext == null || htmltext.equals("")) {
               npc.showChatWindow(player);
            }

            npc.showChatWindow(player);
            return htmltext;
         } else {
            npc.showChatWindow(player);
            return null;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return null;
      } else {
         QuestState qs = player.getQuestState(qnTutorial);
         if (qs == null) {
            return null;
         } else {
            int Ex = qs.getInt("Ex");
            if (Ex <= 1) {
               st.playTutorialVoice("tutorial_voice_011");
               st.showQuestionMark(false, 3);
               qs.set("Ex", "2");
            }

            if (Ex <= 2 && st.getQuestItemsCount(BLUE_GEM) < 1L && getRandom(100) < 100) {
               ((MonsterInstance)npc).dropItem(player, BLUE_GEM, 1L);
               st.playSound("ItemSound.quest_tutorial");
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _999_T1Tutorial(999, qn, "");
   }

   private static class Talk {
      public int raceId;
      public String[] htmlfiles;
      public int npcTyp;
      public int item;

      public Talk(int _raceId, String[] _htmlfiles, int _npcTyp, int _item) {
         this.raceId = _raceId;
         this.htmlfiles = _htmlfiles;
         this.npcTyp = _npcTyp;
         this.item = _item;
      }
   }
}

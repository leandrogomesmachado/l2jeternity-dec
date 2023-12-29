package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;

public class _348_ArrogantSearch extends Quest {
   private static final int YINTZU = 20647;
   private static final int PALIOTE = 20648;
   private static final int ARK_GUARDIAN_ELBEROTH = 27182;
   private static final int ARK_GUARDIAN_SHADOWFANG = 27183;
   private static final int ANGEL_KILLER = 27184;
   private static final int PLATINUM_TRIBE_SHAMAN = 20828;
   private static final int PLATINUM_TRIBE_OVERLORD = 20829;
   private static final int GUARDIAN_ANGEL_1 = 20830;
   private static final int GUARDIAN_ANGEL_2 = 20859;
   private static final int SEAL_ANGEL_1 = 20831;
   private static final int SEAL_ANGEL_2 = 20860;
   private static final int HANELLIN = 30864;
   private static final int HOLY_ARK_OF_SECRECY_1 = 30977;
   private static final int HOLY_ARK_OF_SECRECY_2 = 30978;
   private static final int HOLY_ARK_OF_SECRECY_3 = 30979;
   private static final int ARK_GUARDIANS_CORPSE = 30980;
   private static final int HARNE = 30144;
   private static final int CLAUDIA_ATHEBALT = 31001;
   private static final int MARTIEN = 30645;
   private static final int GUSTAV_ATHEBALDT = 30760;
   private static final int HARDIN = 30832;
   private static final int HEINE = 30969;
   private static final int SHELL_OF_MONSTERS = 14857;
   private static final int HANELLINS_FIRST_LETTER = 4288;
   private static final int HANELLINS_SECOND_LETTER = 4289;
   private static final int HANELLINS_THIRD_LETTER = 4290;
   private static final int FIRST_KEY_OF_ARK = 4291;
   private static final int SECOND_KEY_OF_ARK = 4292;
   private static final int THIRD_KEY_OF_ARK = 4293;
   private static final int WHITE_FABRIC_1 = 4294;
   private static final int BLOODED_FABRIC = 4295;
   private static final int BOOK_OF_SAINT = 4397;
   private static final int BLOOD_OF_SAINT = 4398;
   private static final int BRANCH_OF_SAINT = 4399;
   private static final int WHITE_FABRIC_0 = 4400;
   private static final int ANTIDOTE = 1831;
   private static final int HEALING_POTION = 1061;
   private static final int ANIMAL_BONE = 1872;
   private static final int SYNTHETIC_COKES = 1888;
   private static final int ADENA = 57;
   private static final Map<Integer, Object[]> ARKS = new HashMap<>();
   private static final Map<Integer, Object[]> ARK_OWNERS = new HashMap<>();
   private static final Map<Integer, Object[]> BLOODY_OWNERS = new HashMap<>();
   private static final Map<Integer, Object[]> DROPS = new HashMap<>();
   private static final Map<Integer, Object[]> DROPS_29 = new HashMap<>();
   private static final Map<Integer, Object[]> ATTACK_DROPS_24 = new HashMap<>();
   private static final Map<Integer, Object[]> ATTACK_DROPS_25 = new HashMap<>();

   public _348_ArrogantSearch(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30864);
      this.addTalkId(30864);
      this.addTalkId(30980);
      this.addAttackId(20828);
      this.addAttackId(20829);
      ARK_OWNERS.put(30144, new Object[]{4288, 4398, "30144-01.htm", "30144-02.htm", "30144-03.htm", new int[]{-418, 44174, -3568}});
      ARK_OWNERS.put(31001, new Object[]{4289, 4397, "31001-01.htm", "31001-02.htm", "31001-03.htm", new int[]{181472, 7158, -2725}});
      ARK_OWNERS.put(30645, new Object[]{4290, 4399, "30645-01.htm", "30645-02.htm", "30645-03.htm", new int[]{50693, 158674, 376}});
      ARKS.put(30977, new Object[]{4291, 0, "30977-01.htm", "30977-02.htm", "30977-03.htm", 4398});
      ARKS.put(30978, new Object[]{4292, 27182, "That doesn't belong to you.  Don't touch it!", "30978-02.htm", "30978-03.htm", 4397});
      ARKS.put(30979, new Object[]{4293, 27183, "Get off my sight, you infidels!", "30979-02.htm", "30979-03.htm", 4399});
      BLOODY_OWNERS.put(30760, new Object[]{3, "athebaldt_delivery", "30760-01.htm", "30760-01a.htm", "30760-01b.htm"});
      BLOODY_OWNERS.put(30832, new Object[]{1, "hardin_delivery", "30832-01.htm", "30832-01a.htm", "30832-01b.htm"});
      BLOODY_OWNERS.put(30969, new Object[]{6, "heine_delivery", "30969-01.htm", "30969-01a.htm", "30969-01b.htm"});
      DROPS.put(20647, new Object[]{2, 14857, 1, 10, 0});
      DROPS.put(20648, new Object[]{2, 14857, 1, 10, 0});
      DROPS.put(27184, new Object[]{5, 4291, 1, 100, 0});
      DROPS.put(27182, new Object[]{5, 4292, 1, 100, 0});
      DROPS.put(27183, new Object[]{5, 4293, 1, 100, 0});
      DROPS.put(20828, new Object[]{25, 4295, 1, 10, 4294});
      DROPS.put(20829, new Object[]{25, 4295, 1, 10, 4294});
      DROPS.put(20830, new Object[]{26, 4295, 10, 25, 4294});
      DROPS.put(20859, new Object[]{26, 4295, 10, 25, 4294});
      DROPS.put(20831, new Object[]{26, 4295, 10, 25, 4294});
      DROPS.put(20860, new Object[]{26, 4295, 10, 25, 4294});
      DROPS_29.put(20830, new Object[]{29, 4295, 10, 25, 4294});
      DROPS_29.put(20859, new Object[]{29, 4295, 10, 25, 4294});
      DROPS_29.put(20831, new Object[]{29, 4295, 10, 25, 4294});
      DROPS_29.put(20860, new Object[]{29, 4295, 10, 25, 4294});
      ATTACK_DROPS_24.put(20828, new Object[]{24, 4295, 1, 2, 4294});
      ATTACK_DROPS_24.put(20829, new Object[]{24, 4295, 1, 200, 4294});
      ATTACK_DROPS_25.put(20828, new Object[]{25, 4295, 1, 2, 4294});
      ATTACK_DROPS_25.put(20829, new Object[]{25, 4295, 1, 200, 4294});

      for(int i : ARK_OWNERS.keySet()) {
         this.addTalkId(i);
      }

      for(int i : ARKS.keySet()) {
         this.addTalkId(i);
      }

      for(int i : BLOODY_OWNERS.keySet()) {
         this.addTalkId(i);
      }

      for(int i : DROPS.keySet()) {
         this.addKillId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30864-02c.htm")) {
            st.setState((byte)1);
            st.set("cond", "2");
            st.set("reward1", "0");
            st.set("athebaldt_delivery", "0");
            st.set("hardin_delivery", "0");
            st.set("heine_delivery", "0");
         } else if (event.equalsIgnoreCase("30864_04a")) {
            st.set("cond", "4");
            htmltext = "30864-04c.htm";
            st.set("companions", "0");
         } else if (event.equalsIgnoreCase("30864_04b")) {
            st.set("cond", "3");
            st.set("companions", "1");
            st.takeItems(14857, -1L);
            htmltext = "not yet implemented";
         } else if (event.equalsIgnoreCase("30864_07")) {
            htmltext = "30864-07b.htm";
         } else if (event.equalsIgnoreCase("30864_07b")) {
            htmltext = "30864-07c.htm";
         } else if (event.equalsIgnoreCase("30864_07c")) {
            htmltext = "30864-07d.htm";
         } else if (event.equalsIgnoreCase("30864_07meet")) {
            htmltext = "30864-07meet.htm";
            st.set("cond", "24");
         } else if (event.equalsIgnoreCase("30864_07money")) {
            htmltext = "30864-07money.htm";
            st.set("cond", "25");
         } else if (event.equalsIgnoreCase("30864_08")) {
            htmltext = "30864-08b.htm";
         } else if (event.equalsIgnoreCase("30864_08b")) {
            htmltext = "30864-08c.htm";
            st.giveItems(4294, 9L);
            st.set("cond", "26");
         } else if (event.equalsIgnoreCase("30864_09")) {
            st.set("cond", "27");
            htmltext = "30864-09c.htm";
         } else if (event.equalsIgnoreCase("30864_10continue")) {
            htmltext = "30864-08c.htm";
            st.giveItems(4294, 10L);
            st.set("athebaldt_delivery", "0");
            st.set("hardin_delivery", "0");
            st.set("heine_delivery", "0");
            st.set("cond", "29");
         } else if (event.equalsIgnoreCase("30864_10quit")) {
            htmltext = "30864-10c.htm";
            st.takeItems(4294, -1L);
            st.takeItems(4295, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else if (npc.getId() != 30864 & st.getState() != 1) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int reward1 = st.getInt("reward1");
         if (npc.getId() == 30864) {
            if (st.getState() == 0) {
               if (st.getQuestItemsCount(4295) == 1L) {
                  htmltext = "30864-Baium.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() < 60) {
                  st.exitQuest(true);
                  htmltext = "30864-01.htm";
               } else if (cond == 0) {
                  htmltext = "30864-02.htm";
               } else if (cond == 1) {
                  htmltext = "30864-02.htm";
               }
            } else if (cond == 2) {
               if (st.getQuestItemsCount(14857) == 0L) {
                  htmltext = "30864-03.htm";
               } else {
                  st.takeItems(14857, -1L);
                  htmltext = "30864-04.htm";
               }
            } else if (cond == 4) {
               st.set("cond", "5");
               st.giveItems(4288, 1L);
               st.giveItems(4289, 1L);
               st.giveItems(4290, 1L);
               htmltext = "30864-05.htm";
            } else if (cond == 5 && st.getQuestItemsCount(4397) + st.getQuestItemsCount(4398) + st.getQuestItemsCount(4399) < 3L) {
               htmltext = "30864-05.htm";
            } else if (cond == 5) {
               htmltext = "30864-06.htm";
               st.takeItems(4397, -1L);
               st.takeItems(4398, -1L);
               st.takeItems(4399, -1L);
               st.set("cond", "22");
            } else if (cond == 22 && st.getQuestItemsCount(1831) < 5L && st.getQuestItemsCount(1061) < 1L) {
               htmltext = "30864-06a.htm";
            } else if (cond == 22 && st.getQuestItemsCount(4294) > 0L) {
               htmltext = "30864-07c.htm";
            } else if (cond == 22) {
               st.takeItems(1831, 5L);
               st.takeItems(1061, 1L);
               if (st.getInt("companions") == 0) {
                  htmltext = "30864-07.htm";
                  st.giveItems(4294, 1L);
               } else {
                  st.set("cond", "23");
                  htmltext = "not implemented yet";
                  st.giveItems(4400, 3L);
               }
            } else if (cond == 24 && st.getQuestItemsCount(4295) < 1L) {
               htmltext = "30864-07a.htm";
            } else if (cond == 25 && st.getQuestItemsCount(4295) < 1L) {
               htmltext = "30864-07a.htm";
            } else if (cond == 25 && reward1 > 0) {
               htmltext = "30864-08b.htm";
            } else if (cond == 25) {
               htmltext = "30864-08.htm";
               st.giveItems(1872, 2L);
               st.giveItems(1888, 2L);
               int lowbgrade = getRandom(10) + 4103;
               st.giveItems(lowbgrade, 1L);
               st.set("reward1", "1");
            } else if (cond == 26 && st.getQuestItemsCount(4294) > 0L) {
               htmltext = "30864-09a.htm";
            } else if (cond == 26 && st.getQuestItemsCount(4295) < 10L) {
               htmltext = "30864-09b.htm";
               st.giveItems(57, 5000L);
               st.takeItems(4295, -1L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else if (cond == 26) {
               htmltext = "30864-09.htm";
            } else if (cond == 27 && st.getInt("athebaldt_delivery") + st.getInt("hardin_delivery") + st.getInt("heine_delivery") < 3) {
               htmltext = "30864-10a.htm";
            } else if (cond == 27) {
               htmltext = "30864-10.htm";
               st.giveItems(1872, 5L);
               int highbgrade = getRandom(8) + 4113;
               st.giveItems(highbgrade, 1L);
               st.set("cond", "28");
            } else if (cond == 28) {
               htmltext = "30864-10b.htm";
            } else if (cond == 29 && st.getQuestItemsCount(4294) > 0L) {
               htmltext = "30864-09a.htm";
            } else if (cond == 29 && st.getQuestItemsCount(4295) < 10L) {
               htmltext = "30864-09b.htm";
               st.giveItems(57, 5000L);
               st.takeItems(4295, -1L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else if (cond == 29) {
               htmltext = "30864-09.htm";
            }
         } else if (cond == 5) {
            int npcId = npc.getId();
            if (ARK_OWNERS.containsKey(npcId)) {
               if (st.getQuestItemsCount(ARK_OWNERS.get(npc.getId())[0]) == 1L) {
                  st.takeItems(ARK_OWNERS.get(npc.getId())[0], 1L);
                  htmltext = (String)ARK_OWNERS.get(npcId)[2];
                  int[] i = (int[])ARK_OWNERS.get(npcId)[5];
                  st.addRadar(i[0], i[1], i[2]);
               } else if (st.getQuestItemsCount(ARK_OWNERS.get(npcId)[1]) < 1L) {
                  htmltext = (String)ARK_OWNERS.get(npcId)[3];
                  int[] i = (int[])ARK_OWNERS.get(npcId)[5];
                  st.addRadar(i[0], i[1], i[2]);
               } else {
                  htmltext = (String)ARK_OWNERS.get(npcId)[4];
               }
            } else if (ARKS.containsKey(npcId)) {
               if (st.getQuestItemsCount(ARKS.get(npcId)[0]) == 0L) {
                  if (ARKS.get(npcId)[1] != 0) {
                     st.addSpawn(ARKS.get(npcId)[1], 120000);
                  }

                  return (String)ARKS.get(npcId)[2];
               }

               if (st.getQuestItemsCount(ARKS.get(npcId)[5]) == 1L) {
                  htmltext = (String)ARKS.get(npcId)[4];
               } else {
                  htmltext = (String)ARKS.get(npcId)[3];
                  st.takeItems(ARKS.get(npcId)[0], 1L);
                  st.giveItems(ARKS.get(npcId)[5], 1L);
               }
            } else if (npcId == 30980) {
               if (st.getQuestItemsCount(4291) == 0L && st.getInt("angelKillerIsDefeated") == 0) {
                  st.addSpawn(27184, 120000);
                  htmltext = "30980-01.htm";
               } else if (st.getQuestItemsCount(4291) == 0L && st.getInt("angelKillerIsDefeated") == 1) {
                  st.giveItems(4291, 1L);
                  htmltext = "30980-02.htm";
               } else {
                  htmltext = "30980-03.htm";
               }
            }
         } else if (cond == 27) {
            int npcId = npc.getId();
            if (BLOODY_OWNERS.containsKey(npcId)) {
               if (st.getInt((String)BLOODY_OWNERS.get(npcId)[1]) < 1) {
                  if (st.getQuestItemsCount(4295) >= (long)((Integer)BLOODY_OWNERS.get(npcId)[0]).intValue()) {
                     st.takeItems(4295, (long)((Integer)BLOODY_OWNERS.get(npcId)[0]).intValue());
                     st.set((String)BLOODY_OWNERS.get(npcId)[1], "1");
                     htmltext = (String)BLOODY_OWNERS.get(npcId)[2];
                  } else {
                     htmltext = (String)BLOODY_OWNERS.get(npcId)[3];
                  }
               } else {
                  htmltext = (String)BLOODY_OWNERS.get(npcId)[4];
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon, Skill skill) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else if (st.getState() != 1) {
         return null;
      } else {
         int npcId = npc.getId();
         if (ATTACK_DROPS_24.containsKey(npcId)) {
            int cond = ATTACK_DROPS_24.get(npcId)[0];
            int chance = ATTACK_DROPS_24.get(npcId)[3];
            if (st.getInt("cond") == cond && getRandom(1000) < chance && st.getQuestItemsCount(ATTACK_DROPS_24.get(npcId)[4]) > 0L) {
               st.giveItems(ATTACK_DROPS_24.get(npcId)[1], (long)((Integer)ATTACK_DROPS_24.get(npcId)[2]).intValue());
               st.playSound("ItemSound.quest_itemget");
               st.takeItems(ATTACK_DROPS_24.get(npcId)[4], 1L);
               if (cond == 24) {
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
            }
         } else if (ATTACK_DROPS_25.containsKey(npcId)) {
            int cond = ATTACK_DROPS_25.get(npcId)[0];
            int chance = ATTACK_DROPS_25.get(npcId)[3];
            if (st.getInt("cond") == cond && getRandom(1000) < chance && st.getQuestItemsCount(ATTACK_DROPS_25.get(npcId)[4]) > 0L) {
               st.giveItems(ATTACK_DROPS_25.get(npcId)[1], (long)((Integer)ATTACK_DROPS_25.get(npcId)[2]).intValue());
               st.playSound("ItemSound.quest_itemget");
               st.takeItems(ATTACK_DROPS_25.get(npcId)[4], 1L);
               if (cond == 24) {
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
            }
         }

         return null;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else if (st.getState() != 1) {
         return null;
      } else {
         int npcId = npc.getId();
         if (DROPS.containsKey(npcId)) {
            int cond = DROPS.get(npcId)[0];
            if (st.getInt("cond") == cond
               && st.getQuestItemsCount(DROPS.get(npcId)[1]) < (long)((Integer)DROPS.get(npcId)[2]).intValue()
               && getRandom(100) < DROPS.get(npcId)[3]
               && (DROPS.get(npcId)[4] == 0 || st.getQuestItemsCount(DROPS.get(npcId)[4]) > 0L)) {
               st.giveItems(DROPS.get(npcId)[1], 1L);
               st.playSound("ItemSound.quest_itemget");
               if (DROPS.get(npcId)[4] != 0) {
                  st.takeItems(DROPS.get(npcId)[4], 1L);
               }
            }

            if (cond == 24) {
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            }
         }

         if (DROPS_29.containsKey(npcId)) {
            int cond = DROPS_29.get(npcId)[0];
            if (st.getInt("cond") == cond
               && st.getQuestItemsCount(DROPS_29.get(npcId)[1]) < (long)((Integer)DROPS.get(npcId)[2]).intValue()
               && getRandom(100) < DROPS.get(npcId)[3]
               && (DROPS.get(npcId)[4] == 0 || st.getQuestItemsCount(DROPS.get(npcId)[4]) > 0L)) {
               st.giveItems(DROPS_29.get(npcId)[1], 1L);
               st.playSound("ItemSound.quest_itemget");
               if (DROPS_29.get(npcId)[4] != 0) {
                  st.takeItems(DROPS_29.get(npcId)[4], 1L);
               }
            }
         }

         return npcId == 27184
            ? "пїЅпїЅ, пїЅпїЅпїЅ пїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅ! пїЅпїЅпїЅпїЅ пїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅ, пїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅ пїЅпїЅпїЅпїЅпїЅ"
            : null;
      }
   }

   public static void main(String[] args) {
      new _348_ArrogantSearch(348, _348_ArrogantSearch.class.getSimpleName(), "");
   }
}

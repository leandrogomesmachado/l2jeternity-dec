package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestDrop;
import l2e.gameserver.model.quest.QuestState;

public class _232_TestOfLord extends Quest {
   private static final String qn = "_232_TestOfLord";
   private static int Somak = 30510;
   private static int Manakia = 30515;
   private static int Jakal = 30558;
   private static int Sumari = 30564;
   private static int Kakai = 30565;
   private static int Varkees = 30566;
   private static int Tantus = 30567;
   private static int Hatos = 30568;
   private static int Takuna = 30641;
   private static int Chianta = 30642;
   private static int First_Orc = 30643;
   private static int Ancestor_Martankus = 30649;
   private static int Marsh_Spider = 20233;
   private static int Breka_Orc_Shaman = 20269;
   private static int Breka_Orc_Overlord = 20270;
   private static int Enchanted_Monstereye = 20564;
   private static int Timak_Orc = 20583;
   private static int Timak_Orc_Archer = 20584;
   private static int Timak_Orc_Soldier = 20585;
   private static int Timak_Orc_Warrior = 20586;
   private static int Timak_Orc_Shaman = 20587;
   private static int Timak_Orc_Overlord = 20588;
   private static int Ragna_Orc_Overlord = 20778;
   private static int Ragna_Orc_Seer = 20779;
   private static int MARK_OF_LORD = 3390;
   private static int BONE_ARROW = 1341;
   private static int TIMAK_ORC_SKULL = 3403;
   private static int BREKA_ORC_FANG = 3398;
   private static int RAGNA_ORC_HEAD = 3414;
   private static int RAGNA_CHIEF_NOTICE = 3415;
   private static int MARSH_SPIDER_FEELER = 3407;
   private static int MARSH_SPIDER_FEET = 3408;
   private static int CORNEA_OF_EN_MONSTEREYE = 3410;
   private static int ORDEAL_NECKLACE = 3391;
   private static int VARKEES_CHARM = 3392;
   private static int TANTUS_CHARM = 3393;
   private static int HATOS_CHARM = 3394;
   private static int TAKUNA_CHARM = 3395;
   private static int CHIANTA_CHARM = 3396;
   private static int MANAKIAS_ORDERS = 3397;
   private static int MANAKIAS_AMULET = 3399;
   private static int HUGE_ORC_FANG = 3400;
   private static int SUMARIS_LETTER = 3401;
   private static int URUTU_BLADE = 3402;
   private static int SWORD_INTO_SKULL = 3404;
   private static int NERUGA_AXE_BLADE = 3405;
   private static int AXE_OF_CEREMONY = 3406;
   private static int HANDIWORK_SPIDER_BROOCH = 3409;
   private static int MONSTEREYE_WOODCARVING = 3411;
   private static int BEAR_FANG_NECKLACE = 3412;
   private static int MARTANKUS_CHARM = 3413;
   private static int IMMORTAL_FLAME = 3416;
   private static Map<Integer, QuestDrop> DROPLIST = new HashMap<>();

   public _232_TestOfLord(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(Kakai);
      this.addTalkId(Kakai);
      this.addTalkId(Somak);
      this.addTalkId(Manakia);
      this.addTalkId(Jakal);
      this.addTalkId(Sumari);
      this.addTalkId(Varkees);
      this.addTalkId(Tantus);
      this.addTalkId(Hatos);
      this.addTalkId(Takuna);
      this.addTalkId(Chianta);
      this.addTalkId(First_Orc);
      this.addTalkId(Ancestor_Martankus);
      DROPLIST.put(Timak_Orc, new QuestDrop(1, 10, 50).addItem(TIMAK_ORC_SKULL));
      DROPLIST.put(Timak_Orc_Archer, new QuestDrop(1, 10, 55).addItem(TIMAK_ORC_SKULL));
      DROPLIST.put(Timak_Orc_Soldier, new QuestDrop(1, 10, 60).addItem(TIMAK_ORC_SKULL));
      DROPLIST.put(Timak_Orc_Warrior, new QuestDrop(1, 10, 65).addItem(TIMAK_ORC_SKULL));
      DROPLIST.put(Timak_Orc_Shaman, new QuestDrop(1, 10, 70).addItem(TIMAK_ORC_SKULL));
      DROPLIST.put(Timak_Orc_Overlord, new QuestDrop(1, 10, 75).addItem(TIMAK_ORC_SKULL));
      DROPLIST.put(Breka_Orc_Shaman, new QuestDrop(1, 20, 40).addItem(BREKA_ORC_FANG));
      DROPLIST.put(Breka_Orc_Overlord, new QuestDrop(1, 20, 50).addItem(BREKA_ORC_FANG));
      DROPLIST.put(Ragna_Orc_Overlord, new QuestDrop(4, 1, 100).addItem(RAGNA_ORC_HEAD));
      DROPLIST.put(Ragna_Orc_Seer, new QuestDrop(4, 1, 100).addItem(RAGNA_CHIEF_NOTICE));
      DROPLIST.put(Marsh_Spider, new QuestDrop(1, 10, 100).addItem(MARSH_SPIDER_FEELER).addItem(MARSH_SPIDER_FEET));
      DROPLIST.put(Enchanted_Monstereye, new QuestDrop(1, 20, 90).addItem(CORNEA_OF_EN_MONSTEREYE));

      for(int kill_id : DROPLIST.keySet()) {
         this.addKillId(kill_id);
      }

      for(QuestDrop drop : DROPLIST.values()) {
         for(int item_id : drop.itemList) {
            this.registerQuestItems(new int[]{item_id});
         }
      }

      this.questItemIds = new int[]{
         ORDEAL_NECKLACE,
         VARKEES_CHARM,
         TANTUS_CHARM,
         HATOS_CHARM,
         TAKUNA_CHARM,
         CHIANTA_CHARM,
         MANAKIAS_ORDERS,
         MANAKIAS_AMULET,
         HUGE_ORC_FANG,
         SUMARIS_LETTER,
         URUTU_BLADE,
         SWORD_INTO_SKULL,
         NERUGA_AXE_BLADE,
         AXE_OF_CEREMONY,
         HANDIWORK_SPIDER_BROOCH,
         MONSTEREYE_WOODCARVING,
         BEAR_FANG_NECKLACE,
         MARTANKUS_CHARM,
         IMMORTAL_FLAME
      };
   }

   private static void spawn_First_Orc(QuestState st) {
      st.addSpawn(First_Orc, 21036, -107690, -3038);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_232_TestOfLord");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30565-05.htm")) {
            st.giveItems(ORDEAL_NECKLACE, 1L);
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30565-12.htm") && st.getQuestItemsCount(IMMORTAL_FLAME) > 0L) {
            st.takeItems(IMMORTAL_FLAME, -1L);
            st.giveItems(MARK_OF_LORD, 1L);
            st.addExpAndSp(894888, 61408);
            st.giveItems(57, 161806L);
            if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
               st.giveItems(7562, 92L);
               st.giveItems(8870, 15L);
               player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
            }

            st.playSound("ItemSound.quest_finish");
            st.unset("cond");
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("30565-08.htm")) {
            st.takeItems(SWORD_INTO_SKULL, -1L);
            st.takeItems(AXE_OF_CEREMONY, -1L);
            st.takeItems(MONSTEREYE_WOODCARVING, -1L);
            st.takeItems(HANDIWORK_SPIDER_BROOCH, -1L);
            st.takeItems(ORDEAL_NECKLACE, -1L);
            st.takeItems(HUGE_ORC_FANG, -1L);
            st.giveItems(BEAR_FANG_NECKLACE, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "3");
         } else if (event.equalsIgnoreCase("30566-02.htm")) {
            st.giveItems(VARKEES_CHARM, 1L);
         } else if (event.equalsIgnoreCase("30567-02.htm")) {
            st.giveItems(TANTUS_CHARM, 1L);
         } else if (event.equalsIgnoreCase("30558-02.htm") && st.getQuestItemsCount(57) >= 1000L) {
            st.takeItems(57, 1000L);
            st.giveItems(NERUGA_AXE_BLADE, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30568-02.htm")) {
            st.giveItems(HATOS_CHARM, 1L);
         } else if (event.equalsIgnoreCase("30641-02.htm")) {
            st.giveItems(TAKUNA_CHARM, 1L);
         } else if (event.equalsIgnoreCase("30642-02.htm")) {
            st.giveItems(CHIANTA_CHARM, 1L);
         } else if (event.equalsIgnoreCase("30649-04.htm") && st.getQuestItemsCount(BEAR_FANG_NECKLACE) > 0L) {
            st.takeItems(BEAR_FANG_NECKLACE, -1L);
            st.giveItems(MARTANKUS_CHARM, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "4");
         } else if (event.equalsIgnoreCase("30649-07.htm")) {
            st.set("cond", "6");
            spawn_First_Orc(st);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_232_TestOfLord");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == Kakai) {
                  if (player.getRace().ordinal() != 3) {
                     st.exitQuest(true);
                     htmltext = "30565-01.htm";
                  } else if (player.getClassId().getId() != 50) {
                     st.exitQuest(true);
                     htmltext = "30565-02.htm";
                  } else if (player.getLevel() < 39) {
                     st.exitQuest(true);
                     htmltext = "30565-03.htm";
                  } else {
                     htmltext = "30565-04.htm";
                  }
               }
               break;
            case 1:
               long ORDEAL_NECKLACE_COUNT = st.getQuestItemsCount(ORDEAL_NECKLACE);
               long HUGE_ORC_FANG_COUNT = st.getQuestItemsCount(HUGE_ORC_FANG);
               long SWORD_INTO_SKULL_COUNT = st.getQuestItemsCount(SWORD_INTO_SKULL);
               long AXE_OF_CEREMONY_COUNT = st.getQuestItemsCount(AXE_OF_CEREMONY);
               long MONSTEREYE_WOODCARVING_COUNT = st.getQuestItemsCount(MONSTEREYE_WOODCARVING);
               long HANDIWORK_SPIDER_BROOCH_COUNT = st.getQuestItemsCount(HANDIWORK_SPIDER_BROOCH);
               long BEAR_FANG_NECKLACE_COUNT = st.getQuestItemsCount(BEAR_FANG_NECKLACE);
               long MARTANKUS_CHARM_COUNT = st.getQuestItemsCount(MARTANKUS_CHARM);
               long IMMORTAL_FLAME_COUNT = st.getQuestItemsCount(IMMORTAL_FLAME);
               long VARKEES_CHARM_COUNT = st.getQuestItemsCount(VARKEES_CHARM);
               long MANAKIAS_AMULET_COUNT = st.getQuestItemsCount(MANAKIAS_AMULET);
               long MANAKIAS_ORDERS_COUNT = st.getQuestItemsCount(MANAKIAS_ORDERS);
               long BREKA_ORC_FANG_COUNT = st.getQuestItemsCount(BREKA_ORC_FANG);
               long TANTUS_CHARM_COUNT = st.getQuestItemsCount(TANTUS_CHARM);
               long NERUGA_AXE_BLADE_COUNT = st.getQuestItemsCount(NERUGA_AXE_BLADE);
               long HATOS_CHARM_COUNT = st.getQuestItemsCount(HATOS_CHARM);
               long URUTU_BLADE_COUNT = st.getQuestItemsCount(URUTU_BLADE);
               long TIMAK_ORC_SKULL_COUNT = st.getQuestItemsCount(TIMAK_ORC_SKULL);
               long SUMARIS_LETTER_COUNT = st.getQuestItemsCount(SUMARIS_LETTER);
               long TAKUNA_CHARM_COUNT = st.getQuestItemsCount(TAKUNA_CHARM);
               if (npcId == Kakai) {
                  if (ORDEAL_NECKLACE_COUNT > 0L) {
                     htmltext = this.cond1Complete(st) ? "30565-07.htm" : "30565-06.htm";
                  } else if (BEAR_FANG_NECKLACE_COUNT > 0L) {
                     htmltext = "30565-09.htm";
                  } else if (MARTANKUS_CHARM_COUNT > 0L) {
                     htmltext = "30565-10.htm";
                  } else if (IMMORTAL_FLAME_COUNT > 0L) {
                     htmltext = "30565-11.htm";
                  }
               } else if (npcId == Varkees && ORDEAL_NECKLACE_COUNT > 0L) {
                  if (HUGE_ORC_FANG_COUNT > 0L) {
                     htmltext = "30566-05.htm";
                  } else if (VARKEES_CHARM_COUNT == 0L) {
                     htmltext = "30566-01.htm";
                  } else if (MANAKIAS_AMULET_COUNT == 0L) {
                     htmltext = "30566-03.htm";
                  } else {
                     st.takeItems(VARKEES_CHARM, -1L);
                     st.takeItems(MANAKIAS_AMULET, -1L);
                     st.giveItems(HUGE_ORC_FANG, 1L);
                     if (this.cond1Complete(st)) {
                        st.playSound("ItemSound.quest_jackpot");
                        st.set("cond", "2");
                     } else {
                        st.playSound("ItemSound.quest_middle");
                     }

                     htmltext = "30566-04.htm";
                  }
               } else if (npcId == Manakia && ORDEAL_NECKLACE_COUNT > 0L) {
                  if (VARKEES_CHARM_COUNT > 0L && HUGE_ORC_FANG_COUNT == 0L) {
                     if (MANAKIAS_AMULET_COUNT == 0L) {
                        if (MANAKIAS_ORDERS_COUNT == 0L) {
                           st.giveItems(MANAKIAS_ORDERS, 1L);
                           htmltext = "30515-01.htm";
                        } else if (BREKA_ORC_FANG_COUNT < 20L) {
                           htmltext = "30515-02.htm";
                        } else {
                           st.takeItems(MANAKIAS_ORDERS, -1L);
                           st.takeItems(BREKA_ORC_FANG, -1L);
                           st.giveItems(MANAKIAS_AMULET, 1L);
                           st.playSound("ItemSound.quest_middle");
                           htmltext = "30515-03.htm";
                        }
                     } else if (MANAKIAS_ORDERS_COUNT == 0L) {
                        htmltext = "30515-04.htm";
                     }
                  } else if (VARKEES_CHARM_COUNT == 0L && HUGE_ORC_FANG_COUNT > 0L && MANAKIAS_AMULET_COUNT == 0L && MANAKIAS_ORDERS_COUNT == 0L) {
                     htmltext = "30515-05.htm";
                  }
               } else if (npcId == Tantus) {
                  if (AXE_OF_CEREMONY_COUNT == 0L) {
                     if (TANTUS_CHARM_COUNT == 0L) {
                        htmltext = "30567-01.htm";
                     } else if (NERUGA_AXE_BLADE_COUNT != 0L && st.getQuestItemsCount(BONE_ARROW) >= 1000L) {
                        st.takeItems(TANTUS_CHARM, -1L);
                        st.takeItems(NERUGA_AXE_BLADE, -1L);
                        st.takeItems(BONE_ARROW, 1000L);
                        st.giveItems(AXE_OF_CEREMONY, 1L);
                        if (this.cond1Complete(st)) {
                           st.playSound("ItemSound.quest_jackpot");
                           st.set("cond", "2");
                        } else {
                           st.playSound("ItemSound.quest_middle");
                        }

                        htmltext = "30567-04.htm";
                     } else {
                        htmltext = "30567-03.htm";
                     }
                  } else if (TANTUS_CHARM_COUNT == 0L) {
                     htmltext = "30567-05.htm";
                  }
               } else if (npcId == Jakal) {
                  if (TANTUS_CHARM_COUNT > 0L && AXE_OF_CEREMONY_COUNT == 0L) {
                     if (NERUGA_AXE_BLADE_COUNT > 0L) {
                        htmltext = "30558-04.htm";
                     } else {
                        htmltext = st.getQuestItemsCount(57) < 1000L ? "30558-03.htm" : "30558-01.htm";
                     }
                  } else if (TANTUS_CHARM_COUNT == 0L && AXE_OF_CEREMONY_COUNT > 0L) {
                     htmltext = "30558-05.htm";
                  }
               } else if (npcId == Hatos) {
                  if (SWORD_INTO_SKULL_COUNT == 0L) {
                     if (HATOS_CHARM_COUNT == 0L) {
                        htmltext = "30568-01.htm";
                     } else if (URUTU_BLADE_COUNT != 0L && TIMAK_ORC_SKULL_COUNT >= 10L) {
                        st.takeItems(HATOS_CHARM, -1L);
                        st.takeItems(URUTU_BLADE, -1L);
                        st.takeItems(TIMAK_ORC_SKULL, -1L);
                        st.giveItems(SWORD_INTO_SKULL, 1L);
                        if (this.cond1Complete(st)) {
                           st.playSound("ItemSound.quest_jackpot");
                           st.set("cond", "2");
                        } else {
                           st.playSound("ItemSound.quest_middle");
                        }

                        htmltext = "30568-04.htm";
                     } else {
                        htmltext = "30568-03.htm";
                     }
                  } else if (HATOS_CHARM_COUNT == 0L) {
                     htmltext = "30568-05.htm";
                  }
               } else if (npcId == Sumari) {
                  if (HATOS_CHARM_COUNT > 0L && SWORD_INTO_SKULL_COUNT == 0L) {
                     if (URUTU_BLADE_COUNT == 0L) {
                        if (SUMARIS_LETTER_COUNT > 0L) {
                           htmltext = "30564-02.htm";
                        } else {
                           st.giveItems(SUMARIS_LETTER, 1L);
                           st.playSound("ItemSound.quest_middle");
                           htmltext = "30564-01.htm";
                        }
                     } else if (SUMARIS_LETTER_COUNT == 0L) {
                        htmltext = "30564-03.htm";
                     }
                  } else if (HATOS_CHARM_COUNT == 0L && SWORD_INTO_SKULL_COUNT > 0L && URUTU_BLADE_COUNT == 0L && SUMARIS_LETTER_COUNT == 0L) {
                     htmltext = "30564-04.htm";
                  }
               } else if (npcId == Somak) {
                  if (SWORD_INTO_SKULL_COUNT == 0L) {
                     if (URUTU_BLADE_COUNT == 0L && HATOS_CHARM_COUNT > 0L && SUMARIS_LETTER_COUNT > 0L) {
                        st.takeItems(SUMARIS_LETTER, -1L);
                        st.giveItems(URUTU_BLADE, 1L);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "30510-01.htm";
                     } else if (URUTU_BLADE_COUNT > 0L && HATOS_CHARM_COUNT > 0L && SUMARIS_LETTER_COUNT == 0L) {
                        htmltext = "30510-02.htm";
                     }
                  } else if (URUTU_BLADE_COUNT == 0L && HATOS_CHARM_COUNT == 0L && SUMARIS_LETTER_COUNT == 0L) {
                     htmltext = "30510-03.htm";
                  }
               } else if (npcId == Takuna) {
                  if (HANDIWORK_SPIDER_BROOCH_COUNT == 0L) {
                     if (TAKUNA_CHARM_COUNT == 0L) {
                        htmltext = "30641-01.htm";
                     } else if (st.getQuestItemsCount(MARSH_SPIDER_FEELER) >= 10L && st.getQuestItemsCount(MARSH_SPIDER_FEET) >= 10L) {
                        st.takeItems(MARSH_SPIDER_FEELER, -1L);
                        st.takeItems(MARSH_SPIDER_FEET, -1L);
                        st.takeItems(TAKUNA_CHARM, -1L);
                        st.giveItems(HANDIWORK_SPIDER_BROOCH, 1L);
                        if (this.cond1Complete(st)) {
                           st.playSound("ItemSound.quest_jackpot");
                           st.set("cond", "2");
                        } else {
                           st.playSound("ItemSound.quest_middle");
                        }

                        htmltext = "30641-04.htm";
                     } else {
                        htmltext = "30641-03.htm";
                     }
                  } else if (TAKUNA_CHARM_COUNT == 0L) {
                     htmltext = "30641-05.htm";
                  }
               } else if (npcId == Chianta) {
                  long CHIANTA_CHARM_COUNT = st.getQuestItemsCount(CHIANTA_CHARM);
                  if (MONSTEREYE_WOODCARVING_COUNT == 0L) {
                     if (CHIANTA_CHARM_COUNT == 0L) {
                        htmltext = "30642-01.htm";
                     } else if (st.getQuestItemsCount(CORNEA_OF_EN_MONSTEREYE) < 20L) {
                        htmltext = "30642-03.htm";
                     } else {
                        st.takeItems(CORNEA_OF_EN_MONSTEREYE, -1L);
                        st.takeItems(CHIANTA_CHARM, -1L);
                        st.giveItems(MONSTEREYE_WOODCARVING, 1L);
                        if (this.cond1Complete(st)) {
                           st.playSound("ItemSound.quest_jackpot");
                           st.set("cond", "2");
                        } else {
                           st.playSound("ItemSound.quest_middle");
                        }

                        htmltext = "30642-04.htm";
                     }
                  } else if (CHIANTA_CHARM_COUNT == 0L) {
                     htmltext = "30642-05.htm";
                  }
               } else if (npcId == Ancestor_Martankus) {
                  if (BEAR_FANG_NECKLACE_COUNT > 0L) {
                     htmltext = "30649-01.htm";
                  } else if (MARTANKUS_CHARM_COUNT > 0L) {
                     if (cond != 5 && (st.getQuestItemsCount(RAGNA_CHIEF_NOTICE) <= 0L || st.getQuestItemsCount(RAGNA_ORC_HEAD) <= 0L)) {
                        htmltext = "30649-05.htm";
                     } else {
                        st.takeItems(MARTANKUS_CHARM, -1L);
                        st.takeItems(RAGNA_ORC_HEAD, -1L);
                        st.takeItems(RAGNA_CHIEF_NOTICE, -1L);
                        st.giveItems(IMMORTAL_FLAME, 1L);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "30649-06.htm";
                     }
                  } else if (cond == 6 || cond == 7) {
                     htmltext = "30649-08.htm";
                  }
               } else if (npcId == First_Orc && st.getQuestItemsCount(IMMORTAL_FLAME) > 0L) {
                  st.set("cond", "7");
                  htmltext = "30643-01.htm";
               }
               break;
            case 2:
               if (npcId == Kakai) {
                  htmltext = getAlreadyCompletedMsg(player);
               }
         }

         return htmltext;
      }
   }

   private boolean cond1Complete(QuestState st) {
      long HUGE_ORC_FANG_COUNT = st.getQuestItemsCount(HUGE_ORC_FANG);
      long SWORD_INTO_SKULL_COUNT = st.getQuestItemsCount(SWORD_INTO_SKULL);
      long AXE_OF_CEREMONY_COUNT = st.getQuestItemsCount(AXE_OF_CEREMONY);
      long MONSTEREYE_WOODCARVING_COUNT = st.getQuestItemsCount(MONSTEREYE_WOODCARVING);
      long HANDIWORK_SPIDER_BROOCH_COUNT = st.getQuestItemsCount(HANDIWORK_SPIDER_BROOCH);
      return HUGE_ORC_FANG_COUNT > 0L
         && SWORD_INTO_SKULL_COUNT > 0L
         && AXE_OF_CEREMONY_COUNT > 0L
         && MONSTEREYE_WOODCARVING_COUNT > 0L
         && HANDIWORK_SPIDER_BROOCH_COUNT > 0L;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_232_TestOfLord");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         QuestDrop _drop = DROPLIST.get(npcId);
         if (_drop == null) {
            return null;
         } else {
            int cond = st.getInt("cond");

            for(int item_id : _drop.itemList) {
               long ORDEAL_NECKLACE_COUNT = st.getQuestItemsCount(ORDEAL_NECKLACE);
               if ((
                     item_id != TIMAK_ORC_SKULL
                        || ORDEAL_NECKLACE_COUNT > 0L && st.getQuestItemsCount(HATOS_CHARM) > 0L && st.getQuestItemsCount(SWORD_INTO_SKULL) == 0L
                  )
                  && (
                     item_id != BREKA_ORC_FANG
                        || ORDEAL_NECKLACE_COUNT > 0L && st.getQuestItemsCount(VARKEES_CHARM) > 0L && st.getQuestItemsCount(MANAKIAS_ORDERS) > 0L
                  )
                  && (npcId != Marsh_Spider || ORDEAL_NECKLACE_COUNT > 0L && st.getQuestItemsCount(TAKUNA_CHARM) > 0L)
                  && (npcId != Enchanted_Monstereye || ORDEAL_NECKLACE_COUNT > 0L && st.getQuestItemsCount(CHIANTA_CHARM) > 0L)) {
                  long count = st.getQuestItemsCount(item_id);
                  if (cond == _drop.condition && count < (long)_drop.maxcount && Rnd.chance(_drop.chance)) {
                     st.giveItems(item_id, 1L);
                     if (count + 1L == (long)_drop.maxcount) {
                        if (cond == 4 && st.getQuestItemsCount(RAGNA_ORC_HEAD) > 0L && st.getQuestItemsCount(RAGNA_CHIEF_NOTICE) > 0L) {
                           st.set("cond", "5");
                        }

                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _232_TestOfLord(232, "_232_TestOfLord", "");
   }
}

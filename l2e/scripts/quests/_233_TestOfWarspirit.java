package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _233_TestOfWarspirit extends Quest {
   private static final int PRIESTESS_VIVYAN = 30030;
   private static final int TRADER_SARIEN = 30436;
   private static final int SEER_RACOY = 30507;
   private static final int SEER_SOMAK = 30510;
   private static final int SEER_MANAKIA = 30515;
   private static final int SHADOW_ORIM = 30630;
   private static final int ANCESTOR_MARTANKUS = 30649;
   private static final int SEER_PEKIRON = 30682;
   private static final int VENDETTA_TOTEM = 2880;
   private static final int TAMLIN_ORC_HEAD = 2881;
   private static final int WARSPIRIT_TOTEM = 2882;
   private static final int ORIMS_CONTRACT = 2883;
   private static final int PORTAS_EYE = 2884;
   private static final int EXCUROS_SCALE = 2885;
   private static final int MORDEOS_TALON = 2886;
   private static final int BRAKIS_REMAINS1 = 2887;
   private static final int PEKIRONS_TOTEM = 2888;
   private static final int TONARS_SKULL = 2889;
   private static final int TONARS_RIB_BONE = 2890;
   private static final int TONARS_SPINE = 2891;
   private static final int TONARS_ARM_BONE = 2892;
   private static final int TONARS_THIGH_BONE = 2893;
   private static final int TONARS_REMAINS1 = 2894;
   private static final int MANAKIAS_TOTEM = 2895;
   private static final int HERMODTS_SKULL = 2896;
   private static final int HERMODTS_RIB_BONE = 2897;
   private static final int HERMODTS_SPINE = 2898;
   private static final int HERMODTS_ARM_BONE = 2899;
   private static final int HERMODTS_THIGH_BONE = 2900;
   private static final int HERMODTS_REMAINS1 = 2901;
   private static final int RACOYS_TOTEM = 2902;
   private static final int VIVIANTES_LETTER = 2903;
   private static final int INSECT_DIAGRAM_BOOK = 2904;
   private static final int KIRUNAS_SKULL = 2905;
   private static final int KIRUNAS_RIB_BONE = 2906;
   private static final int KIRUNAS_SPINE = 2907;
   private static final int KIRUNAS_ARM_BONE = 2908;
   private static final int KIRUNAS_THIGH_BONE = 2909;
   private static final int KIRUNAS_REMAINS1 = 2910;
   private static final int BRAKIS_REMAINS2 = 2911;
   private static final int TONARS_REMAINS2 = 2912;
   private static final int HERMODTS_REMAINS2 = 2913;
   private static final int KIRUNAS_REMAINS2 = 2914;
   private static final int MARK_OF_WARSPIRIT = 2879;
   private static final int DIMENSIONAL_DIAMOND = 7562;
   private static final int NOBLE_ANT = 20089;
   private static final int NOBLE_ANT_LEADER = 20090;
   private static final int MEDUSA = 20158;
   private static final int PORTA = 20213;
   private static final int EXCURO = 20214;
   private static final int MORDERO = 20215;
   private static final int LETO_LIZARDMAN_SHAMAN = 20581;
   private static final int LETO_LIZARDMAN_OVERLORD = 20582;
   private static final int TAMLIN_ORC = 20601;
   private static final int TAMLIN_ORC_ARCHER = 20602;
   private static final int STENOA_GORGON_QUEEN = 27108;
   private static final int MIN_LEVEL = 39;

   public _233_TestOfWarspirit(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30510);
      this.addTalkId(new int[]{30510, 30030, 30436, 30507, 30515, 30630, 30649, 30682});
      this.addKillId(new int[]{20089, 20090, 20158, 20213, 20214, 20215, 20581, 20582, 20601, 20602, 27108});
      this.registerQuestItems(
         new int[]{
            2880,
            2881,
            2882,
            2883,
            2884,
            2885,
            2886,
            2887,
            2888,
            2889,
            2890,
            2891,
            2892,
            2893,
            2894,
            2895,
            2896,
            2897,
            2898,
            2899,
            2900,
            2901,
            2902,
            2903,
            2904,
            2905,
            2906,
            2907,
            2908,
            2909,
            2910,
            2911,
            2912,
            2913,
            2914
         }
      );
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, false);
      if (qs == null) {
         return null;
      } else {
         String htmltext = null;
         switch(event) {
            case "ACCEPT":
               if (qs.isCreated()) {
                  qs.startQuest();
                  if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                     giveItems(player, 7562, 92L);
                     player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                  }

                  htmltext = "30510-05e.htm";
               }
               break;
            case "30510-05a.htm":
            case "30510-05b.htm":
            case "30510-05c.htm":
            case "30510-05d.htm":
            case "30510-05.htm":
            case "30030-02.htm":
            case "30030-03.htm":
            case "30630-02.htm":
            case "30630-03.htm":
            case "30649-02.htm":
               htmltext = event;
               break;
            case "30030-04.htm":
               giveItems(player, 2903, 1L);
               htmltext = event;
               break;
            case "30507-02.htm":
               giveItems(player, 2902, 1L);
               htmltext = event;
               break;
            case "30515-02.htm":
               giveItems(player, 2895, 1L);
               htmltext = event;
               break;
            case "30630-04.htm":
               giveItems(player, 2883, 1L);
               htmltext = event;
               break;
            case "30649-03.htm":
               if (hasQuestItems(player, 2912)) {
                  this.giveAdena(player, 161806L, true);
                  giveItems(player, 2879, 1L);
                  addExpAndSp(player, 894888L, 61408);
                  qs.exitQuest(false, true);
                  player.sendPacket(new SocialAction(player.getObjectId(), 3));
                  htmltext = event;
               }
               break;
            case "30682-02.htm":
               giveItems(player, 2888, 1L);
               htmltext = event;
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState qs = this.getQuestState(killer, false);
      if (qs != null && qs.isStarted() && Util.checkIfInRange(1500, npc, killer, true)) {
         switch(npc.getId()) {
            case 20089:
            case 20090:
               if (hasQuestItems(killer, new int[]{2902, 2904})) {
                  int i0 = getRandom(100);
                  if (i0 > 65) {
                     if (!hasQuestItems(killer, 2909)) {
                        giveItems(killer, 2909, 1L);
                        playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     } else if (!hasQuestItems(killer, 2908)) {
                        giveItems(killer, 2908, 1L);
                        playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     }
                  } else if (i0 > 30) {
                     if (!hasQuestItems(killer, 2907)) {
                        giveItems(killer, 2907, 1L);
                        playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     } else if (!hasQuestItems(killer, 2906)) {
                        giveItems(killer, 2906, 1L);
                        playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     }
                  } else if (i0 > 0 && !hasQuestItems(killer, 2905)) {
                     giveItems(killer, 2905, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  }
               }
               break;
            case 20158:
               if (hasQuestItems(killer, 2895)) {
                  if (!hasQuestItems(killer, 2897)) {
                     giveItems(killer, 2897, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else if (!hasQuestItems(killer, 2898)) {
                     giveItems(killer, 2898, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else if (!hasQuestItems(killer, 2899)) {
                     giveItems(killer, 2899, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else if (!hasQuestItems(killer, 2900)) {
                     giveItems(killer, 2900, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  }
               }
               break;
            case 20213:
               if (hasQuestItems(killer, 2883)) {
                  giveItemRandomly(killer, npc, 2884, 2L, 10L, 1.0, true);
               }
               break;
            case 20214:
               if (hasQuestItems(killer, 2883)) {
                  giveItemRandomly(killer, npc, 2885, 5L, 10L, 1.0, true);
               }
               break;
            case 20215:
               if (hasQuestItems(killer, 2883)) {
                  giveItemRandomly(killer, npc, 2886, 5L, 10L, 1.0, true);
               }
               break;
            case 20581:
            case 20582:
               if (hasQuestItems(killer, 2888)) {
                  if (!hasQuestItems(killer, 2889)) {
                     giveItems(killer, 2889, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else if (!hasQuestItems(killer, 2890)) {
                     giveItems(killer, 2890, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else if (!hasQuestItems(killer, 2891)) {
                     giveItems(killer, 2891, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else if (!hasQuestItems(killer, 2892)) {
                     giveItems(killer, 2892, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else if (!hasQuestItems(killer, 2893)) {
                     giveItems(killer, 2893, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  }
               }
               break;
            case 20601:
            case 20602:
               if (hasQuestItems(killer, 2880) && giveItemRandomly(killer, npc, 2881, 1L, 13L, 1.0, true)) {
                  qs.setCond(4, true);
               }
               break;
            case 27108:
               if (hasQuestItems(killer, 2895) && !hasQuestItems(killer, 2896)) {
                  giveItems(killer, 2896, 1L);
                  playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
               }
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, true);
      String htmltext = getNoQuestMsg(player);
      if (qs.isCreated()) {
         if (npc.getId() == 30510) {
            if (player.getRace() == Race.Orc) {
               if (player.getClassId() == ClassId.orcShaman) {
                  if (player.getLevel() < 39) {
                     htmltext = "30510-03.htm";
                  } else {
                     htmltext = "30510-04.htm";
                  }
               } else {
                  htmltext = "30510-02.htm";
               }
            } else {
               htmltext = "30510-01.htm";
            }
         }
      } else if (qs.isStarted()) {
         switch(npc.getId()) {
            case 30030:
               if (hasQuestItems(player, 2902) && !this.hasAtLeastOneQuestItem(player, new int[]{2903, 2904})) {
                  htmltext = "30030-01.htm";
               } else if (hasQuestItems(player, new int[]{2902, 2903}) && !hasQuestItems(player, 2904)) {
                  htmltext = "30030-05.htm";
               } else if (hasQuestItems(player, new int[]{2902, 2904}) && !hasQuestItems(player, 2903)) {
                  htmltext = "30030-06.htm";
               } else if (!hasQuestItems(player, 2902) && this.hasAtLeastOneQuestItem(player, new int[]{2910, 2914, 2880})) {
                  htmltext = "30030-07.htm";
               }
               break;
            case 30436:
               if (hasQuestItems(player, new int[]{2902, 2903}) && !hasQuestItems(player, 2904)) {
                  takeItems(player, 2903, 1L);
                  giveItems(player, 2904, 1L);
                  htmltext = "30436-01.htm";
               } else if (hasQuestItems(player, new int[]{2902, 2904}) && !hasQuestItems(player, 2903)) {
                  htmltext = "30436-02.htm";
               } else if (!hasQuestItems(player, 2902) && this.hasAtLeastOneQuestItem(player, new int[]{2910, 2914, 2880})) {
                  htmltext = "30436-03.htm";
               }
               break;
            case 30507:
               if (!this.hasAtLeastOneQuestItem(player, new int[]{2902, 2910, 2914, 2880})) {
                  htmltext = "30507-01.htm";
               } else if (hasQuestItems(player, 2902) && !this.hasAtLeastOneQuestItem(player, new int[]{2903, 2904})) {
                  htmltext = "30507-03.htm";
               } else if (hasQuestItems(player, new int[]{2902, 2903}) && !hasQuestItems(player, 2904)) {
                  htmltext = "30507-04.htm";
               } else if (hasQuestItems(player, new int[]{2902, 2904}) && !hasQuestItems(player, 2903)) {
                  if (hasQuestItems(player, new int[]{2905, 2906, 2907, 2908, 2909})) {
                     takeItems(player, 2902, 1L);
                     takeItems(player, 2904, 1L);
                     takeItems(player, 2905, 1L);
                     takeItems(player, 2906, 1L);
                     takeItems(player, 2907, 1L);
                     takeItems(player, 2908, 1L);
                     takeItems(player, 2909, 1L);
                     giveItems(player, 2910, 1L);
                     if (hasQuestItems(player, new int[]{2887, 2901, 2894})) {
                        qs.setCond(2);
                     }

                     htmltext = "30507-06.htm";
                  } else {
                     htmltext = "30507-05.htm";
                  }
               } else if (!hasQuestItems(player, 2902) && this.hasAtLeastOneQuestItem(player, new int[]{2910, 2914, 2880})) {
                  htmltext = "30507-07.htm";
               }
               break;
            case 30510:
               if (!this.hasAtLeastOneQuestItem(player, new int[]{2880, 2882})) {
                  if (hasQuestItems(player, new int[]{2887, 2901, 2910, 2894})) {
                     giveItems(player, 2880, 1L);
                     takeItems(player, 2887, 1L);
                     takeItems(player, 2894, 1L);
                     takeItems(player, 2901, 1L);
                     takeItems(player, 2910, 1L);
                     qs.setCond(3);
                     htmltext = "30510-07.htm";
                  } else {
                     htmltext = "30510-06.htm";
                  }
               } else if (hasQuestItems(player, 2880)) {
                  if (getQuestItemsCount(player, 2881) < 13L) {
                     htmltext = "30510-08.htm";
                  } else {
                     takeItems(player, 2880, 1L);
                     giveItems(player, 2882, 1L);
                     giveItems(player, 2911, 1L);
                     giveItems(player, 2912, 1L);
                     giveItems(player, 2913, 1L);
                     giveItems(player, 2914, 1L);
                     qs.setCond(5);
                     htmltext = "30510-09.htm";
                  }
               } else if (hasQuestItems(player, 2882)) {
                  htmltext = "30510-10.htm";
               }
               break;
            case 30515:
               if (!this.hasAtLeastOneQuestItem(player, new int[]{2895, 2913, 2880, 2901})) {
                  htmltext = "30515-01.htm";
               } else if (hasQuestItems(player, 2895)) {
                  if (hasQuestItems(player, new int[]{2896, 2897, 2898, 2899, 2900})) {
                     takeItems(player, 2895, 1L);
                     takeItems(player, 2896, 1L);
                     takeItems(player, 2897, 1L);
                     takeItems(player, 2898, 1L);
                     takeItems(player, 2899, 1L);
                     takeItems(player, 2900, 1L);
                     giveItems(player, 2901, 1L);
                     if (hasQuestItems(player, new int[]{2887, 2910, 2894})) {
                        qs.setCond(2);
                     }

                     htmltext = "30515-04.htm";
                  } else {
                     htmltext = "30515-03.htm";
                  }
               } else if (!hasQuestItems(player, 2895) && this.hasAtLeastOneQuestItem(player, new int[]{2901, 2913, 2880})) {
                  htmltext = "30515-05.htm";
               }
               break;
            case 30630:
               if (!this.hasAtLeastOneQuestItem(player, new int[]{2883, 2887, 2911, 2880})) {
                  htmltext = "30630-01.htm";
               } else if (hasQuestItems(player, 2883)) {
                  if (getQuestItemsCount(player, 2884) + getQuestItemsCount(player, 2885) + getQuestItemsCount(player, 2886) < 30L) {
                     htmltext = "30630-05.htm";
                  } else {
                     takeItems(player, 2883, 1L);
                     takeItems(player, 2884, -1L);
                     takeItems(player, 2885, -1L);
                     takeItems(player, 2886, -1L);
                     giveItems(player, 2887, 1L);
                     if (hasQuestItems(player, new int[]{2901, 2910, 2894})) {
                        qs.setCond(2);
                     }

                     htmltext = "30630-06.htm";
                  }
               } else if (!hasQuestItems(player, 2883) && this.hasAtLeastOneQuestItem(player, new int[]{2887, 2911, 2880})) {
                  htmltext = "30630-07.htm";
               }
               break;
            case 30649:
               if (hasQuestItems(player, 2882)) {
                  htmltext = "30649-01.htm";
               }
               break;
            case 30682:
               if (!this.hasAtLeastOneQuestItem(player, new int[]{2888, 2894, 2912, 2880})) {
                  htmltext = "30682-01.htm";
               } else if (hasQuestItems(player, 2888)) {
                  if (hasQuestItems(player, new int[]{2889, 2890, 2891, 2892, 2893})) {
                     takeItems(player, 2888, 1L);
                     takeItems(player, 2889, 1L);
                     takeItems(player, 2890, 1L);
                     takeItems(player, 2891, 1L);
                     takeItems(player, 2892, 1L);
                     takeItems(player, 2893, 1L);
                     giveItems(player, 2894, 1L);
                     if (hasQuestItems(player, new int[]{2887, 2901, 2910})) {
                        qs.setCond(2);
                     }

                     htmltext = "30682-04.htm";
                  } else {
                     htmltext = "30682-03.htm";
                  }
               } else if (!hasQuestItems(player, 2888) && this.hasAtLeastOneQuestItem(player, new int[]{2894, 2912, 2880})) {
                  htmltext = "30682-05.htm";
               }
         }
      } else if (qs.isCompleted() && npc.getId() == 30510) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _233_TestOfWarspirit(233, _233_TestOfWarspirit.class.getSimpleName(), "");
   }
}

package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _220_TestimonyOfGlory extends Quest {
   private static final int PREFECT_KASMAN = 30501;
   private static final int PREFECT_VOKIAN = 30514;
   private static final int SEER_MANAKIA = 30515;
   private static final int FLAME_LORD_KAKAI = 30565;
   private static final int SEER_TANAPI = 30571;
   private static final int BREKA_CHIEF_VOLTAR = 30615;
   private static final int ENKU_CHIEF_KEPRA = 30616;
   private static final int TUREK_CHIEF_BURAI = 30617;
   private static final int LEUNT_CHIEF_HARAK = 30618;
   private static final int VUKU_CHIEF_DRIKO = 30619;
   private static final int GANDI_CHIEF_CHIANTA = 30642;
   private static final int VOKIANS_ORDER = 3204;
   private static final int MANASHEN_SHARD = 3205;
   private static final int TYRANT_TALON = 3206;
   private static final int GUARDIAN_BASILISK_FANG = 3207;
   private static final int VOKIANS_ORDER2 = 3208;
   private static final int NECKLACE_OF_AUTHORITY = 3209;
   private static final int CHIANTA_1ST_ORDER = 3210;
   private static final int SCEPTER_OF_BREKA = 3211;
   private static final int SCEPTER_OF_ENKU = 3212;
   private static final int SCEPTER_OF_VUKU = 3213;
   private static final int SCEPTER_OF_TUREK = 3214;
   private static final int SCEPTER_OF_TUNATH = 3215;
   private static final int CHIANTA_2ND_ORDER = 3216;
   private static final int CHIANTA_3RD_ORDER = 3217;
   private static final int TAMLIN_ORC_SKULL = 3218;
   private static final int TIMAK_ORC_HEAD = 3219;
   private static final int SCEPTER_BOX = 3220;
   private static final int PASHIKAS_HEAD = 3221;
   private static final int VULTUS_HEAD = 3222;
   private static final int GLOVE_OF_VOLTAR = 3223;
   private static final int ENKU_OVERLORD_HEAD = 3224;
   private static final int GLOVE_OF_KEPRA = 3225;
   private static final int MAKUM_BUGBEAR_HEAD = 3226;
   private static final int GLOVE_OF_BURAI = 3227;
   private static final int MANAKIA_1ST_LETTER = 3228;
   private static final int MANAKIA_2ND_LETTER = 3229;
   private static final int KASMANS_1ST_LETTER = 3230;
   private static final int KASMANS_2ND_LETTER = 3231;
   private static final int KASMANS_3RD_LETTER = 3232;
   private static final int DRIKOS_CONTRACT = 3233;
   private static final int STAKATO_DRONE_HUSK = 3234;
   private static final int TANAPIS_ORDER = 3235;
   private static final int SCEPTER_OF_TANTOS = 3236;
   private static final int RITUAL_BOX = 3237;
   private static final int MARK_OF_GLORY = 3203;
   private static final int DIMENSIONAL_DIAMOND = 7562;
   private static final int TYRANT = 20192;
   private static final int TYRANT_KINGPIN = 20193;
   private static final int MARSH_STAKATO_DRONE = 20234;
   private static final int GUARDIAN_BASILISK = 20550;
   private static final int MANASHEN_GARGOYLE = 20563;
   private static final int TIMAK_ORC = 20583;
   private static final int TIMAK_ORC_ARCHER = 20584;
   private static final int TIMAK_ORC_SOLDIER = 20585;
   private static final int TIMAK_ORC_WARRIOR = 20586;
   private static final int TIMAK_ORC_SHAMAN = 20587;
   private static final int TIMAK_ORC_OVERLORD = 20588;
   private static final int TAMLIN_ORC = 20601;
   private static final int TAMLIN_ORC_ARCHER = 20602;
   private static final int RAGNA_ORC_OVERLORD = 20778;
   private static final int RAGNA_ORC_SEER = 20779;
   private static final int PASHIKA_SON_OF_VOLTAR = 27080;
   private static final int VULTUS_SON_OF_VOLTAR = 27081;
   private static final int ENKU_ORC_OVERLORD = 27082;
   private static final int MAKUM_BUGBEAR_THUG = 27083;
   private static final int REVENANT_OF_TANTOS_CHIEF = 27086;
   private static final int MIN_LEVEL = 37;

   public _220_TestimonyOfGlory(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30514);
      this.addTalkId(new int[]{30514, 30501, 30515, 30565, 30571, 30615, 30616, 30617, 30618, 30619, 30642});
      this.addKillId(
         new int[]{20192, 20193, 20234, 20550, 20563, 20583, 20584, 20585, 20586, 20587, 20588, 20601, 20602, 20778, 20779, 27080, 27081, 27082, 27083, 27086}
      );
      this.addAttackId(new int[]{20778, 20779, 27086});
      this.questItemIds = new int[]{
         3204,
         3205,
         3206,
         3207,
         3208,
         3209,
         3210,
         3211,
         3212,
         3213,
         3214,
         3215,
         3216,
         3217,
         3218,
         3219,
         3220,
         3221,
         3222,
         3223,
         3224,
         3225,
         3226,
         3227,
         3228,
         3229,
         3230,
         3231,
         3232,
         3233,
         3234,
         3235,
         3236,
         3237
      };
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
                  playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  giveItems(player, 3204, 1L);
                  if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                     giveItems(player, 7562, 109L);
                     player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                  }

                  htmltext = "30514-05a.htm";
               }
               break;
            case "30514-04.htm":
            case "30514-07.htm":
            case "30571-02.htm":
            case "30615-03.htm":
            case "30616-03.htm":
            case "30642-02.htm":
            case "30642-06.htm":
            case "30642-08.htm":
               htmltext = event;
               break;
            case "30501-02.htm":
               if (hasQuestItems(player, 3213)) {
                  htmltext = event;
               } else if (!this.hasAtLeastOneQuestItem(player, new int[]{3213, 3230})) {
                  giveItems(player, 3230, 1L);
                  player.getRadar().addMarker(-2150, 124443, -3724);
                  htmltext = "30501-03.htm";
               } else if (!hasQuestItems(player, 3213) && this.hasAtLeastOneQuestItem(player, new int[]{3230, 3233})) {
                  player.getRadar().addMarker(-2150, 124443, -3724);
                  htmltext = "30501-04.htm";
               }
               break;
            case "30501-05.htm":
               if (hasQuestItems(player, 3214)) {
                  htmltext = event;
               } else if (!this.hasAtLeastOneQuestItem(player, new int[]{3214, 3231})) {
                  giveItems(player, 3231, 1L);
                  player.getRadar().addMarker(-94294, 110818, -3563);
                  htmltext = "30501-06.htm";
               } else if (!hasQuestItems(player, 3214) && hasQuestItems(player, 3231)) {
                  player.getRadar().addMarker(-94294, 110818, -3563);
                  htmltext = "30501-07.htm";
               }
               break;
            case "30501-08.htm":
               if (hasQuestItems(player, 3215)) {
                  htmltext = event;
               } else if (!this.hasAtLeastOneQuestItem(player, new int[]{3215, 3232})) {
                  giveItems(player, 3232, 1L);
                  player.getRadar().addMarker(-55217, 200628, -3724);
                  htmltext = "30501-09.htm";
               } else if (!hasQuestItems(player, 3215) && hasQuestItems(player, 3232)) {
                  player.getRadar().addMarker(-55217, 200628, -3724);
                  htmltext = "30501-10.htm";
               }
               break;
            case "30515-04.htm":
               if (!hasQuestItems(player, 3211) && hasQuestItems(player, 3228)) {
                  player.getRadar().addMarker(80100, 119991, -2264);
                  htmltext = event;
               } else if (hasQuestItems(player, 3211)) {
                  htmltext = "30515-02.htm";
               } else if (!this.hasAtLeastOneQuestItem(player, new int[]{3211, 3228})) {
                  giveItems(player, 3228, 1L);
                  player.getRadar().addMarker(80100, 119991, -2264);
                  htmltext = "30515-03.htm";
               }
               break;
            case "30515-05.htm":
               if (hasQuestItems(player, 3212)) {
                  htmltext = event;
               } else if (!this.hasAtLeastOneQuestItem(player, new int[]{3212, 3229})) {
                  giveItems(player, 3229, 1L);
                  player.getRadar().addMarker(12805, 189249, -3616);
                  htmltext = "30515-06.htm";
               } else if (!hasQuestItems(player, 3212) && hasQuestItems(player, 3229)) {
                  player.getRadar().addMarker(12805, 189249, -3616);
                  htmltext = "30515-07.htm";
               }
               break;
            case "30571-03.htm":
               if (hasQuestItems(player, 3220)) {
                  takeItems(player, 3220, 1L);
                  giveItems(player, 3235, 1L);
                  qs.setCond(9, true);
                  htmltext = event;
               }
               break;
            case "30615-04.htm":
               if (hasQuestItems(player, 3228)) {
                  giveItems(player, 3223, 1L);
                  takeItems(player, 3228, 1L);
                  qs.addSpawn(27080, 80117, 120039, -2259, 200000);
                  qs.addSpawn(27081, 80058, 120038, -2259, 200000);
                  attackPlayer(qs.addSpawn(27080, 80117, 120039, -2259, 200000), player);
                  attackPlayer(qs.addSpawn(27081, 80058, 120038, -2259, 200000), player);
                  htmltext = event;
               }
               break;
            case "30616-04.htm":
               if (hasQuestItems(player, 3229)) {
                  giveItems(player, 3225, 1L);
                  takeItems(player, 3229, 1L);
                  attackPlayer(qs.addSpawn(27082, 19456, 192245, -3730, 200000), player);
                  attackPlayer(qs.addSpawn(27082, 19539, 192343, -3728, 200000), player);
                  attackPlayer(qs.addSpawn(27082, 19500, 192449, -3729, 200000), player);
                  attackPlayer(qs.addSpawn(27082, 19569, 192482, -3728, 200000), player);
                  htmltext = event;
               }
               break;
            case "30617-03.htm":
               if (hasQuestItems(player, 3231)) {
                  giveItems(player, 3227, 1L);
                  takeItems(player, 3231, 1L);
                  attackPlayer(qs.addSpawn(27083, -94292, 110781, -3701, 200000), player);
                  attackPlayer(qs.addSpawn(27083, -94293, 110861, -3701, 200000), player);
                  htmltext = event;
               }
               break;
            case "30618-03.htm":
               if (hasQuestItems(player, 3232)) {
                  giveItems(player, 3215, 1L);
                  takeItems(player, 3232, 1L);
                  if (hasQuestItems(player, new int[]{3214, 3212, 3211, 3213})) {
                     qs.setCond(5, true);
                  }

                  htmltext = event;
               }
               break;
            case "30619-03.htm":
               if (hasQuestItems(player, 3230)) {
                  giveItems(player, 3233, 1L);
                  takeItems(player, 3230, 1L);
                  htmltext = event;
               }
               break;
            case "30642-03.htm":
               if (hasQuestItems(player, 3208)) {
                  takeItems(player, 3208, 1L);
                  giveItems(player, 3210, 1L);
                  qs.setCond(4, true);
                  htmltext = event;
               }
               break;
            case "30642-07.htm":
               if (hasQuestItems(player, new int[]{3210, 3211, 3213, 3214, 3215, 3212})) {
                  takeItems(player, 3210, 1L);
                  takeItems(player, 3211, 1L);
                  takeItems(player, 3212, 1L);
                  takeItems(player, 3213, 1L);
                  takeItems(player, 3214, 1L);
                  takeItems(player, 3215, 1L);
                  takeItems(player, 3228, 1L);
                  takeItems(player, 3229, 1L);
                  takeItems(player, 3230, 1L);
                  giveItems(player, 3217, 1L);
                  qs.setCond(6, true);
                  htmltext = event;
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      QuestState qs = this.getQuestState(attacker, false);
      if (qs != null && qs.isStarted()) {
         switch(npc.getId()) {
            case 20778:
            case 20779:
               switch(npc.getScriptValue()) {
                  case 0:
                     npc.getVariables().set("lastAttacker", attacker.getObjectId());
                     if (!hasQuestItems(attacker, 3236)) {
                        npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.IS_IT_A_LACKEY_OF_KAKAI), 2000);
                        npc.setScriptValue(1);
                     }

                     return super.onAttack(npc, attacker, damage, isSummon);
                  case 1:
                     npc.setScriptValue(2);
                     return super.onAttack(npc, attacker, damage, isSummon);
                  default:
                     return super.onAttack(npc, attacker, damage, isSummon);
               }
            case 27086:
               switch(npc.getScriptValue()) {
                  case 0:
                     npc.getVariables().set("lastAttacker", attacker.getObjectId());
                     if (!hasQuestItems(attacker, 3236)) {
                        npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.HOW_REGRETFUL_UNJUST_DISHONOR), 2000);
                        npc.setScriptValue(1);
                     }
                     break;
                  case 1:
                     if (!hasQuestItems(attacker, 3236) && npc.getCurrentHp() < npc.getMaxHp() / 3.0) {
                        npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.INDIGNANT_AND_UNFAIR_DEATH), 2000);
                        npc.setScriptValue(2);
                     }
               }
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState qs = this.getQuestState(killer, false);
      if (qs != null && qs.isStarted() && Util.checkIfInRange(1500, npc, killer, true)) {
         switch(npc.getId()) {
            case 20192:
            case 20193:
               if (hasQuestItems(killer, 3204) && getQuestItemsCount(killer, 3206) < 10L) {
                  if (getQuestItemsCount(killer, 3206) == 9L) {
                     giveItems(killer, 3206, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(killer, 3205) >= 10L && getQuestItemsCount(killer, 3207) >= 10L) {
                        qs.setCond(2);
                     }
                  } else {
                     giveItems(killer, 3206, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20234:
               if (!hasQuestItems(killer, 3213) && hasQuestItems(killer, new int[]{3209, 3210, 3233}) && getQuestItemsCount(killer, 3234) < 30L) {
                  if (getQuestItemsCount(killer, 3206) == 29L) {
                     giveItems(killer, 3234, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     giveItems(killer, 3234, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20550:
               if (hasQuestItems(killer, 3204) && getQuestItemsCount(killer, 3207) < 10L) {
                  if (getQuestItemsCount(killer, 3207) == 9L) {
                     giveItems(killer, 3207, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(killer, 3205) >= 10L && getQuestItemsCount(killer, 3206) >= 10L) {
                        qs.setCond(2);
                     }
                  } else {
                     giveItems(killer, 3207, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20563:
               if (hasQuestItems(killer, 3204) && getQuestItemsCount(killer, 3205) < 10L) {
                  if (getQuestItemsCount(killer, 3205) == 9L) {
                     giveItems(killer, 3205, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(killer, 3206) >= 10L && getQuestItemsCount(killer, 3207) >= 10L) {
                        qs.setCond(2);
                     }
                  } else {
                     giveItems(killer, 3205, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20583:
            case 20584:
            case 20585:
            case 20586:
            case 20587:
            case 20588:
               if (hasQuestItems(killer, new int[]{3209, 3217}) && getQuestItemsCount(killer, 3219) < 20L) {
                  if (getQuestItemsCount(killer, 3205) == 19L) {
                     giveItems(killer, 3219, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(killer, 3218) >= 20L) {
                        qs.setCond(7);
                     }
                  } else {
                     giveItems(killer, 3219, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20601:
            case 20602:
               if (hasQuestItems(killer, new int[]{3209, 3217}) && getQuestItemsCount(killer, 3218) < 20L) {
                  if (getQuestItemsCount(killer, 3218) == 19L) {
                     giveItems(killer, 3218, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                     if (getQuestItemsCount(killer, 3219) >= 20L) {
                        qs.setCond(7);
                     }
                  } else {
                     giveItems(killer, 3218, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20778:
            case 20779:
               if (hasQuestItems(killer, 3235) && !hasQuestItems(killer, 3236)) {
                  qs.addSpawn(27086, 200000);
                  npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.TOO_LATE), 2000);
               }
               break;
            case 27080:
               if (hasQuestItems(killer, new int[]{3209, 3210, 3223}) && !hasQuestItems(killer, 3221)) {
                  if (hasQuestItems(killer, 3222)) {
                     giveItems(killer, 3221, 1L);
                     takeItems(killer, 3223, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     giveItems(killer, 3221, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 27081:
               if (hasQuestItems(killer, new int[]{3209, 3210, 3223}) && !hasQuestItems(killer, 3222)) {
                  if (hasQuestItems(killer, 3221)) {
                     giveItems(killer, 3222, 1L);
                     takeItems(killer, 3223, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     giveItems(killer, 3222, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 27082:
               if (hasQuestItems(killer, new int[]{3209, 3210, 3225}) && getQuestItemsCount(killer, 3224) < 4L) {
                  if (getQuestItemsCount(killer, 3224) == 3L) {
                     giveItems(killer, 3224, 1L);
                     takeItems(killer, 3225, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     giveItems(killer, 3224, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 27083:
               if (hasQuestItems(killer, new int[]{3209, 3210, 3227}) && getQuestItemsCount(killer, 3226) < 2L) {
                  if (getQuestItemsCount(killer, 3226) == 1L) {
                     giveItems(killer, 3226, 1L);
                     takeItems(killer, 3227, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     giveItems(killer, 3226, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 27086:
               if (hasQuestItems(killer, 3235) && !hasQuestItems(killer, 3236)) {
                  giveItems(killer, 3236, 1L);
                  npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.ILL_GET_REVENGE_SOMEDAY), 2000);
                  qs.setCond(10, true);
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
         if (npc.getId() == 30514) {
            if (player.getRace() == Race.Orc) {
               if (player.getLevel() >= 37 && player.isInCategory(CategoryType.ORC_2ND_GROUP)) {
                  htmltext = "30514-03.htm";
               } else if (player.getLevel() >= 37) {
                  htmltext = "30514-01a.htm";
               } else {
                  htmltext = "30514-02.htm";
               }
            } else {
               htmltext = "30514-01.htm";
            }
         }
      } else if (qs.isStarted()) {
         switch(npc.getId()) {
            case 30501:
               if (hasQuestItems(player, new int[]{3209, 3210})) {
                  htmltext = "30501-01.htm";
               } else if (this.hasAtLeastOneQuestItem(player, new int[]{3216, 3217, 3220})) {
                  htmltext = "30501-11.htm";
               }
               break;
            case 30514:
               if (hasQuestItems(player, 3204)) {
                  if (getQuestItemsCount(player, 3205) >= 10L && getQuestItemsCount(player, 3206) >= 10L && getQuestItemsCount(player, 3207) >= 10L) {
                     takeItems(player, 3204, 1L);
                     takeItems(player, 3205, -1L);
                     takeItems(player, 3206, -1L);
                     takeItems(player, 3207, -1L);
                     giveItems(player, 3208, 1L);
                     giveItems(player, 3209, 1L);
                     qs.setCond(3, true);
                     htmltext = "30514-08.htm";
                  } else {
                     htmltext = "30514-06.htm";
                  }
               } else if (hasQuestItems(player, new int[]{3208, 3209})) {
                  htmltext = "30514-09.htm";
               } else if (!hasQuestItems(player, 3209) && this.hasAtLeastOneQuestItem(player, new int[]{3208, 3220})) {
                  htmltext = "30514-10.htm";
               }
               break;
            case 30515:
               if (hasQuestItems(player, new int[]{3209, 3210})) {
                  htmltext = "30515-01.htm";
               } else if (this.hasAtLeastOneQuestItem(player, new int[]{3216, 3217, 3220})) {
                  htmltext = "30515-08.htm";
               }
               break;
            case 30565:
               if (!hasQuestItems(player, 3237) && this.hasAtLeastOneQuestItem(player, new int[]{3220, 3235})) {
                  htmltext = "30565-01.htm";
               } else if (hasQuestItems(player, 3237)) {
                  this.giveAdena(player, 262720L, true);
                  giveItems(player, 3203, 1L);
                  addExpAndSp(player, 1448226L, 96648);
                  qs.exitQuest(false, true);
                  player.sendPacket(new SocialAction(player.getObjectId(), 3));
                  htmltext = "30565-02.htm";
               }
               break;
            case 30571:
               if (hasQuestItems(player, 3220)) {
                  htmltext = "30571-01.htm";
               } else if (hasQuestItems(player, 3235)) {
                  if (!hasQuestItems(player, 3236)) {
                     htmltext = "30571-04.htm";
                  } else {
                     takeItems(player, 3235, 1L);
                     takeItems(player, 3236, 1L);
                     giveItems(player, 3237, 1L);
                     qs.setCond(11, true);
                     htmltext = "30571-05.htm";
                  }
               } else if (hasQuestItems(player, 3237)) {
                  htmltext = "30571-06.htm";
               }
               break;
            case 30615:
               if (hasQuestItems(player, new int[]{3209, 3210})) {
                  if (!this.hasAtLeastOneQuestItem(player, new int[]{3211, 3228, 3223, 3221, 3222})) {
                     htmltext = "30615-01.htm";
                  } else if (hasQuestItems(player, 3228)) {
                     htmltext = "30615-02.htm";
                     player.getRadar().removeMarker(80100, 119991, -2264);
                  } else if (!hasQuestItems(player, 3211)
                     && hasQuestItems(player, 3223)
                     && getQuestItemsCount(player, 3221) + getQuestItemsCount(player, 3222) < 2L) {
                     attackPlayer(qs.addSpawn(27080, 80117, 120039, -2259, 200000), player);
                     attackPlayer(qs.addSpawn(27081, 80058, 120038, -2259, 200000), player);
                     htmltext = "30615-05.htm";
                  } else if (hasQuestItems(player, new int[]{3221, 3222})) {
                     giveItems(player, 3211, 1L);
                     takeItems(player, 3221, 1L);
                     takeItems(player, 3222, 1L);
                     if (hasQuestItems(player, new int[]{3212, 3213, 3214, 3215})) {
                        qs.setCond(5, true);
                     }

                     htmltext = "30615-06.htm";
                  } else if (hasQuestItems(player, 3211)) {
                     htmltext = "30615-07.htm";
                  }
               } else if (this.hasAtLeastOneQuestItem(player, new int[]{3216, 3217, 3220})) {
                  htmltext = "30615-08.htm";
               }
               break;
            case 30616:
               if (hasQuestItems(player, new int[]{3209, 3210})) {
                  if (!this.hasAtLeastOneQuestItem(player, new int[]{3212, 3229, 3225}) && getQuestItemsCount(player, 3224) < 4L) {
                     htmltext = "30616-01.htm";
                  } else if (hasQuestItems(player, 3229)) {
                     player.getRadar().removeMarker(12805, 189249, -3616);
                     htmltext = "30616-02.htm";
                  } else if (hasQuestItems(player, 3225) && getQuestItemsCount(player, 3224) < 4L) {
                     attackPlayer(qs.addSpawn(27082, 17710, 189813, -3581, 200000), player);
                     attackPlayer(qs.addSpawn(27082, 17674, 189798, -3581, 200000), player);
                     attackPlayer(qs.addSpawn(27082, 17770, 189852, -3581, 200000), player);
                     attackPlayer(qs.addSpawn(27082, 17803, 189873, -3581, 200000), player);
                     htmltext = "30616-05.htm";
                  } else if (getQuestItemsCount(player, 3224) >= 4L) {
                     giveItems(player, 3212, 1L);
                     takeItems(player, 3224, -1L);
                     if (hasQuestItems(player, new int[]{3211, 3213, 3214, 3215})) {
                        qs.setCond(5, true);
                     }

                     htmltext = "30616-06.htm";
                  } else if (hasQuestItems(player, 3212)) {
                     htmltext = "30616-07.htm";
                  }
               } else if (this.hasAtLeastOneQuestItem(player, new int[]{3216, 3217, 3220})) {
                  htmltext = "30616-08.htm";
               }
               break;
            case 30617:
               if (hasQuestItems(player, new int[]{3209, 3210})) {
                  if (!this.hasAtLeastOneQuestItem(player, new int[]{3214, 3231, 3227, 3226})) {
                     htmltext = "30617-01.htm";
                  } else if (hasQuestItems(player, 3231)) {
                     player.getRadar().removeMarker(-94294, 110818, -3563);
                     htmltext = "30617-02.htm";
                  } else if (hasQuestItems(player, 3227)) {
                     attackPlayer(qs.addSpawn(27083, -94292, 110781, -3701, 200000), player);
                     attackPlayer(qs.addSpawn(27083, -94293, 110861, -3701, 200000), player);
                     htmltext = "30617-04.htm";
                  } else if (getQuestItemsCount(player, 3226) >= 2L) {
                     giveItems(player, 3214, 1L);
                     takeItems(player, 3226, -1L);
                     if (hasQuestItems(player, new int[]{3212, 3211, 3213, 3215})) {
                        qs.setCond(5, true);
                     }

                     htmltext = "30617-05.htm";
                  } else if (hasQuestItems(player, 3214)) {
                     htmltext = "30617-06.htm";
                  }
               } else if (hasQuestItems(player, 3209) && this.hasAtLeastOneQuestItem(player, new int[]{3216, 3217, 3220})) {
                  htmltext = "30617-07.htm";
               }
               break;
            case 30618:
               if (hasQuestItems(player, new int[]{3209, 3210})) {
                  if (!this.hasAtLeastOneQuestItem(player, new int[]{3215, 3232})) {
                     htmltext = "30618-01.htm";
                  } else if (!hasQuestItems(player, 3215) && hasQuestItems(player, 3232)) {
                     player.getRadar().removeMarker(-55217, 200628, -3724);
                     htmltext = "30618-02.htm";
                  } else if (hasQuestItems(player, 3215)) {
                     htmltext = "30618-04.htm";
                  }
               } else if (this.hasAtLeastOneQuestItem(player, new int[]{3216, 3217, 3220})) {
                  htmltext = "30618-05.htm";
               }
               break;
            case 30619:
               if (hasQuestItems(player, new int[]{3209, 3210})) {
                  if (!this.hasAtLeastOneQuestItem(player, new int[]{3213, 3230, 3233})) {
                     htmltext = "30619-01.htm";
                  } else if (!hasQuestItems(player, 3213) && hasQuestItems(player, 3230)) {
                     player.getRadar().removeMarker(-2150, 124443, -3724);
                     htmltext = "30619-02.htm";
                  } else if (!hasQuestItems(player, 3213) && hasQuestItems(player, 3233)) {
                     if (getQuestItemsCount(player, 3234) < 30L) {
                        htmltext = "30619-04.htm";
                     } else {
                        giveItems(player, 3213, 1L);
                        takeItems(player, 3233, 1L);
                        takeItems(player, 3234, -1L);
                        if (hasQuestItems(player, new int[]{3214, 3212, 3211, 3215})) {
                           qs.setCond(5, true);
                        }

                        htmltext = "30619-05.htm";
                     }
                  } else if (hasQuestItems(player, 3213)) {
                     htmltext = "30619-06.htm";
                  }
               } else if (hasQuestItems(player, 3209) && this.hasAtLeastOneQuestItem(player, new int[]{3216, 3217, 3220})) {
                  htmltext = "30619-07.htm";
               }
               break;
            case 30642:
               if (hasQuestItems(player, new int[]{3209, 3208})) {
                  htmltext = "30642-01.htm";
               } else if (hasQuestItems(player, new int[]{3209, 3210})) {
                  if (getQuestItemsCount(player, 3211)
                        + getQuestItemsCount(player, 3213)
                        + getQuestItemsCount(player, 3214)
                        + getQuestItemsCount(player, 3215)
                        + getQuestItemsCount(player, 3212)
                     < 5L) {
                     htmltext = "30642-04.htm";
                  } else if (hasQuestItems(player, new int[]{3211, 3213, 3214, 3215, 3212})) {
                     htmltext = "30642-05.htm";
                  }
               } else if (hasQuestItems(player, new int[]{3209, 3216})) {
                  giveItems(player, 3217, 1L);
                  takeItems(player, 3216, 1L);
                  htmltext = "30642-09.htm";
               } else if (hasQuestItems(player, new int[]{3209, 3217})) {
                  if (getQuestItemsCount(player, 3218) >= 20L && getQuestItemsCount(player, 3219) >= 20L) {
                     takeItems(player, 3209, 1L);
                     takeItems(player, 3217, 1L);
                     takeItems(player, 3218, -1L);
                     takeItems(player, 3219, -1L);
                     giveItems(player, 3220, 1L);
                     qs.setCond(8, true);
                     htmltext = "30642-11.htm";
                  } else {
                     htmltext = "30642-10.htm";
                  }
               } else if (hasQuestItems(player, 3220)) {
                  htmltext = "30642-12.htm";
               } else if (this.hasAtLeastOneQuestItem(player, new int[]{3235, 3237})) {
                  htmltext = "30642-13.htm";
               }
         }
      } else if (qs.isCompleted() && npc.getId() == 30514) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   private static void attackPlayer(Npc npc, Player player) {
      Attackable monster = (Attackable)npc;
      if (monster != null && player != null) {
         monster.setIsRunning(true);
         monster.addDamageHate(player, 0, 999);
         monster.getAI().setIntention(CtrlIntention.ATTACK, player);
      }
   }

   public static void main(String[] args) {
      new _220_TestimonyOfGlory(220, _220_TestimonyOfGlory.class.getSimpleName(), "");
   }
}

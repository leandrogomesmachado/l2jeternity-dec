package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SocialAction;

public final class _229_TestOfWitchcraft extends Quest {
   private static final int GROCER_LARA = 30063;
   private static final int TRADER_ALEXANDRIA = 30098;
   private static final int MAGISTER_IKER = 30110;
   private static final int PRIEST_VADIN = 30188;
   private static final int TRADER_NESTLE = 30314;
   private static final int SIR_KLAUS_VASPER = 30417;
   private static final int LEOPOLD = 30435;
   private static final int MAGISTER_KAIRA = 30476;
   private static final int SHADOW_ORIM = 30630;
   private static final int WARDEN_RODERIK = 30631;
   private static final int WARDEN_ENDRIGO = 30632;
   private static final int FISHER_EVERT = 30633;
   private static final int SWORD_OF_BINDING = 3029;
   private static final int ORIMS_DIAGRAM = 3308;
   private static final int ALEXANDRIAS_BOOK = 3309;
   private static final int IKERS_LIST = 3310;
   private static final int DIRE_WYRM_FANG = 3311;
   private static final int LETO_LIZARDMAN_CHARM = 3312;
   private static final int ENCHANTED_STONE_GOLEM_HEARTSTONE = 3313;
   private static final int LARAS_MEMO = 3314;
   private static final int NESTLES_MEMO = 3315;
   private static final int LEOPOLDS_JOURNAL = 3316;
   private static final int AKLANTOTH_1ST_GEM = 3317;
   private static final int AKLANTOTH_2ND_GEM = 3318;
   private static final int AKLANTOTH_3RD_GEM = 3319;
   private static final int AKLANTOTH_4TH_GEM = 3320;
   private static final int AKLANTOTH_5TH_GEM = 3321;
   private static final int AKLANTOTH_6TH_GEM = 3322;
   private static final int BRIMSTONE_1ST = 3323;
   private static final int ORIMS_INSTRUCTIONS = 3324;
   private static final int ORIMS_1ST_LETTER = 3325;
   private static final int ORIMS_2ND_LETTER = 3326;
   private static final int SIR_VASPERS_LETTER = 3327;
   private static final int VADINS_CRUCIFIX = 3328;
   private static final int TAMLIN_ORC_AMULET = 3329;
   private static final int VADINS_SANCTIONS = 3330;
   private static final int IKERS_AMULET = 3331;
   private static final int SOULTRAP_CRYSTAL = 3332;
   private static final int PURGATORY_KEY = 3333;
   private static final int ZERUEL_BIND_CRYSTAL = 3334;
   private static final int BRIMSTONE_2ND = 3335;
   private static final int MARK_OF_WITCHCRAFT = 3307;
   private static final int DIMENSIONAL_DIAMOND = 7562;
   private static final int DIRE_WYRM = 20557;
   private static final int ENCHANTED_STONE_GOLEM = 20565;
   private static final int LETO_LIZARDMAN = 20577;
   private static final int LETO_LIZARDMAN_ARCHER = 20578;
   private static final int LETO_LIZARDMAN_SOLDIER = 20579;
   private static final int LETO_LIZARDMAN_WARRIOR = 20580;
   private static final int LETO_LIZARDMAN_SHAMAN = 20581;
   private static final int LETO_LIZARDMAN_OVERLORD = 20582;
   private static final int TAMLIN_ORC = 20601;
   private static final int TAMLIN_ORC_ARCHER = 20602;
   private static final int NAMELESS_REVENANT = 27099;
   private static final int SKELETAL_MERCENARY = 27100;
   private static final int DREVANUL_PRINCE_ZERUEL = 27101;
   private static final int MIN_LEVEL = 39;

   public _229_TestOfWitchcraft(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30630);
      this.addTalkId(new int[]{30630, 30063, 30098, 30110, 30188, 30314, 30417, 30435, 30476, 30631, 30632, 30633});
      this.addKillId(new int[]{20557, 20565, 20577, 20578, 20579, 20580, 20581, 20582, 20601, 20602, 27099, 27100, 27101});
      this.addAttackId(new int[]{27099, 27100, 27101});
      this.questItemIds = new int[]{
         3029,
         3308,
         3309,
         3310,
         3311,
         3312,
         3313,
         3314,
         3315,
         3316,
         3317,
         3318,
         3319,
         3320,
         3321,
         3322,
         3323,
         3324,
         3325,
         3326,
         3327,
         3328,
         3329,
         3330,
         3331,
         3332,
         3333,
         3334,
         3335
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
                  giveItems(player, 3308, 1L);
                  if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                     if (player.getClassId() == ClassId.wizard) {
                        giveItems(player, 7562, 122L);
                     } else {
                        giveItems(player, 7562, 104L);
                     }

                     player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                  }

                  htmltext = "30630-08a.htm";
               }
               break;
            case "30630-04.htm":
            case "30630-06.htm":
            case "30630-07.htm":
            case "30630-12.htm":
            case "30630-13.htm":
            case "30630-20.htm":
            case "30630-21.htm":
            case "30098-02.htm":
            case "30110-02.htm":
            case "30417-02.htm":
               htmltext = event;
               break;
            case "30630-14.htm":
               if (hasQuestItems(player, 3309)) {
                  takeItems(player, 3309, 1L);
                  takeItems(player, 3317, 1L);
                  takeItems(player, 3318, 1L);
                  takeItems(player, 3319, 1L);
                  takeItems(player, 3320, 1L);
                  takeItems(player, 3321, 1L);
                  takeItems(player, 3322, 1L);
                  giveItems(player, 3323, 1L);
                  qs.setCond(4, true);
                  qs.addSpawn(27101);
                  htmltext = event;
               }
               break;
            case "30630-16.htm":
               if (hasQuestItems(player, 3323)) {
                  takeItems(player, 3323, 1L);
                  giveItems(player, 3324, 1L);
                  giveItems(player, 3325, 1L);
                  giveItems(player, 3326, 1L);
                  qs.setCond(6, true);
                  htmltext = event;
               }
               break;
            case "30630-22.htm":
               if (hasQuestItems(player, 3334)) {
                  this.giveAdena(player, 372154L, true);
                  giveItems(player, 3307, 1L);
                  addExpAndSp(player, 2058244L, 141240);
                  qs.exitQuest(false, true);
                  player.sendPacket(new SocialAction(player.getObjectId(), 3));
                  htmltext = event;
               }
               break;
            case "30063-02.htm":
               giveItems(player, 3314, 1L);
               htmltext = event;
               break;
            case "30098-03.htm":
               if (hasQuestItems(player, 3308)) {
                  takeItems(player, 3308, 1L);
                  giveItems(player, 3309, 1L);
                  qs.setCond(2, true);
                  htmltext = event;
               }
               break;
            case "30110-03.htm":
               giveItems(player, 3310, 1L);
               htmltext = event;
               break;
            case "30110-08.htm":
               takeItems(player, 3326, 1L);
               giveItems(player, 3331, 1L);
               giveItems(player, 3332, 1L);
               if (hasQuestItems(player, 3029)) {
                  qs.setCond(7, true);
               }

               htmltext = event;
               break;
            case "30314-02.htm":
               giveItems(player, 3315, 1L);
               htmltext = event;
               break;
            case "30417-03.htm":
               if (hasQuestItems(player, 3325)) {
                  takeItems(player, 3325, 1L);
                  giveItems(player, 3327, 1L);
                  htmltext = event;
               }
               break;
            case "30435-02.htm":
               if (hasQuestItems(player, 3315)) {
                  takeItems(player, 3315, 1L);
                  giveItems(player, 3316, 1L);
                  htmltext = event;
               }
               break;
            case "30476-02.htm":
               giveItems(player, 3318, 1L);
               if (hasQuestItems(player, new int[]{3317, 3319, 3320, 3321, 3322})) {
                  qs.setCond(3, true);
               }

               htmltext = event;
               break;
            case "30633-02.htm":
               giveItems(player, 3335, 1L);
               qs.setCond(9, true);
               qs.addSpawn(27101);
               htmltext = event;
         }

         return htmltext;
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      QuestState qs = this.getQuestState(attacker, false);
      if (qs != null && qs.isStarted()) {
         switch(npc.getId()) {
            case 27099:
               if (npc.isScriptValue(0) && hasQuestItems(attacker, new int[]{3309, 3314}) && !hasQuestItems(attacker, 3319)) {
                  npc.setScriptValue(1);
                  npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.I_ABSOLUTELY_CANNOT_GIVE_IT_TO_YOU_IT_IS_MY_PRECIOUS_JEWEL), 2000);
               }
               break;
            case 27100:
               if (npc.isScriptValue(0) && hasQuestItems(attacker, 3316) && !hasQuestItems(attacker, new int[]{3320, 3321, 3322})) {
                  npc.setScriptValue(1);
                  npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.I_ABSOLUTELY_CANNOT_GIVE_IT_TO_YOU_IT_IS_MY_PRECIOUS_JEWEL), 2000);
               }
               break;
            case 27101:
               if (hasQuestItems(attacker, 3323)) {
                  npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.ILL_TAKE_YOUR_LIVES_LATER), 2000);
                  npc.deleteMe();
                  qs.setCond(5, true);
               } else if (hasQuestItems(attacker, new int[]{3324, 3335, 3029, 3332}) && npc.isScriptValue(0) && this.checkWeapon(attacker)) {
                  npc.setScriptValue(1);
                  npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.THAT_SWORD_IS_REALLY), 2000);
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
            case 20557:
               if (hasQuestItems(killer, new int[]{3309, 3310}) && getQuestItemsCount(killer, 3311) < 20L) {
                  giveItems(killer, 3311, 1L);
                  if (getQuestItemsCount(killer, 3311) >= 20L) {
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20565:
               if (hasQuestItems(killer, new int[]{3309, 3310}) && getQuestItemsCount(killer, 3313) < 20L) {
                  giveItems(killer, 3313, 1L);
                  if (getQuestItemsCount(killer, 3313) >= 20L) {
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20577:
            case 20578:
            case 20579:
            case 20580:
            case 20581:
            case 20582:
               if (hasQuestItems(killer, new int[]{3309, 3310}) && getQuestItemsCount(killer, 3312) < 20L) {
                  giveItems(killer, 3312, 1L);
                  if (getQuestItemsCount(killer, 3312) >= 20L) {
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 20601:
            case 20602:
               if (hasQuestItems(killer, 3328) && getRandom(100) < 50 && getQuestItemsCount(killer, 3329) < 20L) {
                  giveItems(killer, 3329, 1L);
                  if (getQuestItemsCount(killer, 3329) >= 20L) {
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                  } else {
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  }
               }
               break;
            case 27099:
               if (hasQuestItems(killer, new int[]{3309, 3314}) && !hasQuestItems(killer, 3319)) {
                  takeItems(killer, 3314, 1L);
                  giveItems(killer, 3319, 1L);
                  playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  if (hasQuestItems(killer, new int[]{3317, 3318, 3320, 3321, 3322})) {
                     qs.setCond(3);
                  }
               }
               break;
            case 27100:
               if (hasQuestItems(killer, 3316) && !hasQuestItems(killer, new int[]{3320, 3321, 3322})) {
                  if (!hasQuestItems(killer, 3320)) {
                     giveItems(killer, 3320, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  } else if (!hasQuestItems(killer, 3321)) {
                     giveItems(killer, 3321, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  } else if (!hasQuestItems(killer, 3322)) {
                     takeItems(killer, 3316, 1L);
                     giveItems(killer, 3322, 1L);
                     playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                     if (hasQuestItems(killer, new int[]{3317, 3318, 3319})) {
                        qs.setCond(3);
                     }
                  }
               }
               break;
            case 27101:
               if (hasQuestItems(killer, new int[]{3324, 3335, 3029, 3332}) && killer.getActiveWeaponItem().getId() == 3029) {
                  npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.NO_I_HAVENT_COMPLETELY_FINISHED_THE_COMMAND_FOR_DESTRUCTION_AND_SLAUGHTER_YET), 2000);
                  takeItems(killer, 3332, 1L);
                  giveItems(killer, 3333, 1L);
                  giveItems(killer, 3334, 1L);
                  takeItems(killer, 3335, 1L);
                  playSound(killer, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
                  qs.setCond(10);
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
         if (npc.getId() == 30630) {
            if (player.getClassId() != ClassId.wizard && player.getClassId() != ClassId.knight && player.getClassId() != ClassId.palusKnight) {
               htmltext = "30630-01.htm";
            } else if (player.getLevel() >= 39) {
               if (player.getClassId() == ClassId.wizard) {
                  htmltext = "30630-03.htm";
               } else {
                  htmltext = "30630-05.htm";
               }
            } else {
               htmltext = "30630-02.htm";
            }
         }
      } else if (qs.isStarted()) {
         switch(npc.getId()) {
            case 30063:
               if (hasQuestItems(player, 3309)) {
                  if (!this.hasAtLeastOneQuestItem(player, new int[]{3314, 3319})) {
                     htmltext = "30063-01.htm";
                  } else if (!hasQuestItems(player, 3319) && hasQuestItems(player, 3314)) {
                     htmltext = "30063-03.htm";
                  } else if (!hasQuestItems(player, 3314) && hasQuestItems(player, 3319)) {
                     htmltext = "30063-04.htm";
                  }
               } else if (this.hasAtLeastOneQuestItem(player, new int[]{3323, 3324})) {
                  htmltext = "30063-05.htm";
               }
               break;
            case 30098:
               if (hasQuestItems(player, 3308)) {
                  htmltext = "30098-01.htm";
               } else if (hasQuestItems(player, 3309)) {
                  htmltext = "30098-04.htm";
               } else if (hasQuestItems(player, new int[]{3324, 3323})) {
                  htmltext = "30098-05.htm";
               }
               break;
            case 30110:
               if (hasQuestItems(player, 3309)) {
                  if (!this.hasAtLeastOneQuestItem(player, new int[]{3310, 3317})) {
                     htmltext = "30110-01.htm";
                  } else if (hasQuestItems(player, 3310)) {
                     if (getQuestItemsCount(player, 3311) >= 20L && getQuestItemsCount(player, 3312) >= 20L && getQuestItemsCount(player, 3313) >= 20L) {
                        takeItems(player, 3310, 1L);
                        takeItems(player, 3311, -1L);
                        takeItems(player, 3312, -1L);
                        takeItems(player, 3313, -1L);
                        giveItems(player, 3317, 1L);
                        if (hasQuestItems(player, new int[]{3318, 3319, 3320, 3321, 3322})) {
                           qs.setCond(3, true);
                        }

                        htmltext = "30110-05.htm";
                     } else {
                        htmltext = "30110-04.htm";
                     }
                  } else if (!hasQuestItems(player, 3310) && hasQuestItems(player, 3317)) {
                     htmltext = "30110-06.htm";
                  }
               } else if (hasQuestItems(player, 3324)) {
                  if (!this.hasAtLeastOneQuestItem(player, new int[]{3332, 3334})) {
                     htmltext = "30110-07.htm";
                  } else if (!hasQuestItems(player, 3334) && hasQuestItems(player, 3332)) {
                     htmltext = "30110-09.htm";
                  } else if (!hasQuestItems(player, 3332) && hasQuestItems(player, 3334)) {
                     htmltext = "30110-10.htm";
                  }
               }
               break;
            case 30188:
               if (hasQuestItems(player, new int[]{3324, 3327})) {
                  takeItems(player, 3327, 1L);
                  giveItems(player, 3328, 1L);
                  htmltext = "30188-01.htm";
               } else if (hasQuestItems(player, 3328)) {
                  if (getQuestItemsCount(player, 3329) < 20L) {
                     htmltext = "30188-02.htm";
                  } else {
                     takeItems(player, 3328, 1L);
                     takeItems(player, 3329, -1L);
                     giveItems(player, 3330, 1L);
                     htmltext = "30188-03.htm";
                  }
               } else if (hasQuestItems(player, 3324)) {
                  if (hasQuestItems(player, 3330)) {
                     htmltext = "30188-04.htm";
                  } else if (hasQuestItems(player, 3029)) {
                     htmltext = "30188-05.htm";
                  }
               }
               break;
            case 30314:
               if (hasQuestItems(player, 3309)) {
                  if (!this.hasAtLeastOneQuestItem(player, new int[]{3316, 3315, 3320, 3321, 3322})) {
                     htmltext = "30314-01.htm";
                  } else if (hasQuestItems(player, 3315) && !hasQuestItems(player, 3316)) {
                     htmltext = "30314-03.htm";
                  } else if (!hasQuestItems(player, 3315) && this.hasAtLeastOneQuestItem(player, new int[]{3316, 3320, 3321, 3322})) {
                     htmltext = "30314-04.htm";
                  }
               }
               break;
            case 30417:
               if (hasQuestItems(player, 3324)) {
                  if (hasQuestItems(player, 3325)) {
                     htmltext = "30417-01.htm";
                  } else if (hasQuestItems(player, 3327)) {
                     htmltext = "30417-04.htm";
                  } else if (hasQuestItems(player, 3330)) {
                     giveItems(player, 3029, 1L);
                     takeItems(player, 3330, 1L);
                     if (hasQuestItems(player, 3332)) {
                        qs.setCond(7, true);
                     }

                     htmltext = "30417-05.htm";
                  } else if (hasQuestItems(player, 3029)) {
                     htmltext = "30417-06.htm";
                  }
               }
               break;
            case 30435:
               if (hasQuestItems(player, 3309)) {
                  if (hasQuestItems(player, 3315) && !hasQuestItems(player, 3316)) {
                     htmltext = "30435-01.htm";
                  } else if (hasQuestItems(player, 3316) && !hasQuestItems(player, 3315)) {
                     htmltext = "30435-03.htm";
                  } else if (hasQuestItems(player, new int[]{3320, 3321, 3322})) {
                     htmltext = "30435-04.htm";
                  }
               } else if (this.hasAtLeastOneQuestItem(player, new int[]{3323, 3324})) {
                  htmltext = "30435-05.htm";
               }
               break;
            case 30476:
               if (hasQuestItems(player, 3309)) {
                  if (!hasQuestItems(player, 3318)) {
                     htmltext = "30476-01.htm";
                  } else {
                     htmltext = "30476-03.htm";
                  }
               } else if (this.hasAtLeastOneQuestItem(player, new int[]{3323, 3324})) {
                  htmltext = "30476-04.htm";
               }
               break;
            case 30630:
               if (hasQuestItems(player, 3308)) {
                  htmltext = "30630-09.htm";
               } else if (hasQuestItems(player, 3309)) {
                  if (hasQuestItems(player, new int[]{3317, 3318, 3319, 3320, 3321, 3322})) {
                     htmltext = "30630-11.htm";
                  } else {
                     htmltext = "30630-10.htm";
                  }
               } else if (hasQuestItems(player, 3323)) {
                  htmltext = "30630-15.htm";
               } else if (hasQuestItems(player, 3324) && !this.hasAtLeastOneQuestItem(player, new int[]{3029, 3332})) {
                  htmltext = "30630-17.htm";
               }

               if (hasQuestItems(player, new int[]{3029, 3332})) {
                  qs.setCond(8, true);
                  htmltext = "30630-18.htm";
               } else if (hasQuestItems(player, new int[]{3029, 3334})) {
                  htmltext = "30630-19.htm";
               }
               break;
            case 30631:
               if (hasQuestItems(player, 3309) && this.hasAtLeastOneQuestItem(player, new int[]{3314, 3319})) {
                  htmltext = "30631-01.htm";
               }
               break;
            case 30632:
               if (hasQuestItems(player, 3309) && this.hasAtLeastOneQuestItem(player, new int[]{3314, 3319})) {
                  htmltext = "30632-01.htm";
               }
               break;
            case 30633:
               if (hasQuestItems(player, 3324)) {
                  if (hasQuestItems(player, new int[]{3332, 3029}) && !hasQuestItems(player, 3335)) {
                     htmltext = "30633-01.htm";
                  } else if (hasQuestItems(player, new int[]{3332, 3335}) && !hasQuestItems(player, 3334)) {
                     qs.addSpawn(27101);
                     htmltext = "30633-02.htm";
                  } else if (hasQuestItems(player, 3334) && !this.hasAtLeastOneQuestItem(player, new int[]{3332, 3335})) {
                     htmltext = "30633-03.htm";
                  }
               }
         }
      } else if (qs.isCompleted() && npc.getId() == 30630) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   private boolean checkWeapon(Player player) {
      ItemInstance weapon = player.getActiveWeaponInstance();
      return weapon != null && weapon.getId() == 3029;
   }

   public static void main(String[] args) {
      new _229_TestOfWitchcraft(229, _229_TestOfWitchcraft.class.getSimpleName(), "");
   }
}

package l2e.scripts.custom;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class NewbieGuideSystem extends Quest {
   private static final List<Integer> guides = new ArrayList<>();
   private static final int SS_NOVICE = 5789;
   private static final int SPS_NOVICE = 5790;
   private static final int SCROLL_ID = 8594;
   private static final int COUPON_ONE = 7832;
   private static final int COUPON_TWO = 7833;

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      int reward = st.getInt("reward");
      int level = player.getLevel();
      if (event.equalsIgnoreCase("newbie_give_weapon_coupon")) {
         if (level >= 6 && level <= 19 && player.getPkKills() <= 0) {
            if (st.getInt("reward") >= 2) {
               return "no-weapcoups.htm";
            } else {
               showOnScreenMsg(
                  player, NpcStringId.ACQUISITION_OF_WEAPON_EXCHANGE_COUPON_FOR_BEGINNERS_COMPLETE_N_GO_SPEAK_WITH_THE_NEWBIE_GUIDE, 2, 5000, new String[0]
               );
               st.giveItems(7832, 1L);
               st.giveItems(7832, 1L);
               st.giveItems(7832, 1L);
               st.giveItems(7832, 1L);
               st.giveItems(7832, 1L);
               st.set("reward", "2");
               return "coupons-list.htm";
            }
         } else {
            return "no-weapcoups-1.htm";
         }
      } else if (event.equalsIgnoreCase("newbie_show_weapon")) {
         if (level >= 6 && level <= 19 && player.getPkKills() <= 0) {
            MultiSellParser.getInstance().separateAndSend(305986001, player, npc, false);
            return "newbieitems-list.htm";
         } else {
            return "no-weapon-warehouse.htm";
         }
      } else {
         if (event.equalsIgnoreCase("newbie_give_armor_coupon")) {
            if (reward == 3) {
               return "no-armorcoups.htm";
            }

            if (level > 19 && level < 40 && player.getPkKills() < 5 && player.getClassId().level() == 1) {
               st.giveItems(7833, 1L);
               st.set("reward", "3");
               return "armorcoups.htm";
            }

            if (level < 20 || player.getPkKills() > 0 || player.getClassId().level() != 1) {
               return "no-armorcoups-1.htm";
            }
         } else if (event.equalsIgnoreCase("newbie_show_armor")) {
            if (level >= 20 && player.getPkKills() <= 0 && player.getClassId().level() == 1) {
               MultiSellParser.getInstance().separateAndSend(305986002, player, npc, false);
               return "newbieitems-list.htm";
            }

            return "no-armor-warehouse.htm";
         }

         npc.showChatWindow(player);
         return "";
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      if (Config.DISABLE_TUTORIAL) {
         npc.showChatWindow(player);
         return null;
      } else {
         QuestState st = player.getQuestState(this.getName());
         if (st == null) {
            st = this.newQuestState(player);
         }

         int npcId = npc.getId();
         int step = st.getInt("step");
         int reward = st.getInt("reward");
         int level = player.getLevel();
         boolean isMage = player.getClassId().isMage();
         switch(npcId) {
            case 30598:
               if (reward == 0) {
                  if (isMage) {
                     st.playTutorialVoice("tutorial_voice_027");
                     st.giveItems(5790, 100L);
                  } else {
                     st.playTutorialVoice("tutorial_voice_026");
                     st.giveItems(5789, 200L);
                  }

                  st.giveItems(8594, 2L);
                  st.addExpAndSp(68, 50);
                  st.set("reward", "1");
                  st.set("step", "1");
                  st.setState((byte)1);
                  st.addRadar(-84436, 242793, -3720);
                  return "Human01.htm";
               }

               if (level < 6 && reward >= 1) {
                  if (player.getQuestState("_001_LettersOfLove") == null || player.getQuestState("_001_LettersOfLove").getState() == 1) {
                     st.addRadar(-84436, 242793, -3720);
                     return "Human011.htm";
                  }

                  if (player.getQuestState("_001_LettersOfLove").getState() == 2) {
                     if (step == 1) {
                        st.rewardItems(57, 695L);
                        st.addExpAndSp(3154, 127);
                        st.set("step", "2");
                        st.addRadar(-82236, 241573, -3728);
                        return "Human02.htm";
                     }

                     st.addRadar(-82236, 241573, -3728);
                     return "Human022.htm";
                  }
               }

               if (level > 5 && level < 40 && player.getPkKills() == 0 && player.getClassId().level() == 0 && reward == 1) {
                  return "Level6.htm";
               }

               if (level > 5 && level < 10 && reward >= 1 && step < 3) {
                  if (st.getQuestItemsCount(5789) >= 5000L || st.getQuestItemsCount(5790) >= 2000L) {
                     st.rewardItems(57, 11567L);
                     st.addExpAndSp(36942, 1541);
                     st.set("step", "3");
                     if (isMage) {
                        st.addRadar(-91008, 248016, -3560);
                        return "Human03m.htm";
                     }

                     st.addRadar(-71384, 258304, -3104);
                     return "Human03.htm";
                  }

                  if (player.getQuestState("_257_TheGuardIsBusy") == null || player.getQuestState("_257_TheGuardIsBusy").getState() == 1) {
                     st.addRadar(-82236, 241573, -3728);
                     return "Human022.htm";
                  }
               }

               if (level > 9 && level < 15 && reward >= 1 && step < 4) {
                  if (isMage) {
                     if (player.getQuestState("_104_SpiritOfMirrors") == null || player.getQuestState("_104_SpiritOfMirrors").getState() == 1) {
                        st.addRadar(-91008, 248016, -3560);
                        return "Human033m.htm";
                     }

                     if (player.getQuestState("_104_SpiritOfMirrors").getState() == 2) {
                        st.rewardItems(57, 31752L);
                        st.addExpAndSp(152653, 6914);
                        st.set("step", "4");
                        st.addRadar(-84057, 242832, -3728);
                        return "Human04.htm";
                     }
                  }

                  if (player.getQuestState("_101_SwordOfSolidarity") == null || player.getQuestState("_101_SwordOfSolidarity").getState() == 1) {
                     st.addRadar(-71384, 258304, -3104);
                     return "Human033.htm";
                  }

                  if (player.getQuestState("_101_SwordOfSolidarity").getState() == 2) {
                     st.rewardItems(57, 31752L);
                     st.addExpAndSp(152653, 6914);
                     st.set("step", "4");
                     st.addRadar(-84057, 242832, -3728);
                     return "Human04.htm";
                  }
               }

               if (level > 14 && level < 18 && reward >= 1 && step < 5) {
                  if (player.getQuestState("_151_CureForFeverDisease") == null) {
                     st.addRadar(-84057, 242832, -3728);
                     return "Human04.htm";
                  }

                  if (player.getQuestState("_151_CureForFeverDisease").getState() == 1 || player.getQuestState("_151_CureForFeverDisease").getState() == 2) {
                     st.rewardItems(57, 13648L);
                     st.addExpAndSp(285670, 58155);
                     st.set("step", "5");
                     return "Human05.htm";
                  }
               }

               if (level <= 17 && step <= 4) {
                  break;
               }

               return "Human05.htm";
            case 30599:
               if (reward == 0) {
                  if (isMage) {
                     st.playTutorialVoice("tutorial_voice_027");
                     st.giveItems(5790, 100L);
                  } else {
                     st.playTutorialVoice("tutorial_voice_026");
                     st.giveItems(5789, 200L);
                  }

                  st.giveItems(8594, 2L);
                  st.addExpAndSp(68, 50);
                  st.setState((byte)1);
                  st.set("reward", "1");
                  st.set("step", "1");
                  st.addRadar(42978, 49115, -2992);
                  return "Elven01.htm";
               }

               if (level < 6 && reward >= 1) {
                  if (player.getQuestState("_002_WhatWomenWant") == null || player.getQuestState("_002_WhatWomenWant").getState() == 1) {
                     st.addRadar(42978, 49115, -2992);
                     return "Elven011.htm";
                  }

                  if (player.getQuestState("_002_WhatWomenWant").getState() == 2) {
                     if (step == 1) {
                        st.rewardItems(57, 695L);
                        st.addExpAndSp(3154, 127);
                        st.set("step", "2");
                        st.addRadar(42812, 51138, -2992);
                        return "Elven02.htm";
                     }

                     st.addRadar(42812, 51138, -2992);
                     return "Elven022.htm";
                  }
               }

               if (level > 5 && level < 40 && player.getPkKills() > 0 && player.getClassId().level() == 0 && reward == 1) {
                  return "Level6.htm";
               }

               if (level > 5 && level < 10 && reward >= 1 && step < 3) {
                  if (st.getQuestItemsCount(5789) >= 5000L || st.getQuestItemsCount(5790) >= 2000L) {
                     st.rewardItems(57, 11567L);
                     st.addExpAndSp(36942, 1541);
                     st.set("step", "3");
                     st.addRadar(47595, 51569, -2992);
                     return "Elven03.htm";
                  }

                  if (player.getQuestState("_260_HuntTheOrcs") == null || player.getQuestState("_260_HuntTheOrcs").getState() == 1) {
                     st.addRadar(42812, 51138, -2992);
                     return "Elven022.htm";
                  }
               }

               if (level > 9 && level < 15 && reward >= 1 && step < 4) {
                  if (player.getQuestState("_105_SkirmishWithOrcs") == null || player.getQuestState("_105_SkirmishWithOrcs").getState() == 1) {
                     st.addRadar(47595, 51569, -2992);
                     return "Elven03.htm";
                  }

                  if (player.getQuestState("_105_SkirmishWithOrcs").getState() == 2) {
                     st.rewardItems(57, 31752L);
                     st.addExpAndSp(152653, 6914);
                     st.set("step", "4");
                     st.addRadar(45859, 50827, -3056);
                     return "Elven04.htm";
                  }
               }

               if (level > 14 && level < 18 && reward >= 1 && step < 5) {
                  if (player.getQuestState("_261_CollectorsDream") == null || player.getQuestState("_261_CollectorsDream").getState() == 1) {
                     st.addRadar(45859, 50827, -3056);
                     return "Elven04.htm";
                  }

                  if (player.getQuestState("_261_CollectorsDream").getState() == 2) {
                     st.rewardItems(57, 13648L);
                     st.addExpAndSp(285670, 58155);
                     st.set("step", "5");
                     return "Elven05.htm";
                  }
               }

               if (level > 17 || step > 4) {
                  return "Elven05.htm";
               }
               break;
            case 30600:
               if (reward == 0) {
                  if (isMage) {
                     st.playTutorialVoice("tutorial_voice_027");
                     st.giveItems(5790, 100L);
                  } else {
                     st.playTutorialVoice("tutorial_voice_026");
                     st.giveItems(5789, 200L);
                  }

                  st.giveItems(8594, 2L);
                  st.addExpAndSp(68, 50);
                  st.setState((byte)1);
                  st.set("reward", "1");
                  st.set("step", "1");
                  st.addRadar(25856, 10832, -3736);
                  return "Delf01.htm";
               }

               if (level < 6 && reward >= 1) {
                  if (player.getQuestState("_166_MassOfDarkness") == null || player.getQuestState("_166_MassOfDarkness").getState() == 1) {
                     st.addRadar(25856, 10832, -3736);
                     return "Delf011.htm";
                  }

                  if (player.getQuestState("_166_MassOfDarkness").getState() == 2) {
                     if (step == 1) {
                        st.rewardItems(57, 695L);
                        st.addExpAndSp(3154, 127);
                        st.set("step", "2");
                        st.addRadar(7644, 18048, -4392);
                        return "Delf02.htm";
                     }

                     st.addRadar(7644, 18048, -4392);
                     return "Delf022.htm";
                  }
               }

               if (level > 5 && level < 40 && player.getPkKills() > 0 && player.getClassId().level() == 0 && reward == 1) {
                  return "Level6.htm";
               }

               if (level > 5 && level < 10 && reward >= 1 && step < 3) {
                  if (st.getQuestItemsCount(5789) >= 5000L || st.getQuestItemsCount(5790) >= 2000L) {
                     st.rewardItems(57, 11567L);
                     st.addExpAndSp(36942, 1541);
                     st.set("step", "3");
                     if (isMage) {
                        st.addRadar(10775, 14190, -4256);
                        return "Delf03m.htm";
                     }

                     st.addRadar(10584, 17581, -4568);
                     return "Delf03.htm";
                  }

                  if (player.getQuestState("_265_ChainsOfSlavery") == null || player.getQuestState("_265_ChainsOfSlavery").getState() == 1) {
                     st.addRadar(7644, 18048, -4392);
                     return "Delf022.htm";
                  }
               }

               if (level > 9 && level < 15 && reward >= 1 && step < 4) {
                  if (isMage) {
                     if (player.getQuestState("_106_ForgottenTruth") == null || player.getQuestState("_106_ForgottenTruth").getState() == 1) {
                        st.addRadar(10775, 14190, -4256);
                        return "Delf033m.htm";
                     }

                     if (player.getQuestState("_106_ForgottenTruth").getState() == 2) {
                        st.rewardItems(57, 31752L);
                        st.addExpAndSp(152653, 6914);
                        st.set("step", "4");
                        st.addRadar(11258, 14431, -4256);
                        return "Delf04.htm";
                     }
                  }

                  if (player.getQuestState("_103_SpiritOfCraftsman") == null || player.getQuestState("_103_SpiritOfCraftsman").getState() == 1) {
                     st.addRadar(10584, 17581, -4568);
                     return "Delf033.htm";
                  }

                  if (player.getQuestState("_103_SpiritOfCraftsman").getState() == 2) {
                     st.rewardItems(57, 31752L);
                     st.addExpAndSp(152653, 6914);
                     st.set("step", "4");
                     st.addRadar(11258, 14431, -4256);
                     return "Delf04.htm";
                  }
               }

               if (level > 14 && level < 18 && reward >= 1 && step < 5) {
                  if (player.getQuestState("_169_NightmareChildren") == null) {
                     st.addRadar(11258, 14431, -4256);
                     return "Delf04.htm";
                  }

                  if (player.getQuestState("_169_NightmareChildren").getState() == 1 || player.getQuestState("_169_NightmareChildren").getState() == 2) {
                     st.rewardItems(57, 13648L);
                     st.addExpAndSp(285670, 58155);
                     st.set("step", "5");
                     return "Delf05.htm";
                  }
               }

               if (level <= 17 && step <= 4) {
                  break;
               }

               return "Delf05.htm";
            case 30601:
               if (reward == 0) {
                  if (isMage) {
                     st.playTutorialVoice("tutorial_voice_027");
                     st.giveItems(5790, 100L);
                  } else {
                     st.playTutorialVoice("tutorial_voice_026");
                     st.giveItems(5789, 200L);
                  }

                  st.giveItems(8594, 2L);
                  st.addExpAndSp(68, 50);
                  st.setState((byte)1);
                  st.set("reward", "1");
                  st.set("step", "1");
                  st.addRadar(112656, -174864, -608);
                  return "Dwarf01.htm";
               }

               if (level < 6 && reward >= 1) {
                  if (player.getQuestState("_005_MinersFavor") == null || player.getQuestState("_005_MinersFavor").getState() == 1) {
                     st.addRadar(112656, -174864, -608);
                     return "Dwarf011.htm";
                  }

                  if (player.getQuestState("_005_MinersFavor").getState() == 2) {
                     if (step == 1) {
                        st.rewardItems(57, 695L);
                        st.addExpAndSp(3154, 127);
                        st.set("step", "2");
                        st.addRadar(116103, -178407, -944);
                        return "Dwarf02.htm";
                     }

                     st.addRadar(116103, -178407, -944);
                     return "Dwarf022.htm";
                  }
               }

               if (level > 5 && level < 40 && player.getPkKills() > 0 && player.getClassId().level() == 0 && reward == 1) {
                  return "Level6.htm";
               }

               if (level > 5 && level < 10 && reward >= 1 && step < 3) {
                  if (st.getQuestItemsCount(5789) >= 5000L || st.getQuestItemsCount(5790) >= 2000L) {
                     st.rewardItems(57, 11567L);
                     st.addExpAndSp(36942, 1541);
                     st.set("step", "3");
                     st.addRadar(115717, -183488, -1472);
                     return "Dwarf03.htm";
                  }

                  if (player.getQuestState("_293_HiddenVein") == null || player.getQuestState("_293_HiddenVein").getState() == 1) {
                     st.addRadar(116103, -178407, -944);
                     return "Dwarf022.htm";
                  }
               }

               if (level > 9 && level < 15 && reward >= 1 && step < 4) {
                  if (player.getQuestState("_108_JumbleTumbleDiamondFuss") == null || player.getQuestState("_108_JumbleTumbleDiamondFuss").getState() == 1) {
                     st.addRadar(115717, -183488, -1472);
                     return "Dwarf033.htm";
                  }

                  if (player.getQuestState("_108_JumbleTumbleDiamondFuss").getState() == 2) {
                     st.rewardItems(57, 31752L);
                     st.addExpAndSp(152653, 6914);
                     st.set("step", "4");
                     st.addRadar(116268, -177524, -880);
                     return "Dwarf04.htm";
                  }
               }

               if (level > 14 && level < 18 && reward >= 1 && step < 5) {
                  if (player.getQuestState("_296_SilkOfTarantula") == null || player.getQuestState("_296_SilkOfTarantula").getState() == 1) {
                     st.addRadar(116268, -177524, -880);
                     return "Dwarf04.htm";
                  }

                  if (player.getQuestState("_296_SilkOfTarantula").getState() == 1 && player.getQuestState("_296_SilkOfTarantula").getInt("onlyone") == 1) {
                     st.rewardItems(57, 13648L);
                     st.addExpAndSp(285670, 58155);
                     st.set("step", "5");
                     return "Dwarf05.htm";
                  }
               }

               if (level > 17 || step > 4) {
                  return "Dwarf05.htm";
               }
               break;
            case 30602:
               if (reward == 0) {
                  if (isMage) {
                     st.playTutorialVoice("tutorial_voice_027");
                     st.giveItems(5790, 100L);
                  } else {
                     st.playTutorialVoice("tutorial_voice_026");
                     st.giveItems(5789, 200L);
                  }

                  st.giveItems(8594, 2L);
                  st.addExpAndSp(68, 50);
                  st.setState((byte)1);
                  st.set("reward", "1");
                  st.set("step", "1");
                  st.addRadar(-47360, -113791, -224);
                  return "Orc01.htm";
               }

               if (level < 6 && reward >= 1) {
                  if (player.getQuestState("_004_LongLiveThePaagrioLord") == null || player.getQuestState("_004_LongLiveThePaagrioLord").getState() == 1) {
                     st.addRadar(-47360, -113791, -224);
                     return "Orc011.htm";
                  }

                  if (player.getQuestState("_004_LongLiveThePaagrioLord").getState() == 2) {
                     if (step == 1) {
                        st.rewardItems(57, 695L);
                        st.addExpAndSp(3154, 127);
                        st.set("step", "2");
                        st.addRadar(-46802, -114011, -112);
                        return "Orc02.htm";
                     }

                     st.addRadar(-46802, -114011, -112);
                     return "Orc022.htm";
                  }
               }

               if (level > 5 && level < 40 && player.getPkKills() > 0 && player.getClassId().level() == 0 && reward == 1) {
                  return "Level6.htm";
               }

               if (level > 5 && level < 10 && reward >= 1 && step < 3) {
                  if (st.getQuestItemsCount(5789) >= 5000L || st.getQuestItemsCount(5790) >= 2000L) {
                     st.rewardItems(57, 11567L);
                     st.addExpAndSp(36942, 1541);
                     st.set("step", "3");
                     st.addRadar(-46808, -113184, -112);
                     return "Orc03.htm";
                  }

                  if (player.getQuestState("_273_InvadersOfHolyland") == null || player.getQuestState("_273_InvadersOfHolyland").getState() == 1) {
                     st.addRadar(-46802, -114011, -112);
                     return "Orc022.htm";
                  }
               }

               if (level > 9 && level < 15 && reward >= 1 && step < 4) {
                  if (player.getQuestState("_107_MercilessPunishment") == null || player.getQuestState("_107_MercilessPunishment").getState() == 1) {
                     st.addRadar(-46808, -113184, -112);
                     return "Orc033.htm";
                  }

                  if (player.getQuestState("_107_MercilessPunishment").getState() == 2) {
                     st.rewardItems(57, 31752L);
                     st.addExpAndSp(152653, 6914);
                     st.set("step", "4");
                     st.addRadar(-45863, -112621, -200);
                     return "Orc04.htm";
                  }
               }

               if (level > 14 && level < 18 && reward >= 1 && step < 5) {
                  if (player.getQuestState("_276_TotemOfTheHestui") == null || player.getQuestState("_276_TotemOfTheHestui").getState() == 1) {
                     st.addRadar(-45863, -112621, -200);
                     return "Orc04.htm";
                  }

                  if (player.getQuestState("_276_TotemOfTheHestui").getState() == 2) {
                     st.rewardItems(57, 13648L);
                     st.addExpAndSp(285670, 58155);
                     st.set("step", "5");
                     return "Orc05.htm";
                  }
               }

               if (level > 17 || step > 4) {
                  return "Orc05.htm";
               }
               break;
            case 31076:
            case 31077:
               npc.showChatWindow(player);
               return "";
            case 32135:
               if (reward == 0) {
                  if (isMage) {
                     st.playTutorialVoice("tutorial_voice_027");
                     st.giveItems(5790, 100L);
                  } else {
                     st.playTutorialVoice("tutorial_voice_026");
                     st.giveItems(5789, 200L);
                  }

                  st.giveItems(8594, 2L);
                  st.addExpAndSp(68, 50);
                  st.setState((byte)1);
                  st.set("reward", "1");
                  st.set("step", "1");
                  st.addRadar(-119378, 49242, 8);
                  return "Kamael01.htm";
               }

               if (level < 6 && reward >= 1) {
                  if (player.getQuestState("_174_SupplyCheck") == null || player.getQuestState("_174_SupplyCheck").getState() == 1) {
                     st.addRadar(-119378, 49242, 8);
                     return "Kamael011.htm";
                  }

                  if (player.getQuestState("_174_SupplyCheck").getState() == 2) {
                     if (step == 1) {
                        st.rewardItems(57, 695L);
                        st.addExpAndSp(3154, 127);
                        st.set("step", "2");
                        st.addRadar(-119378, 49242, 8);
                        return "Kamael02.htm";
                     }

                     st.addRadar(-119378, 49242, 8);
                     return "Kamael022.htm";
                  }
               }

               if (level > 5 && level < 40 && player.getPkKills() > 0 && player.getClassId().level() == 0 && reward == 1) {
                  return "Level6.htm";
               }

               if (level > 5 && level < 10 && reward >= 1 && step < 3) {
                  if (st.getQuestItemsCount(5789) >= 5000L || st.getQuestItemsCount(5790) >= 2000L) {
                     st.rewardItems(57, 11567L);
                     st.addExpAndSp(36942, 1541);
                     st.set("step", "3");
                     st.addRadar(-118080, 42835, 712);
                     return "Kamael03.htm";
                  }

                  if (player.getQuestState("_281_HeadForTheHills") == null || player.getQuestState("_281_HeadForTheHills").getState() == 1) {
                     st.addRadar(-119378, 49242, 8);
                     return "Kamael022.htm";
                  }
               }

               if (level > 9 && level < 15 && reward >= 1 && step < 4) {
                  if (player.getQuestState("_175_TheWayOfTheWarrior") == null || player.getQuestState("_175_TheWayOfTheWarrior").getState() == 1) {
                     st.addRadar(-118080, 42835, 712);
                     return "Kamael033.htm";
                  }

                  if (player.getQuestState("_175_TheWayOfTheWarrior").getState() == 2) {
                     st.rewardItems(57, 31752L);
                     st.addExpAndSp(152653, 6914);
                     st.set("step", "4");
                     st.addRadar(-125872, 38208, 1232);
                     return "Kamael04.htm";
                  }
               }

               if (level > 14 && level < 18 && reward >= 1 && step < 5) {
                  if (player.getQuestState("_283_TheFewTheProudTheBrave") == null || player.getQuestState("_283_TheFewTheProudTheBrave").getState() == 1) {
                     st.addRadar(-125872, 38208, 1232);
                     return "Kamael04.htm";
                  }

                  if (player.getQuestState("_283_TheFewTheProudTheBrave").getState() == 1
                     && player.getQuestState("_283_TheFewTheProudTheBrave").getInt("onlyone") == 1) {
                     st.rewardItems(57, 13648L);
                     st.addExpAndSp(285670, 58155);
                     st.set("step", "5");
                     return "Kamael05.htm";
                  }
               }

               if (level > 17 || step > 4) {
                  return "Kamael05.htm";
               }
         }

         npc.showChatWindow(player);
         return "";
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (guides.contains(npc.getId())) {
         return "newbieitems-list.htm";
      } else {
         npc.showChatWindow(player);
         return "";
      }
   }

   public NewbieGuideSystem(int id, String name, String desc) {
      super(id, name, desc);
      int[] ids = new int[]{30598, 30599, 30600, 30601, 30602, 31076, 31077, 32135};

      for(int i : ids) {
         this.addStartNpc(i);
         this.addFirstTalkId(i);
         this.addTalkId(i);
         guides.add(i);
      }
   }

   public static void main(String[] args) {
      new NewbieGuideSystem(-1, NewbieGuideSystem.class.getSimpleName(), "custom");
   }
}

package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _234_FatesWhisper extends Quest {
   public _234_FatesWhisper(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31002);
      this.addTalkId(new int[]{30178, 30182, 30833, 30847, 31002});
      this.addTalkId(new int[]{31027, 31028, 31030, 31029});
      this.addAttackId(29020);
      this.addKillId(new int[]{20823, 20826, 20827, 20828, 20829, 20830, 20831, 20860});
      this.questItemIds = new int[]{14361, 14362, 4665, 4666, 4667, 4668, 4669, 4670, 4671, 4672, 4673};
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, true);
      String htmltext = getNoQuestMsg(player);
      switch(npc.getId()) {
         case 30178:
            switch(qs.getMemoState()) {
               case 6:
                  return "30178-01.htm";
               case 7:
                  return "30178-03.htm";
               case 8:
                  return "30178-04.htm";
               default:
                  return htmltext;
            }
         case 30182:
            if (qs.isMemoState(4) && !qs.hasQuestItems(4672)) {
               return "30182-01.htm";
            }

            if (qs.isMemoState(4) && qs.hasQuestItems(4672)) {
               return "30182-05.htm";
            }

            if (qs.getMemoState() >= 5) {
               return "30182-06.htm";
            }
         case 30833:
            if (qs.isMemoState(7)) {
               return "30833-01.htm";
            }

            long bloodyFabricCount = qs.getQuestItemsCount(14361);
            long whiteFabricCount = qs.getQuestItemsCount(14362);
            long whiteBloodyFabricCount = bloodyFabricCount + whiteFabricCount;
            if (qs.isMemoState(8) && !qs.hasQuestItems(4673) && whiteBloodyFabricCount <= 0L) {
               return "30833-03.htm";
            }

            if (qs.isMemoState(8) && qs.hasQuestItems(4673) && whiteBloodyFabricCount <= 0L) {
               qs.giveItems(4671, 1L);
               qs.takeItems(4673, 1L);
               qs.setMemoState(9);
               qs.setCond(10, true);
               qs.showQuestionMark(false, 234);
               return "30833-04.htm";
            }

            if (qs.isMemoState(8) && !qs.hasQuestItems(4673) && bloodyFabricCount < 30L && whiteBloodyFabricCount >= 30L) {
               return "30833-03c.htm";
            }

            if (qs.isMemoState(8) && !qs.hasQuestItems(4673) && bloodyFabricCount >= 30L && whiteBloodyFabricCount >= 30L) {
               qs.giveItems(4671, 1L);
               qs.takeItems(14361, -1L);
               qs.setMemoState(9);
               qs.setCond(10, true);
               qs.showQuestionMark(false, 234);
               return "30833-03d.htm";
            }

            if (qs.isMemoState(8) && !qs.hasQuestItems(4673) && whiteBloodyFabricCount < 30L && whiteBloodyFabricCount > 0L) {
               qs.giveItems(14362, 30L - whiteFabricCount);
               qs.takeItems(14361, -1L);
               return "30833-03e.htm";
            }

            if (qs.getMemoState() >= 9) {
               return "30833-05.htm";
            }
            break;
         case 30847:
            if (qs.isMemoState(5)) {
               if (qs.hasQuestItems(4670)) {
                  return "30847-02.htm";
               }

               qs.giveItems(4670, 1L);
               return "30847-01.htm";
            }

            if (qs.getMemoState() >= 6) {
               return "30847-03.htm";
            }
            break;
         case 31002:
            if (qs.isCreated() && player.getLevel() >= 75) {
               return "31002-01.htm";
            }

            if (qs.isCreated() && player.getLevel() < 75) {
               return "31002-01a.htm";
            }

            if (qs.isCompleted()) {
               return getAlreadyCompletedMsg(player);
            }

            if (qs.isMemoState(1) && !qs.hasQuestItems(4666)) {
               return "31002-09.htm";
            }

            if (qs.isMemoState(1) && qs.hasQuestItems(4666)) {
               return "31002-10.htm";
            }

            if (qs.isMemoState(2) && !qs.hasQuestItems(4667, 4668, 4669)) {
               return "31002-12.htm";
            }

            if (qs.isMemoState(2) && qs.hasQuestItems(4667, 4668, 4669)) {
               return "31002-13.htm";
            }

            if (qs.isMemoState(4) && !qs.hasQuestItems(4672)) {
               return "31002-15.htm";
            }

            if (qs.isMemoState(4) && qs.hasQuestItems(4672)) {
               return "31002-16.htm";
            }

            if (qs.isMemoState(5) && !qs.hasQuestItems(4670)) {
               return "31002-18.htm";
            }

            if (qs.isMemoState(5) && qs.hasQuestItems(4670)) {
               return "31002-19.htm";
            }

            if (qs.getMemoState() < 9 && qs.getMemoState() >= 6) {
               return "31002-21.htm";
            }

            if (qs.isMemoState(9) && qs.hasQuestItems(4671)) {
               return "31002-22.htm";
            }

            if (qs.isMemoState(10) && qs.getQuestItemsCount(1460) < 984L) {
               return "31002-24.htm";
            }

            if (qs.isMemoState(10) && qs.getQuestItemsCount(1460) >= 984L) {
               return "31002-25.htm";
            }

            switch(qs.getMemoState()) {
               case 11:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{79, 4717, 4718, 4719})) {
                     return "31002-35.htm";
                  }

                  return "31002-35a.htm";
               case 12:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{4828, 4829, 4830, 287})) {
                     return "31002-36.htm";
                  }

                  return "31002-36a.htm";
               case 13:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{4858, 4859, 4860, 97})) {
                     return "31002-37.htm";
                  }

                  return "31002-37a.htm";
               case 14:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{4753, 4754, 4755, 175})) {
                     return "31002-38.htm";
                  }

                  return "31002-38a.htm";
               case 15:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{4900, 4901, 4902, 210})) {
                     return "31002-39.htm";
                  }

                  return "31002-39a.htm";
               case 16:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{4780, 4781, 4782, 234})) {
                     return "31002-40.htm";
                  }

                  return "31002-40a.htm";
               case 17:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{4804, 4805, 4806, 268})) {
                     return "31002-41.htm";
                  }

                  return "31002-41a.htm";
               case 18:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{4750, 4751, 4752, 171})) {
                     return "31002-42.htm";
                  }

                  return "31002-42a.htm";
               case 19:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{2626})) {
                     return "31002-43.htm";
                  }

                  return "31002-43a.htm";
               case 20:
               case 21:
               case 22:
               case 23:
               case 24:
               case 25:
               case 26:
               case 27:
               case 28:
               case 29:
               case 30:
               case 31:
               case 32:
               case 33:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 39:
               case 40:
               default:
                  return htmltext;
               case 41:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{7883, 8105, 8106, 8107})) {
                     return "31002-43b.htm";
                  }

                  return "31002-43c.htm";
               case 42:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{7889, 8117, 8118, 8119})) {
                     return "31002-43d.htm";
                  }

                  return "31002-43e.htm";
               case 43:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{7901, 8132, 8133, 8134})) {
                     return "31002-43f.htm";
                  }

                  return "31002-43g.htm";
               case 44:
                  if (this.hasAtLeastOneQuestItem(player, new int[]{7893, 8144, 8145, 8146})) {
                     return "31002-43h.htm";
                  }

                  return "31002-43i.htm";
            }
         case 31027:
            if (qs.isMemoState(1) && !qs.hasQuestItems(4666)) {
               qs.giveItems(4666, 1L);
               qs.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
               return "31027-01.htm";
            }

            if (qs.getMemoState() > 1 || qs.hasQuestItems(4666)) {
               return "31027-02.htm";
            }
            break;
         case 31028:
            if (qs.isMemoState(2) && !qs.hasQuestItems(4667)) {
               qs.giveItems(4667, 1L);
               qs.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
               return "31028-01.htm";
            }

            if (!qs.isMemoState(2) || qs.hasQuestItems(4667)) {
               return "31028-02.htm";
            }
            break;
         case 31029:
            if (qs.isMemoState(2) && !qs.hasQuestItems(4668)) {
               qs.giveItems(4668, 1L);
               qs.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
               return "31029-01.htm";
            }

            if (!qs.isMemoState(2) || qs.hasQuestItems(4668)) {
               return "31029-02.htm";
            }
            break;
         case 31030:
            if (qs.isMemoState(2) && !qs.hasQuestItems(4669)) {
               qs.giveItems(4669, 1L);
               qs.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
               return "31030-01.htm";
            }

            if (!qs.isMemoState(2) || qs.hasQuestItems(4669)) {
               return "31030-02.htm";
            }
      }

      return htmltext;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (player != null) {
         QuestState qs = this.getQuestState(player, false);
         if (qs == null) {
            return null;
         } else {
            String htmltext = null;
            if (event.equals("QUEST_ACCEPTED")) {
               qs.setMemoState(1);
               qs.startQuest();
               qs.showQuestionMark(false, 234);
               qs.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ACCEPT);
               return "31002-06.htm";
            } else if (event.contains(".htm")) {
               return event;
            } else {
               int npcId = npc.getId();
               int eventID = Integer.parseInt(event);
               switch(npcId) {
                  case 30178:
                     switch(eventID) {
                        case 1:
                           qs.setMemoState(7);
                           qs.setCond(6);
                           qs.showQuestionMark(false, 234);
                           qs.playSound(Quest.QuestSound.ITEMSOUND_QUEST_MIDDLE);
                           return "30178-02.htm";
                     }
                  case 30182:
                     switch(eventID) {
                        case 1:
                           return "30182-02.htm";
                        case 2:
                           return "30182-03.htm";
                        case 3:
                           if (qs.isMemoState(4) && !qs.hasQuestItems(4672)) {
                              qs.giveItems(4672, 1L);
                              return "30182-04.htm";
                           }

                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 30833:
                     switch(eventID) {
                        case 1:
                           if (qs.isMemoState(7)) {
                              return "30833-02.htm";
                           }

                           return htmltext;
                        case 2:
                           if (qs.isMemoState(7)) {
                              qs.giveItems(4665, 1L);
                              qs.setMemoState(8);
                              qs.setCond(7, true);
                              qs.showQuestionMark(false, 234);
                              return "30833-03a.htm";
                           }

                           return htmltext;
                        case 3:
                           if (qs.isMemoState(7)) {
                              qs.giveItems(14362, 30L);
                              qs.setMemoState(8);
                              qs.setCond(8, true);
                              qs.showQuestionMark(false, 234);
                              return "30833-03b.htm";
                           }

                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 31002:
                     switch(eventID) {
                        case 1:
                           return "31002-02.htm";
                        case 2:
                           return "31002-03.htm";
                        case 3:
                           return "31002-04.htm";
                        case 4:
                           if (!qs.isCompleted() && player.getLevel() >= 75) {
                              return "31002-05.htm";
                           }
                           break;
                        case 5:
                           if (qs.isMemoState(1) && qs.hasQuestItems(4666)) {
                              qs.takeItems(4666, 1L);
                              qs.setMemoState(2);
                              qs.setCond(2, true);
                              qs.showQuestionMark(false, 234);
                              return "31002-11.htm";
                           }
                           break;
                        case 6:
                           if (qs.isMemoState(2) && qs.hasQuestItems(4667, 4668, 4669)) {
                              qs.takeItems(4667, -1L);
                              qs.takeItems(4668, -1L);
                              qs.takeItems(4669, -1L);
                              qs.setMemoState(4);
                              qs.setCond(3, true);
                              qs.showQuestionMark(false, 234);
                              return "31002-14.htm";
                           }
                           break;
                        case 7:
                           if (qs.isMemoState(4) && qs.hasQuestItems(4672)) {
                              qs.takeItems(4672, 1L);
                              qs.setMemoState(5);
                              qs.setCond(4, true);
                              qs.showQuestionMark(false, 234);
                              return "31002-17.htm";
                           }
                           break;
                        case 8:
                           if (qs.isMemoState(5) && qs.hasQuestItems(4670)) {
                              qs.takeItems(4670, 1L);
                              qs.setMemoState(6);
                              qs.setCond(5, true);
                              qs.showQuestionMark(false, 234);
                              return "31002-20.htm";
                           }
                           break;
                        case 9:
                           if (qs.isMemoState(9) && qs.hasQuestItems(4671)) {
                              qs.takeItems(4671, 1L);
                              qs.setMemoState(10);
                              qs.setCond(11, true);
                              qs.showQuestionMark(false, 234);
                              return "31002-23.htm";
                           }
                           break;
                        case 10:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(11);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-26.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 11:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(19);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-26a.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 12:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(12);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-27.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 13:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(13);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-28.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 14:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(14);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-29.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 15:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(15);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-30.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 16:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(16);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-31.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 17:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(17);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-32.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 18:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(18);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-33.htm";
                              }

                              return "31002-34.htm";
                           }
                        case 19:
                        case 20:
                        case 33:
                        case 34:
                        case 35:
                        case 36:
                        case 37:
                        case 38:
                        case 39:
                        case 40:
                        default:
                           break;
                        case 21:
                           if (this.calculateReward(qs, player, 80)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 22:
                           if (this.calculateReward(qs, player, 288)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 23:
                           if (this.calculateReward(qs, player, 98)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 24:
                           if (this.calculateReward(qs, player, 150)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 25:
                           if (this.calculateReward(qs, player, 212)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 26:
                           if (this.calculateReward(qs, player, 235)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 27:
                           if (this.calculateReward(qs, player, 269)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 28:
                           if (this.calculateReward(qs, player, 2504)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 29:
                           if (this.calculateReward(qs, player, 5233)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 30:
                           if (this.calculateReward(qs, player, 7884)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 31:
                           if (this.calculateReward(qs, player, 7894)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 32:
                           if (this.calculateReward(qs, player, 7899)) {
                              return "31002-44.htm";
                           }
                           break;
                        case 41:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(41);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-33a.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 42:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(42);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-33b.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 43:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(43);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-33c.htm";
                              }

                              return "31002-34.htm";
                           }
                           break;
                        case 44:
                           if (qs.isMemoState(10)) {
                              if (qs.getQuestItemsCount(1460) >= 984L) {
                                 qs.takeItems(1460, 984L);
                                 qs.setMemoState(44);
                                 qs.setCond(12, true);
                                 qs.showQuestionMark(false, 234);
                                 return "31002-33d.htm";
                              }

                              return "31002-34.htm";
                           }
                     }
               }

               return htmltext;
            }
         }
      } else {
         if (event.equals("23401") || event.equals("23402") || event.equals("23403") || event.equals("23404")) {
            npc.decayMe();
         }

         return super.onAdvEvent(event, npc, player);
      }
   }

   private boolean calculateReward(QuestState qs, Player player, int reward) {
      switch(qs.getMemoState()) {
         case 11:
            return this.getReward(qs, player, 79, 4717, 4718, 4719, reward);
         case 12:
            return this.getReward(qs, player, 287, 4828, 4829, 4830, reward);
         case 13:
            return this.getReward(qs, player, 97, 4858, 4859, 4860, reward);
         case 14:
            return this.getReward(qs, player, 175, 4753, 4754, 4755, reward);
         case 15:
            return this.getReward(qs, player, 210, 4900, 4901, 4902, reward);
         case 16:
            return this.getReward(qs, player, 234, 4780, 4781, 4782, reward);
         case 17:
            return this.getReward(qs, player, 268, 4804, 4805, 4806, reward);
         case 18:
            return this.getReward(qs, player, 171, 4750, 4751, 4752, reward);
         case 19:
            return this.getReward(qs, player, 2626, 0, 0, 0, reward);
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         default:
            return false;
         case 41:
            return this.getReward(qs, player, 7883, 8105, 8106, 8107, reward);
         case 42:
            return this.getReward(qs, player, 7889, 8117, 8118, 8119, reward);
         case 43:
            return this.getReward(qs, player, 7901, 8132, 8133, 8134, reward);
         case 44:
            return this.getReward(qs, player, 7893, 8144, 8145, 8146, reward);
      }
   }

   private boolean getReward(QuestState qs, Player player, int item1, int item2, int item3, int item4, int reward) {
      if (this.hasAtLeastOneQuestItem(player, new int[]{item1, item2, item3, item4})) {
         qs.giveItems(reward, 1L);
         qs.giveItems(5011, 1L);
         if (qs.hasQuestItems(item1)) {
            qs.takeItems(item1, 1L);
         } else if (qs.hasQuestItems(item2)) {
            qs.takeItems(item2, 1L);
         } else if (qs.hasQuestItems(item3)) {
            qs.takeItems(item3, 1L);
         } else if (qs.hasQuestItems(item4)) {
            qs.takeItems(item4, 1L);
         }

         qs.exitQuest(false, true);
         player.broadcastSocialAction(3);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState qs = this.getRandomPartyMemberState(killer, -1, 2, npc);
      if (qs != null) {
         switch(npc.getId()) {
            case 20823:
            case 20826:
            case 20827:
            case 20828:
            case 20829:
            case 20830:
            case 20831:
            case 20860:
               giveItemRandomly(qs.getPlayer(), npc, 14361, 1L, 0L, 1.0, false);
               qs.takeItems(14362, 1L);
               if (qs.getQuestItemsCount(14361) >= 29L) {
                  qs.setCond(9, true);
                  qs.showQuestionMark(false, 234);
               } else {
                  qs.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
               }
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      QuestState qs = this.getQuestState(attacker, false);
      if (qs != null && npc.getId() == 29020 && attacker.getActiveWeaponItem() != null && attacker.getActiveWeaponItem().getId() == 4665) {
         qs.takeItems(4665, 1L);
         qs.giveItems(4673, 1L);
         qs.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
         npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.WHO_DARES_TO_TRY_AND_STEAL_MY_NOBLE_BLOOD), 2000);
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public boolean checkPartyMember(QuestState qs, Npc npc) {
      return qs.hasQuestItems(14362) && qs.isMemoState(8);
   }

   public static void main(String[] args) {
      new _234_FatesWhisper(234, _234_FatesWhisper.class.getSimpleName(), "");
   }
}

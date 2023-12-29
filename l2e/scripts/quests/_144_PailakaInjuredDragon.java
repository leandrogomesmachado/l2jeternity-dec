package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SpecialCamera;

public class _144_PailakaInjuredDragon extends Quest {
   private static int buff_counter = 5;
   private static boolean _hasDoneAnimation = false;
   private static final int[][] BUFFS = new int[][]{
      {4357, 2}, {4342, 2}, {4356, 3}, {4355, 3}, {4351, 6}, {4345, 3}, {4358, 3}, {4359, 3}, {4360, 3}, {4352, 2}, {4354, 4}, {4347, 6}
   };
   private static final List<_144_PailakaInjuredDragon.PailakaDrop> DROPLIST = new ArrayList<>();

   public _144_PailakaInjuredDragon(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32499);
      this.addFirstTalkId(new int[]{32499, 32502, 32509, 32512});
      this.addTalkId(new int[]{32499, 32502, 32509, 32512});
      this.addAggroRangeEnterId(new int[]{18660});
      this.addAttackId(18660);
      this.addKillId(
         new int[]{
            18637,
            18643,
            18651,
            18647,
            18660,
            18636,
            18642,
            18646,
            18654,
            18635,
            18657,
            18653,
            18649,
            18650,
            18655,
            18659,
            18658,
            18656,
            18652,
            18640,
            18645,
            18648,
            18644,
            18641
         }
      );
      this.questItemIds = new int[]{13052, 13053, 13054, 13056, 13057, 13032, 13033};
   }

   private static final void dropItem(Npc mob, Player player) {
      Collections.shuffle(DROPLIST);

      for(_144_PailakaInjuredDragon.PailakaDrop pd : DROPLIST) {
         if (getRandom(100) < pd.getChance()) {
            ((MonsterInstance)mob).dropItem(player, pd.getItemID(), (long)getRandom(1, 6));
            return;
         }
      }
   }

   private static void giveBuff(Npc npc, Player player, int skillId, int level) {
      npc.setTarget(player);
      npc.doCast(SkillsParser.getInstance().getInfo(skillId, level));
      --buff_counter;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("32499-02.htm")) {
            if (cond == 0) {
               st.startQuest();
            }
         } else if (event.equalsIgnoreCase("32499-05.htm")) {
            if (cond == 1) {
               st.setCond(2, true);
            }
         } else if (event.equalsIgnoreCase("32502-05.htm")) {
            if (cond == 2) {
               st.setCond(3, true);
               if (!st.hasQuestItems(13052)) {
                  st.giveItems(13052, 1L);
               }
            }
         } else if (event.equalsIgnoreCase("32509-02.htm")) {
            switch(cond) {
               case 2:
               case 3:
                  return "32509-07.htm";
               case 4:
                  st.setCond(5, true);
                  st.takeItems(13052, 1L);
                  st.takeItems(13056, 1L);
                  st.giveItems(13053, 1L);
                  return "32509-02.htm";
               case 5:
                  return "32509-01.htm";
               case 6:
                  st.setCond(7, true);
                  st.takeItems(13053, 1L);
                  st.takeItems(13057, 1L);
                  st.giveItems(13054, 1L);
                  return "32509-03.htm";
               case 7:
                  return "32509-03.htm";
            }
         } else if (event.equalsIgnoreCase("32509-06.htm")) {
            if (buff_counter < 1) {
               return "32509-05.htm";
            }
         } else if (event.equalsIgnoreCase("32512-02.htm")) {
            st.exitQuest(false, true);
            Reflection inst = ReflectionManager.getInstance().getReflection(npc.getReflectionId());
            if (inst != null) {
               inst.setDuration(300000);
               inst.setEmptyDestroyTime(0L);
            }

            player.setVitalityPoints(20000, true);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
         } else {
            if (event.startsWith("buff")) {
               if (buff_counter > 0) {
                  int nr = Integer.parseInt(event.split("buff")[1]);
                  giveBuff(npc, player, BUFFS[nr - 1][0], BUFFS[nr - 1][1]);
                  return "32509-06.htm";
               }

               return "32509-05.htm";
            }

            if (event.equalsIgnoreCase("latana_animation")) {
               _hasDoneAnimation = true;
               npc.abortAttack();
               npc.abortCast();
               npc.getAI().setIntention(CtrlIntention.IDLE);
               player.abortAttack();
               player.abortCast();
               player.stopMove(null);
               player.setTarget(null);
               if (player.hasSummon()) {
                  player.getSummon().abortAttack();
                  player.getSummon().abortCast();
                  player.getSummon().stopMove(null);
                  player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
               }

               player.sendPacket(new SpecialCamera(npc, 600, 0, 0, 1000, 11000, 1, 0, 1, 0, 0));
               this.startQuestTimer("latana_animation2", 1000L, npc, player);
               return null;
            }

            if (event.equalsIgnoreCase("latana_animation2")) {
               npc.doCast(SkillsParser.getInstance().getInfo(5759, 1));
               npc.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, Integer.valueOf(0));
               return null;
            }
         }

         return event;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      return npc.getId() + ".htm";
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         int cond = st.getCond();
         switch(npc.getId()) {
            case 32499:
               switch(st.getState()) {
                  case 0:
                     if (player.getLevel() < 73) {
                        return "32499-no.htm";
                     } else {
                        if (player.getLevel() > 77) {
                           return "32499-no.htm";
                        }

                        return "32499-01.htm";
                     }
                  case 1:
                     if (player.getLevel() < 73) {
                        return "32499-no.htm";
                     } else if (player.getLevel() > 77) {
                        return "32499-no.htm";
                     } else if (cond > 1) {
                        return "32499-06.htm";
                     }
                  case 2:
                     return "32499-completed.htm";
                  default:
                     return "32499-no.htm";
               }
            case 32502:
               if (cond > 2) {
                  return "32502-05.htm";
               }

               return "32502-01.htm";
            case 32509:
               return "32509-00.htm";
            case 32512:
               if (st.getState() == 2) {
                  return "32512-03.htm";
               }

               return "32512-01.htm";
            default:
               return getNoQuestMsg(player);
         }
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.getState() == 1) {
         int cond = st.getCond();
         switch(npc.getId()) {
            case 18635:
            case 18636:
               if (cond == 3 && st.hasQuestItems(13052) && !st.hasQuestItems(13056) && getRandom(100) < 20) {
                  st.setCond(4, true);
                  st.giveItems(13056, 1L);
               }

               this.spawnMageBehind(npc, player, 18644);
               this.checkIfLastInWall(npc);
               break;
            case 18637:
            case 18643:
            case 18647:
            case 18651:
               dropItem(npc, player);
            case 18638:
            case 18639:
            case 18640:
            case 18641:
            case 18644:
            case 18645:
            case 18648:
            case 18652:
            case 18656:
            case 18658:
            default:
               break;
            case 18642:
               if (cond == 3 && st.hasQuestItems(13052) && !st.hasQuestItems(13056) && getRandom(100) < 25) {
                  st.setCond(4, true);
                  st.giveItems(13056, 1L);
               }

               this.spawnMageBehind(npc, player, 18641);
               this.checkIfLastInWall(npc);
               break;
            case 18646:
            case 18654:
               if (cond == 3 && st.hasQuestItems(13052) && !st.hasQuestItems(13056) && getRandom(100) < 40) {
                  st.setCond(4, true);
                  st.giveItems(13056, 1L);
               }

               this.spawnMageBehind(npc, player, 18648);
               this.checkIfLastInWall(npc);
               break;
            case 18649:
            case 18650:
               if (cond == 5 && st.hasQuestItems(13053) && !st.hasQuestItems(13057) && getRandom(100) < 20) {
                  st.setCond(6, true);
                  st.giveItems(13057, 1L);
               }

               this.spawnMageBehind(npc, player, 18645);
               this.checkIfLastInWall(npc);
               break;
            case 18653:
               if (cond == 3 && st.hasQuestItems(13052) && !st.hasQuestItems(13056) && getRandom(100) < 30) {
                  st.setCond(4, true);
                  st.giveItems(13056, 1L);
               }

               this.spawnMageBehind(npc, player, 18640);
               this.checkIfLastInWall(npc);
               break;
            case 18655:
               if (cond == 5 && st.hasQuestItems(13053) && !st.hasQuestItems(13057) && getRandom(100) < 30) {
                  st.setCond(6, true);
                  st.giveItems(13057, 1L);
               }

               this.spawnMageBehind(npc, player, 18656);
               this.checkIfLastInWall(npc);
               break;
            case 18657:
               if (cond == 5 && st.hasQuestItems(13053) && !st.hasQuestItems(13057) && getRandom(100) < 40) {
                  st.setCond(6, true);
                  st.giveItems(13057, 1L);
               }

               this.spawnMageBehind(npc, player, 18652);
               this.checkIfLastInWall(npc);
               break;
            case 18659:
               if (cond == 5 && st.hasQuestItems(13053) && !st.hasQuestItems(13057) && getRandom(100) < 25) {
                  st.setCond(6, true);
                  st.giveItems(13057, 1L);
               }

               this.spawnMageBehind(npc, player, 18658);
               this.checkIfLastInWall(npc);
               break;
            case 18660:
               st.setCond(8, true);
               addSpawn(32512, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0L, false, npc.getReflectionId());
         }

         return super.onKill(npc, player, isSummon);
      } else {
         return null;
      }
   }

   private final void spawnMageBehind(Npc npc, Player player, int mageId) {
      double rads = Math.toRadians(Util.convertHeadingToDegree(npc.getSpawn().getHeading()) + 180.0);
      int mageX = (int)((double)npc.getX() + 150.0 * Math.cos(rads));
      int mageY = (int)((double)npc.getY() + 150.0 * Math.sin(rads));
      Npc mageBack = addSpawn(mageId, mageX, mageY, npc.getZ(), npc.getSpawn().getHeading(), false, 0L, true, npc.getReflectionId());
      mageBack.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, Integer.valueOf(1000));
   }

   private final void checkIfLastInWall(Npc npc) {
      Collection<Npc> knowns = World.getInstance().getAroundNpc(npc, 700, 200);

      for(Npc knownNpc : knowns) {
         if (!knownNpc.isDead()) {
            switch(npc.getId()) {
               case 18635:
               case 18636:
               case 18642:
                  switch(knownNpc.getId()) {
                     case 18635:
                     case 18636:
                     case 18642:
                        return;
                  }
               case 18637:
               case 18638:
               case 18639:
               case 18640:
               case 18641:
               case 18643:
               case 18644:
               case 18645:
               case 18647:
               case 18648:
               case 18651:
               case 18652:
               case 18656:
               case 18658:
               default:
                  break;
               case 18646:
               case 18653:
               case 18654:
                  switch(knownNpc.getId()) {
                     case 18646:
                     case 18653:
                     case 18654:
                        return;
                     default:
                        continue;
                  }
               case 18649:
               case 18650:
               case 18659:
                  switch(knownNpc.getId()) {
                     case 18649:
                     case 18650:
                     case 18659:
                        return;
                     default:
                        continue;
                  }
               case 18655:
               case 18657:
                  switch(knownNpc.getId()) {
                     case 18655:
                     case 18657:
                        return;
                  }
            }
         }
      }

      for(Creature npcs : knowns) {
         if (npcs instanceof Npc && !npcs.isDead()) {
            Npc knownNpc = (Npc)npcs;
            switch(npc.getId()) {
               case 18635:
               case 18636:
               case 18642:
                  switch(knownNpc.getId()) {
                     case 18641:
                     case 18644:
                        knownNpc.abortCast();
                        knownNpc.deleteMe();
                  }
               case 18637:
               case 18638:
               case 18639:
               case 18640:
               case 18641:
               case 18643:
               case 18644:
               case 18645:
               case 18647:
               case 18648:
               case 18651:
               case 18652:
               case 18656:
               case 18658:
               default:
                  break;
               case 18646:
               case 18653:
               case 18654:
                  switch(knownNpc.getId()) {
                     case 18640:
                     case 18648:
                        knownNpc.abortCast();
                        knownNpc.deleteMe();
                     default:
                        continue;
                  }
               case 18649:
               case 18650:
               case 18659:
                  switch(knownNpc.getId()) {
                     case 18645:
                     case 18658:
                        knownNpc.abortCast();
                        knownNpc.deleteMe();
                     default:
                        continue;
                  }
               case 18655:
               case 18657:
                  switch(knownNpc.getId()) {
                     case 18652:
                     case 18656:
                        knownNpc.abortCast();
                        knownNpc.deleteMe();
                  }
            }
         }
      }
   }

   @Override
   public final String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null || st.getState() != 1) {
         return null;
      } else if (isSummon) {
         return null;
      } else {
         switch(npc.getId()) {
            case 18660:
               if (!_hasDoneAnimation) {
                  this.startQuestTimer("latana_animation", 600L, npc, player);
                  return null;
               }
            default:
               return super.onAggroRangeEnter(npc, player, isSummon);
         }
      }
   }

   @Override
   public final String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (attacker == null) {
         return super.onAttack(npc, attacker, damage, isSummon);
      } else {
         switch(npc.getId()) {
            case 18660:
               if (!_hasDoneAnimation) {
                  QuestState st = attacker.getQuestState(this.getName());
                  if (st != null && st.getState() == 1) {
                     this.startQuestTimer("latana_animation", 600L, npc, attacker);
                     return null;
                  }

                  return super.onAttack(npc, attacker, damage, isSummon);
               }
            default:
               return super.onAttack(npc, attacker, damage, isSummon);
         }
      }
   }

   public static void main(String[] args) {
      new _144_PailakaInjuredDragon(144, _144_PailakaInjuredDragon.class.getSimpleName(), "");
   }

   static {
      DROPLIST.add(new _144_PailakaInjuredDragon.PailakaDrop(13033, 80));
      DROPLIST.add(new _144_PailakaInjuredDragon.PailakaDrop(13032, 30));
   }

   private static class PailakaDrop {
      private final int _itemId;
      private final int _chance;

      public PailakaDrop(int itemId, int chance) {
         this._itemId = itemId;
         this._chance = chance;
      }

      public int getItemID() {
         return this._itemId;
      }

      public int getChance() {
         return this._chance;
      }
   }
}

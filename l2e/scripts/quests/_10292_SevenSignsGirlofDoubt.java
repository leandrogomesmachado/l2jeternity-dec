package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10292_SevenSignsGirlofDoubt extends Quest {
   private final Map<Integer, _10292_SevenSignsGirlofDoubt.InstanceHolder> ReflectionWorlds = new HashMap<>();

   public _10292_SevenSignsGirlofDoubt(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32593);
      this.addTalkId(new int[]{32593, 32597, 30832, 32784, 32862, 32617});
      this.addKillId(new int[]{27422, 22801, 22802, 22804, 22805});
      this.questItemIds = new int[]{17226};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int instanceId = npc.getReflectionId();
         _10292_SevenSignsGirlofDoubt.InstanceHolder holder = this.ReflectionWorlds.get(instanceId);
         if (holder == null) {
            holder = new _10292_SevenSignsGirlofDoubt.InstanceHolder();
            this.ReflectionWorlds.put(instanceId, holder);
         }

         if (event.equalsIgnoreCase("evil_despawn")) {
            holder.spawned = false;

            for(Npc h : holder.mobs) {
               if (h != null) {
                  h.deleteMe();
               }
            }

            holder.mobs.clear();
            this.ReflectionWorlds.remove(instanceId);
            return null;
         } else {
            if (npc.getId() == 32593) {
               if (event.equalsIgnoreCase("32593-05.htm")) {
                  st.startQuest();
               }
            } else if (npc.getId() == 32597) {
               if (event.equalsIgnoreCase("32597-08.htm")) {
                  st.setCond(2, true);
               }
            } else if (npc.getId() == 30832) {
               if (event.equalsIgnoreCase("30832-02.htm")) {
                  st.setCond(8, true);
               }
            } else if (npc.getId() == 32784) {
               if (event.equalsIgnoreCase("32784-03.htm")) {
                  st.setCond(3, true);
               } else if (event.equalsIgnoreCase("32784-14.htm")) {
                  st.setCond(7, true);
               } else if (event.equalsIgnoreCase("spawn")) {
                  if (!holder.spawned) {
                     st.takeItems(17226, -1L);
                     holder.spawned = true;
                     Npc evil = addSpawn(27422, 89440, -238016, -9632, 335, false, 0L, false, player.getReflectionId());
                     evil.setIsNoRndWalk(true);
                     holder.mobs.add(evil);
                     Npc evil1 = addSpawn(27424, 89524, -238131, -9632, 56, false, 0L, false, player.getReflectionId());
                     evil1.setIsNoRndWalk(true);
                     holder.mobs.add(evil1);
                     this.startQuestTimer("evil_despawn", 60000L, evil, player);
                     return null;
                  }

                  htmltext = "32593-02.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32593) {
            if (st.getState() == 2) {
               htmltext = "32593-02.htm";
            } else if (player.getLevel() < 81) {
               htmltext = "32593-03.htm";
            } else if (player.getQuestState(_198_SevenSignEmbryo.class.getSimpleName()) == null
               || player.getQuestState(_198_SevenSignEmbryo.class.getSimpleName()).getState() != 2) {
               htmltext = "32593-03.htm";
            } else if (st.getState() == 0) {
               htmltext = "32593-01.htm";
            } else if (st.getCond() >= 1) {
               htmltext = "32593-07.htm";
            }
         } else if (npc.getId() == 32597) {
            if (st.isCond(1)) {
               htmltext = "32597-01.htm";
            } else if (st.isCond(2)) {
               htmltext = "32597-03.htm";
            }
         } else if (npc.getId() == 32784) {
            if (st.isCond(2)) {
               htmltext = "32784-01.htm";
            } else if (st.isCond(3)) {
               htmltext = "32784-04.htm";
            } else if (st.isCond(4)) {
               st.setCond(5, true);
               htmltext = "32784-05.htm";
            } else if (st.isCond(5)) {
               st.playSound("ItemSound.quest_middle");
               htmltext = "32784-05.htm";
            } else if (st.isCond(6)) {
               st.playSound("ItemSound.quest_middle");
               htmltext = "32784-11.htm";
            } else if (st.isCond(8)) {
               if (player.isSubClassActive()) {
                  htmltext = "32784-18.htm";
               } else {
                  st.calcExpAndSp(this.getId());
                  st.exitQuest(false, true);
                  htmltext = "32784-16.htm";
               }
            }
         } else if (npc.getId() == 30832) {
            if (st.isCond(7)) {
               htmltext = "30832-01.htm";
            } else if (st.isCond(8)) {
               htmltext = "30832-04.htm";
            }
         } else if (npc.getId() == 32617 && st.getState() == 1 && st.getCond() >= 1) {
            htmltext = "32617-01.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if ((npc.getId() == 22801 || npc.getId() == 22802 || npc.getId() == 22804 || npc.getId() == 22805)
            && st.isCond(3)
            && st.calcDropItems(this.getId(), 17226, npc.getId(), 10)) {
            st.setCond(4);
         }

         if (st.isCond(5) && npc.getId() == 27422) {
            int instanceid = npc.getReflectionId();
            _10292_SevenSignsGirlofDoubt.InstanceHolder holder = this.ReflectionWorlds.get(instanceid);
            if (holder == null) {
               return null;
            }

            for(Npc h : holder.mobs) {
               if (h != null) {
                  h.deleteMe();
               }
            }

            holder.spawned = false;
            holder.mobs.clear();
            this.ReflectionWorlds.remove(instanceid);
            st.setCond(6, true);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _10292_SevenSignsGirlofDoubt(10292, _10292_SevenSignsGirlofDoubt.class.getSimpleName(), "");
   }

   private static class InstanceHolder {
      List<Npc> mobs = new ArrayList<>();
      boolean spawned = false;

      private InstanceHolder() {
      }
   }
}

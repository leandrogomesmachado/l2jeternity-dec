package l2e.scripts.custom;

import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class DestroyedTumors extends Quest {
   private long warpTimer = 0L;

   public DestroyedTumors(int id, String name, String desc) {
      super(id, name, desc);
      this.addStartNpc(32535);
      this.addFirstTalkId(32535);
      this.addTalkId(32535);
      this.addStartNpc(32536);
      this.addFirstTalkId(32536);
      this.addTalkId(32536);
      this.warpTimer = System.currentTimeMillis();
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
      if (world != null && world.getTemplateId() == 119) {
         if (event.equalsIgnoreCase("examine_tumor")) {
            if (player.getParty() != null && player.getParty().getLeader() == player) {
               htmltext = "32535-1.htm";
            } else {
               htmltext = "32535-2.htm";
            }
         } else if (event.equalsIgnoreCase("showcheckpage")) {
            if (player.getInventory().getItemByItemId(13797) == null) {
               htmltext = "32535-6.htm";
            } else if (this.warpTimer + 60000L > System.currentTimeMillis()) {
               htmltext = "32535-4.htm";
            } else if (world.getTag() <= 0) {
               htmltext = "32535-3.htm";
            } else {
               htmltext = "32535-5a.htm";
            }
         }
      } else if (world != null && world.getTemplateId() == 120) {
         if (event.equalsIgnoreCase("examine_tumor")) {
            if (player.getParty() != null && player.getParty().getLeader() == player) {
               htmltext = "32535-1.htm";
            } else {
               htmltext = "32535-2.htm";
            }
         } else if (event.equalsIgnoreCase("showcheckpage")) {
            if (player.getInventory().getItemByItemId(13797) == null) {
               htmltext = "32535-6.htm";
            } else if (this.warpTimer + 60000L > System.currentTimeMillis()) {
               htmltext = "32535-4.htm";
            } else if (world.getTag() <= 0) {
               htmltext = "32535-3.htm";
            } else {
               htmltext = "32535-5b.htm";
            }
         }
      } else if (world != null && world.getTemplateId() == 121) {
         if (event.equalsIgnoreCase("examine_tumor")) {
            if (npc.getId() == 32536) {
               if (player.getParty() != null && player.getParty().getLeader() == player) {
                  htmltext = "32536-1.htm";
               } else {
                  htmltext = "32536-2.htm";
               }
            }

            if (npc.getId() == 32535) {
               if (player.getParty() != null && player.getParty().getLeader() == player) {
                  htmltext = "32535-7.htm";
               } else {
                  htmltext = "32535-2.htm";
               }
            }
         } else if (event.equalsIgnoreCase("showcheckpage")) {
            if (player.getInventory().getItemByItemId(13797) == null) {
               htmltext = "32535-6.htm";
            } else if (this.warpTimer + 60000L > System.currentTimeMillis()) {
               htmltext = "32535-4.htm";
            } else if (world.getTag() <= 0) {
               htmltext = "32535-3.htm";
            } else {
               htmltext = "32535-5.htm";
            }
         } else if (event.equalsIgnoreCase("reenter")) {
            if (player.getInventory().getItemByItemId(13797) != null && player.getInventory().getItemByItemId(13797).getCount() >= 3L) {
               htmltext = "32535-8.htm";
            } else {
               htmltext = "32535-6.htm";
            }
         }
      } else if (world != null && world.getTemplateId() == 122 && event.equalsIgnoreCase("examine_tumor") && npc.getId() == 32535) {
         htmltext = "32535-4.htm";
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.getId() == 32535) {
         return "32535.htm";
      } else {
         return npc.getId() == 32536 ? "32536.htm" : "";
      }
   }

   public static void main(String[] args) {
      new DestroyedTumors(-1, DestroyedTumors.class.getSimpleName(), "custom");
   }
}

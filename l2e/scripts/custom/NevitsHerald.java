package l2e.scripts.custom;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.NpcSay;

public class NevitsHerald extends Quest {
   private static final List<Npc> spawns = new ArrayList<>();
   private static boolean isActive = false;
   private static final int NevitsHerald = 4326;
   private static final int[] Antharas = new int[]{29019, 29066, 29067, 29068};
   private static final int Valakas = 29028;
   private static final NpcStringId[] spam = new NpcStringId[]{
      NpcStringId.SHOW_RESPECT_TO_THE_HEROES_WHO_DEFEATED_THE_EVIL_DRAGON_AND_PROTECTED_THIS_ADEN_WORLD,
      NpcStringId.SHOUT_TO_CELEBRATE_THE_VICTORY_OF_THE_HEROES,
      NpcStringId.PRAISE_THE_ACHIEVEMENT_OF_THE_HEROES_AND_RECEIVE_NEVITS_BLESSING
   };
   private static final int[][] _spawns = new int[][]{{44168, -48513, -801, 31924}, {147953, 26656, -2205, 20352}, {81918, 148305, -3471, 49151}};

   public NevitsHerald(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(4326);
      this.addStartNpc(4326);
      this.addTalkId(4326);

      for(int _npc : Antharas) {
         this.addKillId(_npc);
      }

      this.addKillId(29028);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      return "4326.htm";
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      if (npc.getId() == 4326) {
         if (event.equalsIgnoreCase("buff")) {
            if (player.getFirstEffect(23312) == null) {
               npc.setTarget(player);
               npc.doCast(SkillsParser.getInstance().getInfo(23312, 1));
               return null;
            }

            htmltext = "4326-1.htm";
         }
      } else {
         if (event.equalsIgnoreCase("text_spam")) {
            this.cancelQuestTimer("text_spam", npc, player);
            npc.broadcastPacket(new NpcSay(4326, 1, 4326, spam[getRandom(0, spam.length - 1)]));
            this.startQuestTimer("text_spam", 60000L, npc, player);
            return null;
         }

         if (event.equalsIgnoreCase("despawn")) {
            this.despawnHeralds();
         }
      }

      return htmltext;
   }

   private void despawnHeralds() {
      if (!spawns.isEmpty()) {
         for(Npc npc : spawns) {
            npc.deleteMe();
         }
      }

      spawns.clear();
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      ExShowScreenMessage message = null;
      if (npc.getId() == 29028) {
         message = new ExShowScreenMessage(NpcStringId.THE_EVIL_FIRE_DRAGON_VALAKAS_HAS_BEEN_DEFEATED, 2, 10000);
      } else {
         message = new ExShowScreenMessage(NpcStringId.THE_EVIL_LAND_DRAGON_ANTHARAS_HAS_BEEN_DEFEATED, 2, 10000);
      }

      message.setUpperEffect(true);

      for(Player onlinePlayer : World.getInstance().getAllPlayers()) {
         if (onlinePlayer != null) {
            onlinePlayer.sendPacket(message);
         }
      }

      if (!isActive) {
         isActive = true;
         spawns.clear();

         for(int[] _spawn : _spawns) {
            Npc herald = addSpawn(4326, _spawn[0], _spawn[1], _spawn[2], _spawn[3], false, 0L);
            if (herald != null) {
               spawns.add(herald);
            }
         }

         this.startQuestTimer("despawn", 14400000L, npc, killer);
         this.startQuestTimer("text_spam", 3000L, npc, killer);
      }

      return null;
   }

   public static void main(String[] args) {
      new NevitsHerald(-1, "NevitsHerald", "custom");
   }
}

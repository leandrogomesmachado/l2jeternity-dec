package l2e.scripts.ai;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class GeneralDilios extends AbstractNpcAI {
   private static final int GENERAL_ID = 32549;
   private static final int GUARD_ID = 32619;
   private Npc _general;
   private final List<Npc> _guards = new ArrayList<>();
   private static final NpcStringId[] diliosText = new NpcStringId[]{
      NpcStringId.MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_WERE_GATHERING_BRAVE_ADVENTURERS_TO_ATTACK_TIATS_MOUNTED_TROOP_THATS_ROOTED_IN_THE_SEED_OF_DESTRUCTION,
      NpcStringId.MESSENGER_INFORM_THE_BROTHERS_IN_KUCEREUS_CLAN_OUTPOST_BRAVE_ADVENTURERS_WHO_HAVE_CHALLENGED_THE_SEED_OF_INFINITY_ARE_CURRENTLY_INFILTRATING_THE_HALL_OF_EROSION_THROUGH_THE_DEFENSIVELY_WEAK_HALL_OF_SUFFERING,
      NpcStringId.MESSENGER_INFORM_THE_PATRONS_OF_THE_KEUCEREUS_ALLIANCE_BASE_THE_SEED_OF_INFINITY_IS_CURRENTLY_SECURED_UNDER_THE_FLAG_OF_THE_KEUCEREUS_ALLIANCE
   };

   private GeneralDilios(String name, String descr) {
      super(name, descr);
      this.findNpcs();
      if (this._general != null && !this._guards.isEmpty()) {
         this.startQuestTimer("command_0", 60000L, null, null);
      } else {
         throw new NullPointerException("Cannot find npcs!");
      }
   }

   private void findNpcs() {
      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn != null) {
            if (spawn.getId() == 32549) {
               this._general = spawn.getLastSpawn();
            } else if (spawn.getId() == 32619) {
               this._guards.add(spawn.getLastSpawn());
            }
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.startsWith("command_")) {
         int value = Integer.parseInt(event.substring(8));
         if (value < 6) {
            this._general.broadcastPacket(new NpcSay(this._general.getObjectId(), 22, 32549, NpcStringId.STABBING_THREE_TIMES), 2000);
            this.startQuestTimer("guard_animation_0", 3400L, null, null);
         } else {
            value = -1;
            this._general.broadcastPacket(new NpcSay(this._general.getObjectId(), 23, 32549, diliosText[getRandom(diliosText.length)]));
         }

         this.startQuestTimer("command_" + (value + 1), 60000L, null, null);
      } else if (event.startsWith("guard_animation_")) {
         int value = Integer.parseInt(event.substring(16));

         for(Npc guard : this._guards) {
            guard.broadcastSocialAction(4);
         }

         if (value < 2) {
            this.startQuestTimer("guard_animation_" + (value + 1), 1500L, null, null);
         }
      }

      return super.onAdvEvent(event, npc, player);
   }

   public static void main(String[] args) {
      new GeneralDilios(GeneralDilios.class.getSimpleName(), "ai");
   }
}

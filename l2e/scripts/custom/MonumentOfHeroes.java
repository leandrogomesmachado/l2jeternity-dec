package l2e.scripts.custom;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.scripts.ai.AbstractNpcAI;

public class MonumentOfHeroes extends AbstractNpcAI {
   private static final int[] MONUMENTS = new int[]{31690, 31769, 31770, 31771, 31772};
   private static final int WINGS_OF_DESTINY_CIRCLET = 6842;
   private static final int[] WEAPONS = new int[]{6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621, 9388, 9389, 9390};

   private MonumentOfHeroes(String name, String descr) {
      super(name, descr);
      this.addStartNpc(MONUMENTS);
      this.addTalkId(MONUMENTS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      switch(event) {
         case "HeroWeapon":
            if (player.isTimeHero()) {
               return "no_hero_weapon.htm";
            }

            if (player.isHero()) {
               return this.hasAtLeastOneQuestItem(player, WEAPONS) ? "already_have_weapon.htm" : "weapon_list.htm";
            }

            return "no_hero_weapon.htm";
         case "HeroCirclet":
            if (player.isTimeHero()) {
               return "no_hero_circlet.htm";
            }

            if (!player.isHero()) {
               return "no_hero_circlet.htm";
            }

            if (hasQuestItems(player, 6842)) {
               return "already_have_circlet.htm";
            }

            giveItems(player, 6842, 1L);
            break;
         default:
            int weaponId = Integer.parseInt(event);
            if (Util.contains(WEAPONS, weaponId)) {
               giveItems(player, weaponId, 1L);
            }
      }

      return super.onAdvEvent(event, npc, player);
   }

   public static void main(String[] args) {
      new MonumentOfHeroes(MonumentOfHeroes.class.getSimpleName(), "custom");
   }
}

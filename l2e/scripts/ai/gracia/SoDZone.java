package l2e.scripts.ai.gracia;

import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneType;
import l2e.scripts.ai.AbstractNpcAI;

public class SoDZone extends AbstractNpcAI {
   private SoDZone(String name, String descr) {
      super(name, descr);
      this.addEnterZoneId(new int[]{60009});
   }

   @Override
   public String onEnterZone(Creature character, ZoneType zone) {
      if (character.getReflectionId() != 0) {
         return super.onEnterZone(character, zone);
      } else {
         if (character.isPlayer() && zone.getId() == 60009 && !SoDManager.isOpened()) {
            character.teleToLocation(-248717, 250260, 4337, true);
         }

         return super.onEnterZone(character, zone);
      }
   }

   public static void main(String[] args) {
      new SoDZone(SoDZone.class.getSimpleName(), "ai");
   }
}

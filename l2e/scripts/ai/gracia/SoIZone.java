package l2e.scripts.ai.gracia;

import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneType;
import l2e.scripts.ai.AbstractNpcAI;

public class SoIZone extends AbstractNpcAI {
   private SoIZone(String name, String descr) {
      super(name, descr);
      this.addEnterZoneId(new int[]{200033});
   }

   @Override
   public String onEnterZone(Creature character, ZoneType zone) {
      if (character.getReflectionId() != 0) {
         return super.onEnterZone(character, zone);
      } else {
         if (character.isPlayer() && zone.getId() == 200033 && SoIManager.getCurrentStage() != 3 && !SoIManager.isSeedOpen()) {
            character.teleToLocation(-183285, 205996, -12896, true);
         }

         return super.onEnterZone(character, zone);
      }
   }

   public static void main(String[] args) {
      new SoIZone(SoIZone.class.getSimpleName(), "ai");
   }
}

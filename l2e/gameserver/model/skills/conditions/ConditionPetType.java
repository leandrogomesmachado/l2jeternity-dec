package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionPetType extends Condition {
   private final int petType;

   public ConditionPetType(int petType) {
      this.petType = petType;
   }

   @Override
   public boolean testImpl(Env env) {
      if (!(env.getCharacter() instanceof PetInstance)) {
         return false;
      } else {
         int npcid = ((Summon)env.getCharacter()).getId();
         if (PetsParser.isStrider(npcid) && this.petType == 1) {
            return true;
         } else if (PetsParser.isGrowUpWolfGroup(npcid) && this.petType == 2) {
            return true;
         } else if (PetsParser.isHatchlingGroup(npcid) && this.petType == 4) {
            return true;
         } else if (PetsParser.isAllWolfGroup(npcid) && this.petType == 8) {
            return true;
         } else if (PetsParser.isBabyPetGroup(npcid) && this.petType == 22) {
            return true;
         } else if (PetsParser.isUpgradeBabyPetGroup(npcid) && this.petType == 50) {
            return true;
         } else {
            return PetsParser.isItemEquipPetGroup(npcid) && this.petType == 100;
         }
      }
   }
}

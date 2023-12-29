package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.serverpackets.InventoryUpdate;

public class FestivalMonsterInstance extends MonsterInstance {
   protected int _bonusMultiplier = 1;

   public FestivalMonsterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.FestivalMonsterInstance);
   }

   public void setOfferingBonus(int bonusMultiplier) {
      this._bonusMultiplier = bonusMultiplier;
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return !(attacker instanceof FestivalMonsterInstance);
   }

   @Override
   public boolean isAggressive() {
      return true;
   }

   @Override
   public boolean hasRandomAnimation() {
      return false;
   }

   @Override
   public void doItemDrop(Creature lastAttacker, Creature mainDamageDealer) {
      Player killingChar = null;
      if (lastAttacker.isPlayer()) {
         killingChar = (Player)lastAttacker;
         Party associatedParty = killingChar.getParty();
         if (associatedParty != null) {
            Player partyLeader = associatedParty.getLeader();
            ItemInstance addedOfferings = partyLeader.getInventory().addItem("Sign", 5901, (long)this._bonusMultiplier, partyLeader, this);
            InventoryUpdate iu = new InventoryUpdate();
            if (addedOfferings.getCount() != (long)this._bonusMultiplier) {
               iu.addModifiedItem(addedOfferings);
            } else {
               iu.addNewItem(addedOfferings);
            }

            partyLeader.sendPacket(iu);
            super.doItemDrop(lastAttacker, mainDamageDealer);
         }
      }
   }
}

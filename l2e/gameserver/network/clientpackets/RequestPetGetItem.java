package l2e.gameserver.network.clientpackets;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public final class RequestPetGetItem extends GameClientPacket {
   private int _objectId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
   }

   @Override
   protected void runImpl() {
      World world = World.getInstance();
      ItemInstance item = (ItemInstance)world.findObject(this._objectId);
      if (item != null && this.getActiveChar() != null && this.getActiveChar().hasPet()) {
         int castleId = MercTicketManager.getInstance().getTicketCastleId(item.getId());
         if (castleId > 0) {
            this.sendActionFailed();
         } else if (FortSiegeManager.getInstance().isCombat(item.getId())) {
            this.sendActionFailed();
         } else {
            PetInstance pet = (PetInstance)this.getClient().getActiveChar().getSummon();
            if (pet.isDead() || pet.isOutOfControl() || pet.isActionsDisabled()) {
               this.sendActionFailed();
            } else if (pet.isUncontrollable()) {
               this.sendPacket(SystemMessageId.WHEN_YOUR_PET_S_HUNGER_GAUGE_IS_AT_0_YOU_CANNOT_USE_YOUR_PET);
            } else {
               pet.getAI().setIntention(CtrlIntention.PICK_UP, item);
            }
         }
      } else {
         this.sendActionFailed();
      }
   }
}

package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.PetNameHolder;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.network.SystemMessageId;

public final class RequestChangePetName extends GameClientPacket {
   private String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Summon pet = activeChar.getSummon();
         if (pet != null) {
            if (!pet.isPet()) {
               activeChar.sendPacket(SystemMessageId.DONT_HAVE_PET);
            } else if (pet.getName() != null) {
               activeChar.sendPacket(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET);
            } else if (PetNameHolder.getInstance().doesPetNameExist(this._name, pet.getTemplate().getId())) {
               activeChar.sendPacket(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET);
            } else if (this._name.length() < 3 || this._name.length() > 16) {
               activeChar.sendMessage("Your pet's name can be up to 16 characters in length.");
            } else if (!PetNameHolder.getInstance().isValidPetName(this._name)) {
               activeChar.sendPacket(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS);
            } else {
               pet.setName(this._name);
               pet.setNameRu(this._name);
               pet.updateAndBroadcastStatus(1);
            }
         }
      }
   }
}

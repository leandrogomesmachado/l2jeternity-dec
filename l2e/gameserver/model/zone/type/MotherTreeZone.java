package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class MotherTreeZone extends ZoneType {
   private int _enterMsg;
   private int _leaveMsg;
   private int _mpRegen;
   private int _hpRegen;

   public MotherTreeZone(int id) {
      super(id);
      this.addZoneId(ZoneId.MOTHER_TREE);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("enterMsgId")) {
         this._enterMsg = Integer.parseInt(value);
      } else if (name.equals("leaveMsgId")) {
         this._leaveMsg = Integer.parseInt(value);
      } else if (name.equals("MpRegenBonus")) {
         this._mpRegen = Integer.parseInt(value);
      } else if (name.equals("HpRegenBonus")) {
         this._hpRegen = Integer.parseInt(value);
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (character.isPlayer()) {
         Player player = character.getActingPlayer();
         if (this._enterMsg != 0) {
            player.sendPacket(SystemMessage.getSystemMessage(this._enterMsg));
         }
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (character.isPlayer()) {
         Player player = character.getActingPlayer();
         if (this._leaveMsg != 0) {
            player.sendPacket(SystemMessage.getSystemMessage(this._leaveMsg));
         }
      }
   }

   public int getMpRegenBonus() {
      return this._mpRegen;
   }

   public int getHpRegenBonus() {
      return this._hpRegen;
   }
}

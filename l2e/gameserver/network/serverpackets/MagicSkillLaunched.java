package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;

public class MagicSkillLaunched extends GameServerPacket {
   private final int _charObjId;
   private final int _skillId;
   private final int _skillLevel;
   private int _numberOfTargets;
   private GameObject[] _targets;
   private final int _singleTargetId;

   public MagicSkillLaunched(Creature cha, int skillId, int skillLevel, GameObject[] targets) {
      this._charObjId = cha.getObjectId();
      this._skillId = skillId;
      this._skillLevel = skillLevel;
      if (targets != null) {
         this._numberOfTargets = targets.length;
         this._targets = targets;
      } else {
         this._numberOfTargets = 1;
         GameObject[] objs = new GameObject[]{cha};
         this._targets = objs;
      }

      this._singleTargetId = 0;
   }

   public MagicSkillLaunched(Creature cha, int skillId, int skillLevel) {
      this._charObjId = cha.getObjectId();
      this._skillId = skillId;
      this._skillLevel = skillLevel;
      this._numberOfTargets = 1;
      this._singleTargetId = cha.getTargetId();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._skillId);
      this.writeD(this._skillLevel);
      this.writeD(this._numberOfTargets);
      if (this._singleTargetId == 0 && this._numberOfTargets != 0) {
         for(GameObject target : this._targets) {
            this.writeD(target.getObjectId());
         }
      } else {
         this.writeD(this._singleTargetId);
      }
   }
}

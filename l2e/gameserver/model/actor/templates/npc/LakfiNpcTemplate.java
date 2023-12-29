package l2e.gameserver.model.actor.templates.npc;

import java.util.List;

public class LakfiNpcTemplate {
   private final int _npcId;
   private final List<LakfiRewardTemplate> _rewards;

   public LakfiNpcTemplate(int npcId, List<LakfiRewardTemplate> rewards) {
      this._npcId = npcId;
      this._rewards = rewards;
   }

   public int getId() {
      return this._npcId;
   }

   public List<LakfiRewardTemplate> getRewards() {
      return this._rewards;
   }
}

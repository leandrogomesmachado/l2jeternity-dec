package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public final class FlyNpcInstance extends NpcInstance {
   public FlyNpcInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.FlyNpcInstance);
      this.setIsFlying(true);
   }
}

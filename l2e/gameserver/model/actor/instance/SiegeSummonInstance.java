package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;

public class SiegeSummonInstance extends ServitorInstance {
   public static final int SIEGE_GOLEM_ID = 14737;
   public static final int HOG_CANNON_ID = 14768;
   public static final int SWOOP_CANNON_ID = 14839;

   public SiegeSummonInstance(int objectId, NpcTemplate template, Player owner, Skill skill) {
      super(objectId, template, owner, skill);
      this.setInstanceType(GameObject.InstanceType.SiegeSummonInstance);
   }
}

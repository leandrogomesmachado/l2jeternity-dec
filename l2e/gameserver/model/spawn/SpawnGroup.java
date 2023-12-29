package l2e.gameserver.model.spawn;

import java.util.logging.Level;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.ControllableMobInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class SpawnGroup extends Spawner {
   private final NpcTemplate _template;

   public SpawnGroup(NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException {
      super(mobTemplate);
      this._template = mobTemplate;
      this.setAmount(1);
   }

   public Npc doGroupSpawn() {
      try {
         if (!this._template.isType("Pet") && !this._template.isType("Minion")) {
            int newlocx = 0;
            int newlocy = 0;
            int newlocz = 0;
            if (this.getX() != 0 || this.getY() != 0) {
               newlocx = this.getX();
               newlocy = this.getY();
               newlocz = this.getZ();
               Npc mob = new ControllableMobInstance(IdFactory.getInstance().getNextId(), this._template);
               mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
               if (this.getHeading() == -1) {
                  mob.setHeading(Rnd.nextInt(61794));
               } else {
                  mob.setHeading(this.getHeading());
               }

               mob.setSpawn(this);
               mob.spawnMe(newlocx, newlocy, newlocz);
               mob.onSpawn();
               if (Config.DEBUG) {
                  _log.finest("Spawned Mob Id: " + this._template.getId() + " ,at: X: " + mob.getX() + " Y: " + mob.getY() + " Z: " + mob.getZ());
               }

               return mob;
            } else {
               return this.getLocationId() == 0 ? null : null;
            }
         } else {
            return null;
         }
      } catch (Exception var5) {
         _log.log(Level.WARNING, "NPC class not found: " + var5.getMessage(), (Throwable)var5);
         return null;
      }
   }
}

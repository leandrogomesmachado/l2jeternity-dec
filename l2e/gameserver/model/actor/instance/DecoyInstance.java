package l2e.gameserver.model.actor.instance;

import java.util.concurrent.Future;
import java.util.logging.Level;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Decoy;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.taskmanager.DecayTaskManager;

public class DecoyInstance extends Decoy {
   private int _totalLifeTime;
   private int _timeRemaining;
   private Future<?> _DecoyLifeTask;
   private Future<?> _HateSpam;

   public DecoyInstance(int objectId, NpcTemplate template, Player owner, int totalLifeTime) {
      super(objectId, template, owner);
      this.setInstanceType(GameObject.InstanceType.DecoyInstance);
      this._totalLifeTime = totalLifeTime;
      this._timeRemaining = this._totalLifeTime;
      int skilllevel = this.getTemplate().getIdTemplate() - 13070;
      this._DecoyLifeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new DecoyInstance.DecoyLifetime(this.getOwner(), this), 1000L, 1000L);
      this._HateSpam = ThreadPoolManager.getInstance()
         .scheduleAtFixedRate(new DecoyInstance.HateSpam(this, SkillsParser.getInstance().getInfo(5272, skilllevel)), 2000L, 5000L);
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this._HateSpam != null) {
         this._HateSpam.cancel(true);
         this._HateSpam = null;
      }

      this._totalLifeTime = 0;
      DecayTaskManager.getInstance().add(this);
      super.onDeath(killer);
   }

   @Override
   public void unSummon(Player owner) {
      if (this._DecoyLifeTask != null) {
         this._DecoyLifeTask.cancel(true);
         this._DecoyLifeTask = null;
      }

      if (this._HateSpam != null) {
         this._HateSpam.cancel(true);
         this._HateSpam = null;
      }

      super.unSummon(owner);
   }

   public void decTimeRemaining(int value) {
      this._timeRemaining -= value;
   }

   public int getTimeRemaining() {
      return this._timeRemaining;
   }

   public int getTotalLifeTime() {
      return this._totalLifeTime;
   }

   @Override
   public double getColRadius() {
      Player player = this.getActingPlayer();
      if (player == null) {
         return 0.0;
      } else if (player.isTransformed()) {
         return player.getTransformation().getCollisionRadius(player);
      } else {
         return player.getAppearance().getSex() ? player.getBaseTemplate().getFCollisionRadiusFemale() : player.getBaseTemplate().getfCollisionRadius();
      }
   }

   @Override
   public double getColHeight() {
      Player player = this.getActingPlayer();
      if (player == null) {
         return 0.0;
      } else if (player.isTransformed()) {
         return player.getTransformation().getCollisionHeight(player);
      } else {
         return player.getAppearance().getSex() ? player.getBaseTemplate().getFCollisionHeightFemale() : player.getBaseTemplate().getfCollisionHeight();
      }
   }

   static class DecoyLifetime implements Runnable {
      private final Player _activeChar;
      private final DecoyInstance _Decoy;

      DecoyLifetime(Player activeChar, DecoyInstance Decoy) {
         this._activeChar = activeChar;
         this._Decoy = Decoy;
      }

      @Override
      public void run() {
         try {
            this._Decoy.decTimeRemaining(1000);
            double newTimeRemaining = (double)this._Decoy.getTimeRemaining();
            if (newTimeRemaining < 0.0) {
               this._Decoy.unSummon(this._activeChar);
            }
         } catch (Exception var3) {
            Creature._log.log(Level.SEVERE, "Decoy Error: ", (Throwable)var3);
         }
      }
   }

   private static class HateSpam implements Runnable {
      private final DecoyInstance _activeChar;
      private final Skill _skill;

      HateSpam(DecoyInstance activeChar, Skill Hate) {
         this._activeChar = activeChar;
         this._skill = Hate;
      }

      @Override
      public void run() {
         try {
            this._activeChar.setTarget(this._activeChar);
            this._activeChar.doCast(this._skill);
         } catch (Throwable var2) {
            Creature._log.log(Level.SEVERE, "Decoy Error: ", var2);
         }
      }
   }
}

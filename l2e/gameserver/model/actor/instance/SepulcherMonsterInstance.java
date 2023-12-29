package l2e.gameserver.model.actor.instance;

import java.util.concurrent.Future;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.FourSepulchersManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.NpcSay;

public class SepulcherMonsterInstance extends MonsterInstance {
   public int mysteriousBoxId = 0;
   protected Future<?> _victimSpawnKeyBoxTask = null;
   protected Future<?> _victimShout = null;
   protected Future<?> _changeImmortalTask = null;
   protected Future<?> _onDeadEventTask = null;

   public SepulcherMonsterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.SepulcherMonsterInstance);
      this.setShowSummonAnimation(true);
      switch(template.getId()) {
         case 25339:
         case 25342:
         case 25346:
         case 25349:
            this.setIsRaid(true);
      }
   }

   @Override
   public void onSpawn() {
      this.setShowSummonAnimation(false);
      switch(this.getId()) {
         case 18150:
         case 18151:
         case 18152:
         case 18153:
         case 18154:
         case 18155:
         case 18156:
         case 18157:
            if (this._victimSpawnKeyBoxTask != null) {
               this._victimSpawnKeyBoxTask.cancel(true);
            }

            this._victimSpawnKeyBoxTask = ThreadPoolManager.getInstance().schedule(new SepulcherMonsterInstance.VictimSpawnKeyBox(this), 300000L);
            if (this._victimShout != null) {
               this._victimShout.cancel(true);
            }

            this._victimShout = ThreadPoolManager.getInstance().schedule(new SepulcherMonsterInstance.VictimShout(this), 5000L);
         case 18196:
         case 18197:
         case 18198:
         case 18199:
         case 18200:
         case 18201:
         case 18202:
         case 18203:
         case 18204:
         case 18205:
         case 18206:
         case 18207:
         case 18208:
         case 18209:
         case 18210:
         case 18211:
         case 18256:
         default:
            break;
         case 18231:
         case 18232:
         case 18233:
         case 18234:
         case 18235:
         case 18236:
         case 18237:
         case 18238:
         case 18239:
         case 18240:
         case 18241:
         case 18242:
         case 18243:
            if (this._changeImmortalTask != null) {
               this._changeImmortalTask.cancel(true);
            }

            this._changeImmortalTask = ThreadPoolManager.getInstance().schedule(new SepulcherMonsterInstance.ChangeImmortal(this), 1600L);
            break;
         case 25339:
         case 25342:
         case 25346:
         case 25349:
            this.setIsRaid(true);
      }

      super.onSpawn();
   }

   @Override
   protected void onDeath(Creature killer) {
      switch(this.getId()) {
         case 18120:
         case 18121:
         case 18122:
         case 18123:
         case 18124:
         case 18125:
         case 18126:
         case 18127:
         case 18128:
         case 18129:
         case 18130:
         case 18131:
         case 18149:
         case 18158:
         case 18159:
         case 18160:
         case 18161:
         case 18162:
         case 18163:
         case 18164:
         case 18165:
         case 18183:
         case 18184:
         case 18212:
         case 18213:
         case 18214:
         case 18215:
         case 18216:
         case 18217:
         case 18218:
         case 18219:
            if (this._onDeadEventTask != null) {
               this._onDeadEventTask.cancel(true);
            }

            this._onDeadEventTask = ThreadPoolManager.getInstance().schedule(new SepulcherMonsterInstance.OnDeadEvent(this), 3500L);
            break;
         case 18141:
         case 18142:
         case 18143:
         case 18144:
         case 18145:
         case 18146:
         case 18147:
         case 18148:
            if (FourSepulchersManager.getInstance().isViscountMobsAnnihilated(this.mysteriousBoxId)) {
               if (this._onDeadEventTask != null) {
                  this._onDeadEventTask.cancel(true);
               }

               this._onDeadEventTask = ThreadPoolManager.getInstance().schedule(new SepulcherMonsterInstance.OnDeadEvent(this), 3500L);
            }
            break;
         case 18150:
         case 18151:
         case 18152:
         case 18153:
         case 18154:
         case 18155:
         case 18156:
         case 18157:
            if (this._victimSpawnKeyBoxTask != null) {
               this._victimSpawnKeyBoxTask.cancel(true);
               this._victimSpawnKeyBoxTask = null;
            }

            if (this._victimShout != null) {
               this._victimShout.cancel(true);
               this._victimShout = null;
            }

            if (this._onDeadEventTask != null) {
               this._onDeadEventTask.cancel(true);
            }

            this._onDeadEventTask = ThreadPoolManager.getInstance().schedule(new SepulcherMonsterInstance.OnDeadEvent(this), 3500L);
            break;
         case 18220:
         case 18221:
         case 18222:
         case 18223:
         case 18224:
         case 18225:
         case 18226:
         case 18227:
         case 18228:
         case 18229:
         case 18230:
         case 18231:
         case 18232:
         case 18233:
         case 18234:
         case 18235:
         case 18236:
         case 18237:
         case 18238:
         case 18239:
         case 18240:
            if (FourSepulchersManager.getInstance().isDukeMobsAnnihilated(this.mysteriousBoxId)) {
               if (this._onDeadEventTask != null) {
                  this._onDeadEventTask.cancel(true);
               }

               this._onDeadEventTask = ThreadPoolManager.getInstance().schedule(new SepulcherMonsterInstance.OnDeadEvent(this), 3500L);
            }
            break;
         case 25339:
         case 25342:
         case 25346:
         case 25349:
            this.giveCup(killer);
            if (this._onDeadEventTask != null) {
               this._onDeadEventTask.cancel(true);
            }

            this._onDeadEventTask = ThreadPoolManager.getInstance().schedule(new SepulcherMonsterInstance.OnDeadEvent(this), 8500L);
      }

      super.onDeath(killer);
   }

   @Override
   public void deleteMe() {
      if (this._victimSpawnKeyBoxTask != null) {
         this._victimSpawnKeyBoxTask.cancel(true);
         this._victimSpawnKeyBoxTask = null;
      }

      if (this._onDeadEventTask != null) {
         this._onDeadEventTask.cancel(true);
         this._onDeadEventTask = null;
      }

      super.deleteMe();
   }

   private void giveCup(Creature killer) {
      String questId = "_620_FourGoblets";
      int cupId = 0;
      int oldBrooch = 7262;
      switch(this.getId()) {
         case 25339:
            cupId = 7256;
            break;
         case 25342:
            cupId = 7257;
            break;
         case 25346:
            cupId = 7258;
            break;
         case 25349:
            cupId = 7259;
      }

      Player player = killer.getActingPlayer();
      if (player != null) {
         if (player.getParty() != null) {
            for(Player mem : player.getParty().getMembers()) {
               QuestState qs = mem.getQuestState("_620_FourGoblets");
               if (qs != null && (qs.isStarted() || qs.isCompleted()) && mem.getInventory().getItemByItemId(7262) == null) {
                  mem.addItem("Quest", cupId, 1L, mem, true);
               }
            }
         } else {
            QuestState qs = player.getQuestState("_620_FourGoblets");
            if (qs != null && (qs.isStarted() || qs.isCompleted()) && player.getInventory().getItemByItemId(7262) == null) {
               player.addItem("Quest", cupId, 1L, player, true);
            }
         }
      }
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return true;
   }

   private static class ChangeImmortal implements Runnable {
      SepulcherMonsterInstance activeChar;

      public ChangeImmortal(SepulcherMonsterInstance mob) {
         this.activeChar = mob;
      }

      @Override
      public void run() {
         Skill fp = SkillsParser.FrequentSkill.FAKE_PETRIFICATION.getSkill();
         fp.getEffects(this.activeChar, this.activeChar, false);
      }
   }

   private static class OnDeadEvent implements Runnable {
      SepulcherMonsterInstance _activeChar;

      public OnDeadEvent(SepulcherMonsterInstance activeChar) {
         this._activeChar = activeChar;
      }

      @Override
      public void run() {
         switch(this._activeChar.getId()) {
            case 18120:
            case 18121:
            case 18122:
            case 18123:
            case 18124:
            case 18125:
            case 18126:
            case 18127:
            case 18128:
            case 18129:
            case 18130:
            case 18131:
            case 18149:
            case 18158:
            case 18159:
            case 18160:
            case 18161:
            case 18162:
            case 18163:
            case 18164:
            case 18165:
            case 18183:
            case 18184:
            case 18212:
            case 18213:
            case 18214:
            case 18215:
            case 18216:
            case 18217:
            case 18218:
            case 18219:
               FourSepulchersManager.getInstance().spawnKeyBox(this._activeChar);
               break;
            case 18141:
            case 18142:
            case 18143:
            case 18144:
            case 18145:
            case 18146:
            case 18147:
            case 18148:
               FourSepulchersManager.getInstance().spawnMonster(this._activeChar.mysteriousBoxId);
               break;
            case 18150:
            case 18151:
            case 18152:
            case 18153:
            case 18154:
            case 18155:
            case 18156:
            case 18157:
               FourSepulchersManager.getInstance().spawnExecutionerOfHalisha(this._activeChar);
               break;
            case 18220:
            case 18221:
            case 18222:
            case 18223:
            case 18224:
            case 18225:
            case 18226:
            case 18227:
            case 18228:
            case 18229:
            case 18230:
            case 18231:
            case 18232:
            case 18233:
            case 18234:
            case 18235:
            case 18236:
            case 18237:
            case 18238:
            case 18239:
            case 18240:
               FourSepulchersManager.getInstance().spawnArchonOfHalisha(this._activeChar.mysteriousBoxId);
               break;
            case 25339:
            case 25342:
            case 25346:
            case 25349:
               FourSepulchersManager.getInstance().spawnEmperorsGraveNpc(this._activeChar.mysteriousBoxId);
         }
      }
   }

   private class VictimShout implements Runnable {
      private final SepulcherMonsterInstance _activeChar;

      public VictimShout(SepulcherMonsterInstance activeChar) {
         this._activeChar = activeChar;
      }

      @Override
      public void run() {
         if (!this._activeChar.isDead()) {
            if (this._activeChar.isVisible()) {
               SepulcherMonsterInstance.this.broadcastPacket(
                  new NpcSay(SepulcherMonsterInstance.this.getObjectId(), 0, SepulcherMonsterInstance.this.getId(), "forgive me!!")
               );
            }
         }
      }
   }

   private class VictimSpawnKeyBox implements Runnable {
      private final SepulcherMonsterInstance _activeChar;

      public VictimSpawnKeyBox(SepulcherMonsterInstance activeChar) {
         this._activeChar = activeChar;
      }

      @Override
      public void run() {
         if (!this._activeChar.isDead()) {
            if (this._activeChar.isVisible()) {
               FourSepulchersManager.getInstance().spawnKeyBox(this._activeChar);
               SepulcherMonsterInstance.this.broadcastPacket(
                  new NpcSay(SepulcherMonsterInstance.this.getObjectId(), 0, SepulcherMonsterInstance.this.getId(), "Many thanks for rescue me.")
               );
               if (SepulcherMonsterInstance.this._victimShout != null) {
                  SepulcherMonsterInstance.this._victimShout.cancel(true);
               }
            }
         }
      }
   }
}

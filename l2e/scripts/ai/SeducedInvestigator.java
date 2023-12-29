package l2e.scripts.ai;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import org.apache.commons.lang3.ArrayUtils;

public class SeducedInvestigator extends Fighter {
   private final int[] _allowedTargets = new int[]{25653, 25654, 25655, 25656, 25657, 25658, 25659, 25660, 25661, 25662, 25663, 25664};
   private long _reuse = 0L;

   public SeducedInvestigator(Attackable actor) {
      super(actor);
      actor.setIsImmobilized(true);
      actor.startHealBlocked(true);
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return false;
      } else {
         for(Npc around : World.getInstance().getAroundNpc(actor, 1000, 200)) {
            if (around != null && ArrayUtils.contains(this._allowedTargets, around.getId()) && !around.isDead()) {
               actor.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, around, Integer.valueOf(300));
            }
         }

         if (Rnd.chance(50) && this._reuse + 30000L < System.currentTimeMillis()) {
            List<Player> players = new ArrayList<>();

            for(Player pl : World.getInstance().getAroundPlayers(actor, 500, 200)) {
               players.add(pl);
            }

            if (players == null || players.size() < 1) {
               return false;
            }

            Player player = players.get(Rnd.get(players.size()));
            if (player.getReflectionId() == actor.getReflectionId()) {
               this._reuse = System.currentTimeMillis();
               int[] buffs = new int[]{5970, 5971, 5972, 5973};
               Skill skill = null;
               if (actor.getId() == 36562) {
                  skill = SkillsParser.getInstance().getInfo(buffs[0], 1);
                  if (player.getFirstEffect(skill) == null) {
                     actor.setTarget(player);
                     actor.doCast(skill);
                  }
               } else if (actor.getId() == 36563) {
                  skill = SkillsParser.getInstance().getInfo(buffs[1], 1);
                  if (player.getFirstEffect(skill) == null) {
                     actor.setTarget(player);
                     actor.doCast(skill);
                  }
               } else if (actor.getId() == 36564) {
                  skill = SkillsParser.getInstance().getInfo(buffs[2], 1);
                  if (player.getFirstEffect(skill) == null) {
                     actor.setTarget(player);
                     actor.doCast(skill);
                  }
               } else {
                  skill = SkillsParser.getInstance().getInfo(buffs[3], 1);
                  if (player.getFirstEffect(skill) == null) {
                     actor.setTarget(player);
                     actor.doCast(skill);
                  }
               }
            }
         }

         return true;
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null) {
         if (!attacker.isPlayable()) {
            if (attacker.getId() == 25659 || attacker.getId() == 25660 || attacker.getId() == 25661) {
               actor.addDamageHate(attacker, 0, 20);
            }

            super.onEvtAttacked(attacker, damage);
         }
      }
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
      if (!target.isPlayer() && !target.isPet() && !target.isSummon()) {
         super.onEvtAggression(target, aggro);
      }
   }

   @Override
   public boolean checkAggression(Creature target) {
      return target.isPlayable() ? false : super.checkAggression(target);
   }
}

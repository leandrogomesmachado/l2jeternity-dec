package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class PrisonGuard extends Fighter {
   public PrisonGuard(Attackable actor) {
      super(actor);
      actor.setIsNoRndWalk(true);
   }

   @Override
   public boolean checkAggression(Creature target) {
      Attackable actor = this.getActiveChar();
      if (actor.isDead() || actor.getId() == 18367) {
         return false;
      } else {
         return target.getFirstEffect(5239) == null ? false : super.checkAggression(target);
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (!actor.isDead() && attacker != null) {
         if (attacker.isSummon() || attacker.isPet()) {
            attacker = attacker.getActingPlayer();
         }

         if (attacker.getFirstEffect(5239) == null) {
            NpcStringId npcString = actor.getId() == 18367 ? NpcStringId.ITS_NOT_EASY_TO_OBTAIN : NpcStringId.YOURE_OUT_OF_YOUR_MIND_COMING_HERE;
            actor.broadcastPacket(new NpcSay(actor.getObjectId(), 22, actor.getId(), npcString), 2000);
            Skill petrification = SkillsParser.getInstance().getInfo(4578, 1);
            if (petrification != null) {
               actor.setTarget(attacker);
               actor.doCast(petrification);
            }
         } else if (actor.getId() == 18367) {
            this.notifyFriends(attacker, damage);
         } else {
            super.onEvtAttacked(attacker, damage);
         }
      }
   }
}

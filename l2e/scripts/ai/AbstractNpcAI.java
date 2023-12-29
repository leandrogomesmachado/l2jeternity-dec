package l2e.scripts.ai;

import java.util.ArrayList;
import java.util.logging.Logger;
import l2e.commons.util.Broadcast;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.listener.ScriptListener;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SocialAction;

public abstract class AbstractNpcAI extends ScriptListener {
   public Logger _log = Logger.getLogger(this.getClass().getSimpleName());

   public AbstractNpcAI(String name, String descr) {
      super(-1, name, descr);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return npc.getId() + ".htm";
   }

   public void registerMobs(int... mobs) {
      this.addAttackId(mobs);
      this.addKillId(mobs);
      this.addSpawnId(mobs);
      this.addSpellFinishedId(mobs);
      this.addSkillSeeId(mobs);
      this.addAggroRangeEnterId(mobs);
      this.addFactionCallId(mobs);
   }

   public void registerMobs(int[] mobs, Quest.QuestEventType... types) {
      for(Quest.QuestEventType type : types) {
         this.addEventId(type, mobs);
      }
   }

   public void registerMobs(Iterable<Integer> mobs, Quest.QuestEventType... types) {
      for(int id : mobs) {
         for(Quest.QuestEventType type : types) {
            this.addEventId(type, new int[]{id});
         }
      }
   }

   protected void broadcastNpcSay(Npc npc, int type, NpcStringId stringId, String... parameters) {
      NpcSay say = new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), stringId);
      if (parameters != null) {
         for(String parameter : parameters) {
            say.addStringParameter(parameter);
         }
      }

      Broadcast.toKnownPlayers(npc, say);
   }

   protected void broadcastNpcSay(Npc npc, int type, String text) {
      Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), text));
   }

   protected void broadcastNpcSay(Npc npc, int type, NpcStringId stringId) {
      Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), stringId));
   }

   protected void broadcastNpcSay(Npc npc, int type, String text, int radius) {
      Broadcast.toKnownPlayersInRadius(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), text), radius);
   }

   protected void broadcastNpcSay(Npc npc, int type, NpcStringId stringId, int radius) {
      Broadcast.toKnownPlayersInRadius(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getIdTemplate(), stringId), radius);
   }

   protected void broadcastSocialAction(Creature character, int actionId) {
      Broadcast.toSelfAndKnownPlayers(character, new SocialAction(character.getObjectId(), actionId));
   }

   protected void broadcastSocialAction(Creature character, int actionId, int radius) {
      Broadcast.toSelfAndKnownPlayersInRadius(character, new SocialAction(character.getObjectId(), actionId), radius);
   }

   protected void attackPlayer(Attackable npc, Playable playable) {
      this.attackPlayer(npc, playable, 999);
   }

   protected void attackPlayer(Npc npc, Playable target, int desire) {
      if (npc instanceof Attackable) {
         ((Attackable)npc).addDamageHate(target, 0, desire);
      }

      npc.setIsRunning(true);
      npc.getAI().setIntention(CtrlIntention.ATTACK, target);
   }

   public Player setRandomPlayerTarget(Npc npc) {
      ArrayList<Player> result = new ArrayList<>();

      for(Player obj : World.getInstance().getAroundPlayers(npc)) {
         if (obj.getZ() >= npc.getZ() - 100 && obj.getZ() <= npc.getZ() + 100 && !obj.isDead()) {
            result.add(obj);
         }
      }

      return !result.isEmpty() && result.size() != 0 ? result.get(getRandom(result.size())) : null;
   }

   public static void main(String[] args) {
   }
}

package l2e.gameserver.model.actor.instance;

import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.WorldRegion;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.serverpackets.SocialAction;

public class GuardInstance extends Attackable {
   public GuardInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.GuardInstance);
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return attacker.isMonster() ? true : super.isAutoAttackable(attacker);
   }

   @Override
   public void returnHome() {
      if (!this.isInsideRadius(this.getSpawn().getX(), this.getSpawn().getY(), 150, false)) {
         this.clearAggroList();
         this.getAI().setIntention(CtrlIntention.MOVING, this.getSpawn().getLocation());
      }
   }

   @Override
   public void onSpawn() {
      this.setIsNoRndWalk(true);
      super.onSpawn();
      if (this.isGlobalAI()) {
         if (this.getAI().getIntention() == CtrlIntention.IDLE) {
            this.getAI().setIntention(CtrlIntention.ACTIVE, null);
         }
      } else {
         WorldRegion region = World.getInstance().getRegion(this.getX(), this.getY(), this.getZ());
         if (region != null && !region.isActive() && !this.isGlobalAI()) {
            ((DefaultAI)this.getAI()).stopAITask();
         }
      }
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/guard/" + pom + ".htm";
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (this.canTarget(player)) {
         if (this.getObjectId() != player.getTargetId()) {
            player.setTarget(this);
         } else if (interact) {
            if (this.containsTarget(player)) {
               player.getAI().setIntention(CtrlIntention.ATTACK, this);
            } else if (!this.canInteract(player)) {
               player.getAI().setIntention(CtrlIntention.INTERACT, this);
            } else {
               this.broadcastPacket(new SocialAction(this.getObjectId(), Rnd.nextInt(8)));
               player.setLastFolkNPC(this);
               List<Quest> qlsa = this.getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
               List<Quest> qlst = this.getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
               if (qlsa != null && !qlsa.isEmpty()) {
                  player.setLastQuestNpcObject(this.getObjectId());
               }

               if (qlst != null && qlst.size() == 1) {
                  qlst.get(0).notifyFirstTalk(this, player);
               } else {
                  this.showChatWindow(player, 0);
               }
            }
         }

         player.sendActionFailed();
      }
   }
}

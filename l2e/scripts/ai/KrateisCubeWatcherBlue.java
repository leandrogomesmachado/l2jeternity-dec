package l2e.scripts.ai;

import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class KrateisCubeWatcherBlue extends DefaultAI {
   public KrateisCubeWatcherBlue(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtThink() {
      Attackable actor = this.getActiveChar();

      for(Player cha : World.getInstance().getAroundPlayers(actor, 600, 200)) {
         if (!cha.isDead() && Rnd.chance(60)) {
            double valCP = cha.getMaxCp() - cha.getCurrentCp();
            if (valCP > 0.0) {
               cha.setCurrentCp(valCP + cha.getCurrentCp());
               cha.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addNumber(Math.round((float)((int)valCP))));
            }

            double valHP = cha.getMaxHp() - cha.getCurrentHp();
            if (valHP > 0.0) {
               cha.setCurrentHp(valHP + cha.getCurrentHp());
               cha.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED).addNumber(Math.round((float)((int)valHP))));
            }

            double valMP = cha.getMaxMp() - cha.getCurrentMp();
            if (valMP > 0.0) {
               cha.setCurrentMp(valMP + cha.getCurrentMp());
               cha.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED).addNumber(Math.round((float)((int)valMP))));
            }
         }
      }
   }

   @Override
   public void onEvtDead(Creature killer) {
      final Attackable actor = this.getActiveChar();
      super.onEvtDead(killer);
      actor.deleteMe();
      ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
         @Override
         public void runImpl() throws Exception {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(18601);
            if (template != null) {
               MonsterInstance a = new MonsterInstance(IdFactory.getInstance().getNextId(), template);
               a.setCurrentHpMp(a.getMaxHp(), a.getMaxMp());
               a.setLocation(actor.getLocation());
               a.spawnMe();
            }
         }
      }, 10000L);
   }
}

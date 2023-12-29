package l2e.scripts.ai.custom;

import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.network.NpcStringId;
import l2e.scripts.ai.AbstractNpcAI;

public class CabaleBuffer extends AbstractNpcAI {
   private static final int DISTANCE_TO_WATCH_OBJECT = 900;
   public static final NpcStringId[] ORATOR_MSG = new NpcStringId[]{
      NpcStringId.THE_DAY_OF_JUDGMENT_IS_NEAR,
      NpcStringId.THE_PROPHECY_OF_DARKNESS_HAS_BEEN_FULFILLED,
      NpcStringId.AS_FORETOLD_IN_THE_PROPHECY_OF_DARKNESS_THE_ERA_OF_CHAOS_HAS_BEGUN,
      NpcStringId.THE_PROPHECY_OF_DARKNESS_HAS_COME_TO_PASS
   };
   public static final NpcStringId[] PREACHER_MSG = new NpcStringId[]{
      NpcStringId.THIS_WORLD_WILL_SOON_BE_ANNIHILATED,
      NpcStringId.ALL_IS_LOST_PREPARE_TO_MEET_THE_GODDESS_OF_DEATH,
      NpcStringId.ALL_IS_LOST_THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED,
      NpcStringId.THE_END_OF_TIME_HAS_COME_THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED
   };
   private static final int ORATOR_FIGTER = 4364;
   private static final int ORATOR_MAGE = 4365;
   private static final int PREACHER_FIGTER = 4361;
   private static final int PREACHER_MAGE = 4362;

   protected CabaleBuffer(String name, String descr) {
      super(name, descr);
      this.addFirstTalkId(new int[]{31094, 31093});
      this.addSpawnId(new int[]{31094, 31093});
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return null;
   }

   @Override
   public String onSpawn(Npc npc) {
      ThreadPoolManager.getInstance().schedule(new CabaleBuffer.CabaleAI(npc), 3000L);
      ThreadPoolManager.getInstance().schedule(new CabaleBuffer.Talk(npc), 60000L);
      return super.onSpawn(npc);
   }

   public void broadcastSay(Npc npc, NpcStringId message, String param, int chance) {
      if (chance == -1) {
         this.broadcastNpcSay(npc, 22, message);
      } else if (getRandom(10000) < chance) {
         this.broadcastNpcSay(npc, 22, message, new String[]{param});
      }
   }

   public int getAbnormalLvl(Player player, int skillId) {
      Effect effect = player.getFirstEffect(skillId);
      return effect != null ? effect.getSkill().getAbnormalLvl() : 0;
   }

   public static void main(String[] args) {
      new CabaleBuffer(CabaleBuffer.class.getSimpleName(), "ai/npc");
   }

   protected class CabaleAI implements Runnable {
      private final Npc _npc;

      protected CabaleAI(Npc npc) {
         this._npc = npc;
      }

      @Override
      public void run() {
         if (this._npc != null && this._npc.isVisible()) {
            boolean isBuffAWinner = false;
            boolean isBuffALoser = false;
            int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
            int losingCabal = 0;
            if (winningCabal == 2) {
               losingCabal = 1;
            } else if (winningCabal == 1) {
               losingCabal = 2;
            }

            for(Player player : World.getInstance().getAroundPlayers(this._npc)) {
               if (player != null && !player.isInvul()) {
                  int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
                  if (playerCabal == winningCabal && playerCabal != 0 && this._npc.getId() == 31094) {
                     if (!player.isMageClass()) {
                        if (this.handleCast(player, 4364)) {
                           if (CabaleBuffer.this.getAbnormalLvl(player, 4364) == 2) {
                              CabaleBuffer.this.broadcastSay(this._npc, NpcStringId.S1_I_GIVE_YOU_THE_BLESSING_OF_PROPHECY, player.getName(), 500);
                           } else {
                              CabaleBuffer.this.broadcastSay(this._npc, NpcStringId.I_BESTOW_UPON_YOU_A_BLESSING, null, 1);
                           }

                           isBuffAWinner = true;
                           continue;
                        }
                     } else if (this.handleCast(player, 4365)) {
                        if (CabaleBuffer.this.getAbnormalLvl(player, 4365) == 2) {
                           CabaleBuffer.this.broadcastSay(this._npc, NpcStringId.S1_I_BESTOW_UPON_YOU_THE_AUTHORITY_OF_THE_ABYSS, player.getName(), 500);
                        } else {
                           CabaleBuffer.this.broadcastSay(this._npc, NpcStringId.HERALD_OF_THE_NEW_ERA_OPEN_YOUR_EYES, null, 1);
                        }

                        isBuffAWinner = true;
                        continue;
                     }
                  } else if (playerCabal == losingCabal && playerCabal != 0 && this._npc.getId() == 31093) {
                     if (!player.isMageClass()) {
                        if (this.handleCast(player, 4361)) {
                           if (CabaleBuffer.this.getAbnormalLvl(player, 4361) == 2) {
                              CabaleBuffer.this.broadcastSay(this._npc, NpcStringId.A_CURSE_UPON_YOU, player.getName(), 500);
                           } else {
                              CabaleBuffer.this.broadcastSay(this._npc, NpcStringId.YOU_DONT_HAVE_ANY_HOPE_YOUR_END_HAS_COME, null, 1);
                           }

                           isBuffALoser = true;
                           continue;
                        }
                     } else if (this.handleCast(player, 4362)) {
                        if (CabaleBuffer.this.getAbnormalLvl(player, 4362) == 2) {
                           CabaleBuffer.this.broadcastSay(this._npc, NpcStringId.S1_YOU_MIGHT_AS_WELL_GIVE_UP, player.getName(), 500);
                        } else {
                           CabaleBuffer.this.broadcastSay(this._npc, NpcStringId.S1_YOU_BRING_AN_ILL_WIND, player.getName(), 1);
                        }

                        isBuffALoser = true;
                        continue;
                     }
                  }

                  if (isBuffAWinner && isBuffALoser) {
                     break;
                  }
               }
            }

            ThreadPoolManager.getInstance().schedule(this, 3000L);
         }
      }

      private boolean handleCast(Player player, int skillId) {
         if (!player.isDead() && player.isVisible() && this._npc.isInsideRadius(player, 900, false, false)) {
            boolean doCast = false;
            int skillLevel = 1;
            int level = CabaleBuffer.this.getAbnormalLvl(player, skillId);
            if (level == 0) {
               doCast = true;
            } else if (level == 1 && Quest.getRandom(100) < 5) {
               doCast = true;
               skillLevel = 2;
            }

            if (doCast) {
               Skill skill = SkillsParser.getInstance().getInfo(skillId, skillLevel);
               this._npc.setTarget(player);
               this._npc.doCast(skill);
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   protected class Talk implements Runnable {
      private final Npc _npc;

      protected Talk(Npc npc) {
         this._npc = npc;
      }

      @Override
      public void run() {
         if (this._npc != null && !this._npc.isDecayed()) {
            NpcStringId[] messages = CabaleBuffer.ORATOR_MSG;
            if (this._npc.getId() == 31093) {
               messages = CabaleBuffer.PREACHER_MSG;
            }

            CabaleBuffer.this.broadcastSay(this._npc, messages[Quest.getRandom(messages.length)], null, -1);
            ThreadPoolManager.getInstance().schedule(this, 60000L);
         }
      }
   }
}

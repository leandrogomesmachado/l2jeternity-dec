package l2e.scripts.ai;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class SeerUgoros extends AbstractNpcAI {
   protected ScheduledFuture<?> _thinkTask = null;
   private static final int _ugoros_pass = 15496;
   private static final int _mid_scale = 15498;
   private static final int _high_scale = 15497;
   private static final int _ugoros_zone = 20706;
   private static final int _seer_ugoros = 18863;
   private static final int _batracos = 32740;
   private static final int _weed_id = 18867;
   protected static Npc _ugoros = null;
   protected static Npc _weed = null;
   protected static boolean _weed_attack = false;
   private static boolean _weed_killed_by_player = false;
   private static boolean _killed_one_weed = false;
   protected static Player _player = null;
   private static final byte ALIVE = 0;
   private static final byte FIGHTING = 1;
   private static final byte DEAD = 2;
   protected static byte STATE = 2;
   private static final Skill _ugoros_skill = SkillsParser.getInstance().getInfo(6426, 1);

   public SeerUgoros(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32740);
      this.addTalkId(32740);
      this.addAttackId(18867);
      this.addKillId(18863);
      this.startQuestTimer("ugoros_respawn", 60000L, null, null);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("ugoros_respawn") && _ugoros == null) {
         _ugoros = addSpawn(18863, 96804, 85604, -3720, 34360, false, 0L);
         this.broadcastInRegion(_ugoros, NpcStringId.LISTEN_OH_TANTAS_I_HAVE_RETURNED_THE_PROPHET_YUGOROS_OF_THE_BLACK_ABYSS_IS_WITH_ME_SO_DO_NOT_BE_AFRAID);
         STATE = 0;
         this.startQuestTimer("ugoros_shout", 120000L, null, null);
      } else if (event.equalsIgnoreCase("ugoros_shout")) {
         if (STATE == 1) {
            ZoneType _zone = ZoneManager.getInstance().getZoneById(20706);
            if (_player == null) {
               STATE = 0;
            } else if (!_zone.isCharacterInZone(_player)) {
               STATE = 0;
               _player = null;
            }
         } else if (STATE == 0) {
            this.broadcastInRegion(_ugoros, NpcStringId.LISTEN_OH_TANTAS_THE_BLACK_ABYSS_IS_FAMISHED_FIND_SOME_FRESH_OFFERINGS);
         }

         this.startQuestTimer("ugoros_shout", 120000L, null, null);
      } else if (event.equalsIgnoreCase("ugoros_attack")) {
         if (_player != null) {
            this.changeAttackTarget(_player);
            NpcSay packet = new NpcSay(
               _ugoros.getObjectId(), 22, _ugoros.getId(), NpcStringId.WELCOME_S1_LET_US_SEE_IF_YOU_HAVE_BROUGHT_A_WORTHY_OFFERING_FOR_THE_BLACK_ABYSS
            );
            packet.addStringParameter(_player.getName().toString());
            _ugoros.broadcastPacket(packet, 2000);
            if (this._thinkTask != null) {
               this._thinkTask.cancel(true);
            }

            this._thinkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SeerUgoros.ThinkTask(), 1000L, 3000L);
         }
      } else if (event.equalsIgnoreCase("weed_check")) {
         if (!_weed_attack || _ugoros == null || _weed == null) {
            _weed = null;
            _weed_attack = false;
         } else if (_weed.isDead() && !_weed_killed_by_player) {
            _killed_one_weed = true;
            _weed = null;
            _weed_attack = false;
            _ugoros.getStatus().setCurrentHp(_ugoros.getStatus().getCurrentHp() + _ugoros.getMaxHp() * 0.2);
            _ugoros.broadcastPacket(
               new NpcSay(
                  _ugoros.getObjectId(),
                  0,
                  _ugoros.getId(),
                  NpcStringId.WHAT_A_FORMIDABLE_FOE_BUT_I_HAVE_THE_ABYSS_WEED_GIVEN_TO_ME_BY_THE_BLACK_ABYSS_LET_ME_SEE
               ),
               2000
            );
         } else {
            this.startQuestTimer("weed_check", 2000L, null, null);
         }
      } else if (event.equalsIgnoreCase("ugoros_expel")) {
         if (_player != null) {
            _player.teleToLocation(94701, 83053, -3580, true);
            _player = null;
         }
      } else if (event.equalsIgnoreCase("teleportInside")) {
         if (STATE != 0) {
            return "<html><body>" + ServerStorage.getInstance().getString(player.getLang(), "288quest.NO_ITEM") + "</body></html>";
         }

         if (player.getInventory().getItemByItemId(15496) == null) {
            QuestState st = player.getQuestState("_423_TakeYourBestShot");
            if (st == null) {
               return "<html><body>" + ServerStorage.getInstance().getString(player.getLang(), "288quest.QUEST_NULL") + "</body></html>";
            }

            return "<html><body>" + ServerStorage.getInstance().getString(player.getLang(), "288quest.NO_ITEM") + "</body></html>";
         }

         STATE = 1;
         _player = player;
         _killed_one_weed = false;
         player.teleToLocation(95984, 85692, -3720, true);
         player.destroyItemByItemId("SeerUgoros", 15496, 1L, npc, true);
         this.startQuestTimer("ugoros_attack", 2000L, null, null);
         QuestState st = player.getQuestState("_288_HandleWithCare");
         if (st != null) {
            st.set("drop", "1");
         }
      } else if (event.equalsIgnoreCase("teleport_back") && player != null) {
         player.teleToLocation(94792, 83542, -3424, true);
         _player = null;
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (npc.isDead()) {
         return null;
      } else {
         if (npc.getId() == 18867) {
            if (_ugoros != null && _weed != null && npc.equals(_weed)) {
               _weed = null;
               _weed_attack = false;
               _weed_killed_by_player = true;
               _ugoros.broadcastPacket(
                  new NpcSay(
                     _ugoros.getObjectId(), 0, _ugoros.getId(), NpcStringId.NO_HOW_DARE_YOU_STOP_ME_FROM_USING_THE_ABYSS_WEED_DO_YOU_KNOW_WHAT_YOU_HAVE_DONE
                  ),
                  2000
               );
               if (this._thinkTask != null) {
                  this._thinkTask.cancel(true);
               }

               this._thinkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SeerUgoros.ThinkTask(), 500L, 3000L);
            }

            npc.doDie(attacker);
         }

         return super.onAttack(npc, attacker, damage, isSummon);
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (npc.getId() == 18863) {
         if (this._thinkTask != null) {
            this._thinkTask.cancel(true);
            this._thinkTask = null;
         }

         STATE = 2;
         this.broadcastInRegion(_ugoros, NpcStringId.AH_HOW_COULD_I_LOSE_OH_BLACK_ABYSS_RECEIVE_ME);
         _ugoros = null;
         addSpawn(32740, 96782, 85918, -3720, 34360, false, 50000L);
         this.startQuestTimer("ugoros_expel", 50000L, null, null);
         this.startQuestTimer("ugoros_respawn", 60000L, null, null);
         QuestState st = player.getQuestState("_288_HandleWithCare");
         if (st != null && st.getInt("cond") == 1 && st.getInt("drop") == 1) {
            if (_killed_one_weed) {
               player.addItem("SeerUgoros", 15498, 1L, npc, true);
               st.set("cond", "2");
            } else {
               player.addItem("SeerUgoros", 15497, 1L, npc, true);
               st.set("cond", "3");
            }

            st.unset("drop");
         }
      }

      return null;
   }

   private void broadcastInRegion(Npc npc, NpcStringId npcString) {
      if (npc != null) {
         NpcSay cs = new NpcSay(npc.getObjectId(), 1, npc.getId(), npcString);
         int region = MapRegionManager.getInstance().getMapRegionLocId(npc.getX(), npc.getY());

         for(Player player : World.getInstance().getAllPlayers()) {
            if (region == MapRegionManager.getInstance().getMapRegionLocId(player.getX(), player.getY()) && Util.checkIfInRange(6000, npc, player, false)) {
               player.sendPacket(cs);
            }
         }
      }
   }

   protected void changeAttackTarget(Creature _attack) {
      ((Attackable)_ugoros).getAI().setIntention(CtrlIntention.IDLE);
      ((Attackable)_ugoros).clearAggroList();
      ((Attackable)_ugoros).setTarget(_attack);
      if (_attack instanceof Attackable) {
         _weed_killed_by_player = false;
         _ugoros.disableSkill(_ugoros_skill, 100000L);
         ((Attackable)_ugoros).setIsRunning(true);
         ((Attackable)_ugoros).addDamageHate(_attack, 0, Integer.MAX_VALUE);
      } else {
         _ugoros.enableSkill(_ugoros_skill);
         ((Attackable)_ugoros).addDamageHate(_attack, 0, 99);
         ((Attackable)_ugoros).setIsRunning(false);
      }

      ((Attackable)_ugoros).getAI().setIntention(CtrlIntention.ATTACK, _attack);
   }

   public static void main(String[] args) {
      new SeerUgoros(SeerUgoros.class.getSimpleName(), "ai");
   }

   private class ThinkTask implements Runnable {
      protected ThinkTask() {
      }

      @Override
      public void run() {
         ZoneType _zone = ZoneManager.getInstance().getZoneById(20706);
         if (SeerUgoros.STATE != 1 || SeerUgoros._player == null || !_zone.isCharacterInZone(SeerUgoros._player) || SeerUgoros._player.isDead()) {
            SeerUgoros.STATE = 0;
            SeerUgoros._player = null;
            if (SeerUgoros.this._thinkTask != null) {
               SeerUgoros.this._thinkTask.cancel(true);
               SeerUgoros.this._thinkTask = null;
            }
         } else if (!SeerUgoros._weed_attack || SeerUgoros._weed == null) {
            if (Quest.getRandom(10) < 6) {
               SeerUgoros._weed = null;

               for(Npc _char : World.getInstance().getAroundNpc(SeerUgoros._ugoros, 2000, 200)) {
                  if (_char instanceof Attackable && !_char.isDead() && ((Attackable)_char).getId() == 18867) {
                     SeerUgoros._weed_attack = true;
                     SeerUgoros._weed = _char;
                     SeerUgoros.this.changeAttackTarget(SeerUgoros._weed);
                     SeerUgoros.this.startQuestTimer("weed_check", 1000L, null, null);
                     break;
                  }
               }

               if (SeerUgoros._weed == null) {
                  SeerUgoros.this.changeAttackTarget(SeerUgoros._player);
               }
            } else {
               SeerUgoros.this.changeAttackTarget(SeerUgoros._player);
            }
         }
      }
   }
}

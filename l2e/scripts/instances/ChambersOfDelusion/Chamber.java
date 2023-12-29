package l2e.scripts.instances.ChambersOfDelusion;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.ReflectionParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.EarthQuake;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.instances.AbstractReflection;

public abstract class Chamber extends AbstractReflection {
   private final int _enterNpc;
   private final int _gkFirst;
   private final int _gkLast;
   private final int _raid;
   private final int _box;
   private final int _reflectionId;
   protected Location[] _coords;
   protected String _boxGroup;

   protected Chamber(String name, String descr, int reflectionId, int enterNpc, int gkFirst, int gkLast, int raid, int box, String boxGroup) {
      super(name, descr);
      this._reflectionId = reflectionId;
      this._enterNpc = enterNpc;
      this._gkFirst = gkFirst;
      this._gkLast = gkLast;
      this._raid = raid;
      this._box = box;
      this._boxGroup = boxGroup;
      this.addStartNpc(this._enterNpc);
      this.addTalkId(this._enterNpc);

      for(int i = this._gkFirst; i <= this._gkLast; ++i) {
         this.addStartNpc(i);
         this.addTalkId(i);
      }

      this.addAttackId(this._box);
      this.addSpellFinishedId(new int[]{this._box});
      this.addEventReceivedId(new int[]{this._box});
      this.addKillId(this._raid);
   }

   private boolean isBigChamber() {
      return this._reflectionId == 131 || this._reflectionId == 132;
   }

   private boolean isBossRoom(Chamber.CDWorld world) {
      return world._currentRoom == this._coords.length - 1;
   }

   protected void changeRoom(Chamber.CDWorld world) {
      Party party = world.getPartyInside();
      Reflection ref = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if ((party != null || world._isSoloEnter) && ref != null) {
         int bossRoomChance = ref.getParams().getInteger("bossRoomChance");
         int newRoom = world._currentRoom;
         if (!this.isBigChamber() || !this.isBossRoom(world)) {
            if (this.isBigChamber() && ref.getInstanceEndTime() - System.currentTimeMillis() < 600000L) {
               newRoom = this._coords.length - 1;
            } else if (!this.isBossRoom(world) && bossRoomChance > 0 && Rnd.chance(bossRoomChance)) {
               newRoom = this._coords.length - 1;
            } else {
               while(newRoom == world._currentRoom) {
                  newRoom = getRandom(this._coords.length - 1);
               }
            }

            if (!world._isSoloEnter) {
               for(Player partyMember : party.getMembers()) {
                  if (world.getReflectionId() == partyMember.getReflectionId()) {
                     partyMember.getAI().setIntention(CtrlIntention.IDLE);
                     this.teleportPlayer(partyMember, this._coords[newRoom], world.getReflectionId());
                  }
               }
            } else if (world.getAllowed() != null) {
               for(int charId : world.getAllowed()) {
                  Player player = World.getInstance().getPlayer(charId);
                  if (player != null) {
                     player.getAI().setIntention(CtrlIntention.IDLE);
                     this.teleportPlayer(player, this._coords[newRoom], world.getReflectionId());
                  }
               }
            }

            world._currentRoom = newRoom;
            if (this.isBigChamber() && this.isBossRoom(world)) {
               ref.setDuration((int)(ref.getInstanceEndTime() - System.currentTimeMillis() + 1200000L));

               for(Npc npc : ref.getNpcs()) {
                  if (npc.getId() == this._gkLast) {
                     npc.broadcastPacket(
                        new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.N21_MINUTES_ARE_ADDED_TO_THE_REMAINING_TIME_IN_THE_INSTANT_ZONE), 2000
                     );
                  }
               }
            } else {
               this.scheduleRoomChange(world, false);
            }
         }
      }
   }

   protected void earthQuake(Chamber.CDWorld world) {
      Party party = world.getPartyInside();
      if (party != null) {
         for(Player partyMember : party.getMembers()) {
            if (world.getReflectionId() == partyMember.getReflectionId()) {
               partyMember.sendPacket(new EarthQuake(partyMember.getX(), partyMember.getY(), partyMember.getZ(), 20, 10));
            }
         }
      }
   }

   protected final synchronized void enterInstance(Player player, Npc npc, int templateId) {
      Party party = player.isInParty() ? player.getParty() : null;
      if (this.enterInstance(player, npc, new Chamber.CDWorld(party), templateId)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ReflectionTemplate template = ReflectionParser.getInstance().getReflectionId(templateId);
         if (template != null) {
            if (party == null
               && (
                  template.getEntryType() == ReflectionTemplate.ReflectionEntryType.SOLO
                     || template.getEntryType() == ReflectionTemplate.ReflectionEntryType.SOLO_PARTY
               )) {
               ((Chamber.CDWorld)world)._isSoloEnter = true;
            }

            if (!((Chamber.CDWorld)world)._isSoloEnter && party != null) {
               for(Player member : party.getMembers()) {
                  if (member != null) {
                     if (hasQuestItems(member, 15311)) {
                        takeItems(member, 15311, -1L);
                     }

                     if (party.isLeader(member)) {
                        giveItems(member, 15311, 1L);
                     }
                  }
               }

               ((Chamber.CDWorld)world)._banishTask = ThreadPoolManager.getInstance()
                  .scheduleAtFixedRate(new Chamber.BanishTask((Chamber.CDWorld)world), 60000L, 60000L);
            }
         }

         this.changeRoom((Chamber.CDWorld)world);
      }
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
         player.setReflectionId(world.getReflectionId());
         if (player.hasSummon()) {
            player.getSummon().setReflectionId(world.getReflectionId());
         }
      } else {
         player.setReflectionId(world.getReflectionId());
         Chamber.CDWorld currentWorld = (Chamber.CDWorld)world;
         Location loc = this._coords[currentWorld._currentRoom];
         this.teleportPlayer(player, loc, world.getReflectionId());
         if (player.hasSummon()) {
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(loc, true);
         }
      }
   }

   protected void exitInstance(Player player) {
      if (player != null && player.isOnline() && player.getReflectionId() != 0) {
         Reflection ref = ReflectionManager.getInstance().getReflection(player.getReflectionId());
         if (ref != null) {
            this.teleportPlayer(player, ref.getReturnLoc(), 0);
            ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
            if (world != null) {
               world.removeAllowed(player.getObjectId());
            }
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = "";
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (player != null && tmpworld != null && tmpworld instanceof Chamber.CDWorld && npc.getId() >= this._gkFirst && npc.getId() <= this._gkLast) {
         Chamber.CDWorld world = (Chamber.CDWorld)tmpworld;
         QuestState st = player.getQuestState(this.getName());
         if (st == null) {
            st = this.newQuestState(player);
         } else {
            if (event.equals("next_room")) {
               if (!world._isSoloEnter) {
                  if (player.getParty() == null) {
                     html.setFile(player, "data/scripts/instances/ChambersOfDelusion/" + player.getLang() + "/no_party.htm");
                     player.sendPacket(html);
                     return null;
                  }

                  if (player.getParty().getLeaderObjectId() != player.getObjectId()) {
                     html.setFile(player, "data/scripts/instances/ChambersOfDelusion/" + player.getLang() + "/no_leader.htm");
                     player.sendPacket(html);
                     return null;
                  }
               }

               if (hasQuestItems(player, 15311)) {
                  st.takeItems(15311, 1L);
                  if (world._roomChangeTask != null) {
                     world._roomChangeTask.cancel(false);
                     world._roomChangeTask = null;
                  }

                  this.changeRoom(world);
                  return null;
               }

               html.setFile(player, "data/scripts/instances/ChambersOfDelusion/" + player.getLang() + "/no_item.htm");
               player.sendPacket(html);
               return null;
            }

            if (event.equals("go_out")) {
               if (!world._isSoloEnter) {
                  if (player.getParty() == null) {
                     html.setFile(player, "data/scripts/instances/ChambersOfDelusion/" + player.getLang() + "/no_party.htm");
                     player.sendPacket(html);
                     return null;
                  }

                  if (player.getParty().getLeaderObjectId() != player.getObjectId()) {
                     html.setFile(player, "data/scripts/instances/ChambersOfDelusion/" + player.getLang() + "/no_leader.htm");
                     player.sendPacket(html);
                     return null;
                  }
               }

               if (world._banishTask != null) {
                  world._banishTask.cancel(true);
               }

               if (world._roomChangeTask != null) {
                  world._roomChangeTask.cancel(false);
                  world._roomChangeTask = null;
               }

               if (!world._isSoloEnter) {
                  for(Player partyMember : player.getParty().getMembers()) {
                     this.exitInstance(partyMember);
                  }
               } else if (world.getAllowed() != null) {
                  for(int charId : world.getAllowed()) {
                     Player pl = World.getInstance().getPlayer(charId);
                     if (pl != null) {
                        this.exitInstance(pl);
                     }
                  }
               }

               Reflection ref = ReflectionManager.getInstance().getReflection(world.getReflectionId());
               if (ref != null) {
                  ref.setEmptyDestroyTime(0L);
               }

               return null;
            }

            if (event.equals("look_party")) {
               if (player.getParty() != null && player.getParty() == world.getPartyInside()) {
                  this.teleportPlayer(player, this._coords[world._currentRoom], world.getReflectionId());
               }

               return null;
            }
         }
      }

      return "";
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, Skill skill) {
      if (!npc.isBusy() && npc.getCurrentHp() < npc.getMaxHp() / 10.0) {
         npc.setBusy(true);
         MonsterInstance box = (MonsterInstance)npc;
         if (getRandom(100) < 25) {
            if (getRandom(100) < 33) {
               box.dropItem(attacker, 4042, (long)((int)(3.0 * Config.RATE_DROP_ITEMS)));
            }

            if (getRandom(100) < 50) {
               box.dropItem(attacker, 4044, (long)((int)(4.0 * Config.RATE_DROP_ITEMS)));
            }

            if (getRandom(100) < 50) {
               box.dropItem(attacker, 4043, (long)((int)(4.0 * Config.RATE_DROP_ITEMS)));
            }

            if (getRandom(100) < 16) {
               box.dropItem(attacker, 9628, (long)((int)(2.0 * Config.RATE_DROP_ITEMS)));
            }

            box.broadcastEvent("SCE_LUCKY", 2000, null);
            box.doCast(new SkillHolder(5758, 1).getSkill());
         } else {
            box.broadcastEvent("SCE_DREAM_FIRE_IN_THE_HOLE", 2000, null);
         }
      }

      return super.onAttack(npc, attacker, damage, isPet, skill);
   }

   @Override
   public String onEventReceived(String eventName, Npc sender, Npc receiver, GameObject reference) {
      switch(eventName) {
         case "SCE_LUCKY":
            receiver.setBusy(true);
            receiver.doCast(new SkillHolder(5758, 1).getSkill());
            break;
         case "SCE_DREAM_FIRE_IN_THE_HOLE":
            receiver.setBusy(true);
            receiver.doCast(new SkillHolder(5376, 4).getSkill());
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isPet) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getPlayerWorld(player);
      if (tmpworld != null && tmpworld instanceof Chamber.CDWorld) {
         Chamber.CDWorld world = (Chamber.CDWorld)tmpworld;
         Reflection ref = ReflectionManager.getInstance().getReflection(world.getReflectionId());
         if (ref != null) {
            ref.spawnByGroup(this._boxGroup);
         }

         if (this.isBigChamber()) {
            this.finishInstance(world, true);
         } else {
            if (world._roomChangeTask != null) {
               world._roomChangeTask.cancel(false);
               world._roomChangeTask = null;
            }

            this.scheduleRoomChange(world, true);
         }
      }

      return super.onKill(npc, player, isPet);
   }

   protected void scheduleRoomChange(Chamber.CDWorld world, boolean bossRoom) {
      if (world != null) {
         Reflection ref = ReflectionManager.getInstance().getReflection(world.getReflectionId());
         long nextInterval = bossRoom ? 60000L : (long)(480 + getRandom(120)) * 1000L;
         if (ref != null && ref.getInstanceEndTime() - System.currentTimeMillis() > nextInterval) {
            world._roomChangeTask = ThreadPoolManager.getInstance().schedule(new Chamber.ChangeRoomTask(world), nextInterval - 5000L);
         }
      }
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      if (npc.getId() == this._box && (skill.getId() == 5376 || skill.getId() == 5758) && !npc.isDead()) {
         npc.doDie(player);
      }

      return super.onSpellFinished(npc, player, skill);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.getId() == this._enterNpc) {
         this.enterInstance(player, npc, this._reflectionId);
      }

      return "";
   }

   public static void main(String[] args) {
   }

   protected class BanishTask implements Runnable {
      Chamber.CDWorld _world;

      BanishTask(Chamber.CDWorld world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Reflection ref = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
            if (ref != null) {
               if (ref.getInstanceEndTime() - System.currentTimeMillis() < 60000L) {
                  if (this._world._banishTask != null) {
                     this._world._banishTask.cancel(false);
                     this._world._banishTask = null;
                  }
               } else {
                  for(int objId : ref.getPlayers()) {
                     Player pl = World.getInstance().getPlayer(objId);
                     if (pl != null
                        && pl.isOnline()
                        && !this._world._isSoloEnter
                        && (this._world._partyInside == null || !pl.isInParty() || this._world._partyInside != pl.getParty())) {
                        Chamber.this.exitInstance(pl);
                     }
                  }
               }
            }
         }
      }
   }

   protected class CDWorld extends ReflectionWorld {
      protected int _currentRoom;
      protected final Party _partyInside;
      protected ScheduledFuture<?> _banishTask;
      protected ScheduledFuture<?> _roomChangeTask;
      protected boolean _isSoloEnter = false;

      protected CDWorld(Party party) {
         this._currentRoom = 0;
         this._partyInside = party;
      }

      protected Party getPartyInside() {
         return this._partyInside;
      }
   }

   protected class ChangeRoomTask implements Runnable {
      Chamber.CDWorld _world;

      ChangeRoomTask(Chamber.CDWorld world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Reflection ref = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
            if (ref != null) {
               Chamber.this.earthQuake(this._world);

               try {
                  Thread.sleep(5000L);
               } catch (InterruptedException var3) {
               }

               Chamber.this.changeRoom(this._world);
            }
         }
      }
   }
}

package l2e.scripts.ai.gracia;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.model.zone.type.EffectZone;
import l2e.scripts.ai.AbstractNpcAI;

public class SoAZone extends AbstractNpcAI {
   private static final Map<Integer, Location> _teleportZones = new HashMap<>();
   private static final int ANNIHILATION_FURNACE = 18928;
   private static final int[] ZONE_BUFFS = new int[]{0, 6443, 6444, 6442};
   private static final int[][] ZONE_BUFFS_LIST = new int[][]{{1, 2, 3}, {1, 3, 2}, {2, 1, 3}, {2, 3, 1}, {3, 2, 1}, {3, 1, 2}};
   private final SoAZone.SeedRegion[] _regionsData = new SoAZone.SeedRegion[3];
   private Long _seedsNextStatusChange;

   private SoAZone(String name, String descr) {
      super(name, descr);
      this.loadSeedRegionData();

      for(int i : _teleportZones.keySet()) {
         this.addEnterZoneId(new int[]{i});
      }

      this.addStartNpc(32739);
      this.addTalkId(32739);
      this.startEffectZonesControl();
   }

   public void loadSeedRegionData() {
      this._regionsData[0] = new SoAZone.SeedRegion(60006, new int[][]{{-180450, 185507, -10544, 11632}, {-180005, 185489, -10544, 11632}});
      this._regionsData[1] = new SoAZone.SeedRegion(60007, new int[][]{{-179600, 186998, -10704, 11632}, {-179295, 186444, -10704, 11632}});
      this._regionsData[2] = new SoAZone.SeedRegion(60008, new int[][]{{-180971, 186361, -10528, 11632}, {-180758, 186739, -10528, 11632}});
      int buffsNow = 0;
      String var = this.loadGlobalQuestVar("SeedNextStatusChange");
      if (!var.equalsIgnoreCase("") && Long.parseLong(var) >= System.currentTimeMillis()) {
         this._seedsNextStatusChange = Long.parseLong(var);
         buffsNow = Integer.parseInt(this.loadGlobalQuestVar("SeedBuffsList"));
      } else {
         buffsNow = getRandom(ZONE_BUFFS_LIST.length);
         this.saveGlobalQuestVar("SeedBuffsList", String.valueOf(buffsNow));
         this._seedsNextStatusChange = this.getNextSeedsStatusChangeTime();
         this.saveGlobalQuestVar("SeedNextStatusChange", String.valueOf(this._seedsNextStatusChange));
      }

      for(int i = 0; i < this._regionsData.length; ++i) {
         this._regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];
      }
   }

   private Long getNextSeedsStatusChangeTime() {
      Calendar reenter = Calendar.getInstance();
      reenter.set(13, 0);
      reenter.set(12, 0);
      reenter.set(11, 13);
      reenter.set(7, 2);
      if (reenter.getTimeInMillis() <= System.currentTimeMillis()) {
         reenter.add(5, 7);
      }

      return reenter.getTimeInMillis();
   }

   private void startEffectZonesControl() {
      for(int i = 0; i < this._regionsData.length; ++i) {
         for(int j = 0; j < this._regionsData[i].af_spawns.length; ++j) {
            this._regionsData[i].af_npcs[j] = addSpawn(
               18928,
               this._regionsData[i].af_spawns[j][0],
               this._regionsData[i].af_spawns[j][1],
               this._regionsData[i].af_spawns[j][2],
               this._regionsData[i].af_spawns[j][3],
               false,
               0L
            );
            this._regionsData[i].af_npcs[j].setDisplayEffect(this._regionsData[i].activeBuff);
         }

         ZoneManager.getInstance().getZoneById(this._regionsData[i].buff_zone, EffectZone.class).addSkill(ZONE_BUFFS[this._regionsData[i].activeBuff], 1);
      }

      this.startQuestTimer("ChangeSeedsStatus", this._seedsNextStatusChange - System.currentTimeMillis(), null, null);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("ChangeSeedsStatus")) {
         int buffsNow = getRandom(ZONE_BUFFS_LIST.length);
         this.saveGlobalQuestVar("SeedBuffsList", String.valueOf(buffsNow));
         this._seedsNextStatusChange = this.getNextSeedsStatusChangeTime();
         this.saveGlobalQuestVar("SeedNextStatusChange", String.valueOf(this._seedsNextStatusChange));

         for(int i = 0; i < this._regionsData.length; ++i) {
            this._regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];

            for(Npc af : this._regionsData[i].af_npcs) {
               af.setDisplayEffect(this._regionsData[i].activeBuff);
            }

            EffectZone zone = ZoneManager.getInstance().getZoneById(this._regionsData[i].buff_zone, EffectZone.class);
            zone.clearSkills();
            zone.addSkill(ZONE_BUFFS[this._regionsData[i].activeBuff], 1);
         }

         this.startQuestTimer("ChangeSeedsStatus", this._seedsNextStatusChange - System.currentTimeMillis(), null, null);
      } else if (event.equalsIgnoreCase("transform")) {
         if (player.getFirstEffect(6408) != null) {
            npc.showChatWindow(player, 2);
         } else {
            npc.setTarget(player);
            npc.doCast(SkillsParser.getInstance().getInfo(6408, 1));
            npc.doCast(SkillsParser.getInstance().getInfo(6649, 1));
            npc.showChatWindow(player, 1);
         }
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onEnterZone(Creature character, ZoneType zone) {
      if (_teleportZones.containsKey(zone.getId())) {
         Location teleLoc = _teleportZones.get(zone.getId());
         character.teleToLocation(teleLoc, false);
      }

      return super.onEnterZone(character, zone);
   }

   public static void main(String[] args) {
      new SoAZone(SoAZone.class.getSimpleName(), "ai");
   }

   static {
      _teleportZones.put(60002, new Location(-213175, 182648, -10992));
      _teleportZones.put(60003, new Location(-181217, 186711, -10528));
      _teleportZones.put(60004, new Location(-180211, 182984, -15152));
      _teleportZones.put(60005, new Location(-179275, 186802, -10720));
   }

   private static class SeedRegion {
      public int buff_zone;
      public int[][] af_spawns;
      public Npc[] af_npcs = new Npc[2];
      public int activeBuff = 0;

      public SeedRegion(int bz, int[][] as) {
         this.buff_zone = bz;
         this.af_spawns = as;
      }
   }
}

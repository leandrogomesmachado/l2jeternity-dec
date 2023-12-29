package l2e.gameserver.model.entity;

import java.util.Calendar;
import java.util.List;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public interface Siegable {
   void startSiege();

   void endSiege();

   SiegeClan getAttackerClan(int var1);

   SiegeClan getAttackerClan(Clan var1);

   List<SiegeClan> getAttackerClans();

   List<Player> getAttackersInZone();

   boolean checkIsAttacker(Clan var1);

   SiegeClan getDefenderClan(int var1);

   SiegeClan getDefenderClan(Clan var1);

   List<SiegeClan> getDefenderClans();

   boolean checkIsDefender(Clan var1);

   List<Npc> getFlag(Clan var1);

   Calendar getSiegeDate();

   boolean giveFame();

   int getFameFrequency();

   int getFameAmount();

   void updateSiege();
}

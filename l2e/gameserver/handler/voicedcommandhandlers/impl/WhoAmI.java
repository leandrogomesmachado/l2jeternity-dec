package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.text.NumberFormat;
import java.util.Locale;
import l2e.commons.util.Strings;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import org.apache.commons.lang3.text.StrBuilder;

public class WhoAmI implements IVoicedCommandHandler {
   private final String[] _commandList = new String[]{"stats"};

   @Override
   public boolean useVoicedCommand(String command, Player player, String targets) {
      if (!Config.ALLOW_STATS_COMMAND) {
         return false;
      } else {
         Player playerToShow = player.isGM() && player.getTarget() != null && player.getTarget().isPlayer() ? player.getTarget().getActingPlayer() : player;
         double hpRegen = Formulas.calcHpRegen(playerToShow);
         double cpRegen = Formulas.calcCpRegen(playerToShow);
         double mpRegen = Formulas.calcMpRegen(playerToShow);
         double hpDrain = playerToShow.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, null, null);
         double hpGain = playerToShow.calcStat(Stats.HEAL_EFFECT, 100.0, null, null);
         double mpGain = playerToShow.calcStat(Stats.MANA_CHARGE, 100.0, null, null);
         double critPerc = 2.0 * playerToShow.getCriticalDmg(null, 1.0, null);
         double critStatic = playerToShow.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0.0, null, null);
         double mCritRate = playerToShow.getMCriticalHit(null, null) * 0.1;
         double blowRate = playerToShow.calcStat(Stats.BLOW_RATE, 0.0, null, null);
         ItemInstance shld = playerToShow.getSecondaryWeaponInstance();
         boolean shield = shld != null && shld.getItem().isShield();
         double shieldDef = shield ? playerToShow.calcStat(Stats.SHIELD_DEFENCE, (double)player.getTemplate().getBaseShldDef(), null, null) : 0.0;
         double shieldRate = shield ? playerToShow.calcStat(Stats.SHIELD_RATE, 0.0, null, null) : 0.0;
         double xpRate = Config.RATE_XP_BY_LVL[playerToShow.getLevel()] * playerToShow.getPremiumBonus().getRateXp();
         double spRate = Config.RATE_SP_BY_LVL[playerToShow.getLevel()] * playerToShow.getPremiumBonus().getRateSp();
         double dropRate = Config.RATE_DROP_ITEMS * playerToShow.getPremiumBonus().getDropItems();
         double spoilRate = Config.RATE_DROP_SPOIL * playerToShow.getPremiumBonus().getDropSpoil();
         double fireResist = playerToShow.calcStat(Stats.FIRE_RES, 0.0, null, null);
         double windResist = playerToShow.calcStat(Stats.WIND_RES, 0.0, null, null);
         double waterResist = playerToShow.calcStat(Stats.WATER_RES, 0.0, null, null);
         double earthResist = playerToShow.calcStat(Stats.EARTH_RES, 0.0, null, null);
         double holyResist = playerToShow.calcStat(Stats.HOLY_RES, 0.0, null, null);
         double unholyResist = playerToShow.calcStat(Stats.DARK_RES, 0.0, null, null);
         double bleedPower = playerToShow.calcStat(Stats.BLEED_PROF, 0.0, null, null);
         double bleedResist = playerToShow.calcStat(Stats.BLEED_VULN, 0.0, null, null);
         double poisonPower = playerToShow.calcStat(Stats.POISON_PROF, 0.0, null, null);
         double poisonResist = playerToShow.calcStat(Stats.POISON_VULN, 0.0, null, null);
         double stunPower = playerToShow.calcStat(Stats.STUN_PROF, 0.0, null, null);
         double stunResist = playerToShow.calcStat(Stats.STUN_VULN, 0.0, null, null);
         double rootPower = playerToShow.calcStat(Stats.ROOT_PROF, 0.0, null, null);
         double rootResist = playerToShow.calcStat(Stats.ROOT_VULN, 0.0, null, null);
         double sleepPower = playerToShow.calcStat(Stats.SLEEP_PROF, 0.0, null, null);
         double sleepResist = playerToShow.calcStat(Stats.SLEEP_VULN, 0.0, null, null);
         double paralyzePower = playerToShow.calcStat(Stats.PARALYZE_PROF, 0.0, null, null);
         double paralyzeResist = playerToShow.calcStat(Stats.PARALYZE_VULN, 0.0, null, null);
         double mentalPower = playerToShow.calcStat(Stats.DERANGEMENT_PROF, 0.0, null, null);
         double mentalResist = playerToShow.calcStat(Stats.DERANGEMENT_VULN, 0.0, null, null);
         double debuffPower = playerToShow.calcStat(Stats.DEBUFF_PROF, 0.0, null, null);
         double debuffResist = playerToShow.calcStat(Stats.DEBUFF_VULN, 0.0, null, null);
         double cancelPower = playerToShow.calcStat(Stats.CANCEL_PROF, 0.0, null, null);
         double cancelResist = playerToShow.calcStat(Stats.CANCEL_VULN, 0.0, null, null);
         double swordResist = 100.0 - playerToShow.calcStat(Stats.SWORD_WPN_VULN, 0.0, null, null);
         double dualResist = 100.0 - playerToShow.calcStat(Stats.DUAL_WPN_VULN, 0.0, null, null);
         double bluntResist = 100.0 - playerToShow.calcStat(Stats.BLUNT_WPN_VULN, 0.0, null, null);
         double daggerResist = 100.0 - playerToShow.calcStat(Stats.DAGGER_WPN_VULN, 0.0, null, null);
         double bowResist = 100.0 - playerToShow.calcStat(Stats.BOW_WPN_VULN, 0.0, null, null);
         double crossbowResist = 100.0 - playerToShow.calcStat(Stats.CROSSBOW_WPN_VULN, 0.0, null, null);
         double poleResist = 100.0 - playerToShow.calcStat(Stats.POLE_WPN_VULN, 0.0, null, null);
         double fistResist = 100.0 - playerToShow.calcStat(Stats.FIST_WPN_VULN, 0.0, null, null);
         double critChanceResist = 100.0 - playerToShow.calcStat(Stats.CRITICAL_RATE, playerToShow.getTemplate().getBaseCritRate(), null, null);
         double critDamResistStatic = playerToShow.calcStat(Stats.CRIT_VULN, playerToShow.getTemplate().getBaseCritVuln(), null, null);
         double critDamResist = 100.0
            - 100.0 * (playerToShow.calcStat(Stats.CRIT_VULN, playerToShow.getTemplate().getBaseCritVuln(), null, null) - critDamResistStatic);
         String dialog = HtmCache.getInstance().getHtm(player, player.getLang(), player.isGM() ? "data/html/mods/whoamiGM.htm" : "data/html/mods/whoami.htm");
         NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
         df.setMaximumFractionDigits(1);
         df.setMinimumFractionDigits(1);
         StrBuilder sb = new StrBuilder(dialog);
         sb.replaceFirst("%hpRegen%", df.format(hpRegen));
         sb.replaceFirst("%cpRegen%", df.format(cpRegen));
         sb.replaceFirst("%mpRegen%", df.format(mpRegen));
         sb.replaceFirst("%hpDrain%", df.format(hpDrain));
         sb.replaceFirst("%hpGain%", df.format(hpGain));
         sb.replaceFirst("%mpGain%", df.format(mpGain));
         sb.replaceFirst("%critPerc%", df.format(critPerc));
         sb.replaceFirst("%critStatic%", df.format(critStatic));
         sb.replaceFirst("%mCritRate%", df.format(mCritRate));
         sb.replaceFirst("%blowRate%", df.format(blowRate));
         sb.replaceFirst("%shieldDef%", df.format(shieldDef));
         sb.replaceFirst("%shieldRate%", df.format(shieldRate));
         sb.replaceFirst("%xpRate%", df.format(xpRate));
         sb.replaceFirst("%spRate%", df.format(spRate));
         sb.replaceFirst("%dropRate%", df.format(dropRate));
         sb.replaceFirst("%spoilRate%", df.format(spoilRate));
         sb.replaceFirst("%fireResist%", df.format(fireResist));
         sb.replaceFirst("%windResist%", df.format(windResist));
         sb.replaceFirst("%waterResist%", df.format(waterResist));
         sb.replaceFirst("%earthResist%", df.format(earthResist));
         sb.replaceFirst("%holyResist%", df.format(holyResist));
         sb.replaceFirst("%darkResist%", df.format(unholyResist));
         sb.replaceFirst("%bleedPower%", df.format(bleedPower));
         sb.replaceFirst("%bleedResist%", df.format(Math.abs(bleedResist)));
         sb.replaceFirst("%poisonPower%", df.format(poisonPower));
         sb.replaceFirst("%poisonResist%", df.format(Math.abs(poisonResist)));
         sb.replaceFirst("%stunPower%", df.format(stunPower));
         sb.replaceFirst("%stunResist%", df.format(Math.abs(stunResist)));
         sb.replaceFirst("%rootPower%", df.format(rootPower));
         sb.replaceFirst("%rootResist%", df.format(Math.abs(rootResist)));
         sb.replaceFirst("%sleepPower%", df.format(sleepPower));
         sb.replaceFirst("%sleepResist%", df.format(Math.abs(sleepResist)));
         sb.replaceFirst("%paralyzePower%", df.format(paralyzePower));
         sb.replaceFirst("%paralyzeResist%", df.format(Math.abs(paralyzeResist)));
         sb.replaceFirst("%mentalPower%", df.format(mentalPower));
         sb.replaceFirst("%mentalResist%", df.format(Math.abs(mentalResist)));
         sb.replaceFirst("%debuffPower%", df.format(debuffPower));
         sb.replaceFirst("%debuffResist%", df.format(Math.abs(debuffResist)));
         sb.replaceFirst("%cancelPower%", df.format(cancelPower));
         sb.replaceFirst("%cancelResist%", df.format(Math.abs(cancelResist)));
         sb.replaceFirst("%swordResist%", df.format(swordResist));
         sb.replaceFirst("%dualResist%", df.format(dualResist));
         sb.replaceFirst("%bluntResist%", df.format(bluntResist));
         sb.replaceFirst("%daggerResist%", df.format(daggerResist));
         sb.replaceFirst("%bowResist%", df.format(bowResist));
         sb.replaceFirst("%crossbowResist%", df.format(crossbowResist));
         sb.replaceFirst("%fistResist%", df.format(fistResist));
         sb.replaceFirst("%poleResist%", df.format(poleResist));
         sb.replaceFirst("%critChanceResist%", df.format(Math.abs(critChanceResist)));
         sb.replaceFirst("%critDamResist%", df.format(critDamResist));
         NpcHtmlMessage msg = new NpcHtmlMessage(0);
         msg.setHtml(player, Strings.bbParse(sb.toString()));
         player.sendPacket(msg);
         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return this._commandList;
   }
}

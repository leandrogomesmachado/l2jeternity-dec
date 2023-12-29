package l2e.gameserver.model.actor.templates.npc.champion;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.skills.effects.AbnormalEffect;

public class ChampionTemplate {
   public int minChance = -1;
   public int maxChance = -1;
   public int minLevel = -1;
   public int maxLevel = -1;
   public boolean isPassive = false;
   public boolean useVitalityRate = false;
   public boolean spawnsInInstances = false;
   public String title = null;
   public float patkMultiplier = 1.0F;
   public float matkMultiplier = 1.0F;
   public float pdefMultiplier = 1.0F;
   public float mdefMultiplier = 1.0F;
   public float atkSpdMultiplier = 1.0F;
   public float matkSpdMultiplier = 1.0F;
   public double hpMultiplier = 1.0;
   public double hpRegenMultiplier = 1.0;
   public double expMultiplier = 1.0;
   public double spMultiplier = 1.0;
   public double itemDropMultiplier = 1.0;
   public double spoilDropMultiplier = 1.0;
   public double adenaMultiplier = 1.0;
   public int weaponEnchant = 0;
   public boolean redCircle = false;
   public boolean blueCircle = false;
   public List<ChampionRewardItem> rewards = new ArrayList<>();
   public List<AbnormalEffect> abnormalEffect = new ArrayList<>();
}

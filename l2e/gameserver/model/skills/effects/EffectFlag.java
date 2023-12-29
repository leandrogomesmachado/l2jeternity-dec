package l2e.gameserver.model.skills.effects;

public enum EffectFlag {
   NONE,
   CHARM_OF_COURAGE,
   CHARM_OF_LUCK,
   PHOENIX_BLESSING,
   NOBLESS_BLESSING,
   SILENT_MOVE,
   PROTECTION_BLESSING,
   RELAXING,
   FEAR,
   CONFUSED,
   MUTED,
   PSYCHICAL_MUTED,
   PSYCHICAL_ATTACK_MUTED,
   DISARMED,
   ROOTED,
   SLEEP,
   STUNNED,
   BETRAYED,
   INVUL,
   PARALYZED,
   BLOCK_RESURRECTION,
   SERVITOR_SHARE,
   SINGLE_TARGET;

   public int getMask() {
      return 1 << this.ordinal();
   }
}

package l2e.gameserver.model.reward;

public enum RewardType {
   RATED_GROUPED,
   NOT_RATED_NOT_GROUPED,
   NOT_RATED_GROUPED,
   SWEEP;

   public static final RewardType[] VALUES = values();
}

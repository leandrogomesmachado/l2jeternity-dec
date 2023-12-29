package l2e.gameserver.model.entity.mods.votereward;

import l2e.commons.dao.JdbcEntity;
import l2e.commons.dao.JdbcEntityState;
import l2e.gameserver.data.holder.VoteRewardHolder;

public class VoteRewardRecord implements JdbcEntity {
   private static final long serialVersionUID = 8665903675445841610L;
   private final String _site;
   private final String _identifier;
   private int _votes;
   private int _lastVoteTime;
   private JdbcEntityState _jdbcEntityState = JdbcEntityState.CREATED;

   public VoteRewardRecord(String site, String identifier, int votes, int lastVoteTime) {
      this._site = site;
      this._identifier = identifier;
      this._votes = votes;
      this._lastVoteTime = lastVoteTime;
   }

   public String getSite() {
      return this._site;
   }

   public String getIdentifier() {
      return this._identifier;
   }

   public int getVotes() {
      return this._votes;
   }

   public int getLastVoteTime() {
      return this._lastVoteTime;
   }

   public void onReceiveReward(int votes, long voteTime) {
      this._votes += votes;
      this._lastVoteTime = (int)(voteTime / 1000L);
      this.setJdbcState(JdbcEntityState.UPDATED);
      this.update();
   }

   @Override
   public void setJdbcState(JdbcEntityState state) {
      this._jdbcEntityState = state;
   }

   @Override
   public JdbcEntityState getJdbcState() {
      return this._jdbcEntityState;
   }

   @Override
   public void save() {
      VoteRewardHolder.getInstance().save(this);
   }

   @Override
   public void delete() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void update() {
      VoteRewardHolder.getInstance().update(this);
   }
}

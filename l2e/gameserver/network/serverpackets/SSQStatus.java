package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;

public class SSQStatus extends GameServerPacket {
   private final int _objectId;
   private final int _page;

   public SSQStatus(int objectId, int recordPage) {
      this._objectId = objectId;
      this._page = recordPage;
   }

   @Override
   protected final void writeImpl() {
      int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
      int totalDawnMembers = SevenSigns.getInstance().getTotalMembers(2);
      int totalDuskMembers = SevenSigns.getInstance().getTotalMembers(1);
      this.writeC(this._page);
      this.writeC(SevenSigns.getInstance().getCurrentPeriod());
      int dawnPercent = 0;
      int duskPercent = 0;
      switch(this._page) {
         case 1:
            this.writeD(SevenSigns.getInstance().getCurrentCycle());
            int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();
            switch(currentPeriod) {
               case 0:
                  this.writeD(SystemMessageId.INITIAL_PERIOD.getId());
                  break;
               case 1:
                  this.writeD(SystemMessageId.SSQ_COMPETITION_UNDERWAY.getId());
                  break;
               case 2:
                  this.writeD(SystemMessageId.RESULTS_PERIOD.getId());
                  break;
               case 3:
                  this.writeD(SystemMessageId.VALIDATION_PERIOD.getId());
            }

            switch(currentPeriod) {
               case 0:
               case 2:
                  this.writeD(SystemMessageId.UNTIL_TODAY_6PM.getId());
                  break;
               case 1:
               case 3:
                  this.writeD(SystemMessageId.UNTIL_MONDAY_6PM.getId());
            }

            this.writeC(SevenSigns.getInstance().getPlayerCabal(this._objectId));
            this.writeC(SevenSigns.getInstance().getPlayerSeal(this._objectId));
            this.writeQ((long)SevenSigns.getInstance().getPlayerStoneContrib(this._objectId));
            this.writeQ((long)SevenSigns.getInstance().getPlayerAdenaCollect(this._objectId));
            double dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(2);
            int dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(2);
            double duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(1);
            int duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(1);
            double totalStoneScore = duskStoneScore + dawnStoneScore;
            int duskStoneScoreProp = 0;
            int dawnStoneScoreProp = 0;
            if (totalStoneScore != 0.0) {
               duskStoneScoreProp = Math.round((float)duskStoneScore / (float)totalStoneScore * 500.0F);
               dawnStoneScoreProp = Math.round((float)dawnStoneScore / (float)totalStoneScore * 500.0F);
            }

            int duskTotalScore = SevenSigns.getInstance().getCurrentScore(1);
            int dawnTotalScore = SevenSigns.getInstance().getCurrentScore(2);
            int totalOverallScore = duskTotalScore + dawnTotalScore;
            if (totalOverallScore != 0) {
               dawnPercent = Math.round((float)dawnTotalScore / (float)totalOverallScore * 100.0F);
               duskPercent = Math.round((float)duskTotalScore / (float)totalOverallScore * 100.0F);
            }

            if (Config.DEBUG) {
               _log.info("Dusk Stone Score: " + duskStoneScore + " - Dawn Stone Score: " + dawnStoneScore);
               _log.info("Dusk Festival Score: " + duskFestivalScore + " - Dawn Festival Score: " + dawnFestivalScore);
               _log.info("Dusk Score: " + duskTotalScore + " - Dawn Score: " + dawnTotalScore);
               _log.info("Overall Score: " + totalOverallScore);
               _log.info("");
               if (totalStoneScore == 0.0) {
                  _log.info("Dusk Prop: 0 - Dawn Prop: 0");
               } else {
                  _log.info("Dusk Prop: " + duskStoneScore / totalStoneScore * 500.0 + " - Dawn Prop: " + dawnStoneScore / totalStoneScore * 500.0);
               }

               _log.info("Dusk %: " + duskPercent + " - Dawn %: " + dawnPercent);
            }

            this.writeQ((long)duskStoneScoreProp);
            this.writeQ((long)duskFestivalScore);
            this.writeQ((long)duskTotalScore);
            this.writeC(duskPercent);
            this.writeQ((long)dawnStoneScoreProp);
            this.writeQ((long)dawnFestivalScore);
            this.writeQ((long)dawnTotalScore);
            this.writeC(dawnPercent);
            break;
         case 2:
            this.writeH(1);
            this.writeC(5);

            for(int i = 0; i < 5; ++i) {
               this.writeC(i + 1);
               this.writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);
               int duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
               int dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);
               this.writeQ((long)duskScore);
               StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(1, i);
               String[] partyMembers = highScoreData.getString("members").split(",");
               if (partyMembers != null) {
                  this.writeC(partyMembers.length);

                  for(String partyMember : partyMembers) {
                     this.writeS(partyMember);
                  }
               } else {
                  this.writeC(0);
               }

               this.writeQ((long)dawnScore);
               highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(2, i);
               partyMembers = highScoreData.getString("members").split(",");
               if (partyMembers != null) {
                  this.writeC(partyMembers.length);

                  for(String partyMember : partyMembers) {
                     this.writeS(partyMember);
                  }
               } else {
                  this.writeC(0);
               }
            }
            break;
         case 3:
            this.writeC(16);
            this.writeC(53);
            this.writeC(3);

            for(int i = 1; i < 4; ++i) {
               int dawnProportion = SevenSigns.getInstance().getSealProportion(i, 2);
               int duskProportion = SevenSigns.getInstance().getSealProportion(i, 1);
               if (Config.DEBUG) {
                  _log.info(
                     SevenSigns.getSealName(i, true)
                        + " = Dawn Prop: "
                        + dawnProportion
                        + "("
                        + dawnProportion / totalDawnMembers * 100
                        + "%), Dusk Prop: "
                        + duskProportion
                        + "("
                        + duskProportion / totalDuskMembers * 100
                        + "%)"
                  );
               }

               this.writeC(i);
               this.writeC(SevenSigns.getInstance().getSealOwner(i));
               if (totalDuskMembers == 0) {
                  if (totalDawnMembers == 0) {
                     this.writeC(0);
                     this.writeC(0);
                  } else {
                     this.writeC(0);
                     this.writeC(Math.round((float)dawnProportion / (float)totalDawnMembers * 100.0F));
                  }
               } else if (totalDawnMembers == 0) {
                  this.writeC(Math.round((float)duskProportion / (float)totalDuskMembers * 100.0F));
                  this.writeC(0);
               } else {
                  this.writeC(Math.round((float)duskProportion / (float)totalDuskMembers * 100.0F));
                  this.writeC(Math.round((float)dawnProportion / (float)totalDawnMembers * 100.0F));
               }
            }
            break;
         case 4:
            this.writeC(winningCabal);
            this.writeC(3);

            for(int i = 1; i < 4; ++i) {
               int dawnProportion = SevenSigns.getInstance().getSealProportion(i, 2);
               int duskProportion = SevenSigns.getInstance().getSealProportion(i, 1);
               dawnPercent = Math.round((float)dawnProportion / (totalDawnMembers == 0 ? 1.0F : (float)totalDawnMembers) * 100.0F);
               duskPercent = Math.round((float)duskProportion / (totalDuskMembers == 0 ? 1.0F : (float)totalDuskMembers) * 100.0F);
               int sealOwner = SevenSigns.getInstance().getSealOwner(i);
               this.writeC(i);
               switch(sealOwner) {
                  case 0:
                     switch(winningCabal) {
                        case 0:
                           this.writeC(0);
                           this.writeD(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
                           continue;
                        case 1:
                           if (duskPercent >= 35) {
                              this.writeC(1);
                              this.writeD(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
                           } else {
                              this.writeC(0);
                              this.writeD(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
                           }
                           continue;
                        case 2:
                           if (dawnPercent >= 35) {
                              this.writeC(2);
                              this.writeD(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
                           } else {
                              this.writeC(0);
                              this.writeD(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
                           }
                        default:
                           continue;
                     }
                  case 1:
                     switch(winningCabal) {
                        case 0:
                           if (duskPercent >= 10) {
                              this.writeC(1);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                           } else {
                              this.writeC(0);
                              this.writeD(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
                           }
                           continue;
                        case 1:
                           if (duskPercent >= 10) {
                              this.writeC(sealOwner);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                           } else {
                              this.writeC(0);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
                           }
                           continue;
                        case 2:
                           if (dawnPercent >= 35) {
                              this.writeC(2);
                              this.writeD(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
                           } else if (duskPercent >= 10) {
                              this.writeC(sealOwner);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                           } else {
                              this.writeC(0);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
                           }
                        default:
                           continue;
                     }
                  case 2:
                     switch(winningCabal) {
                        case 0:
                           if (dawnPercent >= 10) {
                              this.writeC(2);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                           } else {
                              this.writeC(0);
                              this.writeD(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
                           }
                           break;
                        case 1:
                           if (duskPercent >= 35) {
                              this.writeC(1);
                              this.writeD(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
                           } else if (dawnPercent >= 10) {
                              this.writeC(2);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                           } else {
                              this.writeC(0);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
                           }
                           break;
                        case 2:
                           if (dawnPercent >= 10) {
                              this.writeC(sealOwner);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                           } else {
                              this.writeC(0);
                              this.writeD(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
                           }
                     }
               }
            }
      }
   }
}

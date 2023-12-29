package l2e.gameserver.model.entity.mods.votereward.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.Config;
import l2e.gameserver.model.entity.mods.votereward.VoteApiService;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.pair.IntIntPair;
import org.napile.primitive.pair.impl.IntIntPairImpl;

public class L2TopSite extends AbstractAutoRewardSite {
   private static final DateFormat L2TOP_SERVER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   private final String apiKey;
   private final int serverUId;
   private final String serverPrefix;
   private final Pattern webVotesPattern;
   private final Pattern smsVotesPattern;
   private final Pattern voterNamePattern;

   public L2TopSite(MultiValueSet<String> parameters) {
      super(parameters);
      this.apiKey = parameters.getString("api_key");
      this.serverUId = parameters.getInteger("server_uid", 0);
      this.serverPrefix = parameters.getString("server_prefix", "");
      if (StringUtils.isEmpty(this.serverPrefix)) {
         this.webVotesPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} +\\d{2}:\\d{2}:\\d{2})\t+((\\S+))\\s+", 104);
         this.smsVotesPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} +\\d{2}:\\d{2}:\\d{2})\t+((\\S+))\t+x(\\d+)\\s+", 104);
         this.voterNamePattern = Pattern.compile("^(" + Config.CNAME_TEMPLATE + ")$", 64);
      } else {
         this.webVotesPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} +\\d{2}:\\d{2}:\\d{2})\t+(" + this.serverPrefix + "-(\\S+))\\s+", 104);
         this.smsVotesPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} +\\d{2}:\\d{2}:\\d{2})\t+(" + this.serverPrefix + "-(\\S+))\t+x(\\d+)\\s+", 104);
         this.voterNamePattern = Pattern.compile("^" + this.serverPrefix + "-(" + Config.CNAME_TEMPLATE + ")$", 64);
      }
   }

   @Override
   public boolean isEnabled() {
      return !StringUtils.isEmpty(this.apiKey) && this.serverUId != 0 ? super.isEnabled() : false;
   }

   @Override
   protected void parseVotes(Map<String, List<IntIntPair>> votesCache) {
      this.parseVotes(votesCache, false);
      this.parseVotes(votesCache, true);
   }

   private void parseVotes(Map<String, List<IntIntPair>> votesCache, boolean sms) {
      String serverResponse = VoteApiService.getApiResponse(
         String.format("http://l2top.ru/editServ/?adminAct=lastVotes&uid=%d_%s&key=%s", this.serverUId, sms ? "sms" : "web", this.apiKey)
      );
      if (serverResponse != null) {
         Matcher m = sms ? this.smsVotesPattern.matcher(serverResponse) : this.webVotesPattern.matcher(serverResponse);

         while(m.find()) {
            Date voteDate;
            try {
               voteDate = L2TOP_SERVER_DATE_FORMAT.parse(m.group(1));
            } catch (Exception var9) {
               this.error(String.format("Cannot parse voting date: %s", m.group(1)), var9);
               continue;
            }

            Matcher voterNameMatcher = this.voterNamePattern.matcher(m.group(2));
            if (voterNameMatcher.find()) {
               String identifier = voterNameMatcher.group(1);
               List<IntIntPair> votes = votesCache.computeIfAbsent(identifier.toLowerCase(), list -> new ArrayList());
               votes.add(new IntIntPairImpl((int)(voteDate.getTime() / 1000L), sms ? Integer.parseInt(m.group(4)) : 1));
            }
         }
      }
   }

   static {
      L2TOP_SERVER_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
   }
}

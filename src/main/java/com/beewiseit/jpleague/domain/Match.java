package com.beewiseit.jpleague.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
  private String id;
  private MatchComposition teamA;
  private MatchComposition teamB;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class MatchComposition {
    private List<MatchCompositionPlayer> players;
    private Integer score;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class MatchCompositionPlayer {
    private String id;
    private String username;
  }
}

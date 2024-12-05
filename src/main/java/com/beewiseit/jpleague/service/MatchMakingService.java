package com.beewiseit.jpleague.service;

import com.beewiseit.jpleague.domain.Match;
import com.beewiseit.jpleague.domain.Match.MatchCompositionPlayer;
import com.beewiseit.jpleague.domain.PlayersConnectionStats;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MatchMakingService {

  private final ResourceLoader resourceLoader;

  public void suggestTeams(Set<String> players, String leaderA, String leaderB) {
    var stats = generateStats();
    List<String> teamA = new ArrayList<>(List.of(leaderA));
    List<String> teamB = new ArrayList<>(List.of(leaderB));
    var teamAScore = 0d;
    var teamBScore = 0d;
    Set<String> remainingPlayers = new HashSet<>(players);
    remainingPlayers.remove(leaderA);
    remainingPlayers.remove(leaderB);
    for (String player : remainingPlayers) {
      double team1Score = calculateTeamScore(teamA, player, stats);
      double team2Score = calculateTeamScore(teamB, player, stats);

      if (team1Score < team2Score) {
        teamA.add(player);
        teamAScore = team1Score;
      } else if (team1Score > team2Score) {
        teamB.add(player);
        teamBScore = team2Score;
      } else if (teamA.size() < teamB.size()) {
        teamA.add(player);
        teamAScore = team1Score;
      } else {
        teamB.add(player);
        teamBScore = team2Score;
      }
    }
    System.out.println("Team A: "+ teamA + ", Score = " + teamAScore);
    System.out.println("Team B: "+ teamB + ", Score = " + teamBScore);
  }

  public Map<String, PlayersConnectionStats> generateStats() {

    var matchs = readMatches();
    Map<String, PlayersConnectionStats> stats = extractStats(matchs);
    return stats;
  }

  private Map<String, PlayersConnectionStats> extractStats(List<Match> matchs) {
    Map<String, PlayersConnectionStats> stats = new HashMap<>();
    for (Match match : matchs) {
      List<PlayersConnectionStats> teamACouples = generateCouples(match.getTeamA().getPlayers(), match.getTeamA().getScore() - match.getTeamB().getScore());
      List<PlayersConnectionStats> teamBCouples = generateCouples(match.getTeamB().getPlayers(), match.getTeamB().getScore() - match.getTeamA().getScore());
      addStat(stats, teamACouples);
      addStat(stats, teamBCouples);
    }
    return stats;
  }

  private void addStat(Map<String, PlayersConnectionStats> stats, List<PlayersConnectionStats> teamCouples) {
    for (PlayersConnectionStats couple : teamCouples) {
      String coupleId = couple.getPlayers().stream().sorted().collect(Collectors.joining("_"));
      if (stats.containsKey(coupleId)) {
        var currentStat = stats.get(coupleId).getConnectionScore();
        stats.put(coupleId, PlayersConnectionStats.builder()
            .players(couple.getPlayers())
            .connectionScore(currentStat + couple.getConnectionScore())
            .build()
        );
      } else {
        stats.put(coupleId, couple);
      }
    }
  }

  public List<Match> readMatches() {
    List<Match> matches = new ArrayList<>();
    try {
      // Load the CSV file from resources
      Resource resource = resourceLoader.getResource("classpath:data/matchs.csv");
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

      String line;
      while ((line = reader.readLine()) != null) {
        // Skip the header line if present
        if (line.startsWith("teamA")) continue;

        // Parse the CSV line
        String[] parts = line.split(";");
        Set<String> teamA = Arrays.stream(parts[0].split(",")).collect(Collectors.toSet());
        Set<String> teamB = Arrays.stream(parts[1].split(",")).collect(Collectors.toSet());
        Integer teamAScore = Integer.valueOf(parts[2]);
        Integer teamBScore = Integer.valueOf(parts[3]);
        Match match = Match.builder()
            .teamA(Match.MatchComposition.builder()
                .score(teamAScore)
                .players(teamA.stream().map(player -> MatchCompositionPlayer.builder().username(player).build()).toList())
                .build())
            .teamB(Match.MatchComposition.builder()
                .score(teamBScore)
                .players(teamB.stream().map(player -> MatchCompositionPlayer.builder().username(player).build()).toList())
                .build())
            .build();

        matches.add(match);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return matches;
  }

  public static List<PlayersConnectionStats> generateCouples(List<MatchCompositionPlayer> players, Integer goalsDifference) {
    List<PlayersConnectionStats> list = new ArrayList<>();
    for (int i = 0; i < players.size(); i++) {
      for (int j = i + 1; j < players.size(); j++) {
        PlayersConnectionStats build = PlayersConnectionStats.builder()
            .players(List.of(players.get(i).getUsername(), players.get(j).getUsername()))
            .connectionScore(goalsDifference)
            .build();
        list.add(build);
      }
    }
    return list;
  }

  private double calculateTeamScore(final List<String> team, String player, Map<String, PlayersConnectionStats> connectionScores) {
    var teamProjection = new ArrayList<>(team);
    teamProjection.add(player);
    var score = 0D;
    for (int i = 0; i < teamProjection.size(); i++) {
      for (int j = i + 1; j < teamProjection.size(); j++) {
        var coupleId = teamProjection.stream().sorted().collect(Collectors.joining("_"));
        var coupleScore = connectionScores.containsKey(coupleId)
            ? connectionScores.get(coupleId).getConnectionScore()
            : 0D;
        score = score + coupleScore;
      }
    }
    return score;
  }
}

//0753317044
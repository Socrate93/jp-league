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

  public void suggestTeams(Set<String> players) {
    var stats = generateStats();
    balanceTeams(new ArrayList<>(players), stats);
  }

  public Map<String, PlayersConnectionStats> generateStats() {

    var matchs = readMatches();
    return extractStats(matchs);
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

  private double calculateTeamScore(final List<String> team, Map<String, PlayersConnectionStats> connectionScores) {
    var score = 0D;
    for (int i = 0; i < team.size(); i++) {
      for (int j = i + 1; j < team.size(); j++) {
        var coupleId = Stream.of(team.get(i), team.get(j))
            .sorted()
            .collect(Collectors.joining("_"));
        var coupleScore = connectionScores.containsKey(coupleId)
            ? connectionScores.get(coupleId).getConnectionScore()
            : 0D;
        score = score + coupleScore;
      }
    }
    return score;
  }

  private List<List<String>> generateCombinations(List<String> players) {
    List<List<String>> combinations = new ArrayList<>();
    generateCombinationsHelper(players, 0, 5, new ArrayList<>(), combinations);
    return combinations;
  }

  private void generateCombinationsHelper(List<String> players, int start, int teamSize,
                                          List<String> current, List<List<String>> combinations) {
    if (current.size() == teamSize) {
      combinations.add(new ArrayList<>(current));
      return;
    }

    for (int i = start; i < players.size(); i++) {
      current.add(players.get(i));
      generateCombinationsHelper(players, i + 1, teamSize, current, combinations);
      current.remove(current.size() - 1);
    }
  }

  public void balanceTeams(List<String> players, Map<String, PlayersConnectionStats> scores) {
    int n = players.size();
    if (n % 2 != 0) {
      throw new IllegalArgumentException("Number of players must be even.");
    }

    List<List<String>> allCombinations = generateCombinations(players);
    double closestDifference = Double.MAX_VALUE;

    for (List<String> team1 : allCombinations) {
      Set<String> team1Set = new HashSet<>(team1);
      List<String> team2 = new ArrayList<>();

      for (String player : players) {
        if (!team1Set.contains(player)) {
          team2.add(player);
        }
      }

      double scoreTeam1 = calculateTeamScore(team1, scores);
      double scoreTeam2 = calculateTeamScore(team2, scores);
      double difference = Math.abs(scoreTeam1 - scoreTeam2);

      if (difference < closestDifference) {
        closestDifference = difference;
        System.out.println("Team A: " + team1 + ", Score = " + scoreTeam1);
        System.out.println("Team B: " + team2 + ", Score = " + scoreTeam2);
      }
    }
  }
}

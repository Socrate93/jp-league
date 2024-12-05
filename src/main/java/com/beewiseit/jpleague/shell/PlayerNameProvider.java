package com.beewiseit.jpleague.shell;
import com.beewiseit.jpleague.domain.Match;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
class PlayerNameProvider implements ValueProvider {

  private final ResourceLoader resourceLoader;

  private static List<String> ALL_PLAYERS;

  @Override
  public List<CompletionProposal> complete(CompletionContext completionContext) {
    return ALL_PLAYERS.stream()
        .map(CompletionProposal::new)
        .toList();
  }

  @PostConstruct
  void loadPlayers() {
    try {
      // Load the CSV file from resources
      Resource resource = resourceLoader.getResource("classpath:data/players.csv");
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
      ALL_PLAYERS = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        ALL_PLAYERS.add(line.trim());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

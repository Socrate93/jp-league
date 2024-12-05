package com.beewiseit.jpleague.shell;

import com.beewiseit.jpleague.service.MatchMakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.*;

@ShellComponent
@RequiredArgsConstructor
public class PromptShell {

  private final MatchMakingService service;

  @ShellMethod(key = "match", value = "Add match stats")
  public String createMatch(
      @ShellOption(valueProvider = PlayerNameProvider.class) List<String> A,
      @ShellOption(valueProvider = PlayerNameProvider.class) List<String> B
  ) {

    Scanner scanner = new Scanner(System.in);
    System.out.println("Please enter the names of Team A players:");

    Set<String> teamA = readTeam(scanner);
    System.out.println("Please enter the names of Team B players:");
    Set<String> teamB = readTeam(scanner);

    System.out.println("Players collected: " + A);
    System.out.println("Players collected: " + B);
    return "Hello world ";
  }

  @ShellMethod(key = "stats", value = "Generate stats")
  public String stats() {
    System.out.println(service.generateStats());
    return "OK";
  }

  @ShellMethod(key = "teams", value = "Generate teams")
  public String teams(
      @ShellOption(valueProvider = PlayerNameProvider.class) String p1,
      @ShellOption(valueProvider = PlayerNameProvider.class) String p2,
      @ShellOption(valueProvider = PlayerNameProvider.class) String p3,
      @ShellOption(valueProvider = PlayerNameProvider.class) String p4,
      @ShellOption(valueProvider = PlayerNameProvider.class) String p5,
      @ShellOption(valueProvider = PlayerNameProvider.class) String p6,
      @ShellOption(valueProvider = PlayerNameProvider.class) String p7,
      @ShellOption(valueProvider = PlayerNameProvider.class) String p8,
      @ShellOption(valueProvider = PlayerNameProvider.class) String p9,
      @ShellOption(valueProvider = PlayerNameProvider.class) String p10
  ) {

    List<String> players = List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
    var leaders = selectTwoRandomItems(players);
    service.suggestTeams(new HashSet<>(players), leaders[0], leaders[1]);
    return "OK";
  }

  private Set<String> readTeam(Scanner scanner) {
    Set<String> team = new HashSet<>();
    for (int i = 1; i <= 5; i++) {
      System.out.print("Player " + i + ": ");
      String name = scanner.nextLine();
      team.add(name);
    }
    return team;
  }

  public static String[] selectTwoRandomItems(List<String> items) {
    if (items == null || items.size() < 2) {
      throw new IllegalArgumentException("List must contain at least two items.");
    }
    Random random = new Random();
    int index1 = random.nextInt(items.size());
    int index2;
    do {
      index2 = random.nextInt(items.size());
    } while (index1 == index2);
    return new String[]{items.get(index1), items.get(index2)};
  }

}

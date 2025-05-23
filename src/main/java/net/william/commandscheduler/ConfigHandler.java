package net.william.commandscheduler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigHandler {

  public static Path intervalPath;
  public static Path clockPath;
  public static Path onceAtBootPath;

  private static final Path CONFIG_PATH = Paths.get("config", "commandscheduler");

  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(TimeUnit.class, (JsonDeserializer<TimeUnit>) (json, typeOfT, context) -> {
        String value = json.getAsString();
        return TimeUnit.fromString(value);
      })
      .setPrettyPrinting()
      .disableHtmlEscaping()
      .create();

  private static final Logger LOGGER = LoggerFactory.getLogger("CommandScheduler");

  private static final Type INTERVAL_TYPE = new TypeToken<List<Interval>>() {
  }.getType();
  private static final Type CLOCKBASED_TYPE = new TypeToken<List<ClockBased>>() {
  }.getType();
  private static final Type ONCE_TYPE = new TypeToken<List<AtBoot>>() {
  }.getType();

  private static List<Interval> intervalCommands = new ArrayList<>();
  private static List<ClockBased> clockBasedCommands = new ArrayList<>();
  private static List<AtBoot> onceAtBootCommands = new ArrayList<>();

  public static void loadAllCommands() {
    intervalCommands = loadIntervalCommands();
    clockBasedCommands = loadClockBasedCommands();
    onceAtBootCommands = loadOnceAtBootCommands();
  }

  public static List<Interval> loadIntervalCommands() {

    intervalPath = CONFIG_PATH.resolve("intervals.json5");
    List<Interval> list = loadConfig("intervals.json5", INTERVAL_TYPE);

    if (checkForDuplicateIDs(list)) {
      saveIntervalCommands();
    }

    return list;
  }

  public static List<ClockBased> loadClockBasedCommands() {

    clockPath = CONFIG_PATH.resolve("clock_based.json5");
    List<ClockBased> list = loadConfig("clock_based.json5", CLOCKBASED_TYPE);

    // Sort the times for each loaded command
    for (ClockBased cc : list) {
      cc.getTimes().sort((a, b) -> {
        int cmp = Integer.compare(a[0], b[0]); // compare hours
        return cmp != 0 ? cmp : Integer.compare(a[1], b[1]); // if equal, compare minutes
      });
    }

    if (checkForDuplicateIDs(list)) {
      saveClockBasedCommands();
    }

    return list;
  }

  public static List<AtBoot> loadOnceAtBootCommands() {
    onceAtBootPath = CONFIG_PATH.resolve("once_at_boot.json5");
    List<AtBoot> list = loadConfig("once_at_boot.json5", ONCE_TYPE);

    if (checkForDuplicateIDs(list)) {
      saveOnceAtBootCommands();
    }

    return list;
  }

  public static void reloadConfigs() {
    intervalCommands = loadIntervalCommands();
    clockBasedCommands = loadClockBasedCommands();
    onceAtBootCommands = loadOnceAtBootCommands();
  }

  private static <T> List<T> loadConfig(String fileName, Type type) {
    try {
      Path path = CONFIG_PATH.resolve(fileName);

      if (!Files.exists(path)) {
        Files.createDirectories(CONFIG_PATH);
        writeDefaultConfigWithComments(fileName);
      }

      String json = Files.readString(path, StandardCharsets.UTF_8);
      List<T> loaded = gson.fromJson(json, type);
      if (loaded == null)
        return new ArrayList<>();

      // Validate and filter entries
      List<T> validEntries = new ArrayList<>();
      for (T entry : loaded) {
        try {
          if (entry instanceof Interval ic) {
            if (!Interval.isValidInterval(ic.getInterval())) {
              LOGGER.error("Skipping invalid interval for ID '{}': {}", ic.getID(), ic.getInterval());
              continue;
            }
          }
          validEntries.add(entry);
        } catch (Exception e) {
          LOGGER.error("Skipping invalid entry in {}: {}", fileName, e.getMessage());
        }
      }

      return validEntries;
    } catch (Exception e) {
      LOGGER.error("Failed to load {}: {}", fileName, e.getMessage());
      return new ArrayList<>();
    }
  }

  private static <T> void saveConfig(Path path, List<T> list, Type type) {
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      gson.toJson(list, type, writer); // <-- USE THIS instead of list.getClass()
    } catch (IOException e) {
      LOGGER.error("Failed to save config to {}: {}", path, e.getMessage());
    }
  }

  public static <T> boolean checkForDuplicateIDs(List<T> list) {
    Map<String, Integer> idMap = new HashMap<>();
    boolean duplicatesFound = false;

    for (T item : list) {
      if (!(item instanceof Scheduler cmd)) {
        continue;
      }

      String originalID = cmd.getID();
      int duplicateCount = idMap.getOrDefault(originalID, 0);

      if (duplicateCount > 0) {
        // Generate new unique ID
        String newID;
        do {
          newID = originalID + "." + duplicateCount;
          duplicateCount++;
        } while (idMap.containsKey(newID));

        // Update the command's ID
        cmd.setID(newID);
        duplicatesFound = true;
        LOGGER.warn("Renamed duplicate ID '{}' to '{}'", originalID, newID);

        // Track both old and new IDs
        idMap.put(originalID, duplicateCount);
        idMap.put(newID, 0);
      } else {
        idMap.put(originalID, 1);
      }
    }

    return duplicatesFound;
  }

  private static void writeDefaultConfigWithComments(String fileName) throws Exception {
    Path path = CONFIG_PATH.resolve(fileName);

    String commentedJson = switch (fileName) {
      case "intervals.json5" ->
        """
            [
              {
                "ID": "ExampleIntervalCommand",
                "description": "This is the description for the 'interval' scheduler example. This runs every minute.",
                "active": true,
                "command": "say This command runs every minute! Via the CommandScheduler mod. If you are a server moderator, you should probably update this. Run /commandscheduler",
                "interval": 1,
                // units are ticks, seconds, minutes, hours or days
                "unit": "minutes",
                // if the command should run once as the timer starts or not
                "runInstantly": false
              },
              {
                "ID": "ExampleIntervalCommand2",
                "description": "This is the description for the second 'interval' scheduler example. This runs every 7 game ticks, however is deactivated by default.",
                "active": false,
                "command": "say This runs every 7 game ticks. To deactivate, run /commandscheduler deactivate ExampleIntervalCommand2",
                "interval": 7,
                // units are ticks, seconds, minutes, hours or days
                "unit": "ticks",
                // if the command should run once as the timer starts or not
                "runInstantly": false
              }
            ]
            """;
      case "clock_based.json5" ->
        """
            [
              {
                "ID": "ExampleClockBasedCommand",
                "description": "This is the description for the 'clock-based' scheduler example. This runs at 01.00 and at 13.00.",
                "active": false,
                "command": "say The time is either 01.00 or 13.00! (commandscheduler mod)",
                // Use 24h format: HH.mm
                "times": [[1, 0], [13, 0]]
              }
            ]
            """;
      case "once_at_boot.json5" ->
        """
            [
              {
                "ID": "ExampleAtBootCommand",
                "description": "This is the description for the 'at boot' scheduler example. This runs every time the server boots",
                "active": false,
                "command": "say The server has booted! (commandscheduler mod)"
              }
            ]
            """;
      default -> throw new IllegalArgumentException("Unknown config file: " + fileName);
    };

    Files.writeString(path, commentedJson, StandardCharsets.UTF_8);
  }

  public static boolean removeCommandById(String id) {
    boolean removed = false;

    removed |= intervalCommands.removeIf(cmd -> cmd.getID().equals(id));
    removed |= clockBasedCommands.removeIf(cmd -> cmd.getID().equals(id));
    removed |= onceAtBootCommands.removeIf(cmd -> cmd.getID().equals(id));

    if (removed) {
      saveIntervalCommands();
      saveClockBasedCommands();
      saveOnceAtBootCommands();
    }

    return removed;
  }

  public static Object getCommandById(String id) {
    for (Interval ic : intervalCommands) {
      if (ic.getID().equals(id))
        return ic;
    }
    for (ClockBased cc : clockBasedCommands) {
      if (cc.getID().equals(id))
        return cc;
    }
    for (AtBoot oc : onceAtBootCommands) {
      if (oc.getID().equals(id))
        return oc;
    }
    return null;
  }

  public static boolean updateSchedulerId(String oldId, String newId) {
    Object cmd = getCommandById(oldId);
    if (cmd == null || !Scheduler.isValidID(newId))
      return false;

    boolean success = false;

    if (cmd instanceof Interval ic) {
      success = ic.setID(newId);
      if (success)
        saveIntervalCommands();
    } else if (cmd instanceof ClockBased cc) {
      success = cc.setID(newId);
      if (success)
        saveClockBasedCommands();
    } else if (cmd instanceof AtBoot oc) {
      success = oc.setID(newId);
      if (success)
        saveOnceAtBootCommands();
    }

    return success;
  }

  public static void saveIntervalCommands() {
    saveConfig(intervalPath, intervalCommands, INTERVAL_TYPE);
  }

  public static void saveClockBasedCommands() {
    // Sort the times in each ClockBasedCommand before saving
    for (ClockBased cc : clockBasedCommands) {
      cc.getTimes().sort((time1, time2) -> {
        int hourComparison = Integer.compare(time1[0], time2[0]);
        if (hourComparison != 0) {
          return hourComparison; // Sort by hour first
        }
        return Integer.compare(time1[1], time2[1]); // Then by minute
      });
    }

    saveConfig(clockPath, clockBasedCommands, CLOCKBASED_TYPE);
  }

  public static void saveOnceAtBootCommands() {
    saveConfig(onceAtBootPath, onceAtBootCommands, ONCE_TYPE);
  }

  public static List<ClockBased> getClockBasedCommands() {
    return Collections.unmodifiableList(clockBasedCommands);
  }

  public static List<Interval> getIntervalCommands() {
    return Collections.unmodifiableList(intervalCommands);
  }

  public static List<AtBoot> getOnceAtBootCommands() {
    return Collections.unmodifiableList(onceAtBootCommands);
  }

  public static void addClockBasedCommand(ClockBased command) {
    clockBasedCommands.add(command);
  }

  public static void addIntervalCommand(Interval command) {
    intervalCommands.add(command);
  }

  public static void addOnceAtBootCommand(AtBoot command) {
    onceAtBootCommands.add(command);
  }

  public static List<String> getAllSchedulerIDs() {
    List<String> ids = new ArrayList<>();
    for (Scheduler cmd : getIntervalCommands()) {
      ids.add(cmd.getID());
    }
    for (Scheduler cmd : getClockBasedCommands()) {
      ids.add(cmd.getID());
    }
    for (Scheduler cmd : getOnceAtBootCommands()) {
      ids.add(cmd.getID());
    }
    return ids;
  }

  public static List<String> getClockBasedSchedulerIDs() {
    List<String> ids = new ArrayList<>();
    for (Scheduler cmd : getClockBasedCommands()) {
      ids.add(cmd.getID());
    }
    return ids;
  }

}
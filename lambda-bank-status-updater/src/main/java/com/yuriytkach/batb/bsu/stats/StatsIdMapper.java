package com.yuriytkach.batb.bsu.stats;

import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
class StatsIdMapper {

  String mapNameToId(final String name) {
    return switch (name.substring(0, Math.min(4, name.length())).toLowerCase(Locale.getDefault())) {
      case "авто" -> "cars";
      case "тепл" -> "thermals";
      case "дода", "дада" -> "addons";
      case "раці" -> "radios";
      case "дрон" -> "drones";
      case "одяг" -> "clothes";
      case "стар" -> "starlinks";
      case "гене" -> "generators";
      case "меди" -> "medicine";
      case "інше" -> "other";
      case "техн" -> "tech";
      case "брон" -> "armors";
      default -> name;
    };
  }

}

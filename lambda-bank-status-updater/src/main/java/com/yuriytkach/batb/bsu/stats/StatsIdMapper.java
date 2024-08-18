/*
 * Copyright (c) 2024  Commonwealth-22
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
      case "інше", "іншо" -> "other";
      case "техн" -> "tech";
      case "брон" -> "armors";
      default -> name;
    };
  }

}

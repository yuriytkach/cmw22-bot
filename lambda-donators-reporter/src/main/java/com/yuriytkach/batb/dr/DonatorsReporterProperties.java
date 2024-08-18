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

package com.yuriytkach.batb.dr;

import java.util.Map;
import java.util.Set;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "reporter")
public interface DonatorsReporterProperties {

  @WithDefault("0")
  int monthBefore();

  @WithDefault("50000")
  long minAmountCents();

  @WithDefault("3")
  int minCount();

  @WithDefault("/batb/tx/ignored-names")
  String ignoredDonatorsKey();

  Set<String> nameStopWords();

  Map<String, String> replacers();

  String donationStatsNotificationMessage();

}

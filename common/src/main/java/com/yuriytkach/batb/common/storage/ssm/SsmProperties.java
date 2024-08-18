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

package com.yuriytkach.batb.common.storage.ssm;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "app.storage.ssm")
public interface SsmProperties {

  @WithDefault("/batb/")
  String prefix();

  @WithDefault("/tokens")
  String tokens();

  @WithDefault("/accounts")
  String accounts();

  @WithDefault("statuses")
  String statuses();

  @WithDefault("/funds/")
  String funds();

  @WithDefault("/stats")
  String statistics();

  @WithDefault("current")
  String currentFundProp();

  @WithDefault("gsheet/registry")
  String registryGsheet();

  @WithDefault("gsheet/stats")
  String statsGsheet();

  @WithDefault("gsheet/donators")
  String donatorsGsheet();
}

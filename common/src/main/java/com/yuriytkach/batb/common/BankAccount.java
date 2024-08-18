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

package com.yuriytkach.batb.common;

import java.util.Map;

import javax.annotation.Nullable;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@RegisterForReflection
public record BankAccount(
  String id,
  String name,
  @Nullable Map<String, String> properties,
  @Nullable String gsheetStatRow
) { }

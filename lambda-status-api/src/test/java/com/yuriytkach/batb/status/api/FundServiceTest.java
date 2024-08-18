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

package com.yuriytkach.batb.status.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yuriytkach.batb.common.FundInfo;
import com.yuriytkach.batb.common.storage.FundInfoStorage;

@ExtendWith(MockitoExtension.class)
class FundServiceTest {

  @Mock
  private FundInfoStorage fundInfoStorage;

  @InjectMocks
  private FundService tested;

  @Test
  void shouldReturnFundInfo() {
    final FundInfo fundInfo = FundInfo.builder()
      .id("fundraiserId")
      .goal(1000)
      .name("fundraiserName")
      .description("fundraiserDescription")
      .lastSpent(500)
      .lastRaised(6000)
      .lastUpdatedAt(Instant.now())
      .active(true)
      .build();
    when(fundInfoStorage.getById(anyString())).thenReturn(Optional.of(fundInfo));

    final var actual = tested.getFundStatus("fundraiserId");

    assertThat(actual).hasValue(FundraiserStatusResponse.builder()
      .name("fundraiserName")
      .description("fundraiserDescription")
      .raised(60)
      .spent(5)
      .goal(1000)
      .lastUpdatedAt(fundInfo.lastUpdatedAt())
      .active(true)
      .build());

    verify(fundInfoStorage).getById("fundraiserId");
    verifyNoMoreInteractions(fundInfoStorage);
  }

  @Test
  void shouldReturnCurrentFundInfo() {
    final FundInfo fundInfo = FundInfo.builder()
      .id("fundraiserId")
      .goal(1000)
      .name("fundraiserName")
      .description("fundraiserDescription")
      .lastSpent(500)
      .lastRaised(6000)
      .lastUpdatedAt(Instant.now())
      .active(false)
      .build();
    when(fundInfoStorage.getCurrent()).thenReturn(Optional.of(fundInfo));

    final var actual = tested.getFundStatus(FundService.CURRENT_FUND_ID);

    assertThat(actual).hasValue(FundraiserStatusResponse.builder()
      .name("fundraiserName")
      .description("fundraiserDescription")
      .raised(60)
      .spent(5)
      .goal(1000)
      .lastUpdatedAt(fundInfo.lastUpdatedAt())
      .active(false)
      .build());

    verify(fundInfoStorage).getCurrent();
    verifyNoMoreInteractions(fundInfoStorage);
  }
}

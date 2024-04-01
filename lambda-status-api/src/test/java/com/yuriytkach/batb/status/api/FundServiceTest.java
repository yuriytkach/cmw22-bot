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

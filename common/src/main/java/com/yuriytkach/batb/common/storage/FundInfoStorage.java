package com.yuriytkach.batb.common.storage;

import java.util.Optional;

import com.yuriytkach.batb.common.FundInfo;

public interface FundInfoStorage {

  Optional<FundInfo> getCurrent();

  Optional<FundInfo> getById(String fundraiserId);

  void save(FundInfo fundInfo);

}

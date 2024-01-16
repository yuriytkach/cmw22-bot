package com.yuriytkach.batb.common.secret;

import java.util.Optional;

public interface SecretsReader {

  Optional<String> readSecret(final String secretName);

}

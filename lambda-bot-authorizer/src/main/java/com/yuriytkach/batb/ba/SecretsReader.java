package com.yuriytkach.batb.ba;

import java.util.Optional;

public interface SecretsReader {

  Optional<String> readSecret(final String secretName);

}

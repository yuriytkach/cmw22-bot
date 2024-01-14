package com.yuriytkach.batb.cdk;

import software.amazon.awscdk.App;

public final class BankAccountsTrackApp {

  public static void main(final String[] args) {
    final App app = new App();

    new BankAccountsTrackStack(app, "BankAccountsTrackStack");

    app.synth();
  }

}

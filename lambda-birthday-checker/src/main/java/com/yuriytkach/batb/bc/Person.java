package com.yuriytkach.batb.bc;

import java.time.LocalDate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Person(String name, LocalDate birthday, String imgUrl) {

}

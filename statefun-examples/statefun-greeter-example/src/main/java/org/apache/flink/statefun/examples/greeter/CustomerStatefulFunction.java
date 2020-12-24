/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.statefun.examples.greeter;

import org.apache.flink.statefun.examples.greeter.generated.GreetRequest;
import org.apache.flink.statefun.examples.greeter.generated.GreetResponse;
import org.apache.flink.statefun.sdk.Context;
import org.apache.flink.statefun.sdk.FunctionType;
import org.apache.flink.statefun.sdk.StatefulFunction;
import org.apache.flink.statefun.sdk.annotations.Persisted;
import org.apache.flink.statefun.sdk.state.PersistedValue;

import java.util.HashSet;

/**
 * A stateful function that generates a unique greeting for each user based on how many times that
 * user has been seen by the system.
 */
final class CustomerStatefulFunction implements StatefulFunction {

  /**
   * The function type is the unique identifier that identifies this type of function. The type, in
   * conjunction with an identifier, is how routers and other functions can use to reference a
   * particular instance of a greeter function.
   *
   * <p>If this was a multi-module application, the function type could be in different package so
   * functions in other modules could message the greeter without a direct dependency on this class.
   */
  static final FunctionType TYPE = new FunctionType("apache", "customer");

  /**
   * The persisted value for maintaining state about a particular user. The value returned by this
   * field is always scoped to the current user. seenCount is the number of times the user has been
   * greeted.
   */
  @Persisted
  private final PersistedValue<HashSet> seenCount = PersistedValue.of("seen-count", HashSet.class);

  @Override
  public void invoke(Context context, Object input) {
    GreetRequest greetMessage = (GreetRequest) input;
    GreetResponse response = computePersonalizedGreeting(greetMessage);
    context.send(GreetingIO.GREETING_EGRESS_ID, response);
  }

  private GreetResponse computePersonalizedGreeting(GreetRequest greetMessage) {

    final String name = greetMessage.getWho();
    final String action = greetMessage.getAction();
    final int seen = seenCount.getOrDefault(0);
    String greeting = "";
    if (action.equals("add")){
      if (seen < 4){
        seenCount.set(seen + 1);
      }
      greeting = greetAddition(name, seen);
    }
    else if (action.equals("remove")){
      if (seen > 0){
        seenCount.set(seen - 1);
      }
      greeting = greetRemoval(name, seen);
    }

    return GreetResponse.newBuilder().setWho(name).setGreeting(greeting).build();
  }

  private static String greetRemoval(String name, int seen) {
    if (seen <= 0) {
      return String.format("Item %s cannot be removed! Count is %d(empty) ! \uD83D\uDE32", name, seen);
    }
    return String.format("Count of Item %s decreased in inventory. Current count is %d !\uD83D\uDE4C", name, seen - 1);
  }

  private static String greetAddition(String name, int seen) {
    switch (seen) {
      case 0:
      case 1:
      case 2:
      case 3:
        return String.format("Count of Item %s increased in inventory. Current count is %d ! \uD83D\uDE32", name, seen + 1);
      default:
        return String.format("Max capacity(%d) reached for %s.\uD83D\uDE4C", seen, name);
    }
  }
}

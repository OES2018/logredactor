/**
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
package com.cloudera.log4j.redactor;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TestRedaction {

  @Test
  public void testRedaction() {
    // System.setProperty("log4j.debug", "true");
    Logger log = Logger.getLogger(TestRedaction.class);
    log.info("WHERE x=123-45-6789");
    log.info("WHERE x=1234-1234-1234-1234 OR y=1234-1234-1234-1234 OR y=123-45-6789");
    log.info("xgz ABC pepe");
    log.info("password=\"pepe pepe\"");
  }

}

package com.lending.dar360UserService.user.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AESUtilTest {

  @Test
  void testMain() {
    Assertions.assertDoesNotThrow(() -> AESUtil.main());
  }
}
package com.bjoggis.spillhuset.minecraft.domain;

import com.bjoggis.spillhuset.service.SSHService;

public record Ip(String value) {

  public static Ip from(String ip) {
    return new Ip(ip);
  }
}

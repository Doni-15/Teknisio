package com.teknisio.backend.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class idGenerator {
  // Genereted id for request service
  public static String kodePermintaan(){
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
    String waktu = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
    String random = UUID.randomUUID().toString().substring(0, 5).toUpperCase();

    // ex: REQ-195632-25052026-AS123
    return "REQ-" + waktu + "-" + date + "-" + random;
  }
}

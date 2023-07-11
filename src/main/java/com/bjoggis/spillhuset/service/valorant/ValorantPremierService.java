package com.bjoggis.spillhuset.service.valorant;

import com.bjoggis.spillhuset.entity.valorant.ValorantPremierEntry;
import com.bjoggis.spillhuset.properties.SpillhusetProperties;
import com.bjoggis.spillhuset.repository.valorant.ValorantPremierEntryRepository;
import com.bjoggis.spillhuset.service.ChatService;
import java.text.BreakIterator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class ValorantPremierService {

  private final Logger logger = LoggerFactory.getLogger(ValorantPremierService.class);
  private final SpillhusetProperties properties;
  private final ValorantPremierEntryRepository repository;
  private final RestTemplate restTemplate = new RestTemplate();

  private final ChatService chatService;

  record Content(String content) {

  }

  public ValorantPremierService(SpillhusetProperties properties,
      ValorantPremierEntryRepository repository,
      ChatService chatService) {
    this.properties = properties;
    this.repository = repository;
    this.chatService = chatService;
  }

  @Scheduled(cron = "0 0 10 * * ?")
  @Transactional(readOnly = true)
  public void checkPlayTime() {
    logger.info("Checking playtime for " + LocalDate.now());
    Optional<ValorantPremierEntry> opt = repository.findByDate(LocalDate.now());

    if (opt.isPresent()) {
      ValorantPremierEntry entry = opt.get();

      logger.info("Found entry for " + LocalDate.now());
      logger.info("The first LocalDateTime that matches today's date is " + entry.getDate());

      String song = chatService.generateSingleMessage(
          "Lag et valorant dikt om at laget 'Jonas og Oss' skal vinne Valorant Premier kampen vår.",
          "spillhuset-bot", 7L);

      String text =
          "Ny Valorant Premier kamp i kveld klokka " + entry.getDate().toLocalTime() + ".\n**Map: "
              + entry.getMap() + "**" + "\nMøt opp minst 10 minutter før kampstart.\n\n";

      sendMessage(text);

      messageChunks(song).forEach(this::sendMessage);

    } else {
      logger.info("No entry found for " + LocalDate.now());
    }

  }

  private void sendMessage(String message) {
    logger.info("Sending message to webhook: {}", message);
    final Content content = new Content(message);
    restTemplate.postForObject(
        properties.valorant().webhook(),
        content, String.class);
  }

  public List<String> messageChunks(String source) {
    List<String> chunks = new ArrayList<>();

    if (source == null || source.isEmpty()) {
      return chunks;
    }

    BreakIterator boundary = BreakIterator.getWordInstance();
    boundary.setText(source);

    int start = boundary.first();
    int end = boundary.next();
    StringBuilder tmpComp = new StringBuilder();

    while (end != BreakIterator.DONE) {
      String word = source.substring(start, end);

      if ((tmpComp.length() + word.length()) < 2000) {
        tmpComp.append(word);
      } else {
        chunks.add(tmpComp.toString());
        tmpComp = new StringBuilder(word);
      }

      start = end;
      end = boundary.next();
    }

    if (!tmpComp.toString().isEmpty()) {
      chunks.add(tmpComp.toString());
    }
    return chunks;
  }
}

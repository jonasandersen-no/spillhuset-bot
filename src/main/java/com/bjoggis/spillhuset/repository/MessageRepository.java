package com.bjoggis.spillhuset.repository;

import com.bjoggis.spillhuset.entity.Message;
import com.bjoggis.spillhuset.entity.ThreadChannel;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

  List<Message> findByThreadChannel_ThreadId(String threadId);

  Set<Message> findByThreadChannel_ThreadIdOrderByCreatedAsc(String threadId);



  long deleteByThreadChannel(
      ThreadChannel threadChannel);

}

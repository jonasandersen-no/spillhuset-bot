package com.bjoggis.spillhuset.repository;

import com.bjoggis.spillhuset.entity.ThreadChannel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreadChannelRepository extends JpaRepository<ThreadChannel, Long> {

  long deleteByThreadId(
      String threadId);

  Optional<ThreadChannel> findByThreadId(String threadId);

}

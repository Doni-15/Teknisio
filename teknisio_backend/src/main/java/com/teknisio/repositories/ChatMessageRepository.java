package com.teknisio.repositories;

import com.teknisio.model.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

  List<ChatMessage> findByPermintaanLayanan_IdPermintaanOrderByWaktuKirimAsc(UUID idPermintaan);

  long countByPenerima_IdUserAndDibacaFalse(UUID penerimaId);
}
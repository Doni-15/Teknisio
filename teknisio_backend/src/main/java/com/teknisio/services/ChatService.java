package com.teknisio.services;

import com.teknisio.common.exception.ForbiddenException;
import com.teknisio.common.exception.ResourceNotFoundException;
import com.teknisio.dto.requests.ChatMessageRequest;
import com.teknisio.dto.responses.ChatMessageResponse;
import com.teknisio.model.entities.ChatMessage;
import com.teknisio.model.entities.PermintaanLayanan;
import com.teknisio.model.entities.User;
import com.teknisio.repositories.ChatMessageRepository;
import com.teknisio.repositories.PermintaanLayananRepository;
import com.teknisio.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatMessageRepository chatMessageRepository;
  private final PermintaanLayananRepository permintaanLayananRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;

  private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  /**
   * Stores the chat message and broadcasts it to both sender and receiver via STOMP.
   */
  @Transactional
  public ChatMessageResponse sendMessage(UUID senderUserId, ChatMessageRequest request) {
    UUID permintaanId = UUID.fromString(request.getServiceRequestId());

    PermintaanLayanan permintaan = getPermintaanForChat(permintaanId, senderUserId);

    User sender = userRepository.findById(senderUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

    User receiver;
    if (permintaan.getPengguna().getIdUser().equals(senderUserId)) {
      if (permintaan.getTeknisiProfile() == null || permintaan.getTeknisiProfile().getUser() == null) {
        throw new ResourceNotFoundException("Technician has not been assigned to this service request yet");
      }
      receiver = permintaan.getTeknisiProfile().getUser();
    } else {
      receiver = permintaan.getPengguna();
    }

    ChatMessage chat = ChatMessage.builder()
            .permintaanLayanan(permintaan)
            .pengirim(sender)
            .penerima(receiver)
            .pesan(request.getMessage())
            .dibaca(false)
            .build();

    chatMessageRepository.save(chat);

    ChatMessageResponse response = toResponse(chat);

    messagingTemplate.convertAndSend("/topic/chat/" + permintaanId, response);

    log.debug("Chat message sent for serviceRequest {}: {}", permintaanId, request.getMessage());

    return response;
  }

  private PermintaanLayanan getPermintaanForChat(UUID serviceRequestId, UUID currentUserId) {
    PermintaanLayanan permintaan = permintaanLayananRepository.findById(serviceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

    boolean isCustomer = permintaan.getPengguna() != null
            && permintaan.getPengguna().getIdUser().equals(currentUserId);

    boolean isTechnician = permintaan.getTeknisiProfile() != null
            && permintaan.getTeknisiProfile().getUser() != null
            && permintaan.getTeknisiProfile().getUser().getIdUser().equals(currentUserId);

    if (!isCustomer && !isTechnician) {
      throw new ForbiddenException("You are not allowed to access this chat");
    }

    return permintaan;
  }

  /**
   * Returns chat history for a given service request.
   */
  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getChatHistory(UUID serviceRequestId, UUID currentUserId) {
    getPermintaanForChat(serviceRequestId, currentUserId);

    List<ChatMessage> messages = chatMessageRepository
            .findByPermintaanLayanan_IdPermintaanOrderByWaktuKirimAsc(serviceRequestId);

    return messages.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
  }

  /**
   * Marks all messages in a given service request as read for the given user.
   */
  @Transactional
  public void markAsRead(UUID serviceRequestId, UUID userId) {
    getPermintaanForChat(serviceRequestId, userId);

    List<ChatMessage> messages = chatMessageRepository
            .findByPermintaanLayanan_IdPermintaanOrderByWaktuKirimAsc(serviceRequestId);

    for (ChatMessage msg : messages) {
      if (msg.getPenerima().getIdUser().equals(userId) && !msg.getDibaca()) {
        msg.setDibaca(true);
        chatMessageRepository.save(msg);
      }
    }
  }

  /**
   * Returns count of unread messages for a user.
   */
  @Transactional(readOnly = true)
  public long getUnreadCount(UUID userId) {
    return chatMessageRepository.countByPenerima_IdUserAndDibacaFalse(userId);
  }

  private ChatMessageResponse toResponse(ChatMessage chat) {
    return ChatMessageResponse.builder()
            .chatId(chat.getIdChat().toString())
            .serviceRequestId(chat.getPermintaanLayanan().getIdPermintaan().toString())
            .senderId(chat.getPengirim().getIdUser().toString())
            .senderName(chat.getPengirim().getNama())
            .receiverId(chat.getPenerima().getIdUser().toString())
            .message(chat.getPesan())
            .isRead(chat.getDibaca())
            .sentAt(chat.getWaktuKirim() != null
                    ? chat.getWaktuKirim().format(ISO_FORMATTER)
                    : null)
            .build();
  }
}

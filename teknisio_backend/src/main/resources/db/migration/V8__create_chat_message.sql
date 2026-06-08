-- Creates the chat_message table for real-time messaging
-- between customer and technician for a service request.

CREATE TABLE IF NOT EXISTS chat_message (
  id_chat       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_permintaan UUID NOT NULL,
  id_pengirim   UUID NOT NULL,
  id_penerima   UUID NOT NULL,
  pesan         TEXT NOT NULL,
  dibaca        BOOLEAN NOT NULL DEFAULT FALSE,
  waktu_kirim   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

  created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

  CONSTRAINT fk_chat_message_permintaan
    FOREIGN KEY (id_permintaan) REFERENCES permintaan_layanan(id_permintaan)
    ON DELETE CASCADE,

  CONSTRAINT fk_chat_message_pengirim
    FOREIGN KEY (id_pengirim) REFERENCES users(id_user)
    ON DELETE CASCADE,

  CONSTRAINT fk_chat_message_penerima
    FOREIGN KEY (id_penerima) REFERENCES users(id_user)
    ON DELETE CASCADE
);

-- Index for fetching chat history per service request
CREATE INDEX IF NOT EXISTS idx_chat_message_permintaan
  ON chat_message(id_permintaan, waktu_kirim);

-- Index for counting unread messages per receiver
CREATE INDEX IF NOT EXISTS idx_chat_message_penerima_dibaca
  ON chat_message(id_penerima) WHERE dibaca = FALSE;
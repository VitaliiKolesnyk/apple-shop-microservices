package org.service.notificationservice.mapper.impl;

import org.service.notificationservice.dto.ReplyResponse;
import org.service.notificationservice.entity.Reply;
import org.service.notificationservice.mapper.ReplyMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReplyMapperImpl implements ReplyMapper {
    @Override
    public ReplyResponse map(Reply reply) {
        return new ReplyResponse(reply.getBody(), reply.getTimestamp(), reply.getEmailStatus().name());
    }

    @Override
    public List<ReplyResponse> map(List<Reply> replies) {
        return replies.stream()
                .map(this::map)
                .toList();
    }
}

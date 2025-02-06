package notificationservice.mapper;

import org.service.notificationservice.dto.ReplyResponse;
import org.service.notificationservice.entity.Reply;

import java.util.List;

public interface ReplyMapper {

    ReplyResponse map(Reply reply);

    List<ReplyResponse> map(List<Reply> replies);
}

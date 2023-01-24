package com.douyin.repository.message;

import com.douyin.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageMO, String> {

    // 通过实现Repository，自定义条件查询
    List<MessageMO> findAllByToUserIdEqualsOrderByCreateTimeDesc(String toUserId, Pageable pageable);

    // TODO: 删除消息
    // void deleteAllByFromUserIdAndToUserIdAndMsgType();

}
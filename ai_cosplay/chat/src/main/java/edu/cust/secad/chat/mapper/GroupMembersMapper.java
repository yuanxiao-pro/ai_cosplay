package edu.cust.secad.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.cust.secad.model.chat.GroupMembers;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMembersMapper  extends BaseMapper<GroupMembers> {
}

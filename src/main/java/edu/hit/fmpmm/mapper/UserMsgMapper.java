package edu.hit.fmpmm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.hit.fmpmm.domain.web.UserMsg;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMsgMapper extends BaseMapper<UserMsg> {
}

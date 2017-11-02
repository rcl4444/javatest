package cn.zhijian.passport.domain.commandhandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.commands.GainMessageCommand;
import cn.zhijian.passport.commands.ModityMessageCommand;
import cn.zhijian.passport.db.MessageMapper;
import cn.zhijian.passport.db.row.MessageOpertionDetailRow;
import cn.zhijian.passport.db.row.MessageRow;
import cn.zhijian.passport.statustype.MessageType;
import lombok.Data;

public class MessageCommandHandler {
	
	private static Logger logger = LoggerFactory.getLogger(MessageCommandHandler.class);
	
	final MessageMapper dao;
	
	public MessageCommandHandler(MessageMapper dao){
		
		this.dao = dao;
	}
	
	@Data
	private class MessageMain{
		
		final Long id;
		final String content;
		final Date createdAt;
		final List<OperationDetail> operationDetail;
	}
	
	@Data
	private class OperationDetail{
		
		final String linktxt;
		final String linkurl;
		final String linkclass;
	}
	
	@CommandHandler
	public List<Object> getMessage(GainMessageCommand cmd){
		
		List<MessageRow> messages = this.dao.getMessage(cmd.getMessagetype(), cmd.getAccesstype(), cmd.getBelongtype(), 
				cmd.getIsread(), cmd.getIsdelete(), cmd.getPersonid(), cmd.getCorporateid());
		if(cmd.getMessagetype() == MessageType.Operation){
			if(messages.size()>0){
				List<MessageOpertionDetailRow> opertions = this.dao.getOpertionDetail(messages.stream().map(o->o.getId()).collect(Collectors.toList()));
				return messages.stream().map(o->{
					List<OperationDetail> odetail = opertions.stream().filter(oi->oi.getMessageid().equals(o.getId()))
							.map(oi->new OperationDetail(oi.getLinktxt(),oi.getLinkurl(),oi.getLinkclass())).collect(Collectors.toList());
					return new MessageMain(o.getId(),o.getContent(),o.getCreatedAt(),odetail);
				}).collect(Collectors.toList());
			}
			else{
				return new ArrayList<Object>();
			}
		}
		else{
			return messages.stream().map(o->new MessageMain(o.getId(),o.getContent(),o.getCreatedAt(),null)).collect(Collectors.toList());
		}
	}
	
	@CommandHandler
	public Pair< Boolean, String> changeMessageStatus(ModityMessageCommand cmd){
		
		if(cmd.getMessageid().size()==0){
			return Pair.of(false, "请选择操作消息");
		}
		if(cmd.getIsdelete()!=null && cmd.getIsdelete()){
			this.dao.deleteMessage(cmd.getMessageid());
		}
		if(cmd.getIsread()!=null && cmd.getIsread()){
			this.dao.readMessage(cmd.getMessageid());
		}
		return Pair.of(true, "操作完毕");
	}
}

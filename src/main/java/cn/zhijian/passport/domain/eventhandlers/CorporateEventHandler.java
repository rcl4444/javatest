package cn.zhijian.passport.domain.eventhandlers;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.MessageMapper;
import cn.zhijian.passport.db.row.MessageOpertionDetailRow;
import cn.zhijian.passport.db.row.MessageRow;
import cn.zhijian.passport.domain.events.CorporateInvitedPersonEvent;
import cn.zhijian.passport.domain.events.CorporatePassEvent;
import cn.zhijian.passport.statustype.MessageAccessType;
import cn.zhijian.passport.statustype.MessageBelongType;
import cn.zhijian.passport.statustype.MessageOpertionType;
import cn.zhijian.passport.statustype.MessageType;

public class CorporateEventHandler {
	final static Logger logger = LoggerFactory.getLogger(CorporateEventHandler.class);
	
	final MessageMapper messageMapper;
	final SqlSessionManager sqlSessionManager;
	
	public CorporateEventHandler(MessageMapper messageMapper,SqlSessionManager sqlSessionManager){
		
		this.messageMapper = messageMapper;
		this.sqlSessionManager = sqlSessionManager;
	}
	
	@EventHandler
	public void corporatePass(CorporatePassEvent ev){
		
		MessageRow row = new MessageRow();
		row.setContent("您已拥有管理员权限,快去分配员工信息吧.");
		row.setBelongtype(MessageBelongType.Corporate);
		row.setMessagetype(MessageType.Prompt);
		row.setOpertiontype(MessageOpertionType.PromptCorporateApplyResult);
		row.setPersonid(ev.getPersonid());
		row.setCorporateid(ev.getCorporateid());
		row.setAccesstype(MessageAccessType.Pull);
		row.setIsread(false);
		row.setIsdelete(false);
		row.setCreatedAt(new Date());
		this.messageMapper.insert(row);
	}
	
	@EventHandler
	public void invitedPersonApply(CorporateInvitedPersonEvent ev){
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				MessageMapper sessionMessageMapper = session.getMapper(MessageMapper.class);
				MessageRow row = new MessageRow();
				row.setContent("管理员\""+ev.getInvitedpersonname()+"\"邀请您加入\""+ev.getCorporatename()+"\"");
				row.setBelongtype(MessageBelongType.Person);
				row.setMessagetype(MessageType.Operation);
				row.setOpertiontype(MessageOpertionType.OperationCorporateInvitation);
				row.setPersonid(ev.getBeInvitationpersonid());
				row.setAccesstype(MessageAccessType.Pull);
				row.setIsread(false);
				row.setIsdelete(false);
				row.setCreatedAt(new Date());
				sessionMessageMapper.insert(row);
				MessageOpertionDetailRow rejectbtn = new MessageOpertionDetailRow();
				rejectbtn.setLinktxt("拒绝");
				rejectbtn.setLinkclass("reject");
				rejectbtn.setMessageid(row.getId());
				rejectbtn.setLinkurl("/corporate/staff/refuse/" + ev.getInvitationid());
				sessionMessageMapper.insertOpertiondetail(rejectbtn);
				MessageOpertionDetailRow passbtn = new MessageOpertionDetailRow();
				passbtn.setLinktxt("同意");
				passbtn.setLinkclass("pass");
				passbtn.setMessageid(row.getId());
				passbtn.setLinkurl("/corporate/staff/pass/" + ev.getInvitationid());
				sessionMessageMapper.insertOpertiondetail(passbtn);
				session.commit();
			} 
			catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}
}

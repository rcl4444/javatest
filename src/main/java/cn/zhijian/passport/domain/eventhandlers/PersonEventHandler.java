package cn.zhijian.passport.domain.eventhandlers;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.MessageMapper;
import cn.zhijian.passport.db.MessageRelationMapper;
import cn.zhijian.passport.db.row.MessageOpertionDetailRow;
import cn.zhijian.passport.db.row.MessageRelationRow;
import cn.zhijian.passport.db.row.MessageRow;
import cn.zhijian.passport.domain.events.CorporatePassEvent;
import cn.zhijian.passport.domain.events.PersonJoinCorporateEvent;
import cn.zhijian.passport.domain.events.PersonRegisterEvent;
import cn.zhijian.passport.statustype.MessageAccessType;
import cn.zhijian.passport.statustype.MessageBelongType;
import cn.zhijian.passport.statustype.MessageOpertionType;
import cn.zhijian.passport.statustype.MessageSourceType;
import cn.zhijian.passport.statustype.MessageType;

public class PersonEventHandler {
	
	final static Logger logger = LoggerFactory.getLogger(PersonEventHandler.class);
	
	final MessageMapper messageMapper;
	
	final SqlSessionManager sqlSessionManager;
	
	public PersonEventHandler(MessageMapper messageMapper,SqlSessionManager sqlSessionManager){
		
		this.messageMapper = messageMapper;
		this.sqlSessionManager = sqlSessionManager;
	}

	@EventHandler
	public void personRegiste(PersonRegisterEvent ev) {
		
		MessageRow row = new MessageRow();
		row.setContent("账号注册成功,可以正常使用啦!");
		row.setBelongtype(MessageBelongType.Person);
		row.setMessagetype(MessageType.Prompt);
		row.setOpertiontype(MessageOpertionType.PromptAccountRegister);
		row.setPersonid(ev.getPersonid());
		row.setCorporateid(null);
		row.setAccesstype(MessageAccessType.Pull);
		row.setIsread(false);
		row.setIsdelete(false);
		row.setCreatedAt(new Date());
		this.messageMapper.insert(row);
	}
	
	@EventHandler
	public void corporatePass(CorporatePassEvent ev){
		
		MessageRow row = new MessageRow();
		row.setContent("您的公司已审批成功,快去试试企业版吧!");
		row.setBelongtype(MessageBelongType.Person);
		row.setMessagetype(MessageType.Prompt);
		row.setOpertiontype(MessageOpertionType.PromptPersonApplyResult);
		row.setPersonid(ev.getPersonid());
		row.setCorporateid(null);
		row.setAccesstype(MessageAccessType.Pull);
		row.setIsread(false);
		row.setIsdelete(false);
		row.setCreatedAt(new Date());
		this.messageMapper.insert(row);
	}
	
	@EventHandler
	public void joinCorporateApply(PersonJoinCorporateEvent ev){
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				MessageMapper sessionMessageMapper = session.getMapper(MessageMapper.class);
				CorporateMapper sessionCorporateMapper = session.getMapper(CorporateMapper.class);
				MessageRelationMapper sessionMRMapper = session.getMapper(MessageRelationMapper.class);
				MessageRow row = new MessageRow();
				row.setContent("手机号码"+ev.getMobile()+"的会员"+ev.getPersonname()+"申请加入公司");
				row.setBelongtype(MessageBelongType.Corporate);
				row.setMessagetype(MessageType.Operation);
				row.setOpertiontype(MessageOpertionType.OperationPersonApply);
				row.setPersonid(sessionCorporateMapper.findManager(ev.getCorporateid()).getId());
				row.setCorporateid(ev.getCorporateid());
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
				passbtn.setLinktxt("通过");
				passbtn.setLinkclass("pass");
				passbtn.setMessageid(row.getId());
				passbtn.setLinkurl("/corporate/staff/pass/" + ev.getInvitationid());
				sessionMessageMapper.insertOpertiondetail(passbtn);
				MessageRelationRow mrr = new MessageRelationRow();
				mrr.setMessageid(row.getId());
				mrr.setSourceid(ev.getInvitationid());
				mrr.setSourcetype(MessageSourceType.JoinCorporateApply);
				sessionMRMapper.insert(mrr);
				session.commit();
			} 
			catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}
}
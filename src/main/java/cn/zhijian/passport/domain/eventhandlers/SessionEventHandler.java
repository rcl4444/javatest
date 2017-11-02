package cn.zhijian.passport.domain.eventhandlers;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.zhijian.passport.db.ApplicationDao;
import cn.zhijian.passport.db.CurrSessionMapper;
import cn.zhijian.passport.db.MessageMapper;
import cn.zhijian.passport.db.row.CurrSessionRow;
import cn.zhijian.passport.db.row.MessageOpertionDetailRow;
import cn.zhijian.passport.db.row.MessageRow;
import cn.zhijian.passport.domain.events.PersonLoginEvent;
import cn.zhijian.passport.domain.events.PersonLoginOutEvent;
import cn.zhijian.passport.domain.events.PersonLoginReplaceEvent;
import cn.zhijian.passport.session.HttpClientUtil;
import cn.zhijian.passport.statustype.MessageAccessType;
import cn.zhijian.passport.statustype.MessageBelongType;
import cn.zhijian.passport.statustype.MessageOpertionType;
import cn.zhijian.passport.statustype.MessageType;

public class SessionEventHandler {

	final static Logger logger = LoggerFactory.getLogger(SessionEventHandler.class);
	final SqlSessionManager sqlSessionManager;
	protected ObjectMapper mapper = new ObjectMapper();

	public SessionEventHandler(SqlSessionManager sqlSessionManager) {

		this.sqlSessionManager = sqlSessionManager;
	}

	@EventHandler
	public void personLogin(PersonLoginEvent ev) throws JsonProcessingException {

		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				CurrSessionMapper sessionMapper = session.getMapper(CurrSessionMapper.class);
				CurrSessionRow csr = new CurrSessionRow(ev.getContext().getSessionId(),
						ev.getContext().getPerson().getId(), this.mapper.writeValueAsBytes(ev.getContext()));
				sessionMapper.insert(csr);
				session.commit();
			} catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}

	@EventHandler
	public void personLoginReplace(PersonLoginReplaceEvent ev) throws JsonProcessingException {

		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				CurrSessionMapper sessionMapper = session.getMapper(CurrSessionMapper.class);
				ApplicationDao appDao = session.getMapper(ApplicationDao.class);
				sessionMapper.delete(ev.getOldsesssionid());
				CurrSessionRow csr = new CurrSessionRow(ev.getContext().getSessionId(),
						ev.getContext().getPerson().getId(), this.mapper.writeValueAsBytes(ev.getContext()));
				sessionMapper.insert(csr);
				appDao.findSendLoginOutApp().stream().forEach(o -> {
					HttpClientUtil.getInstance().sendHttpPost(o.getLoginouturl() + "?userid="
							+ ev.getContext().getPerson().getId() + "&client_id" + o.getClientid());
				});
				session.commit();
			} catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}

	@EventHandler
	public void personLoginOut(PersonLoginOutEvent ev) {
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				CurrSessionMapper sessionMapper = session.getMapper(CurrSessionMapper.class);
				ApplicationDao appDao = session.getMapper(ApplicationDao.class);
				sessionMapper.delete(ev.getSessionid());
				appDao.findSendLoginOutApp().stream().forEach(o -> {
					HttpClientUtil.getInstance().sendHttpPost(o.getLoginouturl() + "?userid=" + ev.getPersonid() + "&client_id" + o.getClientid());
				});
				session.commit();
			} catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}
}
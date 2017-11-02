package cn.zhijian.passport.admin.commandhandlers;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;

import cn.zhijian.passport.admin.commands.CorporateAuditCommand;
import cn.zhijian.passport.admin.commands.CorporateCertificationCommand;
import cn.zhijian.passport.admin.db.AdminCorporateMapper;
import cn.zhijian.passport.admin.row.SerialNumberRow;
import cn.zhijian.passport.db.row.CorporateRow;
import cn.zhijian.passport.db.row.PersonRow;
import cn.zhijian.passport.domain.events.CorporatePassEvent;
import cn.zhijian.passport.session.SessionStore;
import cn.zhijian.passport.statustype.CorporateEnum;
import cn.zhijian.pay.db.PayMapper;

public class AdminCorporateCommandHandler {

	final CommandGateway cmdGw;
	final SessionStore sessionStore;
	final AdminCorporateMapper adminCorporateMapper;
	final PayMapper payMapper;
	final SqlSessionManager sqlSessionManager;
	final EventBus eventBus;

	public AdminCorporateCommandHandler(CommandGateway cmdGw, SessionStore sessionStore,
			AdminCorporateMapper adminCorporateMapper,PayMapper payMapper,
			SqlSessionManager sqlSessionManager,EventBus eventBus) {
		this.cmdGw = cmdGw;
		this.sessionStore = sessionStore;
		this.adminCorporateMapper = adminCorporateMapper;
		this.payMapper = payMapper;
		this.sqlSessionManager = sqlSessionManager;
		this.eventBus = eventBus;
	}

	@CommandHandler
	public Pair<Boolean, String> corporateAudit(CorporateAuditCommand cmd) {

		CorporateRow row = this.adminCorporateMapper.load(cmd.getCorporateId());
		if (row != null) {
			if(row.getIsPending() == CorporateEnum.Audit_Pass || row.getIsPending().getCode() >= CorporateEnum.Authentication_Not.getCode()){
				return Pair.of(false, "公司已通过审核,不能进行审核操作");
			}
			if (StringUtils.isEmpty(row.getCorporateMark()) && cmd.getStatus() == CorporateEnum.Audit_Pass) {
				String init = "00001";
				SerialNumberRow serialNumberRow = adminCorporateMapper.findSerialNumberTop();

				if (serialNumberRow == null) {

					adminCorporateMapper.insertSerialNumber(new SerialNumberRow(null, "00001"));
				} else {
					init = serialNumberRow.getNid();
					init = init + 1;
					adminCorporateMapper.insertSerialNumber(new SerialNumberRow(null, init));
				}

				String hscode = row.getHsCode().substring(6);
				String uuid = generateCorpMark(init, hscode);

				row.setCorporateMark(uuid);
			}
			if (!StringUtils.isEmpty(cmd.getReason()) && cmd.getStatus() == CorporateEnum.Audit_Reject) {
				row.setAuditreason(cmd.getReason());
			}
			
			row.setIsPending(cmd.getStatus());

			this.adminCorporateMapper.updateCorporate(row);
			PersonRow pr = this.adminCorporateMapper.findOwner(row.getId());
			this.eventBus.publish(new GenericEventMessage<>(new CorporatePassEvent(row.getId(),pr.getId(),new Date(),row.getWalletId())));
			return Pair.of(true, "审核完毕");
		} else {
			return Pair.of(false, "该标识数据不存在");
		}
	}

	@CommandHandler
	public Pair<Boolean, String> corporateAuthentication(CorporateCertificationCommand cmd) {
		CorporateRow row = this.adminCorporateMapper.load(cmd.getCorporateId());
		if (row != null) {

			if (cmd.getStatus() == CorporateEnum.Authentication_Pass) {
				if (row.getIsPending() != CorporateEnum.Authentication_Apply) {
					return Pair.of(true, "该状态不是认证申请");
				}
				if (!StringUtils.isEmpty(cmd.getReason()) && cmd.getStatus() == CorporateEnum.Authentication_Reject) {
					row.setAuditreason(cmd.getReason());
				}
				row.setIsPending(CorporateEnum.Authentication_Pass);
				try (SqlSession session = sqlSessionManager.openSession()) {
					try {
//						payMapper.ModityWalletBalance(1000, row.getWalletId());
						this.adminCorporateMapper.updateCorporate(row);
						session.commit();
					} catch (Exception e) {
						// TODO: handle exception
						session.rollback();
					}
				}
			} else {
				if (row.getIsPending() == CorporateEnum.Authentication_Pass) {
					return Pair.of(true, "该状态已为认证通过，不能否决");
				}
				if (!StringUtils.isEmpty(cmd.getReason()) && cmd.getStatus() == CorporateEnum.Authentication_Reject) {
					row.setAuditreason(cmd.getReason());
				}
				row.setIsPending(CorporateEnum.Authentication_Reject);
				this.adminCorporateMapper.updateCorporate(row);
			}
			return Pair.of(true, "认证操作完毕");
		} else {
			return Pair.of(false, "该标识数据不存在");
		}
	}

	private String generateCorpMark(String num1, String num2) {
		char[] ch = num1.toCharArray();
		int i = 10;
		int sum = 0;
		for (char c : ch) {
			sum = sum + Integer.parseInt(String.valueOf(c)) * i;
			i--;
		}

		char[] ch1 = num2.toCharArray();
		for (char c : ch1) {
			String str = String.valueOf(c);
			if(Pattern.matches("^[A-Za-z]", str)) {
				sum = sum + letterToNum((String.valueOf(c))) * i;
			}
			else {
				sum = sum + Integer.parseInt(String.valueOf(c)) * i;
			}

			i--;
		}
		while (sum >= 11) {
			sum = sum % 11;
		}
		return num1 + sum + num2;
	}
	
    // 将字母转换成数字  
    public int letterToNum(String input) {  
        for (byte b : input.getBytes()) {  
            return (b - 96);  
        }
		return 0;
    }  
    
    private int byteToInt(int i) {
    	return i & 0xFF;
    }
}

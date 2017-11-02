package cn.zhijian.passport.domain.commandhandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.api.CorporateRole.VoucherOperation;
import cn.zhijian.passport.commands.CreateCorporateRoleCommand;
import cn.zhijian.passport.commands.DeleteCorporateRoleCommand;
import cn.zhijian.passport.commands.ModityCorporateRoleCommand;
import cn.zhijian.passport.db.CorporateMapper;
import cn.zhijian.passport.db.CorporateRoleMapper;
import cn.zhijian.passport.db.row.CorporateModuleRow;
import cn.zhijian.passport.db.row.CorporateRoleModuleRow;
import cn.zhijian.passport.db.row.CorporateRoleRow;
import cn.zhijian.passport.db.row.ModuleOperationRow;
import cn.zhijian.passport.db.row.RoleOperationRow;
import cn.zhijian.trade.db.VoucherMapper;
import cn.zhijian.trade.db.row.VoucherRow;

public class CorporateRoleCommandHandler {
	private static Logger logger = LoggerFactory.getLogger(CorporateRoleCommandHandler.class);

	final CorporateMapper corporateMapper;
	final CorporateRoleMapper roleMapper;
	final SqlSessionManager sqlSessionManager;
	final VoucherMapper voucherMapper;

	public CorporateRoleCommandHandler(CorporateMapper corporateMapper,CorporateRoleMapper corporateRoleMapper,
			SqlSessionManager sqlSessionManager,VoucherMapper voucherMapper) {
		this.corporateMapper = corporateMapper;
		this.roleMapper = corporateRoleMapper;
		this.sqlSessionManager = sqlSessionManager;
		this.voucherMapper = voucherMapper;
	}

	@CommandHandler
	public Pair<Boolean,String> CreateCorporateRole(CreateCorporateRoleCommand cmd) throws Exception {
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				if("".equals(cmd.getRole().getRolename()))
				{
					return Pair.of(false, "角色名不能为空");
				}
				Date currDate = new Date();
				CorporateRoleMapper sessionRoleMapper = session.getMapper(CorporateRoleMapper.class);
				
				if(sessionRoleMapper.findByRoleName(cmd.getRole().getRolename(),cmd.getCorporaterId()).size() > 0)
				{
					return Pair.of(false, "角色名\""+cmd.getRole().getRolename()+"\"已存在");
				}
				else{
					List<Long> voucherids = cmd.getRole().getCertificates().stream().map(o->o.getCertificateid()).distinct().collect(Collectors.toList());
					
					List<VoucherRow> vouchers = this.voucherMapper.findByIds(voucherids);
					if(vouchers.size() != voucherids.size()){
						return Pair.of(false, "凭证不存在");
					}
					CorporateRoleRow row = new CorporateRoleRow();
					row.setCorporateid(cmd.getCorporaterId());
					row.setDescription(cmd.getRole().getDescription());
					row.setRolename(cmd.getRole().getRolename());
					row.setCreatedAt(currDate);
					row.setCreatedBy(cmd.getHandleusername());
					sessionRoleMapper.insert(row);
					if(cmd.getRole().getCertificates() != null && cmd.getRole().getCertificates().size() > 0){
						List<Long> moduleids = new ArrayList<>();
						cmd.getRole().getCertificates().stream().forEach(o->{
							moduleids.addAll(o.getOperationids());
						});
						List<Long> dismoduleids = moduleids.stream().distinct().collect(Collectors.toList());
						List<ModuleOperationRow> appModuleOperations = sessionRoleMapper.getAppModuleOperationById(dismoduleids);
						if(dismoduleids.size() != appModuleOperations.size()){
							return Pair.of(false, "存在不合法的操作标识");
						}
						for(VoucherOperation vo :  cmd.getRole().getCertificates()){
							for(Long o : vo.getOperationids()){
								ModuleOperationRow mor = appModuleOperations.stream().filter(oi->oi.getId().equals(o)).findFirst().get();
								RoleOperationRow ro = new RoleOperationRow();
								ro.setRoleid(row.getId());
								ro.setVoucherid(vo.getCertificateid());
								ro.setApplicationid(mor.getApplicationid());
								ro.setModuleid(mor.getModuleid());
								ro.setOperationid(mor.getId());
								ro.setCreatedate(currDate);
								sessionRoleMapper.insertRoleOperation(ro);
							}
						}
					}
					
					if(cmd.getRole().getModuleIds() != null && cmd.getRole().getModuleIds().size() > 0)
					{
						List<CorporateModuleRow> corporateModuleRows = roleMapper.findAllModulebyId(cmd.getRole().getModuleIds());
						for (CorporateModuleRow corporateModuleRow : corporateModuleRows) {
							CorporateRoleModuleRow corporateRoleModuleRow = new CorporateRoleModuleRow();
							corporateRoleModuleRow.setRoleId(row.getId());
							corporateRoleModuleRow.setModuleId(corporateModuleRow.getId());
							corporateRoleModuleRow.setCorporateId(cmd.getCorporaterId());
							corporateRoleModuleRow.setCreatedAt(new Date());
							corporateRoleModuleRow.setCreatedBy(cmd.getHandleusername());
							roleMapper.insertRoleModule(corporateRoleModuleRow);
						}
					}
				}
				session.commit();
				return Pair.of(true, "新建角色完毕");
			} 
			catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}

	@CommandHandler
	public Pair<Boolean,String> ModityCorporateRole(ModityCorporateRoleCommand cmd) throws Exception {

		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				Date currDate = new Date();
				CorporateRoleMapper sessionRoleMapper = session.getMapper(CorporateRoleMapper.class);
				CorporateRoleRow role = sessionRoleMapper.load(cmd.getRole().getId());
				if(role == null){
					return Pair.of(false, "角色信息不存在");
				}
				if(sessionRoleMapper.findByRoleNameAndId(cmd.getRole().getRolename(),role.getCorporateid(),role.getId()).size() > 0)
				{
					return Pair.of(false, "角色名\""+cmd.getRole().getRolename()+"\"已存在");
				}
				List<Long> voucherids = cmd.getRole().getCertificates().stream().map(o->o.getCertificateid()).distinct().collect(Collectors.toList());
				List<VoucherRow> vouchers = this.voucherMapper.findByIds(voucherids);
				if(vouchers.size() != voucherids.size()){
					return Pair.of(false, "凭证不存在");
				}
				else{
					role.setDescription(cmd.getRole().getDescription());
					role.setRolename(cmd.getRole().getRolename());
					role.setUpdatedAt(currDate);
					role.setUpdatedBy(cmd.getHandleusername());
					sessionRoleMapper.update(role, role.getId());
					List<Long> cmdModuleIds = cmd.getRole().getModuleIds() == null || cmd.getRole().getModuleIds().size()==0 ? new ArrayList<>():cmd.getRole().getModuleIds();
					List<VoucherOperation> cmdOperations = cmd.getRole().getCertificates() == null || cmd.getRole().getCertificates().size()==0 
							? new ArrayList<>(): cmd.getRole().getCertificates();
					//all curr role operationids
					List<RoleOperationRow> roleOperations = sessionRoleMapper.getRolePower(role.getId());
					//delete
					List<Long> delroleOperationIds = roleOperations.stream().filter(o->cmdOperations.stream().filter(oi->oi.getCertificateid().equals(o.getVoucherid())).count()==0
							|| cmdOperations.stream().filter(oi->oi.getCertificateid().equals(o.getVoucherid())&&oi.getOperationids().contains(o.getOperationid())).count()==0)
							.map(o->o.getId()).collect(Collectors.toList());
					if(delroleOperationIds.size() > 0){
						sessionRoleMapper.deleteRoleOperationById(delroleOperationIds);
					}
					
					if(role.getId() !=null)
					{
						sessionRoleMapper.deleteRoleModule(role.getId(), cmd.getCorporaterId());
					}
					
					//insert
					List<VoucherOperation> iOperations = new ArrayList<>();
					cmdOperations.stream().forEach(o->{
						if(roleOperations.stream().filter(oi->o.getCertificateid() == oi.getVoucherid()).count() == 0){
							iOperations.add(o);
						}
						List<Long> addOperationIds = o.getOperationids().stream()
								.filter(oii->roleOperations.stream().filter(oi->oi.getVoucherid() == o.getCertificateid() && oi.getOperationid() == oii).count() == 0)
								.collect(Collectors.toList());
						if(addOperationIds.size() > 0){
							iOperations.add(new VoucherOperation(o.getCertificateid(),addOperationIds));
						}
					});
					if(iOperations.size() > 0){
						List<Long> iOperationIds = new ArrayList<>();
						iOperations.stream().forEach(o->iOperationIds.addAll(o.getOperationids()));
						List<ModuleOperationRow> allInOperations = sessionRoleMapper.getAppModuleOperationById(iOperationIds);
						for(VoucherOperation io : iOperations){
							List<ModuleOperationRow> appModuleOperations = allInOperations.stream().filter(o->io.getOperationids().contains(o.getId()))
									.collect(Collectors.toList());
							for(ModuleOperationRow amo :  appModuleOperations){
								RoleOperationRow ro = new RoleOperationRow();
								ro.setVoucherid(io.getCertificateid());
								ro.setRoleid(role.getId());
								ro.setApplicationid(amo.getApplicationid());
								ro.setModuleid(amo.getModuleid());
								ro.setOperationid(amo.getId());
								ro.setCreatedate(currDate);
								sessionRoleMapper.insertRoleOperation(ro);
							}	
						}
					}
					
					if(cmdModuleIds != null && cmdModuleIds.size() > 0)
					{
						List<CorporateModuleRow> corporateModuleRows = roleMapper.findAllModulebyId(cmdModuleIds);
						for (CorporateModuleRow corporateModuleRow : corporateModuleRows) {
							CorporateRoleModuleRow corporateRoleModuleRow = new CorporateRoleModuleRow();
							corporateRoleModuleRow.setRoleId(role.getId());
							corporateRoleModuleRow.setModuleId(corporateModuleRow.getId());
							corporateRoleModuleRow.setCorporateId(cmd.getCorporaterId());
							corporateRoleModuleRow.setCreatedAt(new Date());
							corporateRoleModuleRow.setCreatedBy(cmd.getHandleusername());
							roleMapper.insertRoleModule(corporateRoleModuleRow);
						}
					}
				}
				session.commit();
				return Pair.of(true, "操作完毕");
			} 
			catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}

	@CommandHandler
	public Pair<Boolean,String> DeleteCorporateRole(DeleteCorporateRoleCommand cmd) throws Exception {
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				CorporateRoleMapper sessionRoleMapper = session.getMapper(CorporateRoleMapper.class);
				CorporateRoleRow row = sessionRoleMapper.load(cmd.getRoleid());
				if(row == null){
					return Pair.of(false, "角色信息不存在");
				}
				if(sessionRoleMapper.roleStaffCount(row.getId()) > 0){
					return Pair.of(false, "角色信息已分配,不可删除");
				}
				sessionRoleMapper.delete(row.getId(),cmd.getCorpId());
				sessionRoleMapper.deleteRoleOperationByRoleId(row.getId());
				session.commit();
				return Pair.of(true, "删除角色完毕");
			} 
			catch (Exception e) {
				session.rollback();
				throw e;
			}
		}
	}
}

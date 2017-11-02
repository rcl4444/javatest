package cn.zhijian.passport.admin.commandhandlers;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.commands.GiftPromotionModifyCommand;
import cn.zhijian.passport.admin.commands.PromotionChangeStateCommand;
import cn.zhijian.passport.db.SalesPromotionMapper;
import cn.zhijian.passport.db.row.SalesPromotionRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftMainRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftProductRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftType;
import cn.zhijian.passport.db.row.SalesPromotionRow.SalesPromotionStatus;
import cn.zhijian.passport.db.row.SalesPromotionRow.SalesPromotionType;
import cn.zhijian.passport.statustype.BusinessType;
import cn.zhijian.passport.statustype.CodeEnumUtil;

public class AdminSalePromotionCommandHandler {

	final Logger logger = LoggerFactory.getLogger(AdminSalePromotionCommandHandler.class);
	
	final SqlSessionManager sqlSessionManager;
	
	public AdminSalePromotionCommandHandler(SqlSessionManager sqlSessionManager){
		this.sqlSessionManager = sqlSessionManager;
	}
	
	@CommandHandler
	public Pair<Boolean,String> createGiftProduct(GiftPromotionModifyCommand cmd){
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				Date currDate = new Date();
				SalesPromotionMapper sessionSPMapper = session.getMapper(SalesPromotionMapper.class);
				List<Long> unChangeProductIds = sessionSPMapper.getPromotionGiftProductId(GiftType.Product,SalesPromotionStatus.Start);
				if(cmd.getInput().getId() == null||cmd.getInput().getId() == 0){
					if(cmd.getInput().getProductids().stream().filter(o->unChangeProductIds.contains(o)).count() > 0){
						return Pair.of(false,"产品已经在活动中");
					}
					SalesPromotionRow sp = new SalesPromotionRow();
					sp.setSpname(cmd.getInput().getPromotionname());
					sp.setBegindate(cmd.getInput().getPeriodstart());
					sp.setEnddate(cmd.getInput().getPeriodend());
					sp.setSaletype(SalesPromotionType.Gift);
					sp.setState(SalesPromotionStatus.Default);
					sp.setShowstate(cmd.getInput().getIssign());
					sp.setCreatedBy(cmd.getCurrAccountUserName());
					sp.setCreatedAt(currDate);
					sp.setType(CodeEnumUtil.codeOf(BusinessType.class, cmd.getInput().getType()));
					sessionSPMapper.insert(sp);
					GiftMainRow gm = new GiftMainRow();
					gm.setSalespromotionid(sp.getId());
					gm.setEventid(cmd.getInput().getEvent());
					gm.setPromotiontitle(cmd.getInput().getTitle());
					gm.setGifttype(GiftType.Product);
					sessionSPMapper.insertGiftMain(gm);
					for(Long pid : cmd.getInput().getProductids()){
						GiftProductRow gp = new GiftProductRow();
						gp.setGiftmainid(gm.getId());
						gp.setProductid(pid);
						gp.setPersonnum(cmd.getInput().getPeopleno());
						gp.setDuration(cmd.getInput().getTime());
						sessionSPMapper.insertGiftProduct(gp);	
					}
				}
				else{
					SalesPromotionRow sp = sessionSPMapper.load(cmd.getInput().getId());
					if(sp == null){
						return Pair.of(false, "促销信息不存在");
					}
					GiftMainRow gm = sessionSPMapper.getGiftmain(cmd.getInput().getId());
					if(gm == null){
						return Pair.of(false, "直送信息不存在");
					}
					List<GiftProductRow> gps = sessionSPMapper.getGiftproduct(gm.getId());
					gps.forEach(o->unChangeProductIds.remove(o.getProductid()));
					if(cmd.getInput().getProductids().stream().filter(o->unChangeProductIds.contains(o)).count() > 0){
						return Pair.of(false,"产品已经在活动中");
					}
					sp.setSpname(cmd.getInput().getPromotionname());
					sp.setBegindate(cmd.getInput().getPeriodstart());
					sp.setEnddate(cmd.getInput().getPeriodend());
					sp.setShowstate(cmd.getInput().getIssign());
					sessionSPMapper.update(sp);
					gm.setEventid(cmd.getInput().getEvent());
					gm.setPromotiontitle(cmd.getInput().getTitle());
					sessionSPMapper.updateGiftMain(gm);
					//新增
					List<Long> addproducts = cmd.getInput().getProductids().stream()
							.filter(o->gps.stream().filter(oi->oi.getProductid().equals(o)).count()==0).collect(Collectors.toList());
					for(Long pid : addproducts){
						GiftProductRow gp = new GiftProductRow();
						gp.setGiftmainid(gm.getId());
						gp.setProductid(pid);
						gp.setPersonnum(cmd.getInput().getPeopleno());
						gp.setDuration(cmd.getInput().getTime());
						sessionSPMapper.insertGiftProduct(gp);	
					}
					//删除
					List<Long> deleteProducts = gps.stream().filter(o->!cmd.getInput().getProductids().contains(o.getProductid()))
							.map(o->o.getProductid()).collect(Collectors.toList());
					sessionSPMapper.deleteGiftProduct(deleteProducts);
				}
				session.commit();
				return Pair.of(true, StringUtils.EMPTY);
			} 
			catch (Exception e) {
				session.rollback();
				return Pair.of(false, e.getMessage());
			}
		}
	}

	@CommandHandler
	public Pair<Boolean,String> changePromotionState(PromotionChangeStateCommand cmd){
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				SalesPromotionMapper sessionSPMapper = session.getMapper(SalesPromotionMapper.class);
				SalesPromotionRow sp = sessionSPMapper.load(cmd.getSalepromotionid());
				if(sp == null){
					return Pair.of(false, "促销信息不存在");
				}
				List<Long> gproductids = sessionSPMapper.getGiftProductIdsBySPId(GiftType.Product,sp.getId());
				if(sessionSPMapper.getPromotionGiftProductId(GiftType.Product, SalesPromotionStatus.Start).stream().filter(o->gproductids.contains(o)).count()>0){
					return Pair.of(false, "已有产品在活动中");
				}
				sp.setState(cmd.getState());
				sessionSPMapper.update(sp);
				session.commit();
				return Pair.of(true, StringUtils.EMPTY);
			} 
			catch (Exception e) {
				session.rollback();
				return Pair.of(false, e.getMessage());
			}
		}
	}
}
package cn.zhijian.passport.admin.commandhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.admin.commands.ProductChangeStatusCommand;
import cn.zhijian.passport.admin.commands.ProductCreateCommand;
import cn.zhijian.passport.admin.commands.ProductModifyCommand;
import cn.zhijian.passport.admin.commands.ProductPriceSetCommand;
import cn.zhijian.passport.admin.db.AdminApplicationMapper;
import cn.zhijian.passport.admin.db.AdminProductMapper;
import cn.zhijian.passport.admin.dto.ProductPriceInputDto;
import cn.zhijian.passport.db.row.ApplicationModuleRow;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.db.row.ProductRow.PersonOption;
import cn.zhijian.passport.db.row.ProductRow.ProductAppModuleRow;
import cn.zhijian.passport.db.row.ProductRow.ProductPriceRow;
import cn.zhijian.passport.db.row.ProductRow.ProductStatus;
import cn.zhijian.passport.db.row.ProductRow.UseTimeOption;
import cn.zhijian.passport.statustype.BusinessType;
import cn.zhijian.passport.statustype.CodeEnumUtil;

public class AdminProductCommandHandler {
	
	final Logger logger = LoggerFactory.getLogger(AdminProductCommandHandler.class);
	
	final SqlSessionManager sqlSessionManager;
	
	public AdminProductCommandHandler(SqlSessionManager sqlSessionManager){
		
		this.sqlSessionManager = sqlSessionManager;
	}
	
	@CommandHandler
	public Pair<Boolean,String> createProduct(ProductCreateCommand cmd){
	
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				Date currDate = new Date();
				BusinessType type = CodeEnumUtil.codeOf(BusinessType.class,cmd.getProduct().getType());
				AdminProductMapper productMapper = session.getMapper(AdminProductMapper.class);
				AdminApplicationMapper appMapper = session.getMapper(AdminApplicationMapper.class);
				if(productMapper.getByName(cmd.getProduct().getProductname(), type, null)!=null){
					return Pair.of(false, String.format("应用名称%s已经存在",cmd.getProduct().getProductname()));
				}
				ProductRow addrow = new ProductRow();
				addrow.setProductname(cmd.getProduct().getProductname());
				addrow.setType(type);
				addrow.setStatus(ProductStatus.PullOffShelves);
				addrow.setRemark(cmd.getProduct().getRemark());
				addrow.setDescription(cmd.getProduct().getDescription());
				addrow.setCreatedt(currDate);
				addrow.setAvatarresourceid(cmd.getProduct().getActiveid());
				productMapper.insert(addrow);
				List<ApplicationModuleRow> ams;
				if(cmd.getProduct().getModules().size() > 0){
					ams = appMapper.getAppModuleByIds(cmd.getProduct().getModules());;
				}
				else{
					ams = new ArrayList<>();	
				}
				for(ApplicationModuleRow am: ams){
					ProductAppModuleRow pam = new ProductAppModuleRow();
					pam.setProductid(addrow.getId());
					pam.setApplicationid(am.getApplicationid());
					pam.setApplicationmoduleid(am.getId());
					productMapper.insertProductModule(pam);
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
	public Pair<Boolean,String> modifyProduct(ProductModifyCommand cmd){
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				BusinessType type = CodeEnumUtil.codeOf(BusinessType.class,cmd.getProduct().getType());
				AdminProductMapper productMapper = session.getMapper(AdminProductMapper.class);
				AdminApplicationMapper appMapper = session.getMapper(AdminApplicationMapper.class);
				ProductRow modifyrow = productMapper.load(cmd.getProduct().getId());
				if(modifyrow == null){
					return Pair.of(false, "产品信息不存在");
				}
				if(productMapper.getByName(cmd.getProduct().getProductname(), type, Arrays.asList(cmd.getProduct().getId()))!=null){
					return Pair.of(false, String.format("应用名称%s已经存在",cmd.getProduct().getProductname()));
				}
				modifyrow.setProductname(cmd.getProduct().getProductname());
				modifyrow.setRemark(cmd.getProduct().getRemark());
				modifyrow.setDescription(cmd.getProduct().getDescription());
				modifyrow.setAvatarresourceid(cmd.getProduct().getActiveid());
				productMapper.update(modifyrow);
				List<ProductAppModuleRow> pams = productMapper.getProductModuleByProductId(cmd.getProduct().getId());
				List<ApplicationModuleRow> ams;
				if(cmd.getProduct().getModules().size() > 0){
					ams = appMapper.getAppModuleByIds(cmd.getProduct().getModules());;
				}
				else{
					ams = new ArrayList<>();	
				}
				//delete
				List<Long> deleteids = pams.stream().filter(o->!cmd.getProduct().getModules().contains(o.getApplicationmoduleid())).map(o->o.getId())
					.collect(Collectors.toList());
				if(deleteids.size()>0){
					productMapper.deleteProductModule(deleteids); 
				}
				//insert
				ams = ams.stream().filter(o->pams.stream().filter(oi->oi.getApplicationmoduleid().equals(o.getId())).count()==0).collect(Collectors.toList());
				for(ApplicationModuleRow am: ams){
					ProductAppModuleRow pam = new ProductAppModuleRow();
					pam.setProductid(modifyrow.getId());
					pam.setApplicationid(am.getApplicationid());
					pam.setApplicationmoduleid(am.getId());
					productMapper.insertProductModule(pam);
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
	public Pair<Boolean,String> productStatusChange(ProductChangeStatusCommand cmd){
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				AdminProductMapper productMapper = session.getMapper(AdminProductMapper.class);
				ProductRow modifyrow = productMapper.load(cmd.getProductid());
				if(modifyrow == null){
					return Pair.of(false, "产品信息不存在");
				}
				ProductStatus pStatus = CodeEnumUtil.codeOf(ProductStatus.class, cmd.getStatus());
				if(pStatus.equals(ProductStatus.PutOnShelves)){
					List<ProductPriceRow> pps = productMapper.getProductPriceByProductId(cmd.getProductid());
					if(pps.size() == 0){
						return Pair.of(false, "请设置价格后进行上架操作");
					}
				}
				modifyrow.setStatus(pStatus);
				productMapper.update(modifyrow);
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
	public Pair<Boolean,String> setProductPrice(ProductPriceSetCommand cmd){
		
		try (SqlSession session = sqlSessionManager.openSession()) {
			try {
				AdminProductMapper productMapper = session.getMapper(AdminProductMapper.class);
				ProductRow modifyrow = productMapper.load(cmd.getInput().getProductid());
				if(modifyrow == null){
					return Pair.of(false, "产品信息不存在");
				}
				if(modifyrow.getStatus().equals(ProductStatus.PutOnShelves)){
					return Pair.of(false, "已上架产品不能修改价格");
				}
				List<ProductPriceRow> pps = productMapper.getProductPriceByProductId(cmd.getInput().getProductid());
				for(ProductPriceInputDto.ProductPrice ppi: cmd.getInput().getPrices()){
					UseTimeOption uto = CodeEnumUtil.codeOf(UseTimeOption.class, ppi.getTimeindex());
					PersonOption po = CodeEnumUtil.codeOf(PersonOption.class, ppi.getPeopleindex());
					Optional<ProductPriceRow> p = pps.stream().filter(o->o.getPersonnum().equals(po)&& o.getUsetime().equals(uto)).findFirst();
					ProductPriceRow row;
					if(p.isPresent()){
						row = p.get();
						Boolean isdo = false;
						if(!row.getPersoncost().equals(ppi.getPeopleprice())){
							isdo = true;
							row.setPersoncost(ppi.getPeopleprice());
						}
						if(!row.getApplicationcost().equals(ppi.getTimeprice())){
							isdo=true;
							row.setApplicationcost(ppi.getTimeprice());
						}
						if(isdo){
							productMapper.updateProductPrice(row);
						}
					}
					else{
						row = new ProductPriceRow();
						row.setProductid(modifyrow.getId());
						row.setPersonnum(po);
						row.setUsetime(uto);
						row.setApplicationcost(ppi.getTimeprice());
						row.setPersoncost(ppi.getPeopleprice());
						productMapper.insertProductPrice(row);
					}
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
}
package cn.zhijian.passport.admin.reps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import cn.zhijian.passport.admin.db.AdminProductMapper;
import cn.zhijian.passport.admin.dto.GiftProductDetailDto;
import cn.zhijian.passport.admin.dto.GiftProductDetailDto.GiftProduct;
import cn.zhijian.passport.admin.dto.GiftPromotionListDto;
import cn.zhijian.passport.api.PagedResult;
import cn.zhijian.passport.api.PagingQuery;
import cn.zhijian.passport.api.QueryInfo;
import cn.zhijian.passport.db.BusinessEventMapper;
import cn.zhijian.passport.db.SalesPromotionMapper;
import cn.zhijian.passport.db.row.BusinessEventRow;
import cn.zhijian.passport.db.row.BusinessEventRow.EventStatus;
import cn.zhijian.passport.db.row.BusinessEventRow.EventView;
import cn.zhijian.passport.db.row.GiftProductView;
import cn.zhijian.passport.db.row.ProductRow;
import cn.zhijian.passport.db.row.SalesPromotionRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftMainRow;
import cn.zhijian.passport.db.row.SalesPromotionRow.GiftType;
import cn.zhijian.passport.db.row.SalesPromotionRow.PromotionStatus;
import cn.zhijian.passport.db.row.SalesPromotionRow.SalesPromotionStatus;
import cn.zhijian.passport.statustype.BusinessType;
import cn.zhijian.passport.statustype.CodeEnumUtil;

public class AdminPromotionRepository {

	final SalesPromotionMapper spmapper;
	
	final BusinessEventMapper eventmapper;
	
	final AdminProductMapper productmapper;
	
	public AdminPromotionRepository(SalesPromotionMapper spmapper,BusinessEventMapper eventmapper,AdminProductMapper productmapper){
		this.spmapper = spmapper;
		this.eventmapper = eventmapper;
		this.productmapper = productmapper;
	}
	
	public PagedResult<GiftPromotionListDto> filterGiftPromotion(PagingQuery query){
		
		List<QueryInfo> queryli = new ArrayList<>();
		final Date currDate = new Date();
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for(QueryInfo q :query.getQuery()){
			if(StringUtils.isEmpty(q.getFilterRange())){
				continue;
			}
			if("way".equals(q.getColumn())){
				String[] sarray = q.getFilterRange().split(":");
				QueryInfo nq1 = new QueryInfo();
				nq1.setColumn("b.sign");
				nq1.setFilterRange(sarray[0]);
				nq1.setOperat("=");
				queryli.add(nq1);
				QueryInfo nq2 = new QueryInfo();
				nq2.setColumn("g.gifttype");
				nq2.setFilterRange(sarray[1]);
				nq2.setOperat("=");
				queryli.add(nq2);
			}
			else if("state".equals(q.getColumn())){
				PromotionStatus s = CodeEnumUtil.codeOf(PromotionStatus.class, Integer.parseInt(q.getFilterRange()));
				QueryInfo nq = new QueryInfo();
				switch(s)
				{
					case Default:
						nq.setColumn("s.state");
						nq.setFilterRange(SalesPromotionStatus.Default.getCode().toString());
						nq.setOperat("=");
						queryli.add(nq);
						break;
					case UnStart:
						nq.setColumn("s.state");
						nq.setFilterRange(SalesPromotionStatus.Start.getCode().toString());
						nq.setOperat("=");
						queryli.add(nq);
						break;
					case Continue:
						nq.setColumn("s.state");
						nq.setFilterRange(SalesPromotionStatus.Start.getCode().toString());
						nq.setOperat("=");
						queryli.add(nq);
						QueryInfo begin = new QueryInfo();
						begin.setColumn("s.begindate");
						begin.setFilterRange(dateFormater.format(currDate));
						begin.setOperat("<=");
						queryli.add(begin);
						QueryInfo end = new QueryInfo();
						end.setColumn("s.enddate");
						end.setFilterRange(dateFormater.format(currDate));
						end.setOperat(">=");
						queryli.add(end);
						break;
					case Over:
						nq.setColumn("s.state");
						nq.setFilterRange(SalesPromotionStatus.Start.getCode().toString());
						nq.setOperat("=");
						queryli.add(nq);
						QueryInfo end1 = new QueryInfo();
						end1.setColumn("s.enddate");
						end1.setFilterRange(dateFormater.format(currDate));
						end1.setOperat("<=");
						queryli.add(end1);
						break;
					case Delete:
						break;
				}
			}
			else if("begindate".equals(q.getColumn())){
				QueryInfo nq = new QueryInfo();
				nq.setColumn("s.begindate");
				nq.setFilterRange(q.getFilterRange());
				nq.setOperat(">=");
				queryli.add(nq);
			}
			else if("enddate".equals(q.getColumn())){
				QueryInfo nq = new QueryInfo();
				nq.setColumn("s.enddate");
				nq.setFilterRange(q.getFilterRange());
				nq.setOperat("<=");
				queryli.add(nq);
			}
		}
		query.setQuery(queryli);
		int totalRows = this.spmapper.getGiftPromotionCount(query);
		query.setPageSize(query.getPageSize() == null? 10 : query.getPageSize());
		return new PagedResult<>(this.spmapper.getGiftPromotionPaging(query),totalRows,query.getPageNo(),query.getPageSize())
				.map(o->{
					GiftPromotionListDto row = new GiftPromotionListDto();
					row.setId(o.getId());
					row.setPromotionname(o.getSpname());
					row.setTitle(o.getPromotiontitle());
					row.setPeriod(String.format("%s~%s", dateFormater.format(o.getBegindate()),dateFormater.format(o.getEnddate())));
					row.setWay(o.getEventname()+o.getGifttype().getDes());
					String state = PromotionStatus.Default.getDes();
					if(o.getState().equals(SalesPromotionStatus.Start)&&currDate.getTime() > o.getEnddate().getTime()){
						state = PromotionStatus.Over.getDes();
					}
					else if(o.getState().equals(SalesPromotionStatus.Start)&&currDate.getTime() < o.getBegindate().getTime()){
						state = PromotionStatus.UnStart.getDes();
					}
					else if(o.getState().equals(SalesPromotionStatus.Start)&&currDate.getTime() >= o.getBegindate().getTime() 
							&& currDate.getTime() <= o.getEnddate().getTime()){
						state = PromotionStatus.Continue.getDes();
					}
					row.setState(state);
					row.setCreatedAt(o.getCreatedAt());
					return row;
				});
	}
	
	public Map<String,String> getGiftWayStatus(){
		Map<String,String> result = new HashMap<String,String>();
		List<EventView> events = this.eventmapper.findUniqueEvent();
		for(GiftType gt: GiftType.values()){
			events.stream().forEach(o->{
				result.put(String.format("%s:%s",o.getSign(),gt.getCode()), String.format("%s%s", o.getEventname(),gt.getDes()));
			});
		}
		return result;
	}
	
	public Map<Long,String> getEventByType(BusinessType type){
		Map<Long,String> result = new HashMap<>();
		List<BusinessEventRow> events = this.eventmapper.findByType(type,EventStatus.Start);
		events.forEach(o->{
			result.put(o.getId(), o.getEventname());
		});
		return result;
	}

	public Map<Integer,String> getGiftType(){
		Map<Integer,String> result = new HashMap<>();
		for(GiftType t : GiftType.values()){
			result.put(t.getCode(), t.getDes());
		}
		return result;
	}
	
	public List<ProductRow> getProduct(BusinessType type){
		
		return this.productmapper.getByType(type, this.spmapper.getPromotionGiftProductId(GiftType.Product,SalesPromotionStatus.Start));
	}
	
	public GiftProductDetailDto getGiftProductDetail(Long id){
		
		SalesPromotionRow sp = this.spmapper.load(id);
		if(sp != null){
			GiftMainRow gmain = this.spmapper.getGiftmain(id);
			if(gmain!=null){
				GiftProductDetailDto result = new GiftProductDetailDto();
				result.setPromotionname(sp.getSpname());
				result.setTitle(gmain.getPromotiontitle());
				result.setPeriodstart(sp.getBegindate());
				result.setPeriodend(sp.getEnddate());
				result.setMethod(gmain.getGifttype().getCode());
				result.setEvent(gmain.getEventid());
			    result.setIssign(sp.getShowstate());
			    result.setType(sp.getType().getCode());
			    result.setPeopleno(null);
			    result.setTime(null);
				List<GiftProductView> gproducts = this.spmapper.getProductByGiftId(gmain.getId());
				if(gproducts.size() > 0){
					result.setProducts(gproducts.stream().map(o->{
						GiftProduct row = new GiftProduct();
						row.setId(o.getProductid());
						row.setProductname(o.getProductname());
					    result.setPeopleno(o.getPersonnum());
					    result.setTime(o.getDuration());
						return row;
					}).collect(Collectors.toList()));
				}
				return result;
			}
		}
		return null;
	}
}
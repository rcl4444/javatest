package cn.zhijian.passport.api;

import java.util.List;

import cn.zhijian.passport.statustype.CorporateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorporateEnums {
	List<CorporateEnum> corporateEnums;
}
